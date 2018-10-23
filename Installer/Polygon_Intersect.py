import arcpy
import os
import sys
import math
from arcpy import env

surface1 = arcpy.GetParameterAsText(0)
surface2 = arcpy.GetParameterAsText(1)

new_first_surface = arcpy.GetParameterAsText(2)
new_second_surface = arcpy.GetParameterAsText(3)

tolerance = arcpy.GetParameter(4)

def search_intersect(surface1, surface2, t, Polygon_Intersect):

    k = 0
    array3 = arcpy.Array()
    array4 = arcpy.Array()


    for row2 in arcpy.da.SearchCursor(surface1, ["OID@", "SHAPE@"]):
        for part2 in row2[1]:
            for pnt2 in part2:
                #The first and the last/fourth point are identical
                k = k+1
                if k % 4 != 0:
                    point2 = arcpy.Point(pnt2.X, pnt2.Y, pnt2.Z, row2[0], k)

                    if t == True:
                            newZ = searchInPolygon(surface2, point2, True)
                            point3 = arcpy.Point(pnt2.X, pnt2.Y, newZ, row2[0], k)
                            array3.add(point3)

                            if len(array3) == 3:
                                polygon3 = arcpy.Polygon(array3, None, True, True)
                                array3.removeAll()

                                cursor = arcpy.da.InsertCursor(Polygon_Intersect, ['SHAPE@'])
                                cursor.insertRow([polygon3])
                                del cursor


                    elif t == False:
                            newZ = searchInPolygon(surface2, point2, False)
                            point4 = arcpy.Point(pnt2.X, pnt2.Y, newZ, row2[0], k)
                            array4.add(point4)

                            if len(array4) == 3:
                                polygon4 = arcpy.Polygon(array4, None, True, True)
                                array4.removeAll()

                                cursor = arcpy.da.InsertCursor(Polygon_Intersect, ['SHAPE@'])
                                cursor.insertRow([polygon4])
                                del cursor


def searchInPolygon(surf, point2, bool):

    X4 = point2.X
    Y4 = point2.Y
    Z4 = point2.Z

###################################################################################################################

    out_path = "in_memory"
    out_name = "point_tmp"
    geometry_type = "POINT"
    template = ""
    has_m = "DISABLED"
    has_z = "ENABLED"

    Point_Feature_Class_tmp = arcpy.CreateFeatureclass_management(out_path, out_name,
                                        geometry_type, template, has_m, has_z)

    point_tmp = arcpy.Point(X4,Y4,Z4)
    array = arcpy.Array()
    array.add(point_tmp)
    cursor = arcpy.da.InsertCursor(Point_Feature_Class_tmp, ['SHAPE@'])
    cursor.insertRow(array)
    del cursor
    array.removeAll()

    for row in arcpy.da.SearchCursor(Point_Feature_Class_tmp, ["SHAPE@", "SHAPE@XY"]):
        surf = arcpy.SelectLayerByLocation_management(surf, "INTERSECT", row[0], None, "NEW_SELECTION")

        in_features = Point_Feature_Class_tmp
        near_features = surf
        Point_Feature_Class_tmp = arcpy.Near3D_3d(in_features, near_features, delta="DELTA")

        cursor = arcpy.da.SearchCursor(Point_Feature_Class_tmp, ["NEAR_DELTZ"])
        for row in cursor:
            z = str(row)
            z = z.replace(",", "").replace("(", "").replace(")", "")

            if z != "None":
                newZ = point2.Z + float(z)

###################################################################################################################
                if bool == True:
                    if newZ < point2.Z:
                        return newZ-tolerance
                    else:
                        return point2.Z
                else:
                    if newZ > point2.Z:
                        return newZ+tolerance
                    else:
                        return point2.Z

    return point2.Z

######################################################################################################################

out_path, out_name = new_first_surface.split(".gdb\\")
out_path = out_path + ".gdb"

geometry_type = "POLYGON"
template = ""
has_m = "DISABLED"
has_z = "ENABLED"
Polygon_Intersect1 = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                            has_z)

out_path, out_name = new_second_surface.split(".gdb\\")
out_path = out_path + ".gdb"
geometry_type = "POLYGON"
template = ""
has_m = "DISABLED"
has_z = "ENABLED"
Polygon_Intersect2 = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                            has_z)

arcpy.SelectLayerByAttribute_management(surface1,"CLEAR_SELECTION")
arcpy.SelectLayerByAttribute_management(surface2,"CLEAR_SELECTION")
surface1 = arcpy.SelectLayerByLocation_management(surface1, "INTERSECT_3D", surface2, None, "NEW_SELECTION","INVERT")
arcpy.CopyFeatures_management(surface1, Polygon_Intersect1)

arcpy.SelectLayerByAttribute_management(surface1,"CLEAR_SELECTION")
arcpy.SelectLayerByAttribute_management(surface2,"CLEAR_SELECTION")
surface2 = arcpy.SelectLayerByLocation_management(surface2, "INTERSECT_3D", surface1, None, "NEW_SELECTION","INVERT")
arcpy.CopyFeatures_management(surface2, Polygon_Intersect2)



######################################################################################################################
arcpy.SelectLayerByAttribute_management(surface1,"CLEAR_SELECTION")
arcpy.SelectLayerByAttribute_management(surface2,"CLEAR_SELECTION")
surface1 = arcpy.SelectLayerByLocation_management(surface1, "INTERSECT_3D", surface2, None, "ADD_TO_SELECTION")
surface2 = arcpy.SelectLayerByLocation_management(surface2, "INTERSECT_3D", surface1, None, "ADD_TO_SELECTION")
# Check overlaps between Surface 1 and Surface 2, select overlaps in Polygon 1 and 2
# Create a new Surface 2 with changed elevation
search_intersect(surface1, surface2, False, Polygon_Intersect1)

######################################################################################################################



arcpy.SelectLayerByAttribute_management(surface1,"CLEAR_SELECTION")
arcpy.SelectLayerByAttribute_management(surface2,"CLEAR_SELECTION")
surface1 = arcpy.SelectLayerByLocation_management(surface1, "INTERSECT_3D", surface2, None, "ADD_TO_SELECTION")
surface2 = arcpy.SelectLayerByLocation_management(surface2, "INTERSECT_3D", surface1, None, "ADD_TO_SELECTION")
# Check overlaps between Surface 1 and Surface 2, select overlaps in Polygon 1 and 2
# Create a new Surface 1 with changed elevation
asearch_intersect(surface2, surface1, True, Polygon_Intersect2)

######################################################################################################################

