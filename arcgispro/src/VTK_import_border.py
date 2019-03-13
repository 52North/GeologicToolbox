# -------------------------------------------------------------------------------
# Name:        Parser.py
# Purpose:     This script reads through a number of .vtk files that hold points
#              and cells. It is intended to be run as a script in ArcGIS Pro.
#              The output are Polyline-Feature Classes. Only the border of the
#              polygons are imported.
#
# Author:      Till Riemenschneider
#
# Created:     11/02/2019
#
# Email:       riemenschneider.till@gmail.com
#
# -------------------------------------------------------------------------------

import arcpy
from datetime import datetime
arcpy.env.overwriteOutput = True


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
    # if no dataset name is given, use "dataset_vtk_border"
    else:
        gdb_for_vtk = arcpy.CreateFeatureDataset_management(gdb, "dataset_vtk_border", spatial_reference_for_Vtkdataset)
        arcpy.AddMessage("No Dataset name given. Name set to 'dataset_vtk_border'.")
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
        arcpy.CreateFeatureclass_management(out_path=str(gdb_for_vtk), out_name=name, geometry_type="POLYLINE",
                                            spatial_reference=spatialReference, has_z="ENABLED")
        cursor = arcpy.da.InsertCursor(path, ["SHAPE@"])

        # start reading through all lines
        # increment the lineIndex for every line read
        for line in allLines:
            lineIndex += 1
            lineSplit = line.split(" ")

            # ---- read points if any exist and add them to a list----
            if lineSplit[0] == "POINTS":
                sliceStart = lineIndex
                if (int(lineSplit[1]) / 3) == int((int(lineSplit[1]) / 3)):
                    sliceEnd = int(int(lineSplit[1]) / 3) + sliceStart
                else:
                    sliceEnd = int(int(lineSplit[1]) / 3) + sliceStart + 1
                coordAllPoints = []

                for rowOfPoints in allLines[sliceStart:sliceEnd]:
                    rowOfPointsSplit = rowOfPoints.split(" ")
                    rowOfPointsSplit = rowOfPointsSplit[0:-1]
                    coordOnePoint = [float(rowOfPointsSplit[0]), float(rowOfPointsSplit[1]), float(rowOfPointsSplit[2])]
                    coordAllPoints.append(coordOnePoint)
                    if len(rowOfPointsSplit) > 3:
                        coordOnePoint = [float(rowOfPointsSplit[3]), float(rowOfPointsSplit[4]), float(rowOfPointsSplit[5])]
                        coordAllPoints.append(coordOnePoint)
                    if len(rowOfPointsSplit) > 6:
                        coordOnePoint = [float(rowOfPointsSplit[6]), float(rowOfPointsSplit[7]), float(rowOfPointsSplit[8])]
                        coordAllPoints.append(coordOnePoint)

            # ---- read cells if any exist and add their lines to a list----
            if lineSplit[0] == "CELLS":
                sliceStart2 = lineIndex
                sliceEnd2 = lineIndex + int(lineSplit[1])

                list_of_lines = []
                correspondingPoints = []
                pointsCells = []
                array = arcpy.Array()

                for rowOfCells in allLines[sliceStart2:sliceEnd2]:
                    rowOfCellsSplit = rowOfCells.split(" ")
                    rowOfCellsSplit = rowOfCellsSplit[1:-1]

                    # order the lines of the polygons by the index of their points
                    # and add them to the list of all lines
                    line1 = [int(rowOfCellsSplit[0]), int(rowOfCellsSplit[1])]
                    line2 = [int(rowOfCellsSplit[1]), int(rowOfCellsSplit[2])]
                    line3 = [int(rowOfCellsSplit[2]), int(rowOfCellsSplit[0])]

                    if line1[1] > line1[0]:
                        orderedLine1 = [line1[1], line1[0]]
                    else:
                        orderedLine1 = line1

                    if line2[1] > line2[0]:
                        orderedLine2 = [line2[1], line2[0]]
                    else:
                        orderedLine2 = line2

                    if line3[1] > line3[0]:
                        orderedLine3 = [line3[1], line3[0]]
                    else:
                        orderedLine3 = line3

                    list_of_lines.append(orderedLine1)
                    list_of_lines.append(orderedLine2)
                    list_of_lines.append(orderedLine3)

                # iterate over all lines, eliminating the duplicates
                i = 0
                while i < 5:
                    for item in list_of_lines:
                        occurrences = list_of_lines.count(item)
                        if occurrences > 1:
                            while occurrences > 0:
                                list_of_lines.remove(item)
                                occurrences -= 1
                    i += 1

                # create polylines from the remaining lines
                for line in list_of_lines:
                    point = arcpy.Point(coordAllPoints[int(line[0])][0], coordAllPoints[int(line[0])][1], coordAllPoints[int(line[0])][2])
                    array.add(point)
                    point = arcpy.Point(coordAllPoints[int(line[1])][0], coordAllPoints[int(line[1])][1], coordAllPoints[int(line[1])][2])
                    array.add(point)
                    polyline = arcpy.Polyline(array, spatialReference, has_z)
                    array = arcpy.Array()
                    cursor.insertRow([polyline])

        arcpy.AddMessage(messageFinished)
        logging_file_opened.write(str(datetime.now()) + " " + messageFinished + "\n")

    finally:
        if openedFile:
            openedFile.close()
        if cursor:
            del cursor
if logging_file_opened:
    logging_file_opened.close()
