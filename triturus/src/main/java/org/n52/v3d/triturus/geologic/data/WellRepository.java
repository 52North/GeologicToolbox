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

import org.n52.v3d.triturus.geologic.data.Well.MarkerList;
import org.n52.v3d.triturus.vgis.T3dSRSException;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Repository to manage a set of wells data objects. Basically, well 
 * information tuples &lt;<i>well</i>, <i>loc</i>, <i>kb</i>, <i>md</i>&gt; 
 * consisting of
 * <ul>
 * <li>the well's identifying name (<i>well</i>),</li>
 * <li>the well location <i>loc</i> (at ground-level),</li>
 * <li>the Kelly bushing elevation <i>kb</i> (optional parameter), and</li>
 * <li>the well's maximum depth <i>md</i></li>
 * </ul>
 * and marker information tuples &lt;<i>well</i>, <i>pos</i>, <i>depth</i>, 
 * <i>marker</i>&gt; consisting of
 * <ul>
 * <li>the well's identifying name (<i>well</i>),</li>
 * <li>the absolute marker position <i>poc</i>,</li>
 * <li>marker depth <i>depth</i> referring to the ground level + Kelly 
 *     bushing elevation, if set, see {@link Well#setKellyBushingHeight(double)}, and</li>
 * <li>the marker's identifying name (<i>marker</i>).
 * </ul>
 * are managed.
 * 
 * @author Benno Schmidt
 */
public class WellRepository 
{
	private List<Well> wells = new ArrayList<Well>();

	/**
	 * adds a well information to the repository.
	 * 
	 * @param wellName Well name (unique identifier)
	 * @param loc Well location (at ground level)
	 * @param kb Kelly bushing elevation
	 * @param maxDepth Maximum depth (German: Endteufe)
	 */
	public void addWell(String wellName, VgPoint loc, Double kb, Double maxDepth) 
	{
		// Check, if well feature is already in repository:
		Well w = this.getWellEntry(wellName);
		// If not, then create a new well object:
		if (w == null) {
			w = new Well(wellName);
			wells.add(w);
		}
		
		if (loc != null)
			w.setLocation(loc);
		if (kb != null) 
			w.setKellyBushingHeight(kb);
		if (maxDepth != null) 
			w.setMaxDepth(maxDepth);
	}

	/**
	 * adds a marker information to the repository. Note that depth values 
	 * indicate locations below ground (&quot;-z direction&quot;). The marker 
	 * depth is given with respect to the well's ground level 
	 * <tt>w.groundLevel()</tt> + KB, where KB gives the Kelly bushing height, 
	 * see {@link setKellyBushingHeight}.
	 * 
	 * @param wellName Well name (unique identifier)
	 * @param pos Marker position (with absolute z-value)
	 * @param depth Marker depth
	 * @param markerName Marker name (unique identifier)
	 */
	public void addMarker(String wellName, VgPoint pos, Double depth, String markerName) 
	{
		// Check, if well feature is already in repository:
		Well w = this.getWellEntry(wellName);
		// If not, then create a new well object:
		if (w == null) {
			w = new Well(wellName);
			w.setLocation(pos);
			wells.add(w);
		}

		double dx = 0., dy = 0.;
		VgPoint loc = w.getLocation();
		if (loc != null && pos != null) {
			if (loc.getSRS() != pos.getSRS()) {
				throw new T3dSRSException(
					"Well and marker coordinates refer to different reference systems!");
			}
			dx = pos.getX() - loc.getX();
			dy = pos.getY() - loc.getY();
			// TODO depth is redundant here -> consistency check against pos.getZ()!! 
		}
		
		// Add marker information:
		w.addMarker(depth, markerName, dx, dy);	
	}

	/**
	 * gets the current {@link Well} entry from the repository.
	 * 
	 * @param wellName Well name
	 * @return Well object
	 */
	private Well getWellEntry(String wellName) {
		if (wells.size() <= 0 || wellName == null) 
			return null;
		int i = 0;
		while (i < wells.size()) {
			if (wellName.equals(wells.get(i).getName()))
				return wells.get(i);
			i++;
		}
		return null;
	}

	public List<Well> getWells() {
		return this.wells;
	}

	public int numberWells() {
		return this.wells.size();
	}
	
	public int numberMarkers() {
		int ct = 0;
		for (Well w : this.wells) {
			MarkerList ml = w.getMarkers();
			if (ml != null) {
				ct += ml.arr.size();
			}
		}
		return ct;
	}
}
