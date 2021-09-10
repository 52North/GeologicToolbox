/**
 * Copyright (C) 2019-2020 52North Initiative for Geospatial Open Source
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
package org.n52.v3d.triturus.geologic.util;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.t3dutil.T3dVector;
import org.n52.v3d.triturus.vgis.T3dSRSException;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTriangle;

/**
 * Orientation objects provide strike and dip information about triangles. 
 * This class also provides Clar's notation as often used by geologists. 
 * Moreover <tt>Orientation</tt> objects might be of help carrying out 
 * exposition or inclination analysis tasks. 
 * <br>
 * Note: Triangle vertex ordering will is not considered. It will be assumed 
 * that the triangle always runs downhill and the triangle normal is heading
 * upwards, i.e. the dip value will always be in the range 0 ... 90 degrees. 
 * 
 * @author Benno Schmidt
 */
public class Orientation 
{
    public static final int DEGREE = 0;
    public static final int GRAD = 1;
    public static final int RAD = 2;
        
	private T3dVector dir; // face normal heading upwards
	
	static public double 
		rad2degr = 180./Math.PI,
		rad2gon = 200./Math.PI;
	
	private boolean hasZeroArea, isHorizontal, isVertical;
	
	/**
	 * constructs an orientation object. For triangle coordinates referring to
	 * geographic coordinate systems an exception will be thrown.
	 * 
	 * @param tri Triangle object
	 * @see T3dSRSException
	 */
	public Orientation(VgTriangle tri) {
		if (tri.hasGeographicSRS())
			throw new T3dSRSException("Orientation object construction failed.");
		this.dir = Orientation.direction(tri);
		
		this.hasZeroArea = this.hasZeroArea();
		this.isHorizontal = this.isHorizontal();
		this.isVertical = this.isVertical();
	}

	static private T3dVector direction(VgTriangle tri) {
		VgPoint[] p = tri.getCornerPoints();
		T3dVector 
			v0 = new T3dVector(p[0].getX(), p[0].getY(), p[0].getZ()),
			v1 = new T3dVector(p[1].getX(), p[1].getY(), p[1].getZ()),
			v2 = new T3dVector(p[2].getX(), p[2].getY(), p[2].getZ());
		T3dVector
			dir1 = new T3dVector(
					v1.getX() - v0.getX(),
					v1.getY() - v0.getY(),
					v1.getZ() - v0.getZ()),
			dir2 = new T3dVector(
					v2.getX() - v0.getX(),
					v2.getY() - v0.getY(),
					v2.getZ() - v0.getZ());			
		T3dVector x = crossProduct(dir1, dir2);
		if (x.getZ() < 0.) {
			x.setX(-x.getX()); x.setY(-x.getY()); x.setZ(-x.getZ()); 
		}
		return x;
	}

	/**
	 * checks whether the area of the triangle that has been given in the 
	 * constructor is 0. Note that <tt>this.hasZeroArea()</tt> implies 
	 * <tt>this.isPlain()</tt> and <tt>this.isVertical()</tt>.
	 * 
	 * @return <i>true</i> if the triangle's area is 0
	 * @see #isVertical()
	 */
	public boolean hasZeroArea() {
		return dir.length() == 0.0;
	}
	
	/**
	 * checks whether the triangle that has been given in the constructor is 
	 * plain with respect to the x-y plane (horizontal triangle). Note that this
	 * case also occurs if <tt>this.hasZeroArea()</tt> here.
	 * 
	 * @return <i>true</i> for horizontal orientation
	 * @see #hasZeroArea()
	 */
	public boolean isHorizontal() {
		return dir.getX() == 0. && dir.getY() == 0.; 
	}

	/**
	 * @deprecated
	 * @return {@link #isHorizontal()}
	 */
	public boolean isPlain() {
		return this.isHorizontal();
	}

	/**
	 * checks whether the triangle that has been given in the constructor is 
	 * parallel to the z-axis (vertical triangle). Note that this case also 
	 * occurs if <tt>this.hasZeroArea()</tt> here.
	 * 
	 * @return <i>true</i> for vertical orientation
	 * @see #hasZeroArea()
	 */
	public boolean isVertical() {
		return dir.getZ() == 0.; 
	}

	static private T3dVector crossProduct(T3dVector v1, T3dVector v2)
	{
		return new T3dVector(
			v1.getY() * v2.getZ() - v1.getZ() * v2.getY(),
			v1.getZ() * v2.getX() - v1.getX() * v2.getZ(),
			v1.getX() * v2.getY() - v1.getY() * v2.getX());
	}
	
	/**
	 * calculates the dip value (inclination) for the triangle given in 
	 * the constructor. The result is given in degrees, i.e. in the range 
	 * <i>0 &lt;= dip &lt;= 90</i>. For a horizontal triangle the result will be 0, 
	 * for a vertical triangle +90. Note that the return value will be 0 if 
	 * the triangle's area is 0.
	 * 
	 * @return Dip value in degrees 
	 * @see #hasZeroArea()
	 * @see #isPlain()
	 * @see #isVertical()
	 */
	public double dip() {
		return this.dipRad() * rad2degr; 
	}

	/**
	 * provides the dip value given in degrees as integer in the range 
	 * <i>0 &lt;= dipInt &lt;= 90</i>. Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in degrees
	 * @see #dip()
	 */
	public int dipInt() {
		return (int) Math.round(this.dip()); 
	}

	/**
	 * provides the dip value given in radians in the range 
	 * <i>0 &lt;= dipRad &lt;= PI/2</i>. 
	 * Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in radians
	 * @see #dip()
	 */
	public double dipRad() {
		if (this.hasZeroArea()) 
			return 0.;
		if (this.isVertical()) 
			return Math.PI/2.;
		
		T3dVector horiz = 
			dir.getX() == 0. && dir.getY() == 0.
			? new T3dVector(1., 0., 0.)
			: new T3dVector(dir.getX(), dir.getY(), 0.); 
		double dip = 
			Math.PI/2. 
			- new T3dVector(0., 0., 0.).angle(dir, horiz);
		if (dip < 0. || dip > Math.PI / 2.) {
			throw new T3dException("numerical dip computation error"); 
		}
		return dip;
	}
	
	/**
	 * provides the dip value given in gon as integer in the range 
	 * <i>0 &lt;= dipGon &lt;= 100</i>. 
	 * Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in gon
	 * @see #dip()
	 */
	public double dipGon() {
		return this.dipRad() * rad2gon; 
	}
	
	/**
	 * provides the dip value given in gon as integer in the range 
	 * <i>0 &lt;= dipGonInt &lt;= 100</i>. 
	 * Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in gon
	 * @see #dip()
	 */
	public int dipGonInt() {
		return (int) Math.round(this.dipGon()); 
	}
	
	/**
	 * calculates the azimuth value (exposition or <i>dip direction</i>) for 
	 * the triangle given in the constructor. The result is given in degrees, 
	 * i.e. in the range <i>0.0 &lt;= azimuth &lt; 360.0</i>. For a horizontal or 
	 * vertical triangle the result will be -1. Note that the return value also
	 * will be -1, if the triangle's area is 0.
	 * <br>
	 * For the x-axis heading East and the y-axis heading North, the azimuth is
	 * given as follows:
	 * <table summary="">
	 *   <tr>
	 *     <th>azimuth</th>
	 *     <th>compass direction</th>
	 *   </tr>
	 *   <tr>
	 *     <td>0</td>
	 *     <td>N</td>
	 *   </tr>
	 *   <tr>
	 *     <td>90</td>
	 *     <td>E</td>
	 *   </tr>
	 *   <tr>
	 *     <td>180</td>
	 *     <td>S</td>
	 *   </tr>
	 *   <tr>
	 *     <td>270</td>
	 *     <td>W</td>
	 *   </tr>
	 * </table>
	 *    
	 * @return Azimuth value in degrees, or -1 for horizontal, vertical, and zero triangles
	 * @see #isPlain()
	 * @see #hasZeroArea()
	 */
	public double azimuth() {
		if (this.hasZeroArea || this.isHorizontal || this.isVertical) 
			return -1.;

		return this.azimuthRad() * rad2degr; 
	}

	/**
	 * provides the azimuth value (exposition or <i>dip direction</i>) given 
	 * in degrees as integer in the range <i>0 &lt;= azimuthInt &lt;= 359</i>. 
	 * Also see documentation for <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in degrees, or -1 for horizontal, vertical, and zero triangles
	 * @see #azimuth()
	 */
	public int azimuthInt() {
		if (this.hasZeroArea || this.isHorizontal || this.isVertical) 
			return -1;
		
		int res = (int) Math.round(this.azimuth()); 
		return res >= 360 ? 0 : res;
	}

	/**
	 * provides the azimuth value (exposition or <i>dip direction</i>) given 
	 * in radians in the range <i>0 &lt;= azimuthRad &lt;= 2*PI</i>. Also see 
	 * documentation for <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in radians, or -1 for horizontal, vertical, and zero triangles
	 * @see #azimuth()
	 */
	public double azimuthRad() {
		if (this.hasZeroArea || this.isHorizontal || this.isVertical) 
			return -1.;

		if (dir.getX() == 0.)
			return dir.getY() > 0. ? 0. : Math.PI;
		
		T3dVector 
			v0 = new T3dVector(0., 0., 0.),
			v1 = new T3dVector(0., 1., 0.),
			v2 = new T3dVector(dir.getX(), dir.getY(), 0.);
		double phi = v0.angle(v1, v2);
		return dir.getX() > 0. ? phi : 2. * Math.PI - phi;			
	}

	/**
	 * provides the azimuth value (exposition or <i>dip direction</i>) given 
	 * in gon as integer in the range <i>0.0 &lt;= azimuth &lt; 400.0</i>. 
	 * Also see documentation for <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in gon, or -1 for horizontal, vertical, and zero triangles
	 * @see #azimuth()
	 */
	public double azimuthGon() {
		if (this.hasZeroArea || this.isHorizontal || this.isVertical) 
			return -1;

		return this.azimuthRad() * rad2gon; 
	}

	/**
	 * provides the azimuth value (exposition or <i>dip direction</i>) given 
	 * in gon as integer in the range <i>0 &lt;= azimuthGonInt &lt;= 399</i>. 
	 * Also see documentation for <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in gon, or -1 for horizontal, vertical, and zero triangles
	 * @see #azimuth()
	 */
	public int azimuthGonInt() {
		if (this.hasZeroArea || this.isHorizontal || this.isVertical) 
			return -1;

		return (int) Math.round(this.azimuthGon()); 
	}

	/**
	 * provides the orientation information in Clar notation.
	 * 
	 * @return Orientation in Clar notation
	 */
	public String clarNotation() {
		return this.azimuthInt() + "/" + this.dipInt(); 
	}

	/**
	 * provides the compass direction corresponding to the given orientation. 
	 * Possible result strings are "N", "NE", "E", "SE", "S", "SW", "W", "NW", 
	 * or "-" for horizontal, vertical, and zero triangles
	 * 
	 * @return Compass direction, or "-" if undeterminable
	 * @see #isPlain()
	 */
	public String compassDirection() 
	{
		if (this.hasZeroArea || this.isHorizontal || this.isVertical)
			return "-";
	
		int k = (int) Math.round(this.azimuth() / 45.);
		switch (k) {
			case 0: case 8: return "N"; 
			case 1: return "NE"; 
			case 2: return "E"; 
			case 3: return "SE"; 
			case 4: return "S"; 
			case 5: return "SW"; 
			case 6: return "W"; 
			case 7: return "NW"; 
		}
		return "ERROR"; // this code line should never be reached
	}

	/**
	 * provides the compass direction corresponding to the given orientation as
	 * integer-valued class.
	 * <table summary="">
	 *   <tr>  
	 *     <th>direction</th><th>value</th>
     *   </tr>
	 *   <tr>  
	 *     <td>N</td><td>1</td>
     *   </tr>
	 *   <tr>  
	 *     <td>NE</td><td>2</td>
     *   </tr>
	 *   <tr>  
	 *     <td>E</td><td>3</td>
     *   </tr>
	 *   <tr>  
	 *     <td>SE</td><td>4</td>
     *   </tr>
	 *   <tr>  
	 *     <td>S</td><td>5</td>
     *   </tr>
	 *   <tr>  
	 *     <td>SW</td><td>6</td>
     *   </tr>
	 *   <tr>  
	 *     <td>W</td><td>7</td>
     *   </tr>
	 *   <tr>  
	 *     <td>NW</td><td>8</td>
     *   </tr>
	 *   <tr>  
	 *     <td>-</td><td>0</td>
     *   </tr>
	 * </table>
	 * 
	 * @return Compass direction class
	 * @see #isPlain()
	 */
	public int compassDirectionClass() 
	{
		if (this.hasZeroArea || this.isHorizontal || this.isVertical)
			return 0; // "-"
	
		int k = (int) Math.round(this.azimuth() / 45.);
		switch (k) {
			case 0: case 8: return 1; // N 
			case 1: return 2; // NE 
			case 2: return 3; // E 
			case 3: return 4; // SE
			case 4: return 5; // S
			case 5: return 6; // SW
			case 6: return 7; // W
			case 7: return 8; // NW
		}
		return 0; // this code line should never be reached
	}
	
	/**
	 * calculates the strike value (direction of intersection line of triangle 
	 * and x-y plane) for the triangle given in the constructor. The result is 
	 * given in degrees, i.e. in the range <i>0 &lt;= strike &lt; 180</i>. 
	 * For a horizontal triangle the result will be -1. Note that the return 
	 * value will be -1 if the triangle's area is 0.
	 * 
	 * @return Strike value in degrees, or -1 for horizontal or zero triangles
	 * @see #hasZeroArea()
	 * @see #isPlain()
	 */
	public double strike() {
		if (this.hasZeroArea || this.isHorizontal)
			return -1.;

		return this.strikeRad() * rad2degr; 
	}

	/**
	 * provides the strike value given in degrees as integer in the range 
	 * <i>0 &lt;= strikeInt &lt;= 179</i>. 
	 * Also see documentation for <tt>this#strike</tt>.
	 * 
	 * @return Strike in degrees, or -1 for horizontal or zero triangles 
	 * @see #strike()
	 */
	public int strikeInt() {
		if (this.hasZeroArea || this.isHorizontal)
			return -1; 
		
		int val = (int) Math.round(this.strike()); 
		if (val == 180) val = 0;
		return val;
	}

	/**
	 * provides the strike value given in radians in the range 
	 * <i>0 &lt;= strike &lt; PI</i>. 
	 * Also see documentation for <tt>this#strike</tt>.
	 * 
	 * @return Strike in radians, or -1 for horizontal or zero triangles 
	 * @see #strike()
	 */
	public double strikeRad() {
		if (this.hasZeroArea || this.isHorizontal)
			return -1.;
		
		double azimuth;
		// The following code has been copied from azimuthRad():
		if (dir.getX() == 0.)
			azimuth = dir.getY() > 0. ? 0. : Math.PI;
		else {
			T3dVector 
				v0 = new T3dVector(0., 0., 0.),
				v1 = new T3dVector(0., 1., 0.),
				v2 = new T3dVector(dir.getX(), dir.getY(), 0.);
			double phi = v0.angle(v1, v2);
			azimuth = dir.getX() > 0. ? phi : 2. * Math.PI - phi;			
		}
		
		double strike = azimuth + Math.PI/2.;
		while (strike >= Math.PI) { strike -= Math.PI; }
		return strike; 
	}
	
	/**
	 * provides the strike value given in gon as integer in the range 
	 * <i>0 &lt;= strikeGon &lt; 200</i>. 
	 * Also see documentation for <tt>this#strike</tt>.
	 * 
	 * @return Strike in gon, or -1 for horizontal or zero triangles 
	 * @see #strike()
	 */
	public double strikeGon() {
		if (this.hasZeroArea || this.isHorizontal)
			return -1.;
		
		return this.strikeRad() * rad2gon; 
	}
	
	/**
	 * provides the strike value given in gon as integer in the range 
	 * <i>0 &lt;= strikeGonInt &lt;= 199</i>. 
	 * Also see documentation for <tt>this#strike</tt>.
	 * 
	 * @return Strike in gon, or -1 for horizontal or zero triangles 
	 * @see #strike()
	 */
	public int strikeGonInt() {
		if (this.hasZeroArea || this.isHorizontal)
			return -1;
		
		return (int) Math.round(this.strikeGon()); 
	}
}
