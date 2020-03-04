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
 * info@52north.org
 */
package org.n52.v3d.triturus.geologic.util;

import org.n52.v3d.triturus.gisimplm.GmEnvelope;
import org.n52.v3d.triturus.vgis.VgEnvelope;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * @author Moritz Wollenhaupt <moritz.wollenhaupt@hs-bochum.de>
 */
public class CRSRecommender {
    
    private static final String 
    	EPSG_25832 = "25832",   // UTM32N without zone identifier
    	EPSG_4647  = "4647",    // UTM32N with zone identifier 32
    	EPSG_31467 = "31467";   // DHDN/GK3
    private static final GmEnvelope 
    	hessenBBOX25832 = new GmEnvelope(  400000,   600000, 5445000, 5735000, -10000, 10000),
    	hessenBBOX4647  = new GmEnvelope(32400000, 32600000, 5445000, 5735000, -10000, 10000),
    	hessenBBOX31467 = new GmEnvelope( 3400000,  3600200, 5446000, 5737000, -10000, 10000);

    
    public static String recommendEPSG(VgEnvelope envelope) {
        VgPoint center = envelope.getCenterPoint();
        if (hessenBBOX31467.contains(center)) {
            System.out.println("Found matching EPSG in BBOX: " + EPSG_31467);
            return EPSG_31467;
        }
        if (hessenBBOX25832.contains(center)) {
            System.out.println("Found matching EPSG in BBOX: " + EPSG_25832);
            return EPSG_25832;
        }
        if (hessenBBOX4647.contains(center)) {
            System.out.println("Found matching EPSG in BBOX: " + EPSG_4647);
            return EPSG_4647;
        }
        return "UNKNOWN EPSG"; // Todo: Throw Exception
    }
}
