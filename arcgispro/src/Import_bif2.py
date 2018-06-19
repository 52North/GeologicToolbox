import arcpy

Input_BIF2 = arcpy.GetParameterAsText(0)
Output_BIF2 = arcpy.GetParameterAsText(1)

Input_BIF2List = Input_BIF2.split(";")

Continuous_borehole = arcpy.GetParameter(2)

cursor = None

if Continuous_borehole == True:
    out_path, out_name = Output_BIF2.split(".gdb\\")
    out_path = out_path + ".gdb"

    geometry_type = "POLYLINE"
    template = ""
    has_m = "DISABLED"
    has_z = "ENABLED"

    Output_BIF2_Line = arcpy.CreateFeatureclass_management(out_path, out_name + "_Continuous_borehole", geometry_type,
                                                           template, has_m, has_z)

    fields = ["BLIDM", "Name", "Laenge", "Beginn_Kernstrecke", "Ansatzpunkt_Rechtswert", "Ansatzpunkt_Hochwert",
              "Ansatzpunkt_Hoehe", "Nadelabweichung", "erster_Bohrtag", "letzter_Bohrtag", "Art_der_Bohrung",
              "Richtung_der_Bohrung", "Auftraggeber", "Bohrverfahren", "Gemarkung_Oertlichkeit", "Status",
              "Neigung_am_Ansatzpunkt", "Richtung_am_Ansatzpunkt", "Bohranlage"]

    for row in fields:
        arcpy.AddField_management(Output_BIF2_Line, row, "TEXT")

    cursor = arcpy.da.InsertCursor(Output_BIF2_Line,
                                   ['SHAPE@', 'BLIDM', 'Name', 'Laenge', 'Beginn_Kernstrecke', 'Ansatzpunkt_Rechtswert',
                                    'Ansatzpunkt_Hochwert', 'Ansatzpunkt_Hoehe', 'Nadelabweichung', 'erster_Bohrtag',
                                    'letzter_Bohrtag', 'Art_der_Bohrung', 'Richtung_der_Bohrung', 'Auftraggeber',
                                    'Bohrverfahren', 'Gemarkung_Oertlichkeit', 'Status', 'Neigung_am_Ansatzpunkt',
                                    'Richtung_am_Ansatzpunkt', 'Bohranlage'])

else:
    out_path, out_name = Output_BIF2.split(".gdb\\")
    out_path = out_path + ".gdb"

    geometry_type = "POLYLINE"
    template = ""
    has_m = "DISABLED"
    has_z = "ENABLED"

    Output_BIF2_Splited_Line = arcpy.CreateFeatureclass_management(out_path, out_name, geometry_type, template, has_m,
                                                                   has_z)

    fields = ["BLIDM", "Name", "Laenge", "Beginn_Kernstrecke", "Ansatzpunkt_Rechtswert", "Ansatzpunkt_Hochwert",
              "Ansatzpunkt_Hoehe", "Nadelabweichung", "erster_Bohrtag", "letzter_Bohrtag", "Art_der_Bohrung",
              "Richtung_der_Bohrung", "Auftraggeber", "Bohrverfahren", "Gemarkung_Oertlichkeit", "Status",
              "Neigung_am_Ansatzpunkt", "Richtung_am_Ansatzpunkt", "Bohranlage", "Schicht_Schicht_ID",
              "Schicht_Bohrmeter", "Schicht_Maechtigkeit", "Schicht_Winkel_am_Kern", "Schicht_Gestein_Code",
              "Schicht_Punkt_Rechtswert", "Schicht_Punkt_Hochwert", "Schicht_Punkt_Hoehe"]

    for row in fields:
        arcpy.AddField_management(Output_BIF2_Splited_Line, row, "TEXT")

    cursor = arcpy.da.InsertCursor(Output_BIF2_Splited_Line,
                                   ['SHAPE@', 'BLIDM', 'Name', 'Laenge', 'Beginn_Kernstrecke', 'Ansatzpunkt_Rechtswert',
                                    'Ansatzpunkt_Hochwert', 'Ansatzpunkt_Hoehe', 'Nadelabweichung', 'erster_Bohrtag',
                                    'letzter_Bohrtag', 'Art_der_Bohrung', 'Richtung_der_Bohrung', 'Auftraggeber',
                                    'Bohrverfahren', 'Gemarkung_Oertlichkeit', 'Status', 'Neigung_am_Ansatzpunkt',
                                    'Richtung_am_Ansatzpunkt', 'Bohranlage', 'Schicht_Schicht_ID', 'Schicht_Bohrmeter',
                                    'Schicht_Maechtigkeit', 'Schicht_Winkel_am_Kern', 'Schicht_Gestein_Code',
                                    'Schicht_Punkt_Rechtswert', 'Schicht_Punkt_Hochwert', 'Schicht_Punkt_Hoehe'])

##################################################################################################################
array = arcpy.Array()
array2 = arcpy.Array()

for BIF2 in Input_BIF2List:
    array2.removeAll()
    BIF2 = BIF2.replace("'", "")

    ###############################################################################################################

    BIF2 = open(BIF2, "r", encoding="utf-16")
    X = 0
    Y = 0
    Z = 0
    Schicht_Gestein_Code_List = list()
    Auftraggeber_List = list()
    Bohranlage_List = list()

    for line in BIF2:
        if Continuous_borehole == True:
            if line.rsplit()[0] == "Bl.Verlauf.Punkt.Rechtswert:":
                X = float(line.rsplit()[1])
            if line.rsplit()[0] == "Bl.Verlauf.Punkt.Hochwert:":
                Y = float(line.rsplit()[1])
            if line.rsplit()[0] == "Bl.Verlauf.Punkt.Hoehe:":
                Z = float(line.rsplit()[1])
                array.add(arcpy.Point(X, Y, Z))

        if line.rsplit()[0] == "Bl.BLIDM:":
            BLIDM = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Name:":
            Name = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Laenge:":
            Laenge = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Beginn_Kernstrecke:":
            Beginn_Kernstrecke = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Ansatzpunkt.Rechtswert:":
            Ansatzpunkt_Rechtswert = float(line.split(' ', 1)[1])

        if line.rsplit()[0] == "Bl.Beschreibung.Ansatzpunkt.Hochwert:":
            Ansatzpunkt_Hochwert = float(line.split(' ', 1)[1])

        if line.rsplit()[0] == "Bl.Beschreibung.Ansatzpunkt.Hoehe:":
            Ansatzpunkt_Hoehe = float(line.split(' ', 1)[1])
            array2.add(arcpy.Point(Ansatzpunkt_Rechtswert, Ansatzpunkt_Hochwert, Ansatzpunkt_Hoehe))

        if line.rsplit()[0] == "Bl.Beschreibung.Nadelabweichung:":
            Nadelabweichung = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.erster_Bohrtag:":
            erster_Bohrtag = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.letzter_Bohrtag:":
            letzter_Bohrtag = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Art_der_Bohrung:":
            Art_der_Bohrung = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Richtung_der_Bohrung:":
            Richtung_der_Bohrung = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Auftraggeber.Text:":
            Auftraggeber = [line.split(' ', 1)[1]]
            Auftraggeber = Auftraggeber[0]

            Auftraggeber_List.append(Auftraggeber)

        if line.rsplit()[0] == "Bl.Beschreibung.Bohrverfahren:":
            Bohrverfahren = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Gemarkung/Oertlichkeit:":
            Gemarkung_Oertlichkeit = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Status:":
            Status = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Neigung_am_Ansatzpunkt:":
            Neigung_am_Ansatzpunkt = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Richtung_am_Ansatzpunkt:":
            Richtung_am_Ansatzpunkt = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Beschreibung.Bohranlage.Text:":
            Bohranlage = [line.split(' ', 1)[1]]
            Bohranlage = Bohranlage[0]

            Bohranlage_List.append(Bohranlage)

        ####################################################################################################

        if line.rsplit()[0] == "#Bl.Schicht.Schicht-ID:":
            Schicht_Schicht_ID = int(line.split(' ', 1)[1])

        if line.rsplit()[0] == "Bl.Schicht.Bohrmeter:":
            Schicht_Bohrmeter = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Schicht.Maechtigkeit:":
            Schicht_Maechtigkeit = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Schicht.Winkel_am_Kern:":
            Schicht_Winkel_am_Kern = line.split(' ', 1)[1]

        if line.rsplit()[0] == "Bl.Schicht.Gestein.Code:":
            Schicht_Gestein_Code = [line.split(' ', 1)[1]]
            Schicht_Gestein_Code = Schicht_Gestein_Code[0]

            Schicht_Gestein_Code_List.append(Schicht_Gestein_Code)

        if line.rsplit()[0] == "Bl.Schicht.Punkt.Rechtswert:":
            Schicht_Punkt_Rechtswert = [line.split(' ', 1)[1]]
            Schicht_Punkt_Rechtswert = float(Schicht_Punkt_Rechtswert[0])

        if line.rsplit()[0] == "Bl.Schicht.Punkt.Hochwert:":
            Schicht_Punkt_Hochwert = [line.split(' ', 1)[1]]
            Schicht_Punkt_Hochwert = float(Schicht_Punkt_Hochwert[0])

        if line.rsplit()[0] == "Bl.Schicht.Punkt.Hoehe:":
            Schicht_Punkt_Hoehe = [line.split(' ', 1)[1]]
            Schicht_Punkt_Hoehe = float(Schicht_Punkt_Hoehe[0])

            if len(Auftraggeber_List) == 1:
                Auftraggeber = ''.join(Auftraggeber_List)
            else:
                Auftraggeber = str(Auftraggeber_List).replace("\\n'", "").replace("['", "").replace("'", "").replace(
                    "]", "")

            if len(Bohranlage_List) == 1:
                Bohranlage = ''.join(Bohranlage_List)
            else:
                Bohranlage = str(Bohranlage_List).replace("\\n'", "").replace("['", "").replace("'", "").replace("]",
                                                                                                                 "")

            if len(Schicht_Gestein_Code_List) == 1:
                Schicht_Gestein_Code = ''.join(Schicht_Gestein_Code_List)
            else:
                Schicht_Gestein_Code = str(Schicht_Gestein_Code_List).replace("\\n'", "").replace("['", "").replace("'",
                                                                                                                    "").replace(
                    "]", "")

            point = arcpy.Point(Schicht_Punkt_Rechtswert, Schicht_Punkt_Hochwert, Schicht_Punkt_Hoehe)
            array2.add(point)

            array3 = arcpy.Array()
            array3.add(array2.getObject(Schicht_Schicht_ID - 1))
            array3.add(array2.getObject(Schicht_Schicht_ID))

            polyline2 = arcpy.Polyline(array3, None, True, False)

            array3.removeAll()

            cursor.insertRow(
                [polyline2, BLIDM, Name, Laenge, Beginn_Kernstrecke, Ansatzpunkt_Rechtswert, Ansatzpunkt_Hochwert,
                 Ansatzpunkt_Hoehe, Nadelabweichung, erster_Bohrtag, letzter_Bohrtag, Art_der_Bohrung,
                 Richtung_der_Bohrung, Auftraggeber, Bohrverfahren, Gemarkung_Oertlichkeit, Status,
                 Neigung_am_Ansatzpunkt, Richtung_am_Ansatzpunkt, Bohranlage, Schicht_Schicht_ID, Schicht_Bohrmeter,
                 Schicht_Maechtigkeit, Schicht_Winkel_am_Kern, Schicht_Gestein_Code, Schicht_Punkt_Rechtswert,
                 Schicht_Punkt_Hochwert, Schicht_Punkt_Hoehe])

            Schicht_Gestein_Code_List.clear()

    if Continuous_borehole == True:
        polyline = arcpy.Polyline(array, None, True, False)

        cursor.insertRow(
            [polyline, BLIDM, Name, Laenge, Beginn_Kernstrecke, Ansatzpunkt_Rechtswert, Ansatzpunkt_Hochwert,
             Ansatzpunkt_Hoehe, Nadelabweichung, erster_Bohrtag, letzter_Bohrtag, Art_der_Bohrung, Richtung_der_Bohrung,
             Auftraggeber, Bohrverfahren, Gemarkung_Oertlichkeit, Status, Neigung_am_Ansatzpunkt,
             Richtung_am_Ansatzpunkt, Bohranlage])

del cursor
