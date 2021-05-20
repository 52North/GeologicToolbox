/**
 * Copyright (C) 2018 52 North Initiative for Geospatial Open Source
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
 * Contact: Benno Schmidt and Martin May, 52 North Initiative for Geospatial
 * Open Source Software GmbH, Martin-Luther-King-Weg 24, 48155 Muenster,
 * Germany, info@52north.org
 */
package org.n52.v3d.triturus.geologic.importers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;

import org.n52.v3d.triturus.core.IoObject;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINGeometry;
import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.vgis.VgPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Reader to import a TIN from a GOCAD TSurf file. Note that there are some
 * restrictions concerning this implementation:
 * <ol>
 * <li>GOCAD data file format version 1.0 is assumed.</li>
 * <li>Vertex numbers given in the TSurf file will not be processed. For the
 * n-th given vertex n will be assumed as index number, with n >= 1.</li>
 * <li>Coordinate system support has not been implemented yet, i.e.
 * coordinates are processed as they are given.</li>
 * </ol>
 * For more details, see source code.
 *
 * @author Benno Schmidt
 */
public class IoGocadTSurfReader extends IoObject
{
	private String logInfo = IoGocadTSurfReader.class.getName();

	private List<GocadDataInfo> info;
	private List<GmSimpleTINFeature> tins;

	public IoGocadTSurfReader() {
		info = new ArrayList<GocadDataInfo>();
		tins = new ArrayList<GmSimpleTINFeature>();
	}

	@Override
	public String log() {
		return logInfo;
	}

    /**
     * reads a set of TINs from a given file or URL location.
     *
     * @param location File path or valid URL
     * @return List of TINs, or <i>null</i> if an error occurs
     * @throws org.n52.v3d.triturus.core.T3dException
     * @throws org.n52.v3d.triturus.core.T3dNotYetImplException
     */
    public List<GmSimpleTINFeature> read(String location)
    {
    	return this.read(location, 2);
    }

    public List<GmSimpleTINFeature> read(String location, int untilPass)
    {
        BufferedReader reader;
        List<GmSimpleTINFeature> tins = null;

        try {
        	for (int pass = 1; pass <= untilPass; pass++) {
				if (location.startsWith("http"))
					reader = this.createBufferedReader(new URL(location));
				else
					reader = this.createBufferedReader(location);

				switch (pass) {
				case 1:
					info = this.readGocadTSurf_Pass1(reader, location);
					break;
				case 2:
					tins = this.readGocadTSurf_Pass2(reader, location);
					break;
				}
				reader.close();
        	}
        }
        catch (T3dException e) {
			throw e;
        }
        catch (MalformedURLException e) {
			e.printStackTrace();
		}
        catch (IOException e) {
			e.printStackTrace();
        }

        return tins;
    }

    /**
     * gets information about the content of a GOCAD file. The method returns
     * single information objects for each GOCAD object which is part of the
     * GOCAD file.
     *
     * @param location File path or valid URL
     * @return Information objects, or <i>null</i> if an error occurs
     */
    public List<GocadDataInfo> getInfo(String location) {
    	this.read(location, 1);
    	return info;
    }

    private BufferedReader createBufferedReader(URL url) {
		InputStream is = null;
		BufferedReader reader;
		try {
			is = url.openStream();
			reader = new BufferedReader(new InputStreamReader(is));
		} catch (IOException e) {
			System.out.println("<IoGocadTSurfReader> Data import failed: " + url);
			e.printStackTrace();
			throw new T3dException("Could not open import stream " + url);
		}
		return reader;
    }

    private BufferedReader createBufferedReader(String filename) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader reader;
        try {
        	if (is == null)
        		is = new FileInputStream(filename);
        	reader = new BufferedReader(new InputStreamReader(is));
        }
        catch (Exception e) {
        	System.out.println("<IoGocadTSurfReader> Data import failed: " + filename);
        	e.printStackTrace();
			throw new T3dException("Could not open file " + filename);
        }
        return reader;
    }

    private List<GocadDataInfo>
    	readGocadTSurf_Pass1(BufferedReader reader, String location)
    	throws T3dException
    {
        int lineNumber = 0;
    	String line;
        String tok1, tok2, tok3;

        info = new ArrayList<GocadDataInfo>();

        try { // Quick and dirty "parser" implementation:
            line = reader.readLine();
            lineNumber++;

            while (line != null)
            {
	            GocadDataInfo lInfo = null;

	            boolean objectBegin = false;
	            while (line != null && !objectBegin) {
					tok1 = getStrTok(line, 1, " ");
					if ("GOCAD".equalsIgnoreCase(tok1)) {
						lInfo = new GocadDataInfo();
						lInfo.setObjectType(getStrTok(line, 2, " "));
						objectBegin = true;
					}

					line = reader.readLine();
					lineNumber++;
	            }

	            boolean objectEnd = false;
	            while (line != null && !objectEnd) {
	            	tok1 = getStrTok(line, 1, " ");

	            	if ("name:".equalsIgnoreCase(tok1)) {
	            		if (tok1 != null)
	            			lInfo.setObjectName(getStrTok(line, 2, " "));
	            	}
	            	if ("VRTX".equalsIgnoreCase(tok1)) {
	            		lInfo.incrVrtxCount();
	            	}
	            	if ("PVRTX".equalsIgnoreCase(tok1)) {
	            		lInfo.incrPvrtxCount();
	            	}
	            	if ("ATOM".equalsIgnoreCase(tok1)) {
	            		lInfo.incrAtomCount();
	            	}
	            	if ("TRGL".equalsIgnoreCase(tok1)) {
	            		lInfo.incrTrglCount();
	            	}
	            	if ("BORDER".equalsIgnoreCase(tok1)) {
	            		lInfo.incrBorderCount();
	            	}
	            	if ("BSTONE".equalsIgnoreCase(tok1)) {
	            		lInfo.incrBstoneCount();
	            	}
	            	if ("GEOLOGICAL_TYPE".equalsIgnoreCase(tok1)) {
	            		if (tok1 != null)
	            			lInfo.setGeologicalType(getStrTok(line, 2, " "));
	            	}
	            	if ("GEOLOGICAL_FEATURE".equalsIgnoreCase(tok1)) {
	            		if (tok1 != null)
	            			lInfo.setGeologicalFeature(getStrTok(line, 2, " "));
	            	}
	            	if ("STRATIGRAPHIC_POSITION".equalsIgnoreCase(tok1)) {
	            		if (tok1 != null)
	            			lInfo.setStratigraphicPosition(getStrTok(line, 2, " "));
	            	}
	            	if (
	            		"*border*color:".equalsIgnoreCase(tok1) ||
	            		"*solid*color:".equalsIgnoreCase(tok1))
	            	{
	            		if (tok1 != null) {
	            			boolean solid =
	            				tok1.equalsIgnoreCase("*solid*color") ? true : false;
	            			T3dColor col = new T3dColor();
	            			tok2 = getStrTok(line, 2, ":");
			            	if (tok2.contains("#")) {
				            	tok3 = getStrTok(line, 2, "#");
				            	col.setHexEncodedValue("0x" + tok3);
			            	} else {
				            	float
				            		r = toFloat(getStrTok(tok2, 1, " ")),
				            		g = toFloat(getStrTok(tok2, 2, " ")),
				            		b = toFloat(getStrTok(tok2, 3, " ")),
				            		a = toFloat(getStrTok(tok2, 4, " "));
				            	col.setRGBA(r, g, b, a);
			            	}
			            	if (solid)
		            			lInfo.setSolidColor(col);
			            	else
			            		lInfo.setBorderColor(col);
	            		}
	            	}
	            	if ("BEGIN_MEMBERS".equalsIgnoreCase(tok1)) {
	            		objectEnd = true;
	            	}
	            	if ("END_MEMBERS".equalsIgnoreCase(tok1)) {
	            		objectEnd = true;
	            	}
	            	if ("END".equalsIgnoreCase(tok1)) {
	            		objectEnd = true;
	            	}

	                line = reader.readLine();
	                lineNumber++;
	            }

            	info.add(lInfo);
            }

            System.out.println(
            	"Scanned " + lineNumber + " lines of GOCAD data " +
            	"from \"" + location + "\".");
            return info;
        }
        catch (FileNotFoundException e) {
			throw new T3dException(
					"Could not access file \"" + location + "\".");
        }
        catch (IOException e) {
			throw new T3dException(e.getMessage());
        }
        catch (T3dException e) {
			throw new T3dException(e.getMessage());
        }
        catch (Exception e) {
			e.printStackTrace();
			throw new T3dException(
					"Parser error in \"" + location + "\":" + lineNumber);
        }
    }

    private List<GmSimpleTINFeature>
		readGocadTSurf_Pass2(BufferedReader reader, String location)
		throws T3dException
	{
		int lineNumber = 0;
		String line;
		String tok1;

		tins = new ArrayList<GmSimpleTINFeature>();
		int i = 0;
		String lastObj = null;

	    try { // Quick and dirty "parser" implementation:
	        line = reader.readLine();
	        lineNumber++;

	        while (line != null)
	        {
	            GocadDataInfo lInfo = info.get(i);
	            // lInfo gives information about the object to be read next

	            ArrayList<VgPoint> points = new ArrayList<VgPoint>();
	            ArrayList<long[]> triangles = new ArrayList<long[]>();

	            boolean objectEnd = false;
	            while (line != null && !objectEnd) {
	            	tok1 = getStrTok(line, 1, " ");

	            	if (
	            		"VRTX".equalsIgnoreCase(tok1) ||
	            		"PVRTX".equalsIgnoreCase(tok1))
	            	{
		            	//long id = toLong(getStrTok(line, 2, " "));
		            	double
		            		x = toDouble(getStrTok(line, 3, " ")),
		            		y = toDouble(getStrTok(line, 4, " ")),
		            		z = toDouble(getStrTok(line, 5, " "));
		            	VgPoint p = new GmPoint(x, y, z);
		            	points.add(p); // without id control yet :-(
	            	}
	            	if ("ATOM".equalsIgnoreCase(tok1)) {
		            	long
		            		//id1 = toLong(getStrTok(line, 2, " ")),
		            		id2 = toLong(getStrTok(line, 3, " "));
	            		// Duplicate point (again without id control...):
	            		points.add(points.get((int) id2 - 1));
	            	}
	            	if ("TRGL".equalsIgnoreCase(tok1)) {
		            	long
		            		id1 = toLong(getStrTok(line, 2, " ")),
		            		id2 = toLong(getStrTok(line, 3, " ")),
		            		id3 = toLong(getStrTok(line, 4, " "));
		            	triangles.add(new long[]{id1, id2, id3});
	            	}
	            	if ("GOCAD".equalsIgnoreCase(tok1)) {
	            		// found next object, so object data have been read completely
						if (lastObj != null)
							objectEnd = true;
						else
							lastObj = getStrTok(line, 2, " ");
	            	}
	            	if ("BEGIN_MEMBERS".equalsIgnoreCase(tok1)) {
	            		//objectEnd = true;
	            	}
	            	if ("END_MEMBERS".equalsIgnoreCase(tok1)) {
	            		//objectEnd = true;
	            	}
	            	if ("END".equalsIgnoreCase(tok1)) {
	            		//objectEnd = true;
	            	}

	            	if (!objectEnd) {
	            		line = reader.readLine();
	            		lineNumber++;
	            	}
	            }

	            if (lInfo.getObjectType().equalsIgnoreCase("TSurf")) {
		            GmSimpleTINFeature tin = new GmSimpleTINFeature();
		            GmSimpleTINGeometry tinGeom = (GmSimpleTINGeometry) tin.getGeometry();
					if (points.size() != lInfo.getNumberOfVertices()) {
						throw new T3dException(
								"Assertion violation: Vertex count difference!"
								+ " (Read "	+ points.size() + " VRTX/PVRTX elems"
								+ " while info is " + lInfo.getNumberOfVertices() + ".)"
								+ " line#:" + lineNumber
								+ " info = " + lInfo);
					}
					tinGeom.newPointList(points.size());
		            //tin.setBoundsInvalid(); // for performance reasons! TODO
		            for (int ii = 0; ii < points.size(); ii++) {
		                tinGeom.setPoint(ii, points.get(ii));
		            }
		            // Triangle mesh:
		            tinGeom.newTriangleList(triangles.size());
		            for (int ii = 0; ii < triangles.size(); ii++) {
		            	long[] tri = triangles.get(ii);
		                tinGeom.setTriangle(ii,
		                	(int)(tri[0] - 1), (int)(tri[1] - 1), (int)(tri[2] - 1));
		                // TODO in Triturus fuer long ergaenzen und dann auch long verwenden!
		            }

		            tins.add(tin);
	            }

				i++;

				line = reader.readLine();
				lineNumber++;
	        }
	    }
	    catch (FileNotFoundException e) {
			throw new T3dException(
					"Could not access file \"" + location + "\".");
	    }
	    catch (IOException e) {
			throw new T3dException(e.getMessage());
	    }
	    catch (T3dException e) {
			throw new T3dException(e.getMessage());
	    }
	    catch (Exception e) {
			e.printStackTrace();
			throw new T3dException(
					"Parser error in \"" + location + "\":" + lineNumber);
	    }

        System.out.println("Imported " + tins.size() + " TSurf object(s).");
        return tins;
	}

    // Private helpers:

    private String getStrTok(String str, int i, String sep) throws T3dException
    {
        // extract i-th token (i >= 1!) from a string with 'sep' as separator

        ArrayList<String> strArr = new ArrayList<String>();
        strArr.add(str);
        int i0 = 0, i1 = 0, k = 0;
        while (i1 >= 0) {
           i1 = str.indexOf(sep, i0);
           if (i1 >= 0) {
        	   	String found = str.substring(i0, i1);
				if (found.length() > 0) {
					if (k == 0)
						strArr.set(0, found);
					else
						strArr.add(found);
					k++;
				}
				i0 = i1 + 1;
			}
        }
        strArr.add(str.substring(i0));
        if (i < 1)
            throw new T3dException("Logical parser error.");
        return (String) strArr.get(i - 1);
    }

    private double toDouble(String str) {
        return Double.parseDouble(str);
    }

    private float toFloat(String str) {
        return Float.parseFloat(str);
    }

    private long toLong(String str) {
        return Long.parseLong(str);
    }
}
