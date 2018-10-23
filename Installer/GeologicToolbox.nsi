# set the name of the installer
Name "Geologic Toolbox"
OutFile "GeologicToolbox.exe"

# define the standard ArcGIS Pro directory
InstallDir "C:\Program Files\ArcGIS\Pro\"

# create the dialog windows
Page directory
Page instfiles

# create the section
Section

# define the setout path and the files which should be copied in that folder
SetOutPath $INSTDIR\Resources\ArcToolBox\Toolboxes

File .\GoCAD_import.py
File .\GoCAD_import_color.py
File .\Import_bif2.py
File .\Import_Tin.py
File .\Polygon_Intersect.py
File .\Profile_cut.py
File .\SurfaceTools.tbx
 
SectionEnd