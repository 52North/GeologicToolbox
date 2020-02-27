/**
 * Copyright (C) 2019 52 North Initiative for Geospatial Open Source
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
 * b.schmidt@52north.org
 */
package org.n52.v3d.triturus.geologic.examples;

import java.util.ArrayList;
import java.util.List;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.importers.IoGocadTSurfReader;
import org.n52.v3d.triturus.geologic.util.Orientation;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINGeometry;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.gisimplm.IoPointListWriter;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Geologic Toolbox example application: Generates a CSV file holding 
 * dip-azimuth-elevation value triples for a TIN resp. geologic surface and
 * additionally a second CSV file with dip-azimuth points transformed to polar
 * coordinates, i.e. points referring to a dip-azimuth hemisphere (radius 1).
 * <br/>
 * Dip-azimuth-elevation file:
 * <table>
 *   <tr>
 *     <td>phi</td> 
 *     <td>azimuth angle as described in {@link Orientation#azimuth()}
 *     .<</td>
 *     <td>range 0 ... 360 [degrees]</td]
 *   </tr>
 *   <tr>
 *     <td>r</td> 
 *     <td>dip value as distance from the origin (0, 0, 0)</td>
 *     <td>range 0 ... +90 [degrees]</td]
 *   </tr>
 *   <tr>
 *   	<td>z</td>
 *   	<td>elevation value (e.g., depth below ground)</td>
 *   	<td>double number</td>
 *   </tr>
 * </table>
 * 
 * @author Benno Schmidt
 */
public class DipAzimuthPlot
{
	private final String
		inFilename = "/projects/GeologicToolbox/data/s_geologie_Zechstein_ts",
		outFilename1 = "/projects/GeologicToolbox/data/s_geologie_Zechstein_dip-azimuth-z.csv", 
		outFilename2 = "/projects/GeologicToolbox/data/s_geologie_Zechstein_dip-azimuth-hemisphere.csv"; 

	public static void main(String args[]) {
		DipAzimuthPlot app = new DipAzimuthPlot();
		GmSimpleTINGeometry surf = app.input();
		app.output(surf, 1);
		app.output(surf, 2);
	}

	public GmSimpleTINGeometry input()
	{
		GmSimpleTINGeometry res = null;	
		try {
			// Read TSurf model from GOCAD data file:
			IoGocadTSurfReader reader = new IoGocadTSurfReader();
			GmSimpleTINFeature surf = reader.read(inFilename).get(0);
			res = (GmSimpleTINGeometry) surf.getGeometry();
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void output(GmSimpleTINGeometry tin, int run)
	{ 
		try {
			// Set up point list:
			double dip, phi, x, y, z;
			List<VgPoint> points = new ArrayList<VgPoint>();
			for (int i = 0; i < tin.numberOfTriangles(); i++) {
				Orientation orient = new Orientation(tin.getTriangle(i)); 
				dip = orient.dip();
				phi = orient.azimuth();
				if (run == 1) {
					x = dip;
					y = phi;
					z = ((GmTriangle) tin.getTriangle(i)).getCenterPoint().getZ(); 
					// TODO The GmTriangle cast might fail...
				} else { // run == 2
					x = Math.sin((-phi + 90.) * Math.PI/180.) * Math.sin(dip * Math.PI/180.);
					y = Math.cos((-phi + 90.) * Math.PI/180.) * Math.sin(dip * Math.PI/180.);
					z = -Math.cos(dip * Math.PI/180.);
				}
				points.add(new GmPoint(x, y, z));
			}
			
			// Generate ASCII file output:
			IoPointListWriter writer = new IoPointListWriter(IoPointListWriter.CSV);
			writer.writeHeaderLine(true);
			String outFilename;
			if (run == 1) {
				writer.setFieldNames("dip", "azimuth", "z");
				outFilename = outFilename1;
			} else {
				writer.setFieldNames("x", "y", "z");
				outFilename = outFilename2;
			}
			writer.writeToFile(points, outFilename);
			System.out.println("Wrote the file \"" + outFilename + "\".");
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
	}
}
