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
package org.n52.v3d.triturus.geologic.util;

import org.n52.v3d.triturus.t3dutil.T3dVector;
import org.n52.v3d.triturus.vgis.T3dSRSException;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTriangle;

/**
 * Orientation objects provide azimuth and dip information about triangles. 
 * This class also provides Clar's notation as often used by geologists. 
 * Moreover <tt>Orientation</tt> objects might be of help carrying out 
 * exposition or inclination analysis tasks. 
 * 
 * @author Benno Schmidt
 */
public class Orientation 
{
	private T3dVector dir; 
	
	static public double 
		rad2degr = 180./Math.PI,
		rad2gon = 200./Math.PI;
	
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
		return crossProduct(dir1, dir2);
	}

	/**
	 * checks whether the area of the triangle that has been given in the 
	 * constructor is 0. Note that <tt>this.hasZeroArea()</tt> implies 
	 * <tt>this.isPlain()</tt> and <tt>this.isVertical()</tt>.
	 * 
	 * @return <i>true</i> if the triangle's area is 0
	 * @see {@link #hasZeroArea()}
	 * @see {@link #isVertical()}
	 */
	public boolean hasZeroArea() {
		return dir.length() == 0.0;
	}
	
	/**
	 * checks whether the triangle that has been given in the constructor is 
	 * plain with respect to the xy-plane (horizontal triangle). Note that this
	 * case also occurs if <tt>this.hasZeroArea()</tt> here.
	 * 
	 * @return <i>true</i> for horizontal orientation
	 * @see {@link #hasZeroArea()}
	 */
	public boolean isPlain() {
		return dir.getX() == 0. && dir.getY() == 0.; 
	}

	/**
	 * checks whether the triangle that has been given in the constructor is 
	 * parallel to the z-axis (vertical triangle). Note that this case also 
	 * occurs if <tt>this.hasZeroArea()</tt> here.
	 * 
	 * @return <i>true</i> for vertical orientation
	 * @see {@link #hasZeroArea()}
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
	 * <i>0 ... 90</i>. For a horizontal triangle the result will be 0, for
	 * a vertical triangle +90. Note that the return value will be 0 if the 
	 * triangle's area is 0.
	 * 
	 * @return Dip value in degrees 
	 * @see {@link #hasZeroArea()}
	 * @see {@link #isPlain()}
	 * @see {@link #isVertical()}
	 */
	public double dip() {
		return this.dipRad() * rad2degr; 
	}

	/**
	 * provides the dip value given in degrees as integer in the range 
	 * <i>0 ... 90</i>. Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in degrees
	 * @see {@link #dip()}
	 */
	public int dipInt() {
		return (int) Math.round(this.dip()); 
	}

	/**
	 * provides the dip value given in radians in the range. Also see 
	 * documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in radians
	 * @see {@link #dip()}
	 */
	public double dipRad() {
		if (this.hasZeroArea()) 
			return 0.;
		if (this.isVertical()) 
			return Math.PI / 2.;
		
		T3dVector horiz = new T3dVector(dir.getX(), dir.getY(), 0.); 
		return new T3dVector(0., 0., 0.).angle(dir, horiz); 
	}
	
	/**
	 * provides the dip value given in gon as integer in the range 
	 * <i>0 ... 100</i>. Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in gon
	 * @see {@link #dip()}
	 */
	public double dipGon() {
		return this.dipRad() * rad2gon; 
	}
	
	/**
	 * provides the dip value given in gon as integer in the range 
	 * <i>0 ... 100</i>. Also see documentation for <tt>this#dip</tt>.
	 * 
	 * @return Dip in gon
	 * @see {@link #dip()}
	 */
	public int dipGonInt() {
		return (int) Math.round(this.dipGon()); 
	}
	
	/**
	 * calculates the azimuth value for the triangle given in the 
	 * constructor. The result is given in degrees, i.e. in the range 
	 * <i>0.0 <= azimuth < 360.0</i>. For a horizontal triangle the result will
	 * be 0. Note that the return value will be 0 if the triangle's area is 0.
	 * <br />
	 * For the x-axis heading East and the y-axis heading North, the azimut is
	 * given as follows:
	 * <table>
	 *   <th>
	 *     <td>azimuth</td>
	 *     <td>compass direction</td>
	 *   </th>
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
	 * @return Azimuth value in degrees 
	 * @see {@link #isPlain()}
	 * @see {@link #hasZeroArea()
	 */
	public double azimuth() {
		return this.azimuthRad() * rad2degr; 
	}

	/**
	 * provides the azimuth value (exposition) given in degrees as 
	 * integer in the range <i>0 ... 359</i>. Also see documentation for 
	 * <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in degrees
	 * @see {@link #azimuth()}
	 */
	public int azimuthInt() {
		int res = (int) Math.round(this.azimuth()); 
		return res >= 360 ? 0 : res;
	}

	/**
	 * provides the azimuth value given in radians in the range. Also see 
	 * documentation for <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in radians
	 * @see {@link #azimuth()}
	 */
	public double azimuthRad() {
		if (this.hasZeroArea() || this.isPlain())
			return 0.;
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
	 * provides the azimuth value given in gon as integer in the range 
	 * <i>0.0 <= azimuth < 400.0</i>. Also see documentation for 
	 * <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in gon
	 * @see {@link #azimuth()}
	 */
	public double azimuthGon() {
		return this.azimuthRad() * rad2gon; 
	}

	/**
	 * provides the azimuth value given in gon as integer in the range 
	 * <i>0 ... 400</i>. Also see documentation for <tt>this#azimuth</tt>.
	 * 
	 * @return Azimuth in gon
	 * @see {@link #azimuth()}
	 */
	public double azimuthGonInt() {
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
	 * or "-" for plain triangles. 
	 * 
	 * @return Compass direction
	 * @see {@link #isPlain()}
	 */
	public String compassDirection() 
	{
		if (this.isPlain()) return "-";
	
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
}
