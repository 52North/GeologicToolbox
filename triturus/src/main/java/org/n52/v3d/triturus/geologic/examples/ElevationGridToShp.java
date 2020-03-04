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

import java.io.IOException;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.geologic.exporters.IoShapeWriter;
import org.n52.v3d.triturus.geologic.util.CRSRecommender;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimpleElevationGrid;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.opengis.referencing.FactoryException;

/**
 * @author Moritz Wollenhaupt <moritz.wollenhaupt@hs-bochum.de>
 */
public class ElevationGridToShp {

    private final String outFilename = "grid.shp";

    public static void main(String args[]) {
        new ElevationGridToShp().run();
    }

    public void run() {
        try {

            // generate grid data
            VgPoint orig = new GmPoint(3546713.29, 5602695.25, 0);
            GmSimpleElevationGrid grid = new GmSimpleElevationGrid(100, 100, orig, 100., 100.);
            for (int j = 0; j < grid.numberOfColumns(); j++) {
                for (int i = 0; i < grid.numberOfRows(); i++) {
                    grid.setValue(i, j, 100. + 10. * Math.random() - 5. * (Math.abs(i - 5) + Math.abs(j - 5)));
                }
            }
            // initialize ShapeWriter with geom type you want to write, e.g. tin or elevation grid
            IoShapeWriter shpWriter = new IoShapeWriter(IoShapeWriter.ELEVATION_GRID);
            // init internal featuretypes (you want to write multi polygons in a CRS given by it's EPSG code)
            // optionial you can give attribute types you want to store in the shapefile's geometry
            shpWriter.initFeatureType(IoShapeWriter.MULTI_POLYGON, CRSRecommender.recommendEPSG(grid.envelope()), null);
            // optional: here init attribute types (not shown) -> see GocadTSurfClarNotationToShp example to see how to handle attributes
            // ...
            // ...
            
            // after all configuration: build your feature type, write the geometries and store them into a shapefile
            shpWriter.buildFeatureType();
            shpWriter.writeGeometry(grid);
            shpWriter.writeShapeFile(outFilename);

            System.out.println("Wrote the file \"" + outFilename + "\".");
        } catch (T3dException | T3dNotYetImplException | FactoryException | IOException e) {
            e.printStackTrace();
        }
    }
}
