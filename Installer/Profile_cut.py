import arcpy
import os
import sys
import math
from arcpy import env

Profile_line_Input = arcpy.GetParameterAsText(0)
Surface_Input = arcpy.GetParameterAsText(1)
Profile_line_Output = arcpy.GetParameterAsText(2)
Distance_Input = arcpy.GetParameterAsText(3)
Profile_Points = arcpy.GetParameter(4)

Surface_Input_List = Surface_Input.split(";")

out_path, out_name = Profile_line_Output.split(".gdb\\")
out_path = out_path + ".gdb"

geometry_type = "POLYLINE"
template = ""
has_m = "ENABLED"
has_z = "ENABLED"

Profile_line_Output = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                          has_z)

if Profile_Points == True:
    Profile_Points_Output = arcpy.GetParameterAsText(5)
    out_path, out_name = Profile_Points_Output.split(".gdb\\")
    out_path = out_path + ".gdb"
else:
    out_path = "in_memory"
    out_name = "temp_name"

geometry_type = "POINT"
template = ""
has_m = "DISABLED"
has_z = "ENABLED"

Point_Feature_Class2 = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                           has_z)

for row in arcpy.da.SearchCursor(Profile_line_Input, ["SHAPE@"]):
    line = row[0]

    if arcpy.GetParameter(6) == True:
        X = line.lastPoint.X
        Y = line.lastPoint.Y
    else:
        X = line.firstPoint.X
        Y = line.firstPoint.Y


for Surface in Surface_Input_List:
    i = 0
    out_path = "in_memory"
    out_name = "PROFILE_POINTS"
    geometry_type = "POINT"
    template = ""
    has_m = "DISABLED"
    has_z = "ENABLED"

    Point_Feature_Class = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                            has_z)



    Input_Features = Profile_line_Input
    Output_Feature_Class = Point_Feature_Class
    Point_Placement = "DISTANCE"

    Point_Feature_Class = arcpy.GeneratePointsAlongLines_management (Input_Features, Output_Feature_Class,
                                    Point_Placement, Distance_Input + " Meters", Include_End_Points='END_POINTS')



    point = arcpy.Point()
    point2 = arcpy.Point()
    array = arcpy.Array()
    array2 = arcpy.Array()

    for row in arcpy.da.SearchCursor(Point_Feature_Class, ["SHAPE@", "SHAPE@XY"]):
        surface = arcpy.SelectLayerByLocation_management(Surface, "INTERSECT", row[0], None, "NEW_SELECTION")
        X4, Y4 = row[1]
        Z4 = 100

        point_tmp = arcpy.Point(X4,Y4,Z4)
        array.add(point_tmp)


        out_path = "in_memory"
        out_name = "point_tmp"
        geometry_type = "POINT"
        template = ""
        has_m = "DISABLED"
        has_z = "ENABLED"

        Point_Feature_Class_tmp = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                                  has_z)

        cursor = arcpy.da.InsertCursor(Point_Feature_Class_tmp, ['SHAPE@'])
        cursor.insertRow(array)
        del cursor


        in_features = Point_Feature_Class_tmp
        near_features = surface
        Point_Feature_Class_tmp = arcpy.Near3D_3d(in_features,near_features, delta="DELTA")

        cursor = arcpy.da.SearchCursor(Point_Feature_Class_tmp, ["NEAR_DELTZ"])

        for row in cursor:
            z = str(row)
            z = z.replace(",", "").replace("(", "").replace(")", "")

            if z == "None":
                i = +1

            if z != "None":
                newZ = Z4+float(z)

                point2 = arcpy.Point(X4, Y4, newZ)
                array2.add(point2)

                Surface_part = Surface+"_part_"+str(i)

                arcpy.AddField_management(Point_Feature_Class2, "Surface_name", "TEXT")
                arcpy.AddField_management(Point_Feature_Class2, "Elevation", "FLOAT")
                arcpy.AddField_management(Point_Feature_Class2, "Distance", "FLOAT")
                arcpy.AddField_management(Point_Feature_Class2, "Surface_part", "TEXT")
                cursor = arcpy.da.InsertCursor(Point_Feature_Class2, ["SHAPE@", "Surface_name","Elevation",
                                                                      "Distance", "Surface_part"])

                xyz = (X4, Y4, newZ)
                distance = math.sqrt(((X4-X)**2)+((Y4-Y)**2))

                cursor.insertRow([xyz, Surface, newZ, distance, Surface_part])




        del cursor
        array.removeAll()
        #######################################################################

    #arcpy.Sort_management(Point_Feature_Class2,Point_Feature_Class2,[["Elevation", "ASCENDING"]])

    inFeatures = Point_Feature_Class2
    outFeatures = Profile_line_Output
    lineField = "Surface_part"
    #sort_Field = "Distance"

    # Execute PointsToLine
    arcpy.PointsToLine_management(inFeatures, outFeatures, lineField) #sort_Field)
