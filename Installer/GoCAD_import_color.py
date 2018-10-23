import arcpy
import os
import sys
import math
import re
from arcpy import env

Input_GOCAD = arcpy.GetParameterAsText(0)
Input_GOCAD_List = Input_GOCAD.split(";")
i = 0

R= ""
G =""
B =""
Alpha =""
name_surface = ""

def color_layer(R, G, B, Alpha, name_surface):
    R = int(R)
    G = int(G)
    B = int(B)
    Alpha = 100
    aprx = arcpy.mp.ArcGISProject("CURRENT")
    for m in aprx.listMaps():
        for lyr in m.listLayers():
            if lyr.name == name_surface:
                sym = lyr.symbology
                sym.renderer.symbol.color = {'RGB' : [R, G, B, 100]}
                lyr.symbology = sym

                if arcpy.GetParameter(1) == True:
                    Output_Layer_File = arcpy.GetParameterAsText(2)
                    Output_Layer_File = Output_Layer_File+"\\"+lyr.name
                    arcpy.SaveToLayerFile_management(lyr, Output_Layer_File, "ABSOLUTE")

for GOCAD in Input_GOCAD_List:
    GOCAD = GOCAD.replace("'", "")

    GoCAD_File = open(GOCAD, "r")
    for line in GoCAD_File:

        if (line.rsplit()[0] == "name:"):

            t = os.path.basename(GOCAD)
            name_surface = t.split(".")[0]

        if (line.rsplit()[0] == "*solid*color:"):
            color = line.split(' ', 1)[1]
            match = re.search(r'^#(?:[0-9a-fA-F]{3}){1,2}$', color)
            if match:
                h = color.lstrip('#')
                R, G, B = tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))
                Alpha == 100

            else:
                R, G, B, Alpha = color.split(" ")
                R = int((float(R) * 255).__round__(0))
                G = int((float(G) * 255).__round__(0))
                B = int((float(B) * 255).__round__(0))

                if Alpha == "1":
                    Alpha = 100
                else:
                    Alpha = 0
    arcpy.AddMessage(name_surface)
    color_layer(R, G, B, Alpha, name_surface)