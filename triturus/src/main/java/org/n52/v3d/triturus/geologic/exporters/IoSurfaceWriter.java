/**
 * Copyright (C) 2019 52 North Initiative for Geospatial Open Source
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
package org.n52.v3d.triturus.geologic.exporters;

import org.n52.v3d.triturus.core.IoFormatType;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.geologic.util.Orientation;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.IoAbstractWriter;
import org.n52.v3d.triturus.t3dutil.T3dVector;
import org.n52.v3d.triturus.vgis.VgIndexedTIN;

import java.io.*;
import java.text.DecimalFormat;

/** 
 * Writer which exports geologic surfaces (TINs) to files. Various formats such
 * as VTK might be supported. For export to a VTK polygonal dataset, optionally
 * the triangle's dip and azimuth values as well as the compass directions and
 * triangle normal orientations (z-component > 0, i.e. skyward orientation; 
 * = 0, i.e. vertical triangle; or < 0, i.e. earthward orientation) will be 
 * exported (see switch {@link this#exportOrientationAttributes}), e.g. to 
 * generate datasets to be used in ParaView.
 * 
 * @author Benno Schmidt
 */
public class IoSurfaceWriter extends IoAbstractWriter
{
    private String logString = "";
    private String format;
    private BufferedWriter doc;
    
    public boolean 
    	exportDip = false, 
    	exportAzimuth = false, 
    	exportCompassDirection = false,
    	exportOrientationClass = false;
    
    /**
     * Constructor. As a parameter, format type has to be set. For unsupported
     * file formats, a <tt>T3dNotYetImplException</tt> will be thrown. Currently, 
     * these formats are supported:<br />
     * <ul>
     * <li><i>VTK:</i> VTK 3.0 format (polydata dataset)</li>
     * </ul>
     * 
     * @param format Format string (e.g. <tt></tt>&quot;VTK&quot;</tt>)
     * @see IoSurfaceWriter#VTK_DATASET
     */
    public IoSurfaceWriter(String format) {
        logString = this.getClass().getName();
        this.setFormatType(format);
    }

    public String log() {
        return logString;
    }

    /** 
     * sets the format type.
     * 
     * @param format Format string (e.g. <tt></tt>&quot;VTKDataset&quot;</tt>)
     */
    public void setFormatType(String format)
    {
        this.format = format;
    }

    /**
     * instructs the writer to export dip and azimuth values (as numerical 
     * data) as well as compass directions (as character strings) as 
     * additional triangle attributes to the target file.
     */
    public void exportOrientationAttributes() {
    	this.exportDip = true;
    	this.exportAzimuth = true;
    	this.exportCompassDirection = true;
    	this.exportOrientationClass = true;
    }
    
    /**
     * writes the TIN to a file.
     * 
     * @param tin TIN to be written
     * @param filename File path
     * @throws org.n52.v3d.triturus.core.T3dException
     * @throws org.n52.v3d.triturus.core.T3dNotYetImplException
     */
    public void writeToFile(GmSimpleTINFeature tin, String filename) 
        throws T3dException, T3dNotYetImplException
    {
        int i = 0;
        if (format.equalsIgnoreCase(IoFormatType.VTK_DATASET)) i = 1;
        else if(format.equalsIgnoreCase(IoFormatType.SHP)) i = 2;
        // --> add more formats here...

        try {
            switch (i) {
                case 1: this.writeVTKPolydata(tin, filename); break;
                case 2: this.writeSHPPolydata(tin, filename); break;
                // --> add more formats here...

                default: throw new T3dNotYetImplException("Unsupported file format");
            }
        }
        catch (T3dException | IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeSHPPolydata(GmSimpleTINFeature tin, String filename) throws T3dException, IOException{
        IoShapeWriter shpWriter = new IoShapeWriter();
        shpWriter.initFeatureType(IoShapeWriter.MULTI_POLYGON); // initialize featureType you want to write
//      here: define all featureTypeAttributes -> shpWriter.addXXXFeatureTypeAttribute("attributeName");
        // -->
        shpWriter.addIntegerFeatureTypeAttribute("Test_int");
        shpWriter.addStringFeatureTypeAttribute("Test_str");
        // <--
        shpWriter.buildFeatureType();                           // when all attribute definitions are complete, create final feature type definition
        shpWriter.createSimplePolygonFeatures(tin);             // add tin data you want to write
        shpWriter.writeShapeFile(filename);                     // finally write your shape file
        
    }
    
    private void writeVTKPolydata(GmSimpleTINFeature tin, String filename) 
    
    	throws T3dException
    {
        try {
            doc = new BufferedWriter(new FileWriter(filename));
            
            VgIndexedTIN geom = (VgIndexedTIN) tin.getGeometry();
            
            wl("# vtk DataFile Version 3.0 generated by 52N GeologicToolbox");
            wl("vtk output");
            wl("ASCII");
            wl("DATASET POLYDATA");
            
            DecimalFormat dfXY = this.getDecimalFormatXY();
            DecimalFormat dfZ = this.getDecimalFormatZ();
            
            wl("POINTS " + geom.numberOfPoints() + " float");
            for (int i = 0; i < geom.numberOfPoints(); i++) {
                w(dfXY.format(geom.getPoint(i).getX()));
                w(" " + dfXY.format(geom.getPoint(i).getY()));
                wl(" " + dfZ.format(geom.getPoint(i).getZ()));
            }
            
            w("POLYGONS " + geom.numberOfTriangles());
            wl(" " + (4 * geom.numberOfTriangles()));
            for (int i = 0; i < geom.numberOfTriangles(); i++) {
                w("3"); // number of polygon vertices
                w(" " + geom.getTriangleVertexIndices(i)[0]);
                w(" " + geom.getTriangleVertexIndices(i)[1]);
                w(" " + geom.getTriangleVertexIndices(i)[2]);
                wl();
            }

            if (this.exportDip || this.exportAzimuth || this.exportCompassDirection) {
            	wl("CELL_DATA " + geom.numberOfTriangles());            	
            }

            if (this.exportDip) {
            	wl("SCALARS DIP float 1");
            	wl("LOOKUP_TABLE default");
                for (int i = 0; i < geom.numberOfTriangles(); i++) {
    				Orientation orient = new Orientation(geom.getTriangle(i)); 
    				float dip = (float) orient.dip();
                    wl("" + dip);
                }            	
            }

            if (this.exportDip) {
            	wl("SCALARS AZIMUTH float 1");
            	wl("LOOKUP_TABLE default");
                for (int i = 0; i < geom.numberOfTriangles(); i++) {
    				Orientation orient = new Orientation(geom.getTriangle(i)); 
    				float azimuth = (float) orient.azimuth();
                    wl("" + azimuth);
                }            	
            }

            if (this.exportDip) {
            	wl("SCALARS COMPASS_DIR int 1");
            	wl("LOOKUP_TABLE default");
                for (int i = 0; i < geom.numberOfTriangles(); i++) {
    				Orientation orient = new Orientation(geom.getTriangle(i)); 
    				int compassDir = orient.compassDirectionClass();
                    wl("" + compassDir);
                }            	
            }

            if (this.exportOrientationClass) {
            	wl("SCALARS ORIENTATION_CLASS int 1");
            	wl("LOOKUP_TABLE default");
                for (int i = 0; i < geom.numberOfTriangles(); i++) {
    				T3dVector orient = geom.getTriangle(i).normal();
    				int res = -1;
    				if (orient.getZ() > 0.) {
    					res = +1;
    				} else {
        				if (orient.getZ() < 0.) {
        					res = 2;
        				} else {
        					res = 0;
        				}
    				}
    				wl("" + res);
                }            	
            }

            doc.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void w(String line) {
        try {
            doc.write(line);
        }
        catch (IOException e) {
            throw new T3dException(e.getMessage());
        }
    }


    private void wl(String line) {
        try {
            doc.write(line);
            doc.newLine();
        }
        catch (IOException e) {
            throw new T3dException(e.getMessage());
        }
    }

    private void wl() {
        try {
            doc.newLine();
        }
        catch (IOException e) {
            throw new T3dException(e.getMessage());
        }
    }
}
