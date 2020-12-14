/**
 * Copyright (C) 2018-2019 52North Initiative for Geospatial Open Source
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
 * Contact: Benno Schmidt and Martin May, 52North Initiative for Geospatial 
 * Open Source Software GmbH, Martin-Luther-King-Weg 24, 48155 Muenster, 
 * Germany, info@52north.org
 */
package org.n52.v3d.triturus.geologic.examples;

import org.n52.v3d.triturus.core.IoFormatType;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.exporters.IoSurfaceWriter;
import org.n52.v3d.triturus.geologic.importers.IoGocadTSurfReader;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;

/**
 * Geologic Toolbox example application: Reads a GOCAD TSurf TIN and writes it 
 * to a VTK file to be used in ParaView.
 * 
 * @author Benno Schmidt
 */
public class GocadTSurfParaviewExportExample
{
	private final String
 		inFilename = "/projects/GeologicToolbox/data/s_geologie_Zechstein_ts",
		outFilename = "/projects/GeologicToolbox/data/s_geologie_Zechstein.vtk"; 

	public static void main(String args[]) {
		new GocadTSurfParaviewExportExample().run();
	}
	
	public void run()
	{ 
		try {
			// Read first TSurf model from GOCAD data file...
			IoGocadTSurfReader reader = new IoGocadTSurfReader();
			GmSimpleTINFeature surf = reader.read(inFilename).get(0);
			// ... and generate OBJ output:
			IoSurfaceWriter writer = new IoSurfaceWriter(IoFormatType.VTK_DATASET);
			writer.exportOrientationAttributes(); // to export orientation attributes
			writer.writeToFile(surf, outFilename);
			System.out.println("Wrote the file \"" + outFilename + "\".");
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
	}
}
