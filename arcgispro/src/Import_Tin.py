import arcpy
import os
import sys
from arcpy import env


# Import Dude Tin
Input_Dude_Tin = arcpy.GetParameterAsText(0)
Workspace_Output = arcpy.GetParameterAsText(1)

Dataset_Output, Dataset_name = Workspace_Output.split(".gdb\\")
Dataset_Output = Dataset_Output+".gdb"

Polygon_Dataset = arcpy.CreateFeatureDataset_management(Dataset_Output, Dataset_name)

Input_Dude_Tin_List = Input_Dude_Tin.split(";")

for Input_Tin in Input_Dude_Tin_List:

    #geometry_type = "POLYLINE"
    #template = ""
    #has_m = "ENABLED"
    #has_z = "ENABLED"

    #Polyline_Feature_Class = arcpy.CreateFeatureclass_management("in_memory", "out_name", geometry_type, template,
                                                                 #has_m, has_z)

    t = os.path.basename(Input_Tin)
    name_surface = t.split(".")[0]

    out_path = Polygon_Dataset
    out_name = name_surface
    geometry_type = "POLYGON"
    template = ""
    has_m = "ENABLED"
    has_z = "ENABLED"

    Polygon_Feature_Class = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template,
                                                                has_m, has_z)

    Input_Tin = Input_Tin.replace("'", "")
    Surface1 = open(Input_Tin, "r")
    Surface2 = open(Input_Tin, "r")
    Surface3 = open(Input_Tin, "r")

    array = arcpy.Array()
    array12 = arcpy.Array()
    array22 = arcpy.Array()
    point = arcpy.Point()

    for line in Surface1:
        if (line.rsplit()[0]=="NODES"):
            for line in Surface1:
                if (line.rsplit()[0] == "EDGES"):
                    break
                ID, X, Y, Z = line.split()

                ID = int(ID)
                X = float(X)
                Y = float(Y)
                Z = float(Z)

                point = arcpy.Point(X,Y,Z,False,ID)
                array.add(point)


    for line in Surface2:
        if (line.rsplit()[0] == "EDGES"):
            for line in Surface2:
                if (line.rsplit()[0] == "TRIANGLES"):
                    break
                ID, Point1, Point2, M = line.split()

                ID = int(ID)
                Point1 = int(Point1)
                Point2 = int(Point2)


                array12.add(array.getObject(Point1-1))
                array22.add(array.getObject(Point2-1))

                #polyline = arcpy.Polyline(array2,None,True,False)
                #cursor = arcpy.da.InsertCursor(Polyline_Feature_Class, ['SHAPE@'])
                #cursor.insertRow([polyline])
                #array2.removeAll()

    # arcpy.FeatureToPolygon_management(Polyline_Feature_Class,Polygon_Feature_Class)

    array3 = arcpy.Array()

    for line in Surface3:
        if (line.rsplit()[0] == "TRIANGLES"):
            for line in Surface3:
                ID, P1, P2, P3 = line.split()

                ID = int(ID)
                P1 = int(P1)
                P2 = int(P2)
                P3 = int(P3)


                ID1 = array12.getObject(P1-1).ID
                ID2 = array22.getObject(P1-1).ID

                ID3 = array12.getObject(P2-1).ID
                ID4 = array22.getObject(P2-1).ID

                ID5 = array12.getObject(P3-1).ID
                ID6 = array22.getObject(P3-1).ID

                PointArray =([ID1,ID2,ID3,ID4,ID5,ID6])
                PointSet = set(PointArray)

                #arcpy.AddMessage(PointSet)
                #arcpy.AddMessage("#######")

                #array3.add(array12.getObject(P1-1))
                #array3.add(array22.getObject(P1-1))

                #array3.add(array12.getObject(P2-1))
                #array3.add(array22.getObject(P2-1))

                #array3.add(array12.getObject(P3-1))
                #array3.add(array22.getObject(P3-1))

                for e in PointSet:
                    array3.add(array.getObject(e-1))

                polygon = arcpy.Polygon(array3, None, True, True)
                polygon.M = ID
                Polygon3DLenght = polygon.length3D


                arcpy.AddField_management(Polygon_Feature_Class,'length3D', "FLOAT")
                cursor = arcpy.da.InsertCursor(Polygon_Feature_Class, ['SHAPE@', 'length3D'])
                cursor.insertRow([polygon,Polygon3DLenght])

                array3.removeAll()