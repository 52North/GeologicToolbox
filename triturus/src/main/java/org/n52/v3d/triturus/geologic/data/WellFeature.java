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

import org.n52.v3d.triturus.gisimplm.GmAttrFeature;
import org.n52.v3d.triturus.vgis.VgPoint;

public class WellFeature extends GmAttrFeature
{
	public WellFeature(String wellName) {
		this.addAttribute("NAME", "java.lang.String");
		this.setAttributeValue("NAME", wellName);
		//markers.arr.clear();
	}

	public void setPosition(VgPoint pos) {
		this.setGeometry(pos);
	}

    public void setBlubb() {	// TODO
/*		if (this.hasAttribute("position")) {
			this.setAttributeValue("position", pos);
		} else {
			this.addAttribute("position", "org.n52.v3d.triturus.vgis.VgPoint");
			this.setAttributeValue("position", pos);
		}*/
    }
    
	
	/*
	if (this.hasAttribute("position")) {
		this.setAttributeValue("position", pos);
	} else {
		this.addAttribute("position", "org.n52.v3d.triturus.vgis.VgPoint");
		this.setAttributeValue("position", pos);
	}*/

}
