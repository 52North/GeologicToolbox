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
package org.n52.v3d.triturus.geologic.exporters.util;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.v3d.triturus.geologic.util.Orientation;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.vgis.VgGeomObject;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTriangle;

/**
 * ShapeFileAttribute implementation for ClarNotation attributes. First the
 * shape file coulumns were initialized. After init the calculated attribute
 * values can be added. Note that column initialization and value assignment
 * must be in the same order
 *
 * @author MoritzWollenhaupt
 */
public class ClarNotationShapeFileAttribute extends ShapeFileAttribute {

    // calculable values of ClarNotation calculation
    private boolean dip, dipDir, strike, compassDirection;

    public ClarNotationShapeFileAttribute(boolean dip, boolean dipDir, boolean strike, boolean compassDirection) {
        this.dip = dip;
        this.dipDir = dip;
        this.strike = strike;
        this.compassDirection = compassDirection;
    }

    /**
     * Creates attribute's columns for the selected calculable values
     *
     * @param sftBuilder SimpleFeatureTypeBuilder that represents the shape
     * files basic structure
     */
    @Override
    public void init(SimpleFeatureTypeBuilder sftBuilder) {
        // eachs shape file attribute column contains an identifier and a data type
        if (dip) {
            sftBuilder.add("dip", Double.class);
        }
        if (dipDir) {
            sftBuilder.add("dipdir", Double.class);
        }
        if (strike) {
            sftBuilder.add("strike", Double.class);
        }
        if (compassDirection) {
            sftBuilder.add("compassDir", String.class);
        }
    }

    /**
     * Here the concrete values were calculated and added to the
     * SimpleFeatureBuilder
     *
     * @param sfBuilder SimpleFeatureBuilder that collects column values
     * @param points Pointarray, used for further calculations
     */
    @Override
    public void calcAttributes(SimpleFeatureBuilder sfBuilder, double[] points) {
        VgPoint p1 = new GmPoint(points[0], points[1], points[2]),
                p2 = new GmPoint(points[3], points[4], points[5]),
                p3 = new GmPoint(points[6], points[7], points[8]);
        VgTriangle tri = new GmTriangle(p1, p2, p3);
        tri.setSRS(VgGeomObject.SRSNone);

        Orientation orient = new Orientation(tri);

        if (dip) {
            sfBuilder.add(orient.dip());
        }
        if (dipDir) {
            sfBuilder.add(orient.azimuth());
        }
        if (strike) {
            sfBuilder.add(orient.strike());
        }
        if (compassDirection) {
            sfBuilder.add(orient.compassDirection());
        }

    }

}
