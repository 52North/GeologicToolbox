/**
 * Copyright (C) 2020 52 North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *  - Apache License, version 2.0
 *  - Apache Software License, version 1.0
 *  - GNU Lesser General Public License, version 3
 *  - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *  - Common Development and Distribution License (CDDL), version 1.0.
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License 
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * Contact: Benno Schmidt, 52 North Initiative for Geospatial Open Source 
 * Software GmbH, Martin-Luther-King-Weg 24, 48155 Muenster, Germany, 
 * info@52north.org
 */
package org.n52.v3d.triturus.geologic.importers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.data.Well;
import org.n52.v3d.triturus.geologic.data.WellRepository;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Reader to import well information from CSV export files, e.g. generated by
 * GOCAD). The implementation is based on the following assumptions:
 * <ul>
 * <li>
 *   A whitespace character ' ' is used as delimiter (no support of ';' or ','
 *   has been implemented).
 * </li>
 * <li>
 *   The first line of the input file gives the field names. Supported field
 *   sequences are, e.g.:
 *   <ul>
 *   <li><tt>WELLNAME X Y DATUM KB MAXIMUM_DEPTH</tt> to give well locations, 
 *   or</li> <li><tt>WellName&nbsp;&nbsp;&nbsp;X&nbsp;&nbsp;&nbsp;Y&nbsp;&nbsp;&nbsp;Z&nbsp;&nbsp;&nbsp;MD&nbsp;&nbsp;&nbsp;MarkerName</tt> to give marker 
 *   data.</li>
 *   </ul>
 * </li>
 * <li>
 *   Supported field names are:
 *   <ul>
 *   <li><tt>WELLNAME</tt>: Well identifier (<tt>String</tt>-valued)</li>
 *   <li><tt>X</tt>: <i>x</i>-coordinate</li>
 *   <li><tt>Y</tt>: <i>y</i>-coordinate</li>
 *   <li>
 *     <tt>DATUM</tt>: <i>z</i>-coordinate of well location; considered for 
 *     well locations as <tt>InfoType</tt>.
 *   </li>
 *   <li>
 *     <tt>Z</tt>: <i>z</i>-coordinate of well location, considered for markers
 *     as <tt>InfoType</tt>.
 *   </li>
 *   <li>
 *     <tt>KB</tt>: &quot;Kelly Bushing height&quot;, i.e. the height of the 
 *     drilling floor above the ground level; considered for well locations as 
 *     <tt>InfoType</tt>.
 *   </li>
 *   <li>
 *     <tt>MD</tt>: Marker depth, considered for markers as <tt>InfoType</tt>.
 *   </li>
 *   <li>
 *     <tt>MAXIMUM_DEPTH</tt>: Marker depth, considered for well locations as 
 *     <tt>InfoType</tt>.
 *   </li>
 *   <li>
 *     <tt>MARKERNAME</tt>: Marker identifier (<tt>String</tt>-valued), 
 *     considered for markers as <tt>InfoType</tt>.
 *   </li>		
 *   </ul>	
 * </li>
 * <li>
 *   Non standard-ASCII characters language-specific characters such as German
 *   &quot;umlauts&quot; should be avoided.
 * </li>
 * <li>
 *   Since well and marker names may contain whitespaces, it is assumed that 
 *   <i>x</i>, <i>y</i>, <i>z</i>, and marker depth are given in a continuous 
 *   sequence immediately following the well name while the marker name has to 
 *   be given as last element, i.e. the sequence <tt>well x y z depth marker</tt>!
 * </li>
 * <li>
 *   Double whitespace characters in well and marker names will be ignored 
 *   (respectively purged), e.g. <tt>&quot;Well&nbsp;1&nbsp;&nbsp;B&quot;</tt> 
 *   will be changed to <tt>&quot;Well&nbsp;1&nbsp;B&quot;</tt>.   
 * </li>
 * <li>
 *   To detect the positions of <i>x</i>- and <i>y</i>-coordinates in the file, 
 *   it is assumed that coordinates are given by floating-point numbers &gt;100.000. 
 * </li>
 * </ul>
 * 
 * @author Benno Schmidt
 */
public class IoWellCsvReader 
{
	// TODO: Marker namens "Surface" mit Ground-Level (loc.z) vergleichen
	// TODO: v_ -> virtual well

    /**
     * reads well information from the given CSV file. 
     * 
     * @param filename File name (with path optionally)
     * @return {@link ArrayList} consisting of {@link Well} objects
     * @throws org.n52.v3d.triturus.core.T3dException if an error occurs
     */
    public List<Well> read(String filename) 
    	throws T3dException
    {
        WellRepository wRepo = new WellRepository();
        this.addToRepository(filename, wRepo);
        return wRepo.getWells();        
    }

    /**
     * add the well and marker information given by an well input file to a 
     * given well repository. (See {@link WellRepositoryApp} for an example.)
     * 
     * @param filename File name (with path optionally)
     * @param wRepo Existing well repository
     * @return Resulting well repository (i.e., <tt>wRepo</tt>)
     * @throws T3dException if an error occurs
     */
    public WellRepository addToRepository(String filename, WellRepository wRepo) 
       	throws T3dException
    {
        String line = "";
        int lineNumber = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            // read first input line (field names):
            line = reader.readLine();
            lineNumber++;
            List<String> cols = this.scanTokens(line);
    		InfoType iType = detectInformationType(cols);
    		
    		// read marker data (from line 2 and onwards): 
            line = reader.readLine();
            while (line != null) {
                lineNumber++;

                List<String> tokens = this.scanTokens(line);                
				//System.out.println("\nline #" + lineNumber + ": " + tokens.size() + " tokens");
				//System.out.println(line);
				List<FieldType> tokTypeInfo = this.determineTypes(tokens);
				//for (FieldType type : tokTypeInfo) {
				//	System.out.println("type = " + type);
				//}
				int coordIndex = this.determineCoordPos(tokTypeInfo);

				String _wellName, _markerName;
				Double _x, _y, _z, _datum, _kb, _maximumDepth, _md; 
				
				switch (iType) {
				case Well_Locations:
					_wellName = this.scanWellName(tokens, coordIndex);
					_x = this.scanCoord(tokens, coordIndex);
					_y = this.scanCoord(tokens, coordIndex + 1);
					_datum = this.scanFloat(tokens, coordIndex + 2);
					_kb = this.scanFloat(tokens, coordIndex + 3);
					_maximumDepth = this.scanFloat(tokens, coordIndex + 4);

					if (_wellName != null && _x != null && _y != null && _datum != null) {
						VgPoint pos = new GmPoint(_x, _y, _datum);
						wRepo.addWell(_wellName, pos, _kb, _maximumDepth);
					}
					
					break;
				case Markers:
					_wellName = this.scanWellName(tokens, coordIndex);
					_x = this.scanCoord(tokens, coordIndex);
					_y = this.scanCoord(tokens, coordIndex + 1);
					_z = this.scanFloat(tokens, coordIndex + 2);
					_md = this.scanFloat(tokens, coordIndex + 3);
					_markerName = this.scanMarkerName(tokens, coordIndex + 4);
					
					if (_wellName != null && _x != null && _y != null && _z != null && _markerName != null) {
						VgPoint loc = new GmPoint(_x, _y, _z);
						wRepo.addMarker(_wellName, loc, _md, _markerName);
					}
					
					break;
				case Unknown:
					// TODO
					break;
				}
				
                line = reader.readLine();
            }
            reader.close();
            System.out.println("Read " + lineNumber + " lines from file \"" + filename + "\".");
        }
        catch (FileNotFoundException e) {
        	String msg = "Could not access file \"" + filename + "\".";
        	throw new T3dException(msg);
        }
        catch (IOException e) {
            throw new T3dException(e.getMessage());
        }
        catch (T3dException e) {
            throw new T3dException(e.getMessage());
        }
        catch (Exception e) {
        	String msg = "Parser error in \"" + filename + "\":" + lineNumber;
        	throw new T3dException(msg);
        }

        return wRepo;        
    }
    
	// Scan tokens separated by a whitespace character from the given text line in:
    private List<String> scanTokens(String line) {
    	String[] s = line.split(" ");
    	List<String> tokens = new ArrayList<String>();
		for (String tok : s) {
			if (tok != null && !tok.equalsIgnoreCase(""))
				tokens.add(tok);
		}
    	return tokens;
    }
    
    // Determine data types of given tokens: 
    private enum FieldType { String, Integer, Float, Coord };
	private List<FieldType> determineTypes(List<String> tokens) {
		List<FieldType> resList = new ArrayList<FieldType>();
		for (String tok : tokens) {
			boolean typeInt = true; 
			try { Integer.parseInt(tok); } catch (NumberFormatException e) { typeInt = false; }
			boolean typeFloat = true; 
			Float val = -1.0f;
			try { val = Float.parseFloat(tok); } catch (NumberFormatException e) { typeFloat = false; }
			FieldType res = FieldType.String;
			if (typeFloat) res = (val >= 100000.0f) ? FieldType.Coord : FieldType.Float;
			if (typeInt && !(res == FieldType.Coord)) res = FieldType.Integer;
			resList.add(res);
		}
		return resList;
	}

	private int determineCoordPos(List<FieldType> tokTypeInfo) {
		int i = 0, cand = -1;
		while (i < tokTypeInfo.size() && cand < 0) {
			if (tokTypeInfo.get(i) == FieldType.Coord)
				cand = i;
			i++;	
		}
		if (cand < 0) {
			System.out.println("Could not parse first coordinate from given line.");
			return -1;
		}
		if (tokTypeInfo.size() >= cand + 1) {
			if (tokTypeInfo.get(cand + 1) == FieldType.Coord)
				return cand;
		}
		System.out.println("Could not parse second coordinate from given line.");
		return -1;
	}

	private String scanWellName(List<String> tokens, int coordIndex) {
		if (coordIndex < 1) {
			System.out.println("Could not parse well name from given line.");
			return null;
		}
		String s = tokens.get(0);
		for (int i = 1; i < coordIndex; i++) {
			s = s + " " + tokens.get(i);
		}
		return s;
	}

	private Double scanCoord(List<String> tokens, int pos) {
		try {
			return Double.parseDouble(tokens.get(pos));
		} catch (Exception e) {
			return null;
		}
	}

	private Double scanFloat(List<String> tokens, int pos) {
		if (pos >= tokens.size()) {
			System.out.println("Could not parse float value from given line.");
			return null;
		}
		try {
			return Double.parseDouble(tokens.get(pos));
		} catch (Exception e) {
			return null;
		}
	}

	private String scanMarkerName(List<String> tokens, int pos) {
		if (pos >= tokens.size()) {
			System.out.println("Could not parse marker name from given line.");
			return null;
		}
		String s = tokens.get(pos);
		for (int i = pos + 1; i < tokens.size(); i++) {
			s = s + " " + tokens.get(i);
		}
		return s;
	}

    /**
     * Type of information given in the input file 
     */
    public enum InfoType { Well_Locations, Markers, Unknown };

    // Determine information type: 
	private InfoType detectInformationType(List<String> cols) {
		if (cols == null || cols.size() < 6) 
			return InfoType.Unknown;

		// Control output:
		System.out.print("Field names:");
		for (String col : cols) {
			System.out.print(" \"" + col + "\"");
		}
		System.out.println();

		boolean
			f0WellName = "WELLNAME".equalsIgnoreCase(cols.get(0)),
			f1X = "X".equalsIgnoreCase(cols.get(1)),
			f2Y = "Y".equalsIgnoreCase(cols.get(2)),
			f3Datum = "DATUM".equalsIgnoreCase(cols.get(3)),
			f3Z = "Z".equalsIgnoreCase(cols.get(3)),
			f4KB = "KB".equalsIgnoreCase(cols.get(4)),
			f4MD = "MD".equalsIgnoreCase(cols.get(4)),
			f5MaximumDepth = "MAXIMUM_DEPTH".equalsIgnoreCase(cols.get(5)),
			f5MarkerName = "MARKERNAME".equalsIgnoreCase(cols.get(5));

		InfoType res = InfoType.Unknown;
		if (f0WellName && f1X && f2Y && f3Datum && f4KB && f5MaximumDepth) 
			res = InfoType.Well_Locations;
		if (f0WellName && f1X && f2Y && f3Z && f4MD && f5MarkerName) 
			res = InfoType.Markers;
		System.out.println("Detected InfoType: \"" + res + "\"");
		return res;
	}
}
