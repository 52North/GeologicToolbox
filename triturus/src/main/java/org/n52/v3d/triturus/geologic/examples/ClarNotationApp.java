/**
 * Copyright (C) 2021 52North Initiative for Geospatial Open Source
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
 * Contact: Benno Schmidt, 52North Initiative for Geospatial Open Source 
 * Software GmbH, Martin-Luther-King-Weg 24, 48155 Muenster, Germany, 
 * b.schmidt@52north.org
 */
package org.n52.v3d.triturus.geologic.examples;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.util.Orientation;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.t3dutil.T3dVector;
import org.n52.v3d.triturus.vgis.VgGeomObject;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTriangle;

/**
 * GeologicToolbox example application: Computes strike and dip values for 
 * arbitrary triangle geometries. The triangle's coordinates have to be  
 * given as parameters of the class's <tt>main</tt> method, e.g. by calling
 * the class from the command line:<br/>
 * <br/>
 * <tt>java [...] ClarNotationApp 100 100 10 200 100 10 200 200 10.5</tt>
 * <br/>
 * Note: To control the correctness of the computation algorithm that has been
 * implemented inside the 52n GeologicToolbox, additionally a computation 
 * according to Groshong Jr. (1999, p. 48) is performed.<br/>
 * <i>Ref.: Groshong, R.H. (1999): 3-D Structural Geology. Berlin: Springer.</i>
 *  
 * @author Benno Schmidt
 */
public class ClarNotationApp
{
    public static void main(String args[]) {
        new ClarNotationApp().run(args);
    }
    
    public void run(String args[]) 
    { 
        try {
            VgTriangle tri = this.parseTriangleCoordinates(args);
            if (tri != null) {
                Orientation orient = new Orientation(tri); 
                
                if (orient.hasZeroArea()) 
                    System.out.println("Area is 0.");
                if (orient.isPlain()) 
                    System.out.println("Triangle plain in x-y plane.");
                
                System.out.println("Parsed triangle: " + tri);
                System.out.println(
                    "dip: " + orient.dipInt() + " (" + orient.dip() + ")");
                System.out.println(
                    "azimuth: " + orient.azimuthInt() + " (" + orient.azimuth() + ")");
                System.out.println(
                    "strike: " + orient.strikeInt() + " (" + orient.strike() + ")");
                System.out.println(
                    "Clar: " + orient.clarNotation());
                System.out.println(
                    "Compass direction: " + orient.compassDirection());
                
                controlComputation(tri);
            }
            else
                System.out.println("Error: Could not perform computation!");
        }
        catch (T3dException e) {
            e.printStackTrace();
        }
    }
    
    private VgTriangle parseTriangleCoordinates(String[] args) {
        if (args == null || args.length <= 0) {
            System.out.println("Missing coordinate values!");
            return null;
        }
        if (args.length != 9) {
            System.out.println("Wrong number of coordinates (expected 9 values x1 y1 z1 x2 x2 z2 x3 x3 z3)!");
            System.out.println("(Received " + args.length + " values)");
            return null;
        }
        double 
            x1 = Double.parseDouble(args[0]),
            y1 = Double.parseDouble(args[1]),
            z1 = Double.parseDouble(args[2]),
            x2 = Double.parseDouble(args[3]),
            y2 = Double.parseDouble(args[4]),
            z2 = Double.parseDouble(args[5]),
            x3 = Double.parseDouble(args[6]),
            y3 = Double.parseDouble(args[7]),
            z3 = Double.parseDouble(args[8]);
        VgPoint 
            p1 = new GmPoint(x1, y1, z1),
            p2 = new GmPoint(x2, y2, z2),
            p3 = new GmPoint(x3, y3, z3);
        VgTriangle tri = new GmTriangle(p1, p2, p3);
        tri.setSRS(VgGeomObject.SRSNone);
        return tri;
    }
    
    private void controlComputation(VgTriangle tri) {
        VgPoint[] pt = tri.getCornerPoints();
        double 
            x1 = pt[0].getX(),
            y1 = pt[0].getY(),
            z1 = pt[0].getZ(),
            x2 = pt[1].getX(),
            y2 = pt[1].getY(),
            z2 = pt[1].getZ(),
            x3 = pt[2].getX(),
            y3 = pt[2].getY(),
            z3 = pt[2].getZ();
        double
            A = y1*z2 + z1*y3 + y2*z3 - z2*y3 - z3*y1 - z1*y2,
            B = z2*x3 + z3*x1 + z1*x2 - x1*z2 - z1*x3 - x2*z3,
            C = x1*y2 + y1*x3 + x2*y3 - y2*x3 - y3*x1 - y1*x2,
            E = Math.sqrt(A*A + B*B + C*C);
        double
           theta_ = Math.atan(A / B),
           delta = Math.asin(-Math.cos(Math.PI/2. + Math.acos(C/E)));
        double 
           cellA = A / B,
           cellB = B / E,
           cellE = theta_;
        double azimuth;
        if (cellB <= 0) {
            azimuth = 180. + 180./Math.PI * cellE;
        } else {
            if (cellA >= 0) {
                azimuth = 180./Math.PI * cellE;
            } else {
                azimuth = 360. + 180./Math.PI * cellE;
            }
        }
        System.out.println("Control (Groshong 1999):");
        System.out.println("dip = " + 180./Math.PI * delta);
        System.out.println("azimuth = " + azimuth);
    }
}
