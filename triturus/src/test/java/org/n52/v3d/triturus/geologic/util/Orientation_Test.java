package org.n52.v3d.triturus.geologic.util;

import static org.junit.Assert.*;

import org.junit.*;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.vgis.VgPoint;

public class Orientation_Test 
{
	@Test
	public void test() 
	{
		VgPoint 
			p1 = new GmPoint(0., 2., 0.),
			p2 = new GmPoint(2., 0., 0.),
			p3 = new GmPoint(0., 0., 0.817);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 

		assertTrue(orient.dipInt() == 60);
		assertTrue(orient.azimuthInt() == 45);
		assertTrue(orient.compassDirection().equalsIgnoreCase("NE"));
	}
}
