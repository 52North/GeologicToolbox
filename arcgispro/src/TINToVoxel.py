import arcpy, os, shutil

# only for testing, overwrites existing files
# arcpy.env.overwriteOutput = True

# create feature dataset, to save voxel feature classes, at given path
outputDatasetPoint = arcpy.GetParameterAsText(5)
pointGeodatabase, pointDatasetName = outputDatasetPoint.split(".gdb\\")
pointGeodatabase = pointGeodatabase + ".gdb"
featureDataset = arcpy.CreateFeatureDataset_management(pointGeodatabase, pointDatasetName)

# creates a new directory for temporary datasets, which cannot be saved in a gdb
dir = r"" + pointGeodatabase.split(".gdb")[0] + "/TINRaster"
if not os.path.exists(dir):
    os.makedirs(dir)
os.chdir(dir)
arcpy.env.workspace = dir

# gets list of given input polygon layers
inputFeatureClassList = arcpy.GetParameterAsText(0).split(";")
# gets extent of first feature class as reference value
desc = arcpy.Describe(inputFeatureClassList[0])
xmin = desc.extent.XMin
xmax = desc.extent.XMax
ymin = desc.extent.YMin
ymax = desc.extent.YMax
zmin = desc.extent.ZMin
zmax = desc.extent.ZMax

# gets extent from all input feature classes and creates output feature classes sorted by geological layer
outputFeatureClassList = []
featureDict = {}
for i in inputFeatureClassList:
    desc = arcpy.Describe(i)
    if xmin > desc.extent.XMin:
        xmin = desc.extent.XMin
    if xmax < desc.extent.XMax:
        xmax = desc.extent.XMax
    if ymin > desc.extent.YMin:
        ymin = desc.extent.YMin
    if ymax < desc.extent.YMax:
        ymax = desc.extent.YMax
    if zmin > desc.extent.ZMin:
        zmin = desc.extent.ZMin
    if zmax < desc.extent.ZMax:
        zmax = desc.extent.ZMax

    # gets geological layer name
    searchCursor = arcpy.da.SearchCursor(i, arcpy.GetParameterAsText(4))
    for row in searchCursor:
        geoLayerName = row[0]
        break
    del searchCursor
    geoLayerName = geoLayerName.strip()

    # creates output feature classes named after the geological layer
    if geoLayerName not in featureDict:
        newFeatureClass = arcpy.CreateFeatureclass_management(featureDataset, geoLayerName, "POINT", "", "DISABLED",
                                                              "ENABLED")
        tempDict = {geoLayerName: newFeatureClass}
        featureDict.update(tempDict)
    else:
        newFeatureClass = featureDict[geoLayerName]
    outputFeatureClassList.append(newFeatureClass)

# calculates number and size of voxels based on the input
if arcpy.GetParameterAsText(1) == "Voxelanzahl":
    # only number of x or y due to square base voxel
    numberVoxelX = int(arcpy.GetParameterAsText(2))
    numberVoxelZ = int(arcpy.GetParameterAsText(3))

    widthLength = (xmax - xmin) / numberVoxelX
    numberVoxelY = int(round((ymax - ymin) / widthLength))
    height = (zmax - zmin) / numberVoxelZ

else:
    # only one edge length for x and y due to square base voxel
    widthLength = int(arcpy.GetParameterAsText(2))
    height = int(arcpy.GetParameterAsText(3))

    numberVoxelX = int(round((xmax - xmin) / widthLength))
    numberVoxelY = int(round((ymax - ymin) / widthLength))
    numberVoxelZ = int(round((zmax - zmin) / height))

# prints out the calculated numbers
arcpy.AddMessage("Width and length of the voxels: " + str(widthLength))
arcpy.AddMessage("Height of the voxels: " + str(height))
arcpy.AddMessage("Number of voxels in x direction: " + str(numberVoxelX))
arcpy.AddMessage("Number of voxels in y direction: " + str(numberVoxelY))
arcpy.AddMessage("Number of voxels in z direction: " + str(numberVoxelZ))

# creates rasters derived from tins of the input feature classes
# cellsize of the rasters are the base of the voxels
rasterList = []
for i in inputFeatureClassList:
    desc = arcpy.Describe(i)
    tin = arcpy.ddd.CreateTin(desc.file, "", desc.catalogPath + " Shape.Z masspoints")

    raster = arcpy.ddd.TinRaster(tin, desc.file[:9] + "_ra", "FLOAT", "LINEAR", "CELLSIZE " + str(widthLength))
    rasterList.append(raster)

# writes the voxels sorted in the output feature classes
featureclass = ""
for ix in range(0, numberVoxelX):
    for iy in range(0, numberVoxelY):
        # gets the thresholds of the geological layers for the x, y position out of the rasters
        threshold = []
        for i in rasterList:
            x = xmin + widthLength / 2 + ix * widthLength
            y = ymin + widthLength / 2 + iy * widthLength
            result = arcpy.GetCellValue_management(i, str(x) + " " + str(y))
            s = result.getOutput(0)
            if s == "NoData":
                cell = 0
            else:
                cell = float(s.replace(",", "."))
            threshold.append(cell)

        # links the threshold values with the output feature classes they belong to and sort it by value
        linklist = sorted(zip(threshold, outputFeatureClassList), key=lambda list: list[0])

        # writes the point in the feature class
        for iz in range(0, numberVoxelZ):
            z = zmin + height / 2 + iz * height

            if linklist:
                # checks if current z value is higher than threshold
                while z > linklist[0][0]:
                    linklist.pop(0)
                    if not linklist:
                        break

                if linklist:
                    if featureclass != linklist[0][1]:
                        featureclass = linklist[0][1]
                        curs = arcpy.da.InsertCursor(featureclass, ["SHAPE@"])
                    # andere Möglichkeit(erst koords abspeichern dann schreiben)
                    curs.insertRow([arcpy.Point(x, y, z)])

del curs
# removes the rasters and tins directory
shutil.rmtree(dir)

arcpy.AddMessage("Please follow the instructions to create the final voxel model:")
arcpy.AddMessage("1.: Activate Display 3D symbols in real-world units at Settings -> Display for every feature layer.")
arcpy.AddMessage("2. (opt.): Choose a Vertical Exaggeration at Settings -> Elevation for every feature layer.")
arcpy.AddMessage("3.: Change the symbol of every feature layer to cube, located at 3D model marker.")
arcpy.AddMessage("4.: Change color and size of the cube at Symbology -> Properties -> Layers. Width and length have to be like above. Height like above multiplied by the chosen exaggeration.")
arcpy.AddMessage("Note: If the exaggeration is changed, the height has to be changed too.")
