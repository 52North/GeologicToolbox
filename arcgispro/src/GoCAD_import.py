import arcpy
import os
import sys
from arcpy import env

if sys.version_info > (3,):
    long = int

################################## Read the parameter values ##################################################
# Output_Dataset_Polygon
Output_Dataset_Polygon = arcpy.GetParameterAsText(1)
Polygon_Geodatabase, Polygon_Dataset_name = Output_Dataset_Polygon.split(".gdb\\")
Polygon_Geodatabase = Polygon_Geodatabase+".gdb"

Polygon_Dataset = arcpy.CreateFeatureDataset_management(Polygon_Geodatabase, Polygon_Dataset_name)

# Output_Dataset_Multipatch
Output_Dataset_Multipatch = arcpy.GetParameterAsText(2)
MultiPatch_Geodatabase, MultiPatch_Dataset_name = Output_Dataset_Multipatch.split(".gdb\\")
MultiPatch_Geodatabase = MultiPatch_Geodatabase+".gdb"

MultiPatch_Dataset = arcpy.CreateFeatureDataset_management(MultiPatch_Geodatabase, MultiPatch_Dataset_name)

transfer_attributes = arcpy.GetParameter(3)
#################################################################################################################

# Input_GOCAD_Dataset
Input_GOCAD = arcpy.GetParameterAsText(0)
Input_GOCAD_List = Input_GOCAD.split(";")

for GOCAD in Input_GOCAD_List:

    GOCAD = GOCAD.replace("'", "")

    # Check the first character
    if GOCAD[0].isalpha():
        arcpy.AddMessage(GOCAD)
    else:
        GOCAD = "p" + GOCAD
        arcpy.AddMessage(GOCAD)

    #####################################################################################################

    def make_surface(name_tsurf):

        arcpy.env.workspace = Polygon_Geodatabase

        # Create empty Polygon Feature Class in File-Geodatabase
        out_path = Polygon_Dataset
        out_name = name_tsurf
        geometry_type = "POLYGON"
        template = ""
        has_m = "DISABLED"
        has_z = "ENABLED"

        Polygon_Feature_Class = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template,has_m, has_z)

        # Create empty MultiPatch Feature Class in File-Geodatabase
        out_path = MultiPatch_Dataset
        out_name = name_tsurf+"_Multipatch"
        geometry_type = "MULTIPATCH"
        template = ""
        has_m = "DISABLED"
        has_z = "ENABLED"

        MultiPatch_Feature_Class = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template,has_m, has_z)

        # Create triangle polygons
        GoCAD_File2 = open(GOCAD, "r")
        GoCAD_File3 = open(GOCAD, "r")

        array = arcpy.Array()
        array2 = arcpy.Array()

        for line in GoCAD_File2:
            if (line.rsplit()[0]=="VRTX" or line.rsplit()[0]=="PVRTX"):
                point = arcpy.Point(float(line.rsplit()[2]),float(line.rsplit()[3]),float(line.rsplit()[4]),
                                    False, int(line.rsplit()[1]))

                array.add(point)

            if (line.split()[0] == "END"):
                break

        for line in GoCAD_File3:
            if (line.split()[0] == "TRGL"):
                p1 = long(line.split()[1])
                p2 = long(line.split()[2])
                p3 = long(line.split()[3])

                array2.add(array.getObject(p1-1))
                array2.add(array.getObject(p2-1))
                array2.add(array.getObject(p3-1))
                polygon = arcpy.Polygon(array2, None, True, True)

                array2.removeAll()

                cursor = arcpy.da.InsertCursor(Polygon_Feature_Class,['SHAPE@'])
                cursor.insertRow([polygon])

                del cursor

        return Polygon_Feature_Class, MultiPatch_Feature_Class

    ################################## Main Program ###########################################################

    # Check the first line in the GOCAD File: Checking the data type, object type and version
    GoCAD_File = open(GOCAD, "r")
    name_surface = ""
    for line in GoCAD_File:

        if line.rsplit()[0] == "GOCAD":
            data_type, object_type, version = line.split()

            arcpy.AddMessage("Data_type: " + data_type)
            arcpy.AddMessage("Version: " + version)

            # Check if TSurf / PLine / TSolid
            if object_type == "TSurf":
                arcpy.AddMessage("Object type: " + object_type)
            elif object_type == "PLine":
                arcpy.AddMessage("Object type: " + object_type)
            elif object_type == "TSolid":
                arcpy.AddMessage("Object type: " + object_type)
            else:
                arcpy.AddError("No object type found")

            t = os.path.basename(GOCAD)
            name_surface = t.split(".")[0]

            if name_surface[0].isalpha():
                Polygon_Feature_Class, MultiPatch_Feature_Class = make_surface(name_surface)
                arcpy.AddMessage("END")
            else:
                Polygon_Feature_Class, MultiPatch_Feature_Class = make_surface("p" + name_surface)
                arcpy.AddMessage("END")

        if transfer_attributes == True:
            if (line.rsplit()[0] == "GEOLOGICAL_FEATURE"):
                geological_feature = line.split(' ', 1)[1]

            if(line.rsplit()[0] == "PROJECTION"):
                projection = line.split(' ', 1)[1]

            if(line.rsplit()[0] == "DATUM"):
                datum = line.split(' ', 1)[1]


    if transfer_attributes == True:
        arcpy.AddField_management(Polygon_Feature_Class, "GEOLOGICAL_FEATURE", "TEXT")
        arcpy.AddField_management(Polygon_Feature_Class, "PROJECTION", "TEXT")
        arcpy.AddField_management(Polygon_Feature_Class, "DATUM", "TEXT")

        with arcpy.da.UpdateCursor(Polygon_Feature_Class, "GEOLOGICAL_FEATURE") as cursor:
            for row in cursor:
                row[0] = geological_feature
                cursor.updateRow(row)
        del cursor
        with arcpy.da.UpdateCursor(Polygon_Feature_Class, "PROJECTION") as cursor:
            for row in cursor:
                row[0] = projection
                cursor.updateRow(row)
        del cursor
        with arcpy.da.UpdateCursor(Polygon_Feature_Class, "DATUM") as cursor:
            for row in cursor:
                row[0] = datum
                cursor.updateRow(row)
        del cursor


    lyr = arcpy.MakeFeatureLayer_management(Polygon_Feature_Class, name_surface)
    arcpy.Layer3DToFeatureClass_3d(lyr, MultiPatch_Feature_Class)

    GoCAD_File.close()