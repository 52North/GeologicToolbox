# import arcpy to be able to use the feature of the programming  interface
# import datetime from datetime to get the current time for logging
import arcpy
from datetime import datetime


# define project, in which you work
project = arcpy.mp.ArcGISProject("CURRENT")


# get a specified geodatabase from the user or use the default geodatabase
# of the project
gdb = arcpy.GetParameterAsText(0)
# get the polygon layers (at least 2) from the user and sort them by their name
polygon_layers = arcpy.GetParameterAsText(1)
tin_folder = arcpy.GetParameterAsText(2)
save_mp_in_dataset = arcpy.GetParameterAsText(3)
mp_dataset_name = arcpy.GetParameterAsText(4)
logging_file = arcpy.GetParameterAsText(5)
save_voxel_in_dataset = arcpy.GetParameterAsText(6)
voxel_dataset_name = arcpy.GetParameterAsText(7)
vX = int(arcpy.GetParameterAsText(8))
vY = int(arcpy.GetParameterAsText(9))
vZ = int(arcpy.GetParameterAsText(10))

logging_file_opened = open(logging_file, "w")

if gdb == "":
    arcpy.AddMessage("No geodatabase specified. Using default geodatabase, of the project.")
    logging_file_opened.write(str(datetime.now()) + " No geodatabase specified. Using default geodatabase, of the project.\n")
    gdb = project.defaultGeodatabase
else:
    arcpy.AddMessage("Using specified geodatabase.")
    logging_file_opened.write(str(datetime.now()) + " Using specified geodatabase.\n")

if polygon_layers == "":
    arcpy.AddMessage("No polygon layers specified. Please input at least two\
            polygon layers.")
    logging_file_opened.write(str(datetime.now()) + " None, or not enough Polygon layers as input.\
            Please input at least two polygon layers.\n")
else:
    list_of_polygons = [layer for layer in polygon_layers.split(";")]

sorted_LOP = sorted(list_of_polygons)

arcpy.AddMessage("Read and sorted all polygon layers.")
logging_file_opened.write(str(datetime.now()) + " Read and sorted all polygon layers.\n")

# create TIN for every single polygon layer
list_of_tins = []
spatial_reference = None

if tin_folder == "":
    arcpy.AddMessage("No folder for TINs specified. Using geodatabase.")
    logging_file_opened.write(str(datetime.now()) + " No folder for TINs specified. Using geodatabase.\n")
    tin_folder = gdb
else:
    arcpy.AddMessage("Using specified folder for TINs.")
    logging_file_opened.write(str(datetime.now()) + " Using specified folder for TINs.\n")

arcpy.AddMessage("Creating TINs")
logging_file_opened.write(str(datetime.now()) + " Creating TINs.\n")

# all layers should be in the same reference system
for layer in sorted_LOP:
    # get spatial reference of the layer with arcpy.Describe
    desc = arcpy.Describe(layer)
    spatial_reference = desc.spatialReference.factoryCode
    if spatial_reference == 0:
        spatial_reference = None
    list_of_tins.append(arcpy.ddd.CreateTin(
        tin_folder + "\\" + desc.baseName + "_tin", spatial_reference,
        layer + " Shape.Z masspoints"))

spatial_reference_for_MPDataset = spatial_reference

# check if the box for the Multipatches is ticked
if save_mp_in_dataset == "true":
    arcpy.AddMessage("Creating Dataset for Multipatches...")
    logging_file_opened.write(str(datetime.now()) + " Creating Dataset for Mutltipatches...\n")
    # check if user gave the dataset a name
    if mp_dataset_name != "":
        gdb_for_mp = arcpy.CreateFeatureDataset_management(gdb, mp_dataset_name, spatial_reference_for_MPDataset)
        arcpy.AddMessage("Created Dataset with name: "+str(mp_dataset_name))
        logging_file_opened.write(str(datetime.now()) + " Created Dataset with name: " + str(mp_dataset_name) + "\n")
    else:
        gdb_for_mp = arcpy.CreateFeatureDataset_management(gdb, "dataset_mp", spatial_reference_for_MPDataset)
        arcpy.AddMessage("No Dataset name given. Name set to 'dataset_mp'.")
        logging_file_opened.write(str(datetime.now()) + " No Dataset name given. Name set to 'dataset_mp'.\n")
else:
    gdb_for_mp = gdb

arcpy.AddMessage("Starting extrusion...")
logging_file_opened.write(str(datetime.now()) + " Starting extrusion...\n")

list_of_mp = []

for i, base in enumerate(sorted_LOP[1::2]):
    desc = arcpy.Describe(base)
    try:
        out_feature_class = str(gdb_for_mp) + "\\" + desc.baseName + "_extruded"
        arcpy.ExtrudeBetween_3d(list_of_tins[i*2+1], list_of_tins[i*2], base, out_feature_class)
        arcpy.AddMessage((desc.baseName + " extruded!"))
        logging_file_opened.write(str(datetime.now()) + " " + desc.baseName + " extruded!\n")
        list_of_mp.append(out_feature_class)  # append list of Multipatches to later be used
    except:
        logging_file_opened.write(str(arcpy.GetMessages()))
        arcpy.AddMessage(("A problem occured with: {}. Message: {}.\n".format(desc.baseName, arcpy.GetMessages())))

# check if the box for the Multipatches is ticked
if save_voxel_in_dataset == "true":
    arcpy.AddMessage("Creating Dataset for Voxel...")
    logging_file_opened.write(str(datetime.now()) + " Creating Dataset for Voxel...\n")
    # check if user gave the dataset a name
    if voxel_dataset_name != "":
        gdb_for_voxel = arcpy.CreateFeatureDataset_management(gdb, voxel_dataset_name, spatial_reference_for_MPDataset)
        arcpy.AddMessage("Created Dataset with name: "+str(voxel_dataset_name))
        logging_file_opened.write(str(datetime.now()) + " Created Dataset with name: " + str(voxel_dataset_name)+"\n")
    else:
        gdb_for_voxel = arcpy.CreateFeatureDataset_management(gdb, "dataset_voxel", spatial_reference_for_MPDataset)
        arcpy.AddMessage("No Dataset name given. Name set to 'dataset_voxel'.")
        logging_file_opened.write(str(datetime.now()) + " No Dataset name given. Name set to 'dataset_voxel'.\n")

else:
    gdb_for_voxel = gdb

for mp in list_of_mp:
    # Use arcpy.Describe to get spatial reference and extent of the Features
    desc = arcpy.Describe(mp)
    isClosed = False
    # Check if the Features are closed - only closed features can be used to build Voxel
    arcpy.IsClosed3D_3d(mp)
    with arcpy.da.SearchCursor(mp, ["IsClosed"]) as sC:
        for row in sC:
            if row[0] != "No":
                isClosed = True
    # Only start the process of creating Voxel if the Multipatch-Feature is closed
    if isClosed is True:
        # Create a Point-Feature Class that holds the Voxel
        arcpy.AddMessage("Creating Voxel Feature Class for: {}".format(desc.baseName))
        logging_file_opened.write(str(datetime.now()) + " Creating Voxel Feature Class for: {}\n".format(desc.baseName))
        mp_name = desc.baseName
        spatial_reference = desc.spatialReference.factoryCode
        if spatial_reference == 0:
            spatial_reference = None
        voxelFC = arcpy.CreateFeatureclass_management(
            gdb_for_voxel, mp_name + "_voxelFC", "POINT", "", "DISABLED", "ENABLED",
            spatial_reference)
        # Add a "TYPE"-field to manage which Voxel are in the Multipatch and which are not
        arcpy.AddField_management(voxelFC, "TYPE", "SHORT")

        # Mathematical calculations to determine extent and distance between Voxel
        extent = desc.extent
        xMin, xMax, yMin, yMax, zMin, zMax = extent.XMin, extent.XMax, extent.YMin, extent.YMax, extent.ZMin, extent.ZMax
        # arcpy.AddMessage((xMin, xMax, yMax, yMax, zMin, zMax))

        xDist = xMax - xMin
        yDist = yMax - yMin
        zDist = zMax - zMin

        xStep = xDist / vX
        yStep = yDist / vY
        zStep = zDist / vZ

        cur = arcpy.da.InsertCursor(voxelFC, ["SHAPE@", "TYPE"])

        # Add Voxel to Voxel-Feature Class one by one
        arcpy.AddMessage("creating voxel...")
        logging_file_opened.write(str(datetime.now()) + " creating voxel...\n")
        for i in range(0, vX):
            for j in range(0, vY):
                for k in range(0, vZ):
                    voxel = arcpy.Point(xMin + i * xStep, yMin + j * yStep, zMin + k * zStep)
                    cur.insertRow([voxel, 0])
        del cur

        # Create Table that holds information about whether points are inside of
        # Multipatch or not
        outTbl = arcpy.CreateUniqueName(r"in_memory\outTbl.dbf")

        # Check which points are Inside with "Inside3D_3d"
        # This process makes up for about 95% of the tools runtime
        arcpy.AddMessage("starting inside check...")
        logging_file_opened.write(str(datetime.now()) + " starting inside check...\n")
        inside = arcpy.Inside3D_3d(voxelFC, mp, outTbl)

        # Set cursors on table and Feature Class
        curTbl = arcpy.da.SearchCursor(outTbl, ["TARGET_ID"])
        curFC = arcpy.da.UpdateCursor(voxelFC, ["OID@", "TYPE"])

        # Delete Feature Class rows that are not present in the table
        arcpy.AddMessage("deleting false rows...")
        for row in curTbl:
            for rows in curFC:
                if rows[0] == row[0]:
                    rows[1] = 1
                    curFC.updateRow(rows)
                    break
                else:
                    curFC.deleteRow()
        curFC.reset()

        # Delete rows that were missed after that last row that is present in the Table
        for row in curFC:
            if row[1] == 0:
                curFC.deleteRow()

        del curFC
        del curTbl

        arcpy.AddMessage("Done with: {}".format(desc.baseName))
        logging_file_opened.write(str(datetime.now()) + " Done with: {}\n".format(desc.baseName))
        instructions = "To properly visualize the Voxel: Point Feature "
        instructions += "Class -> Properties -> Display -> Display 3D "
        instructions += "symbols in real-world units -> Symbology -> Symbol "
        instructions += "-> ArcGIS 3D -> Centered Cube -> Properties -> "
        instructions += "Height: {}, Width: {}, Depth: {}".format(zStep, xStep, yStep)
        arcpy.AddMessage(instructions)
        logging_file_opened.write(str(datetime.now()) + " " + instructions + "\n")
    else:
        arcpy.AddMessage("Skipping: {}, because it is not closed.".format(desc.baseName))
        logging_file_opened.write(str(datetime.now()) + " Skipping: {}, because it is not closed.\n".format(desc.baseName))
if logging_file_opened:
    logging_file_opened.close()
