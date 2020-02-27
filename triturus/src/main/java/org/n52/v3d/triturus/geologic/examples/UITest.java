/**
 * Copyright (C) 2020 52North Initiative for Geospatial Open Source
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

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.analysis.FltTINCorrelation;
import org.n52.v3d.triturus.geologic.util.Orientation;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.vgis.VgGeomObject;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTriangle;

public class UITest
{
	public static void main(String args[]) {
		new UITest().run(args);
	}
	
	public void run(String[] args) 
	{ 
		try {
			FltTINCorrelation t = new FltTINCorrelation();

			System.out.println("Moving window size: ");
			for (String entry : t.UIDialog_MovingWindowSizeOptions())
				System.out.println(" " + entry);

			System.out.println("Rasterization width: ");
			for (String entry : t.UIDialog_CellSizeOptions())
				System.out.println(" " + entry);

			System.out.println("z-Conflict handler: ");
			for (String entry : t.UIDialog_ZConflictOptions())
				System.out.println(" " + entry);

			System.out.println("File output format: ");
			for (String entry : t.UIDialog_OutputFormatOptions())
				System.out.println(" " + entry);			
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
	}
}
