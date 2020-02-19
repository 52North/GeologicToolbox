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
