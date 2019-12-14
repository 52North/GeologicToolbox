/**
 * Copyright (C) 2018 52 North Initiative for Geospatial Open Source
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
 * Contact: Benno Schmidt and Martin May, 52 North Initiative for Geospatial 
 * Open Source Software GmbH, Martin-Luther-King-Weg 24, 48155 Muenster, 
 * Germany, info@52north.org
 */
package org.n52.v3d.triturus.geologic.importers;

import org.n52.v3d.triturus.t3dutil.T3dColor;

/**
 * Object holding additional information and meta-data about GOCAD objects.
 * 
 * @author Benno Schmidt
 */
public class GocadDataInfo 
{
	private String 
		objectType = "(undefined)",
		objectName = "(undefined)";
	private long 
		ctVrtx, ctPvrtx, ctTrgl, ctAtom, ctBorder, ctBstone;
	private T3dColor 
		colSolid, colBorder;
	private String
		geologicalType, geologicalFeature, stratigraphicPosition;
	
	/**
	 * gets the object type name.
	 * 
	 * @param return Type name, e.g. "TSurf"
	 */
	public String getObjectType() {
		return objectType;
	}
	
	public void setObjectType(String type) {
		objectType = type;
	}

	/**
	 * gets the object's name.
	 * 
	 * @param return Object name
	 */
	public String getObjectName() {
		return objectName;
	}
	
	public void setObjectName(String name) {
		objectName = name;
	}

	public void incrVrtxCount() { ctVrtx++; }
	public void incrPvrtxCount() { ctPvrtx++; }
	public void incrTrglCount() { ctTrgl++; }
	public void incrAtomCount() { ctAtom++; }
	public void incrBorderCount() { ctBorder++; }
	public void incrBstoneCount() { ctBstone++; }

	public long getNumberOfVertices() { return ctVrtx + ctPvrtx + ctAtom; }
	public long getNumberOfTriangles() { return ctTrgl; }	
	
	public void setGeologicalType(String name) { geologicalType = name; }
	public void setGeologicalFeature(String name) { geologicalFeature = name; }
	public void setStratigraphicPosition(String name) { stratigraphicPosition = name; }

	public void setSolidColor(T3dColor col) {
		colSolid = col;
		if (colBorder == null) colBorder = new T3dColor(.9f, .9f, .9f); // light grey
	}

	public void setBorderColor(T3dColor col) {
		colBorder = col;
		if (colSolid == null) colSolid = new T3dColor(.1f, .1f, .1f); // dark grey
	}

	public String toString() {
		StringBuffer s = new StringBuffer("[");
		{	s.append("(type:\"" + objectType + "\"");
			s.append(", name:\"" + objectName + "\")"); 
		}
		if ("TSurf".equalsIgnoreCase(objectType)) {
			s.append("(#trgl=" + ctTrgl); 
			if (ctVrtx > 0) s.append(", #vrtx=" + ctVrtx); 
			if (ctPvrtx > 0) s.append(", #pvrtx=" + ctPvrtx); 
			if (ctAtom > 0) s.append(", #atom=" + ctAtom); 
			s.append(")");
		}
		if (ctBorder + ctBstone > 0) {
			s.append("(#border=" + ctBorder + ", #bstone=" + ctBstone + ")");
		}
		
		if (geologicalType != null)
			s.append(", geologicalType:\"" + geologicalType + "\"");
		if (geologicalFeature != null)
			s.append(", geologicalFeature:\"" + geologicalFeature + "\"");
		if (stratigraphicPosition != null)
			s.append(", stratigraphicPosition:\"" + stratigraphicPosition + "\"");
		
		if (colSolid != null || colBorder != null) {
			s.append(", (fill:" + colSolid + ", stroke: " + colBorder + ")");
		}
		s.append("]");
		return s.toString();
	}
}
