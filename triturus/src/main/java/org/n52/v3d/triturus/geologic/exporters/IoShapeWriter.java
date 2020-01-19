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
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINGeometry;
import org.n52.v3d.triturus.gisimplm.IoAbstractWriter;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

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
    private final GeometryFactory geomFactory;
    private List<SimpleFeature> features;

    public static final String POINT = "Point";
    public static final String MULTI_POINT = "MultiPoint";
    public static final String MULTI_LINE_STRING = "MultiLineString";
    public static final String MULTI_POLYGON = "MultiPolygon";

    public IoShapeWriter() {
        logString = this.getClass().getName();
        this.geomFactory = JTSFactoryFinder.getGeometryFactory();
        this.features = new ArrayList<>();
    }

    public void initFeatureType(String featureType) throws T3dNotYetImplException, T3dException {
        this.sftBuilder = new SimpleFeatureTypeBuilder();
        this.sftBuilder.setName("GeologicToolbox-FeatureTypeBuilder");
        this.sftBuilder.setCRS(DefaultGeographicCRS.WGS84);
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
        }
        else {
            throw new T3dException(typeName + " does not support read/write access");
        }
    }

    public void createSimplePolygonFeatures(GmSimpleTINFeature tin) throws T3dException {
        if (!this.built) {
            throw new T3dException("You need to build the shapes feature type before adding data to it!");
        }
        GmSimpleTINGeometry geom = (GmSimpleTINGeometry) tin.getGeometry();

        for (int i = 0; i < geom.numberOfTriangles(); i++) {
            int[] triIdx = geom.getTriangleVertexIndices(i);
            CoordinateXYM[] points = new CoordinateXYM[triIdx.length + 1]; // +1 -> last point equals first point
            for (int j = 0; j < triIdx.length; j++) {
                VgPoint p = geom.getPoint(triIdx[j]);
                points[j] = new CoordinateXYM(p.getX(), p.getY(), p.getZ());
            }
            points[points.length - 1] = points[0];

            Polygon poly = geomFactory.createPolygon(points);
            this.sfBuilder.add(poly);
            // here: add polygon attributes -> sfBuilder.add(xxx);
            // --> 
            this.sfBuilder.add(i);
            this.sfBuilder.add("StringAttribute " + i);
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
