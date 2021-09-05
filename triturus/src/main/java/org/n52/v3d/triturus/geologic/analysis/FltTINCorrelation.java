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
import org.n52.v3d.triturus.core.T3dProcFilter;
import org.n52.v3d.triturus.gisimplm.FltTIN2ElevationGrid;
import org.n52.v3d.triturus.gisimplm.GmEnvelope;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimple2dGridGeometry;
import org.n52.v3d.triturus.gisimplm.GmSimpleElevationGrid;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.vgis.VgElevationGrid;
import org.n52.v3d.triturus.vgis.VgEnvelope;
import org.n52.v3d.triturus.vgis.VgEquidistGrid;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Computation of the correlation coefficient of two surfaces modeled as TINs
 * (&quot;triangulated irregular networks&quot;). 
 * <br>
 * The computation will be done with respect to a moving windows of 3x3, 5x5, 
 * ... up to (2<i>n</i> + 1) x (2<i>n</i> + 1) grid cells, where n shall not 
 * exceed the value {@link this#nMax}. For <i>n</i> &lt; 1 or <i>n</i> &gt; 
 * {@link this#nMax}, the {@link this#transform} method will throw an 
 * exception.
 * 
 * @author Maximilian Haverkamp, Benno Schmidt
 */
public class FltTINCorrelation extends T3dProcFilter
{
    private String logString = "";

    /**
     * @deprecated
	 * maximum size of the moving computation window is (2<i>nMax</i> + 1) x 
	 * (2<i>nMax</i> + 1).
	 */
	public short nMax = 6; 
	private short n = 3; // computation window size will be (2n + 1) x (2n + 1)
	private int c = 500;
	private GmSimpleTINFeature tin1, tin2; // Operation inputs
	
	private String z = "max";
	private String u = "ARC_INFO_ASCII_GRID";
	private String s = "Quadrat";
	
	
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
     * @return Maximum window size parameter
     */
    public short getMaxWindowSize() {
    	return nMax;
	}

    /**
     * sets the maximum size of the moving computation window to 
     * (2<i>n</i> + 1) x (2<i>n</i> + 1). Note that <i>n</i> &lt;= <i>nMax</i> 
     * must hold.
     *  
     * @param n Windows size parameter
     */
    public void setWindowSize(short n) {
    	// checkWindowSize(n);
    	this.n = n;
	}

    public short getWindowSize() {
		return n;
	}
    
    /**
     * @deprecated Method not needed anymore, any odd number works
     * @param n
     */
    /*
	private void checkWindowSize(short n) {
		if (n > nMax)
    		throw new T3dException(
    			"Invalid computation window size " + 
    			"(must be < " + (nMax + 1) + ")!");
	}
	*/

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
    
    // TODO Rasterizer-Parameter setzen! Hier: CellSize -> Rueckgabe: Resultierende #Gitterzellen

    /**
     * returns options to choose a reasonable rasterization cell-size.
     * 
     * @return Array holding list entry titles 
     */
    public String[] UIDialog_CellSizeOptions() {
    	Double dx = this.reasonableCellSize();
    	// original Benno:
    	/*
      	String[] res = new String[3];
    	res[0] = "" + Math.round(dx * 2.0) + " (fast computation)";
    	res[1] = "" + Math.round(dx);
    	res[2] = "" + Math.round(dx / 2.0) + " (intensive computation)";
    	*/
    	// new Maximilian:
    	String[] res = new String[6];
    	res[0] = "" + Math.round(dx * 2.0);
    	res[1] = "" + Math.round(750);
    	res[2] = "" + Math.round(dx / 2.0);
    	res[3] = "" + Math.round(250);
    	res[4] = "" + Math.round(100);
    	res[5] = "" + Math.round(50);

    	return res;
    }

	private Double reasonableCellSize() {
		// TODO vernuenftige Logik implementieren!
		return 500.0;
	}

	public void setCellSize(int c) {
		this.c = c;
	}

	public int getCellSize() {
		// TODO Auto-generated method stub
		return c;
	}
	
	public String getWindowForm(){
		return s;
	}
	
	public void setWindowForm(String s){
		this.s = s;
	}
		
    /**
     * returns available output format options.
     * 
     * @return Array holding list entry titles 
     */
    public String[] UIDialog_OutputFormatOptions() {
    	String[] res = new String[2];
    	res[0] = "VTK Dataset File";
    	res[1] = "Arc/Info ASCII Grid";
    	//res[2] = "ESRI Shapefile";
    	return res;
    }

    public String[] UIDialog_WindowformOptions() {
    	String[] res = new String[2];
    	res[0] = "Quadrat";
    	res[1] = "Kreis";
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

    public void setZConflictHandler(String z) {
		this.z = z;
    }
    
	public String getZConflictHandler() {
		return z;
	}
	
	public void setOutputFormat(String outputFormat) {
		this.u = outputFormat;

	}
	public String getOutputFormat() {
		//u= "IoFormatType."+u;
		//System.out.println(u);
		return u;
	}

	/** 
     * computes the correlation coefficient of two elevation-grids.
     *
     * @param tin1 First input TIN
     * @param tin2 Second input TIN
	 * @param windowSize Size of the moving window (as number of raster cells)
     * @param cellSize TIN rasterization resolution
     * @param zConflictHandler Directive how to handle z-value conflicts in 3-D rasterization process
     * @param windowForm Window form parameter (e.g. quadratic or circle-shaped)
     * @return Result grid
     * @throws T3dException if an error occurs
     */
    public VgElevationGrid  transform(
    	GmSimpleTINFeature tin1, GmSimpleTINFeature tin2,
    	short windowSize, int cellSize, String zConflictHandler, String windowForm) 
    	throws T3dException
    {
    	if (tin1 != null) this.tin1 = tin1;
    	if (tin2 != null) this.tin2 = tin2;
    	
        // this.checkWindowSize(n);
        
		try {
			// creating the Bounding Box for the area
			VgEnvelope bbox = GmEnvelope.intersect(tin1.envelope(), tin2.envelope());


			VgEquidistGrid grdGeom = this.setUpGeometry(bbox, cellSize);

			// creating grids
			FltTIN2ElevationGrid gridOne = new FltTIN2ElevationGrid();
			FltTIN2ElevationGrid gridTwo = new FltTIN2ElevationGrid();
			FltTIN2ElevationGrid correlation = new FltTIN2ElevationGrid();
			//setting the conflict handler for occurance of multiple Z Coordinates
			if(zConflictHandler=="max") {
				gridOne.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_MAX_Z);
				gridTwo.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_MAX_Z);
			} else if(zConflictHandler=="min") {
				gridOne.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_MIN_Z);
				gridTwo.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_MIN_Z);
			} else if(zConflictHandler=="avg") {
				gridOne.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_AVG_Z);
				gridTwo.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_AVG_Z);
			}



			gridOne.setGridGeometry(grdGeom);
			gridTwo.setGridGeometry(grdGeom);
			correlation.setGridGeometry(grdGeom);

			VgElevationGrid grd = gridOne.transform(tin1);
			VgElevationGrid grd2 = gridTwo.transform(tin2);
			VgElevationGrid grdCorr = correlation.transform(tin1);


			double[][] grdOneDeviation = new double[grd.numberOfRows()][grd.numberOfColumns()];
			double[][] grdTwoDeviation = new double[grd2.numberOfRows()][grd2.numberOfColumns()];
			double[][] covariance = new double[grd2.numberOfRows()][grd2.numberOfColumns()];

			switch (windowForm){

			case ("Quadrat"):


			//first entry is set with top left window corner on top left grid corner

			for (int i = 0 + ((windowSize - 1) / 2); i < grd.numberOfRows() - ((windowSize + 1) / 2); i++) {

				for (int j = 0 + ((windowSize - 1) / 2); j < grd.numberOfColumns() - ((windowSize + 1) / 2); j++) {
					double meanGridOne = 0;
					double sumX = 0;
					double meanGridOne2 = 0;
					double sumY = 0;
					double sumXY = 0;
					int counter = 0; // auxiliary variable

					{
						// checking if every field in the current window position is set, otherwise the correlation
						// would be irregular
						for (int k = -((windowSize - 1) / 2); k < (windowSize + 1) / 2; k++) {
							for (int l = -((windowSize - 1) / 2); l < (windowSize + 1) / 2; l++) {


								if ((boolean) ((GmSimpleElevationGrid) grd2).isSet(i + k, j + l) != false
										&& (boolean) ((GmSimpleElevationGrid) grd).isSet(i + k, j + l) != false) {

									counter++;
								}



							}
						}
						// calculation means of the current window
						if (counter == (windowSize * windowSize)) { // -1
							for (int k = -((windowSize - 1) / 2); k < (windowSize + 1) / 2; k++) {
								for (int l = -((windowSize - 1) / 2); l < (windowSize + 1) / 2; l++) {

									meanGridOne = meanGridOne + (((GmSimpleElevationGrid) grd).getValue(i + k, j + l));
									meanGridOne2 = meanGridOne2
											+ (((GmSimpleElevationGrid) grd2).getValue(i + k, j + l));



								}
							}

							meanGridOne = meanGridOne / (windowSize * windowSize);
							meanGridOne2 = meanGridOne2 / (windowSize * windowSize);
							//calculating sums in each window
							for (int k = -((windowSize - 1) / 2); k < (windowSize + 1) / 2; k++) {
								for (int l = -((windowSize - 1) / 2); l < (windowSize + 1) / 2; l++) {


									sumX = sumX + Math
											.pow(((GmSimpleElevationGrid) grd).getValue(i + k, j + l) - meanGridOne, 2);
									sumY = sumY + Math.pow(
											((GmSimpleElevationGrid) grd2).getValue(i + k, j + l) - meanGridOne2, 2);
									sumXY = sumXY + (((GmSimpleElevationGrid) grd).getValue(i + k, j + l)
											- meanGridOne)
											* (((GmSimpleElevationGrid) grd2).getValue(i + k, j + l) - meanGridOne2);

								}
							}
						}
					}
					grdOneDeviation[i][j] = sumX;

					grdTwoDeviation[i][j] = sumY;
					covariance[i][j] = sumXY;

				}
			}break;
			case("Kreis"):
				int circleCheck=0;
			for (int z=0; z<=(windowSize-1)/2;z++){
				circleCheck=circleCheck+z;
			}
			circleCheck=windowSize*windowSize-4*circleCheck; //Dreieckszahlen
			System.out.println("CIRCLECHECK: "+circleCheck);
			//first entry is set with top left window corner on top left grid corner

			for (int i = 0 + ((windowSize - 1) / 2); i < grd.numberOfRows() - ((windowSize + 1) / 2); i++) {

				for (int j = 0 + ((windowSize - 1) / 2); j < grd.numberOfColumns() - ((windowSize + 1) / 2); j++) {
					double meanGridOne = 0;
					double sumX = 0;
					double meanGridTwo = 0;
					double sumY = 0;
					double sumXY = 0;
					int counter = 0; // auxiliary variable

					int x=((windowSize - 1) / 2)+1;
					int y=((windowSize - 1) / 2)+1;
						// checking if every field in the current window position is set, otherwise the correlation
						// would be irregular
					for (int k = -(windowSize-1)/2; k<(windowSize+1)/2; k++) {
							if(y>0) {
								x--;
							}else{
								x++;
							}
							y--;

							for (int l = -((windowSize - 1) / 2)+x; l <= ((windowSize - 1) / 2)-x;l++) {


								if ((boolean) ((GmSimpleElevationGrid) grd2).isSet(i + k, j + l) != false
										&& (boolean) ((GmSimpleElevationGrid) grd).isSet(i + k, j + l) != false) {

									counter++;

								}



							}
						}
						// calculation means of the current window


						if (counter == circleCheck) {

							x=((windowSize - 1) / 2)+1;
							y=((windowSize - 1) / 2)+1;

							for (int k = -(windowSize-1)/2; k<(windowSize+1)/2; k++) {
								if(y>0) {
									x--;
								}else{
									x++;
								}
								y--;
								for (int l = -((windowSize - 1) / 2)+x; l <= ((windowSize - 1) / 2)-x;l++) {
									meanGridOne = meanGridOne + (((GmSimpleElevationGrid) grd).getValue(i + k, j + l));
									meanGridTwo = meanGridTwo
											+ (((GmSimpleElevationGrid) grd2).getValue(i + k, j + l));




								}


							}



							meanGridOne = meanGridOne / (circleCheck);
							meanGridTwo = meanGridTwo / (circleCheck);
							//calculating sums in each window
							x=((windowSize - 1) / 2)+1;
							y=((windowSize - 1) / 2)+1;
							for (int k = -(windowSize-1)/2; k<(windowSize+1)/2; k++) {
								if(y>0) {
									x--;
								}else{
									x++;
								}
								y--;
								for (int l = -((windowSize - 1) / 2)+x; l <= ((windowSize - 1) / 2)-x; l++) {



									sumX = sumX + Math
											.pow(((GmSimpleElevationGrid) grd).getValue(i + k, j + l) - meanGridOne, 2);
									sumY = sumY + Math.pow(
											((GmSimpleElevationGrid) grd2).getValue(i + k, j + l) - meanGridTwo, 2);
									sumXY = sumXY + (((GmSimpleElevationGrid) grd).getValue(i + k, j + l)
											- meanGridOne)
											* (((GmSimpleElevationGrid) grd2).getValue(i + k, j + l) - meanGridTwo);


								}
							}

					    }

					grdOneDeviation[i][j] = sumX;

					grdTwoDeviation[i][j] = sumY;
					covariance[i][j] = sumXY;

				}
			}


			break;
			}

			double[][] r = new double[grd2.numberOfRows()][grd2.numberOfColumns()];

			// calculating correlation and setting value
			for (int i = 0; i < r.length; i++) {
				for (int j = 0; j < r[0].length; j++) {
					r[i][j] = covariance[i][j] / (Math.sqrt(grdOneDeviation[i][j]) * Math.sqrt(grdTwoDeviation[i][j]));

					if (r[i][j] > 1 || r[i][j] < -1) { // Check if the calculations are correct
						//System.out.println(r[i][j]);
						//throw new Error("Error in the calculations");
						r[i][j]=Math.round(r[i][j]);
					}

				}
			}


			// unset every not used field for better visualization
			for (int i = 0; i < (grdCorr.numberOfRows()); i++) {
				for (int j = 0; j < (grdCorr.numberOfColumns()); j++) {

					((GmSimpleElevationGrid) grdCorr).unset(i, j);

				}
			}


			// Input of correlations
			for (int i = 0 + ((windowSize - 1) / 2); i < grdCorr.numberOfRows() - ((windowSize + 1) / 2); i++) {
				for (int j = 0 + ((windowSize - 1) / 2); j < grdCorr.numberOfColumns() - ((windowSize + 1) / 2); j++) {
					if (!new Double(r[i][j]).isNaN()) { // When Sum of X or Y is zero, the correlation is NaN
						grdCorr.setValue(i, j, r[i][j]);


					}

				}

			}



			//returning correlation grid
			return grdCorr;


		} catch (T3dException e) {
			e.printStackTrace();
		}

        return null;
    }
    
	private VgEquidistGrid setUpGeometry(VgEnvelope bbox, double cellSize) 
	{
		System.out.println(bbox);
		
		VgPoint origin = new GmPoint(bbox.getXMin(), bbox.getYMin(), 0.0);
		// TODO: origin ist noch ein schraeger Wert -> ist zu runden gemaess cellSize!
		
		int nrows = (int)(Math.floor(bbox.getExtentY() / cellSize)) + 1;
		int ncols = (int)(Math.floor(bbox.getExtentX() / cellSize)) + 1;
		        
		GmSimple2dGridGeometry res = new GmSimple2dGridGeometry(
			ncols, nrows, origin, cellSize, cellSize);
		System.out.println(res);
		return res;
	}
}
