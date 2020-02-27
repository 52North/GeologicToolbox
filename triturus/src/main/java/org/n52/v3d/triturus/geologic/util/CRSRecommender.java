package org.n52.v3d.triturus.geologic.util;

import org.n52.v3d.triturus.gisimplm.GmEnvelope;
import org.n52.v3d.triturus.vgis.VgEnvelope;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * @author Moritz Wollenhaupt <moritz.wollenhaupt@hs-bochum.de>
 */
public class CRSRecommender {
    
    private static final String EPSG_25832 = "25832";   // UTM32N without zone identifier
    private static final GmEnvelope hessenBBOX25832 = new GmEnvelope(400000, 600000, 5445000, 5735000, -10000, 10000);
    
    private static final String EPSG_4647 = "4647";     // UTM32N with zone identifier 32
    private static final GmEnvelope hessenBBOX4647 = new GmEnvelope(32400000, 32600000, 5445000, 5735000, -10000, 10000);
    
    private static final String EPSG_31467 = "31467";   // DHDN/GK3
    private static final GmEnvelope hessenBBOX31467 = new GmEnvelope(3400000, 3600200, 5446000, 5737000, -10000, 10000);
    
    
    public static String recommendEPSG(VgEnvelope envelope) {
        VgPoint center = envelope.getCenterPoint();
        if(hessenBBOX31467.contains(center)) {
            System.out.println("Found matching EPSG in BBOX: " + EPSG_31467);
            return EPSG_31467;
        }
        if(hessenBBOX25832.contains(center)) {
            System.out.println("Found matching EPSG in BBOX: " + EPSG_25832);
            return EPSG_25832;
        }
        if(hessenBBOX4647.contains(center)) {
            System.out.println("Found matching EPSG in BBOX: " + EPSG_4647);
            return EPSG_4647;
        }
        return "UNKNOWN EPSG"; // Todo: Throw Exception
    }
    
}
