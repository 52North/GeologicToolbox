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
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINGeometry;
import org.n52.v3d.triturus.gisimplm.IoAbstractWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Moritz Wollenhaupt
 */
public class IoShapeWriter extends IoAbstractWriter {

    private final String logString;
    private boolean initialized = false;
    private boolean built = false;
    private boolean dataAvailable = false;
    private SimpleFeatureTypeBuilder sftBuilder = null;
    private SimpleFeatureType sft = null;
    private SimpleFeatureBuilder sfBuilder = null;
    private List<SimpleFeature> features;
    private List<ShapeFileAttribute> attributes;

    public static final String POINT = "Point";
    public static final String MULTI_POINT = "MultiPoint";
    public static final String MULTI_LINE_STRING = "MultiLineString";
    public static final String MULTI_POLYGON = "MultiPolygon";

    public IoShapeWriter() {
        logString = this.getClass().getName();
        this.features = new ArrayList<>();
    }
    
    public void initFeatureType(String featureType, String epsg, List<ShapeFileAttribute> attributes) throws T3dNotYetImplException, T3dException, FactoryException {
        initFeatureType(featureType, epsg);
        this.attributes = attributes;
        if (this.attributes != null) {
            for (ShapeFileAttribute attribute : this.attributes) {
                attribute.init(sftBuilder);
            }
        }
    }

    public void initFeatureType(String featureType, String epsg) throws T3dNotYetImplException, T3dException, FactoryException {
        this.sftBuilder = new SimpleFeatureTypeBuilder();
        this.sftBuilder.setName("GeologicToolbox-FeatureTypeBuilder");
        CRSAuthorityFactory crsAuthorityFactory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);
        CoordinateReferenceSystem crs = crsAuthorityFactory.createCoordinateReferenceSystem(epsg);
        this.sftBuilder.setCRS(crs);

        switch (featureType) {
            case POINT:
                throw new T3dNotYetImplException("FeatureType " + POINT + "not yet implemented!");
            case MULTI_POINT:
                throw new T3dNotYetImplException("FeatureType " + MULTI_POINT + "not yet implemented!");
            case MULTI_LINE_STRING:
                throw new T3dNotYetImplException("FeatureType " + MULTI_LINE_STRING + "not yet implemented!");
            case MULTI_POLYGON:
                this.sftBuilder.add("the_geom", MultiPolygon.class);
                this.initialized = true;
                break;
            default:
                throw new T3dException("Unsupported FeatureType!");
        }
        

    }

    public void writeShapeFile(String filename) throws T3dException, IOException {
        if (!this.dataAvailable) {
            throw new T3dException("There is no data available to write the shape file!");
        }
        File newFile = new File(filename);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(this.sft);
        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

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

    public void createPolygonZFeatures(GmSimpleTINFeature tin) throws T3dException {
        if (!this.built) {
            throw new T3dException("You need to build the shapes feature type before adding data to it!");
        }
        GmSimpleTINGeometry geom = (GmSimpleTINGeometry) tin.getGeometry();

        GeometryBuilder gb = new GeometryBuilder();

        for (int i = 0; i < geom.numberOfTriangles(); i++) {
            int[] triIdx = geom.getTriangleVertexIndices(i);

            double[] points = new double[(triIdx.length + 1) * 3];
            int l = triIdx.length;
            for (int j = 0; j < points.length; j += 3) {   // 3 = idx of x, y, z in points[]
                points[j] = geom.getPoint(triIdx[(j / 3) % l]).getX();
                points[j + 1] = geom.getPoint(triIdx[(j / 3) % l]).getY();
                points[j + 2] = geom.getPoint(triIdx[(j / 3) % l]).getZ();
            }

            Polygon poly = gb.polygonZ(points);
            MultiPolygon mPoly = gb.multiPolygon(new Polygon[]{poly});

            this.sfBuilder.add(mPoly);  // add geometry at first!
            // here: add polygon attributes -> sfBuilder.add(xxx);
            // --> 
            if (attributes != null) {
                for (ShapeFileAttribute attribute : attributes) {
                    attribute.calcAttributes(sfBuilder, points);
                }
            }
            // <--
            SimpleFeature feature = sfBuilder.buildFeature(null);
            this.features.add(feature);
        }
        dataAvailable = true;
    }

    public void buildFeatureType() throws T3dException {
        if (this.initialized) {
            this.sft = sftBuilder.buildFeatureType();
            this.sfBuilder = new SimpleFeatureBuilder(sft);
            this.built = true;
        } else {
            throw new T3dException("You need to initialize a featureType before building it!");
        }
    }

    public void addIntegerFeatureTypeAttribute(String attributeName) throws T3dException {
        if (this.initialized) {
            this.sftBuilder.add(attributeName, Integer.class);
        } else {
            throw new T3dException("You need to initialize a featureType before adding attributes!");
        }
    }

    public void addDoubleFeatureTypeAttribute(String attributeName) throws T3dException {
        if (this.initialized) {
            this.sftBuilder.add(attributeName, Double.class);
        } else {
            throw new T3dException("You need to initialize a featureType before adding attributes!");
        }
    }

    public void addStringFeatureTypeAttribute(String attributeName) throws T3dException {
        if (this.initialized) {
            this.sftBuilder.length(32).add(attributeName, String.class);
        } else {
            throw new T3dException("You need to initialize a featureType before adding attributes!");
        }
    }

    @Override
    public String log() {
        return logString;
    }

}
