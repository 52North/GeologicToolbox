package org.n52.v3d.triturus.geologic.util;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.vgis.VgGeomObject;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTriangle;

/**
 * @author MoritzWollenhaupt
 */
public class ClarNotationShapeFileAttribute extends ShapeFileAttribute {

    private boolean hasZeroArea, isPlain, isVertical, dip, azimuth, compassDirection;
    private int angleUnit;

    public ClarNotationShapeFileAttribute(boolean hasZeroArea, boolean isPlain, boolean isVertical,
            boolean dip, boolean azimuth, boolean compassDirection,
            int angleUnit) {
        this.hasZeroArea = hasZeroArea;
        this.isPlain = isPlain;
        this.isVertical = isVertical;
        this.dip = dip;
        this.azimuth = azimuth;
        this.compassDirection = compassDirection;
        this.angleUnit = angleUnit;
    }

    @Override
    public void init(SimpleFeatureTypeBuilder sftBuilder) {
        if (hasZeroArea) {
            sftBuilder.add("hasZeroAre", Boolean.class);
        }
        if (isPlain) {
            sftBuilder.add("isPlain", Boolean.class);
        }
        if (isVertical) {
            sftBuilder.add("isVertical", Boolean.class);
        }
        if (dip) {
            if (angleUnit == Orientation.DEGREE) {
                sftBuilder.add("dipDEG", Double.class);
            }
            if (angleUnit == Orientation.GRAD) {
                sftBuilder.add("dipGRAD", Double.class);
            }
            if (angleUnit == Orientation.RAD) {
                sftBuilder.add("dipRAD", Double.class);
            }
        }
        if (azimuth) {
            if (angleUnit == Orientation.DEGREE) {
                sftBuilder.add("azimuthDEG", Double.class);
            }
            if (angleUnit == Orientation.GRAD) {
                sftBuilder.add("azimuthGRA", Double.class);
            }
            if (angleUnit == Orientation.RAD) {
                sftBuilder.add("azimuthRAD", Double.class);
            }
        }
        if (compassDirection) {
            sftBuilder.add("compassDir", String.class);
        }
    }

    @Override
    public void calcAttributes(SimpleFeatureBuilder sfBuilder, double[] points) {
        VgPoint p1 = new GmPoint(points[0], points[1], points[2]),
                p2 = new GmPoint(points[3], points[4], points[5]),
                p3 = new GmPoint(points[6], points[7], points[8]);
        VgTriangle tri = new GmTriangle(p1, p2, p3);
        tri.setSRS(VgGeomObject.SRSNone);

        Orientation orient = new Orientation(tri);

        if (hasZeroArea) {
            sfBuilder.add(orient.hasZeroArea());
        }
        if (isPlain) {
            sfBuilder.add(orient.isPlain());
        }
        if (isVertical) {
            sfBuilder.add(orient.isVertical());
        }
        if (dip) {
            if (angleUnit == Orientation.DEGREE) {
                sfBuilder.add(orient.dip());
            }
            if (angleUnit == Orientation.GRAD) {
                sfBuilder.add(orient.dipGon());
            }
            if (angleUnit == Orientation.RAD) {
                sfBuilder.add(orient.dipRad());
            }
        }
        if (azimuth) {
            if (angleUnit == Orientation.DEGREE) {
                sfBuilder.add(orient.azimuth());
            }
            if (angleUnit == Orientation.GRAD) {
                sfBuilder.add(orient.azimuthGon());
            }
            if (angleUnit == Orientation.RAD) {
                sfBuilder.add(orient.azimuthRad());
            }
        }
        if (compassDirection) {
            sfBuilder.add(orient.compassDirection());
        }

    }

}
