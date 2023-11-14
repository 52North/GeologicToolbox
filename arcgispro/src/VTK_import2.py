# -------------------------------------------------------------------------------
# Name:        Parser.py
# Purpose:     This script reads through a number of .vtk files thats hold points
#              and cells. It is intended to be run as a script in ArcGIS Pro.
#              The output are Polygon-Feature Classes.
#
# Author:      Till Riemenschneider
#
# Created:     24/01/2019
#
# Email:       riemenschneider.till@gmail.com
#
# -------------------------------------------------------------------------------

import arcpy
from datetime import datetime
arcpy.env.overwriteOutput = True

def replace_special_characters(str): ##
    str = str.replace("\t", " ") 
    str = str.replace("\n", " ") 
    return str

def remove_redundant_blanks(str): ##
    return ' '.join(str.split())

# set output gdb to the current gdb
# read files into a list
# get the names of the files in a list
# check if user wants to save the Feature Classes saved in a dataset
# get the name that the user gave to the dataset
# get the path of the logging file
# open logging file
# set the temporary reference system of the dataset to "None"
files = arcpy.GetParameterAsText(0)
save_vtk_in_dataset = arcpy.GetParameterAsText(1)
vtk_dataset_name = arcpy.GetParameterAsText(2)
logging_file = arcpy.GetParameterAsText(3)

gdb = arcpy.mp.ArcGISProject("CURRENT").defaultGeodatabase

spatial_reference_for_Vtkdataset = None
list_of_files = [file for file in files.split(";")]
list_of_filenames = [str(name.split("\\")[-1]).split(".vtk")[0] for name in list_of_files]
logging_file_opened = open(logging_file, "w")

# check if user wants to save feature classes in dataset
if save_vtk_in_dataset == "true":
    arcpy.AddMessage("Creating Dataset for VTK-Files...")
    logging_file_opened.write(str(datetime.now()) + " Created Dataset for VTK-Files...\n")

    # if a dataset name is given, use it
    if vtk_dataset_name != "":
        gdb_for_vtk = arcpy.CreateFeatureDataset_management(gdb, vtk_dataset_name, spatial_reference_for_Vtkdataset)
        arcpy.AddMessage("Created Dataset with name: "+str(vtk_dataset_name))
        logging_file_opened.write(str(datetime.now()) + " Created Dataset with name: "+str(vtk_dataset_name) + "\n")
    # if no dataset name is given, use "dataset_vtk"
    else:
        gdb_for_vtk = arcpy.CreateFeatureDataset_management(gdb, "dataset_vtk", spatial_reference_for_Vtkdataset)
        arcpy.AddMessage("No Dataset name given. Name set to 'dataset_vtk'.")
        logging_file_opened.write(str(datetime.now()) + " No Dataset name given. Name set to 'dataset_vtk'.\n")
else:
    # if user doesnt want to save in dataset, the current geodatabase of the project is set as the geodatabase
    gdb_for_vtk = gdb


# create a spatial reference, cursor, file, and set has_z to True (we are only using 3d data)
spatialReference = None
cursor = None
openedFile = ""
has_z = True

logging_file_opened.write(str(datetime.now()) + " Starting to parse file(s)...\n")

# enumerate through all vtk files
for i, vtkFile in enumerate(list_of_files):
    try:
        # open the logging file
        # clean up the file path and open the vtk file
        # read all lines into a list
        # get the files name from the list_of_filenames
        # create a polygon feature class for the vtk file
        # put the cursor on the newly created feature class
        vtkFile = vtkFile.replace("'", "")
        openedFile = open(vtkFile, "r")
        allLines = openedFile.readlines()
        lineIndex = 0
        name = list_of_filenames[i]
        messageStart = "Parsing file number " + str(i) + ": " + str(name)
        messageFinished = "Finished with file number " + str(i) + ": " + str(name) + " "
        arcpy.AddMessage(messageStart)
        logging_file_opened.write(str(datetime.now()) + " " + messageStart + "\n")
        path = str(gdb_for_vtk) + "\\" + name
        arcpy.CreateFeatureclass_management(out_path=str(gdb_for_vtk), out_name=name, geometry_type="POLYGON",
                                            spatial_reference=spatialReference, has_z="ENABLED")
        cursor = arcpy.da.InsertCursor(path, ["SHAPE@"])

        # start reading through all lines
        # increment the lineIndex for every line read
        for line in allLines:
            lineIndex += 1
            lineSplit = line.split(" ")

            # ---- read points if any exist and add them to a list----
            if lineSplit[0] == "POINTS":
                sliceStart = lineIndex ##
                sliceEnd = sliceStart + int(lineSplit[1]) ##
                coordAllPoints = []

                for rowOfPoints in allLines[sliceStart:sliceEnd]:
                    rowOfPoints = replace_special_characters(rowOfPoints) ##
                    rowOfPoints = remove_redundant_blanks(rowOfPoints) ##
                    rowOfPointsSplit = rowOfPoints.split(" ")
                    rowOfPointsSplit = rowOfPointsSplit[0:3] ##
                    coordOnePoint = [float(rowOfPointsSplit[0]), float(rowOfPointsSplit[1]), float(rowOfPointsSplit[2])]
                    coordAllPoints.append(coordOnePoint)
                    if len(rowOfPointsSplit) > 3:
                        coordOnePoint = [float(rowOfPointsSplit[3]), float(rowOfPointsSplit[4]), float(rowOfPointsSplit[5])]
                        coordAllPoints.append(coordOnePoint)
                    if len(rowOfPointsSplit) > 6:
                        coordOnePoint = [float(rowOfPointsSplit[6]), float(rowOfPointsSplit[7]), float(rowOfPointsSplit[8])]
                        coordAllPoints.append(coordOnePoint)

            # ---- read cells if any exist and create them from their corresponding points----
            if lineSplit[0] == "CELLS":
                sliceStart2 = lineIndex
                sliceEnd2 = lineIndex + int(lineSplit[1])
                correspondingPoints = []
                pointsCells = []
                array = arcpy.Array()

                for rowOfCells in allLines[sliceStart2:sliceEnd2]:
                    rowOfCells = remove_redundant_blanks(rowOfCells) ##
                    rowOfCellsSplit = rowOfCells.split(" ")
                    rowOfCellsSplit = rowOfCellsSplit[1:4] ## 

                    for i in rowOfCellsSplit:
                        correspondingPoints.append(coordAllPoints[int(i)])
                    pointsCells.append(correspondingPoints)
                    correspondingPoints = []

                for j in range(0, len(pointsCells)):
                    for k in range(0, len(pointsCells[j])):
                        point = arcpy.Point(pointsCells[j][k][0], pointsCells[j][k][1], pointsCells[j][k][2])
                        array.add(point)

                    polygon = arcpy.Polygon(array, spatialReference, has_z)

                    array = arcpy.Array()
                    cursor.insertRow([polygon])

        arcpy.AddMessage(messageFinished)
        logging_file_opened.write(str(datetime.now()) + " " + messageFinished + "\n")

    finally:
        # close all files and delete the cursor to release the lock
        if openedFile:
            openedFile.close()
        if cursor:
            del cursor
if logging_file_opened:
    logging_file_opened.close()
