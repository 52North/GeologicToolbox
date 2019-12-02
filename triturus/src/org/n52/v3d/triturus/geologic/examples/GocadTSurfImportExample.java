/**
 * Copyright (C) 2007-2018 52 North Initiative for Geospatial Open Source
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
package org.n52.v3d.triturus.geologic.examples;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.importers.IoGocadTSurfReader;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.IoTINWriter;

/**
 * Geologic Toolbox example application: Reads a GOCAD TSurf TIN and writes 
 * it to an HTML5 page with an interactive 3D scene in it.
 * 
 * @author Benno Schmidt
 */
public class GocadTSurfImportExample
{
	private final String
		inFilename = "/projects/GeologicToolbox/data/simple_test.ts",
		outFilename = "/projects/GeologicToolbox/data/simple_test.html"; 

	public static void main(String args[]) {
		new GocadTSurfImportExample().run();
	}
	
	public void run() 
	{ 
		try {
			// Read first TSurf model from GOCAD data file...
			IoGocadTSurfReader reader = new IoGocadTSurfReader();
			GmSimpleTINFeature surf = reader.read(inFilename).get(0);
			// ... and generate HTML5/X3DOM output:
			IoTINWriter writer = new IoTINWriter(IoTINWriter.X3DOM);
			writer.writeToFile(surf, outFilename);
			System.out.println("Wrote the file \"" + outFilename + "\".");
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
	}
}
