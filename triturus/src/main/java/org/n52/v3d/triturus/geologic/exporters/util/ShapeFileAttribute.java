package org.n52.v3d.triturus.geologic.exporters.util;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

/**
 * @author MoritzWollenhaupt
 */
public abstract class ShapeFileAttribute {
    
    public abstract void init(SimpleFeatureTypeBuilder sftBuilder);
    public abstract void calcAttributes(SimpleFeatureBuilder sfBuilder, double[] points);
    
}
