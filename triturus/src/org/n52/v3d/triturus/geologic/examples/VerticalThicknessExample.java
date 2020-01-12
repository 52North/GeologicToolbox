/**
 * Copyright (C) 2020 52North Initiative for Geospatial Open Source
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
package org.n52.v3d.triturus.geologic.examples;

import java.util.List;

import org.n52.v3d.triturus.core.IoFormatType;
import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.importers.GocadDataInfo;
import org.n52.v3d.triturus.geologic.importers.IoGocadTSurfReader;
import org.n52.v3d.triturus.gisimplm.FltTIN2ElevationGrid;
import org.n52.v3d.triturus.gisimplm.GmEnvelope;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimple2dGridGeometry;
import org.n52.v3d.triturus.gisimplm.GmSimpleElevationGrid;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.GmSimpleTetrMesh;
import org.n52.v3d.triturus.gisimplm.IoTetrMeshWriter;
import org.n52.v3d.triturus.vgis.VgEnvelope;
import org.n52.v3d.triturus.vgis.VgEquidistGrid;
import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * Geologic Toolbox example application: Reads two GOCAD TSurf TINs and 
 * computes the difference body in between based on a simple 2-D rasterization
 * algorithm. The result is a tetrahedronal mesh with vertical thickness data
 * as cell attributes.
 * 
 * @author Benno Schmidt
 */
public class VerticalThicknessExample
{
	private final String
		inFilename1 = "/projects/GeologicToolbox/data/s_geologie_Rotliegend_ts",
		inFilename2 = "/projects/GeologicToolbox/data/s_geologie_Zechstein_ts",
		outFilename = "/projects/GeologicToolbox/data/s_geologie_body.vtk"; 
	private final double cellSize = 500.;
	

	public static void main(String args[]) {
		new VerticalThicknessExample().run();
	}
	
	public void run() 
	{ 
		try {
			// Give basic information about the surfaces:
			this.provideFileInformation();

			// Read TSurf models from GOCAD data files...
			IoGocadTSurfReader reader = new IoGocadTSurfReader();
			GmSimpleTINFeature 
				surf1 = reader.read(inFilename1).get(0),
				surf2 = reader.read(inFilename2).get(0);

			// Provide some test output:
			System.out.println(surf1.envelope());
			System.out.println(surf2.envelope());
			
			// Set up the target grid's geometry:
			VgEnvelope bbox = GmEnvelope.intersect(
				surf1.envelope(), 
				surf2.envelope());
			System.out.println("Target BBOX: " + bbox);
	        VgEquidistGrid grdGeom = this.setUpGeometry(bbox);
	        System.out.println(grdGeom);

	        // Rasterize input surfaces:
	        FltTIN2ElevationGrid trans = new FltTIN2ElevationGrid();
	        trans.setGridGeometry(grdGeom);
	        trans.setZConflictHandler(FltTIN2ElevationGrid.CONFLICT_TAKE_MAX_Z);
	        GmSimpleElevationGrid 
	        	grid1 = (GmSimpleElevationGrid) trans.transform(surf1),
	        	grid2 = (GmSimpleElevationGrid) trans.transform(surf2);
	        // Note: With respect to the x-y plane, grd1 and grd2 share the 
	        // same grid geometry.
	        System.out.println("grid1: " + grid1);
	        System.out.println("grid2: " + grid2);
	        
	        // Construct tetrahedronal mesh:
	        GmSimpleTetrMesh mesh = 
	        	this.constructTetrahedronalMesh(bbox, grdGeom, grid1, grid2);
	        
	        // Write tetrahedronal mesh to VTK/ParaView file:
            System.out.println("Writing result file \"" + outFilename + "\"...");
            IoTetrMeshWriter writer = new IoTetrMeshWriter(IoFormatType.VTK_DATASET);
            writer.generateVerticalThicknessAttr();
            writer.writeToFile(mesh, outFilename);
            System.out.println("Success!");
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
	}

	private GmSimpleTetrMesh constructTetrahedronalMesh(
		VgEnvelope bbox,
		VgEquidistGrid grdGeom, 
		GmSimpleElevationGrid grid1,
		GmSimpleElevationGrid grid2) 
	{
		int 
			M = grdGeom.numberOfColumns(), 
			N = grdGeom.numberOfRows();
		GmSimpleTetrMesh mesh = new GmSimpleTetrMesh();
		boolean s10, s11, s12, s13, s20, s21, s22, s23;
		int i10, i11, i12, i13, i20, i21, i22, i23;
		VgPoint dummy = bbox.getCenterPoint();
		int ct = 0;
		for (int jj = 0; jj < M; jj++) {
			for (int ii = 0; ii < N; ii++) {
				VgPoint p1 = new GmPoint();
				if (grid1.isSet(ii, jj))
					p1.set(grid1.getPoint(ii, jj));
				else
					p1.set(dummy);
				mesh.addPoint(p1); // this point's index will be 2 * (ii + N * jj)
				if (ct++ != 2 * (ii + N * jj)) throw new T3dException("Corrupt vertex index");
				VgPoint p2 = new GmPoint();
				if (grid2.isSet(ii, jj))
					p2.set(grid2.getPoint(ii, jj));
				else
					p2.set(dummy);
				mesh.addPoint(p2); // this point's index will be 2 * (ii + N * jj) + 1
				if (ct++ != 2 * (ii + N * jj) + 1) throw new T3dException("Corrupt vertex index");
			}
		}
		//System.out.println("ct = " + ct);
		for (int jj = 0; jj < M - 1; jj++) {
			for (int ii = 0; ii < N - 1; ii++) {
				s10 = grid1.isSet(ii,     jj);
				s11 = grid1.isSet(ii,     jj + 1);
				s12 = grid1.isSet(ii + 1, jj + 1);
				s13 = grid1.isSet(ii + 1, jj);
				s20 = grid2.isSet(ii,     jj);
				s21 = grid2.isSet(ii,     jj + 1);
				s22 = grid2.isSet(ii + 1, jj + 1);
				s23 = grid2.isSet(ii + 1, jj);
				i10 = 2 * (ii       + N *  jj     );
				i11 = 2 * (ii       + N * (jj + 1));
				i12 = 2 * ((ii + 1) + N * (jj + 1));
				i13 = 2 * ((ii + 1) + N *  jj     );
				i20 = 2 * (ii       + N *  jj     ) + 1;
				i21 = 2 * (ii       + N * (jj + 1)) + 1;
				i22 = 2 * ((ii + 1) + N * (jj + 1)) + 1;
				i23 = 2 * ((ii + 1) + N *  jj     ) + 1;
				if (s10 && s11 && s12 && s13 && s20 && s21 && s22 & s23) {
					mesh.addTetrahedron(i10, i11, i13, i20);
					mesh.addTetrahedron(i12, i13, i11, i22);
					mesh.addTetrahedron(i21, i22, i20, i11);
					mesh.addTetrahedron(i23, i20, i22, i13);
					mesh.addTetrahedron(i11, i20, i22, i13);
				}	
			}
		}
		System.out.println(mesh);
		return mesh;
	}

	private void provideFileInformation() {
		List<GocadDataInfo> 
			info1 = null, 
			info2 = null;
		try {
			info1 = new IoGocadTSurfReader().getInfo(inFilename1);	        
			info2 = new IoGocadTSurfReader().getInfo(inFilename2);	        
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
		if (info1 != null) {
			for (GocadDataInfo i : info1) {
				System.out.println(i);
			}
		}
		if (info2 != null) {
			for (GocadDataInfo i : info2) {
				System.out.println(i);
			}
		}
	}
	
	private VgEquidistGrid setUpGeometry(VgEnvelope bbox) 
    {
        System.out.println(bbox);

		VgPoint origin = new GmPoint(bbox.getXMin(), bbox.getYMin(), 0.0);
		// TODO: origin ist noch ein schraeger Wert -> ist zu runden gemaess cellSize!
		
		int nrows = (int)(Math.floor(bbox.getExtentY() / cellSize)) + 1;
		int ncols = (int)(Math.floor(bbox.getExtentX() / cellSize)) + 1;
		        
		GmSimple2dGridGeometry res = new GmSimple2dGridGeometry(
			ncols, nrows, origin, cellSize, cellSize);
        return res;
	}
}
