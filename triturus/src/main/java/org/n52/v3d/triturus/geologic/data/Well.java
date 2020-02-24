/**
 * Copyright (C) 2020 52 North Initiative for Geospatial Open Source
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
 * info@52north.org
 */
package org.n52.v3d.triturus.geologic.data;

import java.util.ArrayList;
import java.util.List;

import org.n52.v3d.triturus.geologic.util.Orientation;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Well data object. Instances of this class hold all relevant information 
 * about <i>wells</i> respectively <i>drillings</i> or <i>boreholes</i> in the 
 * ground. Geometrically, here line strings in 3-d space are given, where the
 * line segments' start-points give <i>marker</i> positions. A marker is
 * specified by a unique name. Optionally, additional information such as
 * azimuth and dip values at marker positions, vertical and horizontal position
 * offsets (Kelly bushing height or horizontal deviations from the well 
 * position) or a maximum depth value (German: Endteufe) might be given. 
 * 
 * @author Benno Schmidt
 */
public class Well 
{
	private String name = null;
	private VgPoint loc = null;
	private MarkerList markers = new MarkerList();
	private double kb = 0.;
	private Double maxDepth = null;
	
	/**
	 * Constructor. As a parameter, a unique identifier has to be given.
	 * 
	 * @param wellName Well name 
	 */
	public Well(String wellName) {
		this.name = wellName;
		markers.arr.clear();
	}

	public String getName() {
		return this.name;
	}

	/**
	 * sets the well location at ground level. In the scope of this framework, 
	 * the z-value of <tt>loc</tt> should give the elevation at ground level 
	 * (&quot;datum&quot;). 
	 * 
	 * @param pos Well location
	 */
	public void setLocation(VgPoint loc) {
		this.loc = loc;
	}

	/**
	 * gets the well location at ground level. 
	 * 
	 * @return Well location
	 */
	public VgPoint getLocation() {
		return this.loc;
	}

	/**
	 * gets the ground level's elevation value ((&quot;datum&quot;)), i.e. 
	 * <tt>this.getLocation().getZ()</tt>. If no location has been specified 
	 * for the well, the return-value will be <i>null</i>.
	 * 
	 * @return Ground level elevation
	 */
	public Double groundLevel() {
		return this.loc == null ? null : loc.getZ();
	}
	
	/**
	 * sets the Kelly bushing height (KB), i.e. the height of the drilling floor 
	 * above the ground level. If KB is set to a value that is not 0, the 
	 * wellbore depth measurements will refer to the Kelly Bushing elevation
	 * which is calculated by adding the ground level <tt>this.getLocation().getZ()</tt> 
	 * to the Kelly bushing height KB. 
	 * <br/>
	 * By default, KB is 0.
	 * 
	 * @param kb Kelly bushing height 
	 */
    public void setKellyBushingHeight(double kb) {	
    	this.kb = kb;
    }

    public double getKellyBushingHeight() {	
    	return this.kb;
    }

    /**
     * sets the maximum depth value referring to <tt>this.groundLevel()</tt> 
     * + KB, where KB gives the Kelly bushing height (if set).
     * 
     * @param maxDepth Maximum depth 
     */
	public void setMaxDepth(Double maxDepth) {
		this.maxDepth = maxDepth;
	}

    /**
     * gets the maximum depth value. If this value has not been set, the 
     * method will return <i>null</i>.
     * 
     * @return Maximum depth information
     */    
    public Double getMaxDepth() {
		return maxDepth;
	}

	/**
     * adds a marker to the well object. The given marker depth value will 
     * refer to <tt>this.groundLevel()</tt> + KB, where KB gives the Kelly 
     * bushing height, see {@link setKellyBushingHeight}.
     * 
     * @param depth Marker depth
     * @param markerName Marker name
     * @param dx Offset in x-direction against well location 
     * @param dy Offset in y-direction against well location 
     */
	public void addMarker(
		double depth, 
		String markerName, 
		double dx, double dy) 
	{
		markers.add(new Marker(depth, markerName, dx, dy, null));
	}

	/**
	 * gets the well's markers as {@link MarkerList} object. The <i>depth</i> 
	 * values of the {@link Marker}s are given in ascending order, i.e. the 
	 * resulting list is already sorted.
	 * 
	 * @return Marker list 
	 */
	public MarkerList getMarkers() {
		return markers;
	}

	/**
	 * Marker list holding the &lt;<i>depth</i>, <i>name</i>&gt; triples, where
	 * <i>name</i> gives the marker names which should be unique inside your
	 * application. 
	 */
	public class MarkerList {
		public List<Marker> arr = new ArrayList<Marker>();
		
		/**
		 * adds a {@Marker} consisting of a depth-value and a marker name to 
		 * the list.
		 * 
		 * @param m Marker object
		 */
		public void add(Marker m) {
			if (m == null || m.name == null) {
				System.out.println("Tried to add illegal marker object!");
				return;
			}
			int i = 0;
			while (i < arr.size()) {
				if (m.depth < arr.get(i).depth) {
					arr.add(i, m);
					return;
				}
				i++;
			}
			arr.add(m);
		}
		
		public String toString() {
			StringBuffer s = new StringBuffer();
			for (Marker m : markers.arr) {
				s.append("("+ m.depth + ", " + m.name + ")");
			}
			return s.toString();
		}
	}
	
	/**
	 * Marker information object
	 */
	public class Marker 
	{
		public double depth;
		public String name; 
		public double dx, dy; 
		public Orientation orient; 
	
		/**
		 * Marker information consisting of a depth value (MD) and a unique 
		 * marker name. 
		 * <br/>
		 * Depth values &gt; 0 indicate locations below ground 
		 * (&quot;-z direction&quot;). The marker depth is given with respect 
		 * to <tt>this.groundLevel()</tt> + KB, where KB gives the Kelly 
		 * bushing height, see {@link setKellyBushingHeight}.
		 * <br/>
		 * The {@link Orientation}-object allows to hold dip and azimuth 
		 * information; pass a <i>null</i> value for <tt>orient</tt>, if this 
		 * information is not needed in your application.
		 * 
		 * @param depth Marker depth
		 * @param name Marker name
		 * @param dx Position offset in x-direction (default-value: 0)
		 * @param dy Position offset in y-direction (default-value: 0)
		 * @param orient Orientation information (default-value: null)
		 */
		public Marker(
			double depth, 
			String name, 
			double dx, double dy, 
			Orientation orient) 
		{
			this.depth = depth; this.name = name; dx = 0.0; dy = 0.0; orient = null;
		}
	}
	
	/*
	public GmAttrFeature wellFeature() {
		// TODO generated attributed VgLineSegment instance
		return null;
	}
	*/

	/*
	public GmAttrFeature markerFeature() {
		// TODO generated attributed VgPoint instance
		return null;
	}
	*/

	public String toString() {
		return "<Well>{(NAME: " + this.name + "), " + this.loc + "}";
	}
}
