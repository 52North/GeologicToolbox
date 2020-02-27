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
package org.n52.v3d.triturus.geologic.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.n52.v3d.triturus.core.IoFormatType;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.geologic.exporters.IoShapeWriter;
import org.n52.v3d.triturus.geologic.exporters.IoSurfaceWriter;
import org.n52.v3d.triturus.geologic.importers.IoGocadTSurfReader;
import org.n52.v3d.triturus.geologic.exporters.util.ClarNotationShapeFileAttribute;
import org.n52.v3d.triturus.geologic.exporters.util.ShapeFileAttribute;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.opengis.referencing.FactoryException;

/**
 * @author Moritz Wollenhaupt <moritz.wollenhaupt@hs-bochum.de>
 */
public class GocadTSurfClarNotationToShp {

    private final String inFilename = "res/s_geologie_Rotliegend_ts.ts";
    private final String outFilename = "res/s_geologie_Rotliegend_ts.shp";

    public static void main(String args[]) {
        new GocadTSurfClarNotationToShp().run();
    }

    public void run() {
        try {
            // Read first TSurf model from GOCAD data file...
            IoGocadTSurfReader reader = new IoGocadTSurfReader();
            GmSimpleTINFeature surf = reader.read(inFilename).get(0);
            // ... and generate Shape output:
            IoShapeWriter shpWriter = new IoShapeWriter();
            ClarNotationShapeFileAttribute clarNotationAttribute = new ClarNotationShapeFileAttribute(true, true, true, true);
            List<ShapeFileAttribute> attributes = new ArrayList<>();
            attributes.add(clarNotationAttribute);
            shpWriter.initFeatureType(IoShapeWriter.MULTI_POLYGON, "23033", attributes);
            shpWriter.buildFeatureType();                           
            shpWriter.createPolygonZFeatures(surf);
            shpWriter.writeShapeFile(outFilename);

            System.out.println("Wrote the file \"" + outFilename + "\".");
        } catch (T3dException | T3dNotYetImplException | FactoryException | IOException e) {
            e.printStackTrace();
        }
    }
}
