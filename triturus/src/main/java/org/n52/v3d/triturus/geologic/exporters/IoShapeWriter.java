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
package org.n52.v3d.triturus.geologic.exporters;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.geologic.exporters.util.ShapeFileAttribute;
import org.n52.v3d.triturus.gisimplm.GmSimpleElevationGrid;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINGeometry;
import org.n52.v3d.triturus.gisimplm.IoAbstractWriter;
import org.n52.v3d.triturus.vgis.VgFeature;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * ShapeWriter using GeoTools for writing shape files
 *
 * @author Moritz Wollenhaupt
 */
public class IoShapeWriter extends IoAbstractWriter {

    public static final int TIN = 1;
    public static final int ELEVATION_GRID = 2;

    private int type;
    private final String logString;
    // booleans used for execution in correct order
    private boolean initialized = false;
    private boolean built = false;
    private boolean dataAvailable = false;
    private SimpleFeatureTypeBuilder sftBuilder = null; // builder for the FeatureType you want to use
    private SimpleFeatureType sft = null;
    private SimpleFeatureBuilder sfBuilder = null;      // builder for the Features you want to store
    private List<SimpleFeature> features;
    // a list of attribute types that correspond to the columns of the file
    private List<ShapeFileAttribute> attributes;

    // Identifiers for possible Geometry-Types
    public static final String POINT = "Point";
    public static final String MULTI_POINT = "MultiPoint";
    public static final String MULTI_LINE_STRING = "MultiLineString";
    public static final String MULTI_POLYGON = "MultiPolygon";

    public IoShapeWriter(int type) {
        logString = this.getClass().getName();
        this.type = type;
        this.features = new ArrayList<SimpleFeature>();
    }

    /**
     * First Step for writing a shape file is initializing the FeatureType. Here
     * you have to choose the concrete geometry type you want to store, the CRS
     * by giving a EPSG-String. Further you have to pass a list of
     * ShapeFileAttributes. Here the columns in file to be created are generated
     *
     * @param featureType Identifier for what kind of geometry you want to store
     * @param epsg The CRS where you want to save your file
     * @param attributes The shape file's attributes
     * @throws T3dNotYetImplException
     * @throws T3dException
     * @throws FactoryException
     */
    public void initFeatureType(String featureType, String epsg, List<ShapeFileAttribute> attributes) throws T3dNotYetImplException, T3dException, FactoryException {
        initFeatureType(featureType, epsg);
        this.attributes = attributes;
        if (this.attributes != null) {
            for (ShapeFileAttribute attribute : this.attributes) {
                attribute.init(sftBuilder);
            }
        }
    }

    /**
     * First Step for writing a shape file is initializing the FeatureType. Here
     * you have to choose the concrete geometry type you want to store, the CRS
     * by giving a EPSG-String. No attributes were stored this way. Only writing
     * geometry
     *
     * @param featureType Identifier for what kind of geometry you want to store
     * @param epsg The CRS where you want to save your file
     * @throws T3dNotYetImplException
     * @throws T3dException
     * @throws FactoryException
     */
    public void initFeatureType(String featureType, String epsg) throws T3dNotYetImplException, T3dException, FactoryException {
        // create new SFTBuilder and assign a name
        this.sftBuilder = new SimpleFeatureTypeBuilder();
        this.sftBuilder.setName("GeologicToolbox-FeatureTypeBuilder");
        // adjust the correct CRS
        CRSAuthorityFactory crsAuthorityFactory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);
        CoordinateReferenceSystem crs = crsAuthorityFactory.createCoordinateReferenceSystem(epsg);
        this.sftBuilder.setCRS(crs);
        // the first column of a shape file has always to contain the geometry
        switch (featureType) {
            case POINT:
                throw new T3dNotYetImplException("FeatureType " + POINT + "not yet implemented!");
            case MULTI_POINT:
                throw new T3dNotYetImplException("FeatureType " + MULTI_POINT + "not yet implemented!");
            case MULTI_LINE_STRING:
                throw new T3dNotYetImplException("FeatureType " + MULTI_LINE_STRING + "not yet implemented!");
            case MULTI_POLYGON:
                this.sftBuilder.add("the_geom", MultiPolygon.class);
                this.initialized = true;    // FeatureTypeBuilder-init complete
                break;
            default:
                throw new T3dException("Unsupported FeatureType!");
        }
    }

    /**
     * Second step for writing a shape file. Once the geometry- and
     * attribute-columns have been defined, a SimpleFeatureBuilder (building the
     * geom + adding attributes to them) must be created on the basis of the
     * SimpleFeatureTypeBuilder
     *
     * @throws T3dException
     */
    public void buildFeatureType() throws T3dException {
        if (this.initialized) {
            this.sft = sftBuilder.buildFeatureType();
            this.sfBuilder = new SimpleFeatureBuilder(sft);
            this.built = true;
        } else {
            throw new T3dException("You need to initialize a featureType before building it!");
        }
    }

    /**
     * Third step for writing a shape file. In this case a VgFeature, matching
     * the given type is written to a shape file. You have to write your own
     * method in this way, if you want to store other geometries
     *
     * @param feature The Feature, e.g. a TIN instance you want to write into
     * the shape file
     * @throws T3dException
     */
    public void writeGeometry(VgFeature feature) {
        // check correct execution order
        if (!this.built) {
            throw new T3dException("You need to build the shapes feature type before adding data to it!");
        }

        switch (type) {
            case TIN:
                writeTIN((GmSimpleTINFeature) feature);
                dataAvailable = true;
                break;
            case ELEVATION_GRID:
                writeElevationGrid((GmSimpleElevationGrid) feature);
                dataAvailable = true;
                break;
            default:
                throw new T3dException("No valid feature type");
        }
        

    }

    /**
     * Third step for writing a shape file. In this case a tin is written to a
     * shape file. You have to write your own method in this way, if you want to
     * store other geometries
     *
     * @param tin The Tin instance you want to write into the shape file
     * @throws T3dException
     */
    private void writeTIN(GmSimpleTINFeature tin) {
        // Cast object for access of points/triangles
        GmSimpleTINGeometry geom = (GmSimpleTINGeometry) tin.getGeometry();

        // GeoTool's GeometryBuilder builds geometries for you by giving them coordinates
        GeometryBuilder gb = new GeometryBuilder();
        // iterate all triangles
        for (int i = 0; i < geom.numberOfTriangles(); i++) {
            // internal every point is accessed by it's index
            int[] triIdx = geom.getTriangleVertexIndices(i);

            // Store the X, Y and Z coordinates of each vertex in an array
            double[] points = new double[(triIdx.length + 1) * 3];
            int l = triIdx.length;
            for (int j = 0; j < points.length; j += 3) {   // 3 = idx of x, y, z in points[]
                points[j] = geom.getPoint(triIdx[(j / 3) % l]).getX();
                points[j + 1] = geom.getPoint(triIdx[(j / 3) % l]).getY();
                points[j + 2] = geom.getPoint(triIdx[(j / 3) % l]).getZ();
            }

            // create a new polygon -> you have to wrap it in an multiPolygon, otherwise you won't get a threedimensional geometry representation
            // a shape file only knows the data type MultiPolygon
            Polygon poly = gb.polygonZ(points);
            MultiPolygon mPoly = gb.multiPolygon(new Polygon[]{poly});

            this.sfBuilder.add(mPoly);  // add geometry at first!
            // now all shape file attributes will be calculated. 
            // it is important to store them in the same order than the shape file's table columns were created!
            if (attributes != null) {
                for (ShapeFileAttribute attribute : attributes) {
                    attribute.calcAttributes(sfBuilder, points);
                }
            }
            // finally build your feature and add it to the feature collection you want to write 
            SimpleFeature feature = sfBuilder.buildFeature(null);
            this.features.add(feature);
        }
        
    }

    private void writeElevationGrid(GmSimpleElevationGrid grid) {
        // GeoTool's GeometryBuilder builds geometries for you by giving them coordinates
        GeometryBuilder gb = new GeometryBuilder();
        // Iterate grid's vertices
        for (int i = 0; i < grid.numberOfRows() - 1; i++) {
            for (int j = 0; j < grid.numberOfColumns() - 1; j++) {
                VgPoint p1 = grid.getPoint(i, j);
                VgPoint p2 = grid.getPoint(i, j + 1);
                VgPoint p3 = grid.getPoint(i + 1, j + 1);
                VgPoint p4 = grid.getPoint(i + 1, j);

                double[] coords = new double[12];
                coords[0] = p1.getX();
                coords[1] = p1.getY();
                coords[2] = p1.getZ();
                coords[3] = p2.getX();
                coords[4] = p2.getY();
                coords[5] = p2.getZ();
                coords[6] = p3.getX();
                coords[7] = p3.getY();
                coords[8] = p3.getZ();
                coords[9] = p4.getX();
                coords[10] = p4.getY();
                coords[11] = p4.getZ();

                // create a new polygon -> you have to wrap it in an multiPolygon, otherwise you won't get a threedimensional geometry representation
                // a shape file only knows the data type MultiPolygon
                Polygon poly = gb.polygonZ(coords);
                MultiPolygon mPoly = gb.multiPolygon(new Polygon[]{poly});

                this.sfBuilder.add(mPoly);  // add geometry at first!
                // now all shape file attributes will be calculated. 
                // it is important to store them in the same order than the shape file's table columns were created!
                if (attributes != null) {
                    for (ShapeFileAttribute attribute : attributes) {
                        attribute.calcAttributes(sfBuilder, coords);
                    }
                }
                // finally build your feature and add it to the feature collection you want to write 
                SimpleFeature feature = sfBuilder.buildFeature(null);
                this.features.add(feature);

            }
        }

    }

    /**
     * Foruth and last step to write a shape file. This is where the Features
     * stored in the FeatureCollection are written to the file
     *
     * @param path The file's path to be created
     * @throws T3dException
     * @throws IOException
     */
    public void writeShapeFile(String path) throws T3dException, IOException {
        // check for correct execution order
        if (!this.dataAvailable) {
            throw new T3dException("There is no data available to write the shape file!");
        }
        // create ne File object, open dataStore
        File newFile = new File(path);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        // optional: add url and create spaital index
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(this.sft);
        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        // try to write all Feautres via transaction -> rollback on failure possible (database like)
        Transaction transaction = new DefaultTransaction("create");
        if (featureSource instanceof SimpleFeatureStore) {
            // wrap list of features in SimpleFeatureCollection
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(this.sft, this.features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (IOException e) {
                transaction.rollback();
            } finally {
                transaction.close();
            }
        } else {
            throw new T3dException(typeName + " does not support read/write access");
        }
    }

    @Override
    public String log() {
        return logString;
    }

}
