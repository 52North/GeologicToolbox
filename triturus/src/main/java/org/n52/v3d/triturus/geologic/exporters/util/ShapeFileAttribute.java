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

/**
 * Abstract class for shape file attribution. First the shape file coulumns were
 * initialized. After init the calculated attribute values can be added. Note
 * that column initialization and value assignment must be in the same order
 *
 * @author MoritzWollenhaupt
 */
public abstract class ShapeFileAttribute {

    /**
     * Creates the attribute columns with their data types, that will be stored
     * in the shape file
     *
     * @param sftBuilder SimpleFeatureType represents the SimpleFeature's basic
     * structure
     */
    public abstract void init(SimpleFeatureTypeBuilder sftBuilder);

    /**
     * Calculates and adds the geometries attributes
     *
     * @param sfBuilder SimpleFeatureBuilder that adds the attributes
     * @param points Points used for caluclation
     */
    public abstract void calcAttributes(SimpleFeatureBuilder sfBuilder, double[] points);

}
