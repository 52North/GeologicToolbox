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
 * b.schmidt@52north.org
 */
package org.n52.v3d.triturus.geologic.examples;

import org.n52.v3d.triturus.core.IoFormatType;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimpleElevationGrid;
import org.n52.v3d.triturus.gisimplm.IoElevationGridWriter;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Geologic Toolbox example application: Generates a very simple elevation grid and writes it to 
 * an Wavefront OBJ file named "hello_world.obj" in the working directory.
 * 
 * @author Benno Schmidt
 */
public class HelloWorld
{
    private final String filename = "data/hello_world.obj"; 
    // ... you might want to change path and filename above.


    public static void main(String args[]) {
        HelloWorld app = new HelloWorld();
        app.run();
    }

    public void run() 
    { 
        // Construct grid of size 10 x 10:
        VgPoint orig = new GmPoint(0, 0, 0);
        GmSimpleElevationGrid grid = new GmSimpleElevationGrid(10, 10, orig, 100., 100.);

        // Assign some elevation values:
        for (int j = 0; j < grid.numberOfColumns(); j++) {
            for (int i = 0; i < grid.numberOfRows(); i++) {
                grid.setValue(i, j, 100. + 10. * Math.random() - 5. * (Math.abs(i - 5) + Math.abs(j - 5)));
            }
        }
        System.out.println(grid.minimalElevation());
        System.out.println(grid.maximalElevation());

        // Write OBJ output:
        IoElevationGridWriter writer = new IoElevationGridWriter(IoFormatType.OBJ);
        writer.writeToFile(grid, filename);
        System.out.println("Wrote output file \"" + filename + "\"...");
        System.out.println("Success!");
    }
}
