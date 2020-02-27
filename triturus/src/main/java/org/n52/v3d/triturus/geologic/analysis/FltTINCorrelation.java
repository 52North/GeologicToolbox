/**
 * Copyright (C) 2018 52North Initiative for Geospatial Open Source
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
 * Contact: Benno Schmidt and Martin May, 52North Initiative for Geospatial
 * Open Source Software GmbH, Martin-Luther-King-Weg 24, 48155 Muenster,
 * Germany, info@52north.org
 */
package org.n52.v3d.triturus.geologic.analysis;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.core.T3dProcFilter;
import org.n52.v3d.triturus.gisimplm.GmSimpleElevationGrid;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;

/**
 * Computation of the correlation coefficient of two surfaces modeled as TINs
 * (&quot;triangulated irregular networks&quot;). 
 * <br/>
 * The computation will be done with respect to a moving windows of 3x3, 5x5, 
 * ... up to (2<i>n</i> + 1) x (2<i>n</i> + 1) grid cells, where n shall not 
 * exceed the value {@link this#nMax}. For <i>n</i> &lt; 1 or <i>n</i> &gt; 
 * {@link this#nMax}, the {@link this#transform} method will throw an 
 * exception.
 * <br/>
 * TODO: This method has not been implemented yet!
 * 
 * @author Benno Schmidt
 */
public class FltTINCorrelation extends T3dProcFilter
{
    private String logString = "";

    /**
	 * maximum size of the moving computation window is (2<i>nMax</i> + 1) x 
	 * (2<i>nMax</i> + 1).
	 */
	public short nMax = 6;
	private short n = 3; // computation window size will be (2n + 1) x (2n + 1)
	
	private GmSimpleTINFeature tin1, tin2; // Operation inputs

	
    public FltTINCorrelation() {
        logString = this.getClass().getName();
    }
                                                                                     
    public String log() {
        return logString;
    }
   
	public void setTin1(GmSimpleTINFeature tin1) {
		this.tin1 = tin1;
	}

	public void setTin2(GmSimpleTINFeature tin2) {
		this.tin2 = tin2;
	}

	/**
     * gets the maximum size <i>nMax</i> of the moving computation window, i.e.
     * windows of size 3x3, 5x5, ..., (2<i>nMax</i> + 1) x (2<i>nMax</i> + 1) 
     * will be available to compute the grid holding the correlation 
     * coefficient values.
     *  
     * @short nMax Maximum window size parameter
     */
    public short getMaxWindowSize() {
    	return nMax;
	}

    /**
     * sets the maximum size of the moving computation window to 
     * (2<i>n</i> + 1) x (2<i>n</i> + 1). Note that <i>n</i> <= <i>nMax</i> 
     * must hold.
     *  
     * @param n Windows size parameter
     */
    public void setWindowSize(short n) {
    	checkWindowSize(n);
    	this.n = n;
	}

	private void checkWindowSize(short n) {
		if (n > nMax)
    		throw new T3dException(
    			"Invalid computation window size " + 
    			"(must be < " + (nMax + 1) + ")!");
	}

    /**
     * returns the available window size names.
     * 
     * @return Array holding the list entry titles "3x3", "5x5", ...
     */
    public String[] UIDialog_MovingWindowSizeOptions() {
    	String[] res = new String[nMax];
    	for (int i = 1; i <= nMax; i++) {
    		res[i - 1] = "" + (2*i + 1) + "x" + (2*i + 1);
    	}
    	return res;
    }
   
    // TODO Window stuff is redundant -> refactor!
    
    // TODO Rasterizer-Parameter setzen! Hier: CellSize -> Rückgabe: Resultierende #Gitterzellen

    /**
     * returns options to choose a reasonable rasterization cell-size.
     * 
     * @return Array holding list entry titles 
     */
    public String[] UIDialog_CellSizeOptions() {
    	Double dx = this.reasonableCellSize();
    	String[] res = new String[3];
    	res[0] = "" + Math.round(dx * 2.0) + " (fast computation)";
    	res[1] = "" + Math.round(dx);
    	res[2] = "" + Math.round(dx / 2.0) + " (intensive computation)";
    	return res;
    }

	private Double reasonableCellSize() {
		// TODO vernünftige Logik implementieren!
		return 500.0;
	}

    /**
     * returns available output format options.
     * 
     * @return Array holding list entry titles 
     */
    public String[] UIDialog_OutputFormatOptions() {
    	String[] res = new String[3];
    	res[0] = "ESRI Shapefile";
    	res[1] = "Arc/Info ASCII Grid";
    	res[2] = "VTK Dataset File";
    	return res;
    }

    /**
     * returns available z-conflict handling options.
     * 
     * @return Array holding list entry titles 
     */
    public String[] UIDialog_ZConflictOptions() {
    	String[] res = new String[2];
    	res[0] = "maximal depth";
    	res[1] = "minimal depth";
    	// TODO Maybe add more options in the future...
    	return res;
    }

	/** 
     * calculates the correlation coefficient of two elevation-grids.
     *
     * @param tin1 First input TIN
     * @param tin2 Second input TIN
     * @return Result grid
     * @throws T3dException
     */
    public GmSimpleElevationGrid transform(
    	GmSimpleTINFeature tin1, GmSimpleTINFeature tin2) 
    	throws T3dException
    {
    	if (tin1 != null) this.tin1 = tin1;
    	if (tin2 != null) this.tin2 = tin2;
    	
        this.checkWindowSize(n);
        
        throw new T3dNotYetImplException();
        
        //return null;
    }    
}
