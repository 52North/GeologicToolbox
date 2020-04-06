package org.n52.v3d.triturus.geologic.util;

import static org.junit.Assert.*;

import org.junit.*;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmTriangle;
import org.n52.v3d.triturus.vgis.VgPoint;

public class Orientation_Test 
{
	@Test
	public void test0_0() 
	{
		VgPoint 
			p1 = new GmPoint(0, 2, 0),
			p2 = new GmPoint(2, 0, 0),
			p3 = new GmPoint(0, 0, Math.sqrt(8.) / 2.);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 0_0:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 135);
		assertTrue(orient.dipInt() == 45);
		assertTrue(orient.azimuthInt() == 45);
		assertTrue(orient.compassDirection().equalsIgnoreCase("NE"));
	}

	private void dump(Orientation orient) {
		System.out.println("strike = " + orient.strikeInt());
		System.out.println("dip = " + orient.dipInt());
		System.out.println("dipDir = " + orient.azimuthInt());
	}
	
	@Test
	public void test0_1() 
	{
		VgPoint p1 = new GmPoint(3552128, 5641740, 222);
		Orientation orient = new Orientation(new GmTriangle(p1, p1, p1)); 
		System.out.println("Test 0_1:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == -1);
		assertTrue(orient.dipInt() == 0);
		assertTrue(orient.azimuthInt() == -1); 
		assertTrue(orient.compassDirection().equalsIgnoreCase("-")); 
	}

	@Test
	public void test1() 
	{
		VgPoint 
			p1 = new GmPoint(1, 1, 0),
			p2 = new GmPoint(-1, 1, 0),
			p3 = new GmPoint(0, 0, 1);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 1:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 90);
		assertTrue(orient.dipInt() == 45);
		assertTrue(orient.azimuthInt() == 0); 
		assertTrue(orient.compassDirection().equalsIgnoreCase("N")); 
	}

	@Test
	public void test2() 
	{
		VgPoint 
			p1 = new GmPoint(0, 0, 0),
			p2 = new GmPoint(0, 0, 1),
			p3 = new GmPoint(0, -1, 1);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 2:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 0); // TODO eigentl. 180 entspr. besser 0!
		assertTrue(orient.dipInt() == 90);
		assertTrue(orient.azimuthInt() == -1); // undefined resp. "NaN"
		assertTrue(orient.compassDirection().equalsIgnoreCase("-")); // undefined
	}

	@Test
	public void test3() 
	{
		VgPoint 
			p1 = new GmPoint(1, 1, 0),
			p2 = new GmPoint(-1, -1, 0),
			p3 = new GmPoint(-1, 1, 0);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 3:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == -1); // undefined/"NaN"
		assertTrue(orient.dipInt() == 0);
		assertTrue(orient.azimuthInt() == -1); // undefined/"NaN"
		assertTrue(orient.compassDirection().equalsIgnoreCase("-")); // undefined
	}

	@Test
	public void test4() 
	{
		VgPoint 
			p1 = new GmPoint(-1, 0, 0),
			p2 = new GmPoint(1, 0, 0),
			p3 = new GmPoint(0, 1, 1);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 4:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 90);
		assertTrue(orient.dipInt() == 45);
		assertTrue(orient.azimuthInt() == 180); 
		assertTrue(orient.compassDirection().equalsIgnoreCase("S")); 
	}

	@Test
	public void test5() 
	{
		VgPoint 
			p1 = new GmPoint(-1, -1, 0),
			p2 = new GmPoint(0, 1, 1),
			p3 = new GmPoint(0, -1, 1);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 5:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 0); // TODO 180 -> 0 diehl
		assertTrue(orient.dipInt() == 45);
		assertTrue(orient.azimuthInt() == 270); 
		assertTrue(orient.compassDirection().equalsIgnoreCase("W")); 
	}

	@Test
	public void test6() 
	{
		VgPoint 
			p1 = new GmPoint(-1, 2, 0),
			p2 = new GmPoint(1, -5, 2),
			p3 = new GmPoint(1, 1, 1);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 6:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 22);
		assertTrue(orient.dipInt() == 24);
		assertTrue(orient.azimuthInt() == 292);
		assertTrue(orient.compassDirection().equalsIgnoreCase("W"));
	}

	@Test
	public void test7() 
	{
		VgPoint 
			p1 = new GmPoint(0, -9, 9),
			p2 = new GmPoint(0, 1, -7),
			p3 = new GmPoint(-3, -4, 6);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 7:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 136);
		assertTrue(orient.dipInt() == 67);
		assertTrue(orient.azimuthInt() == 46);
		assertTrue(orient.compassDirection().equalsIgnoreCase("NE"));
	}

	@Test
	public void test8() 
	{
		VgPoint 
			p1 = new GmPoint(5, 11, 10),
			p2 = new GmPoint(10, 1, 8),
			p3 = new GmPoint(0, 21, 3);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 8:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 153);
		assertTrue(orient.dipInt() == 90);
		assertTrue(orient.azimuthInt() == -1); // undefined/"NaN"
		assertTrue(orient.compassDirection().equalsIgnoreCase("-")); // undefined
	}

	@Test
	public void test9() 
	{
		VgPoint 
			p1 = new GmPoint(3552128, 5641740, 222),
			p2 = new GmPoint(3552333.8, 5642000, 345.8),
			p3 = new GmPoint(3552095, 5642111.11, -271.65);
		Orientation orient = new Orientation(new GmTriangle(p1, p2, p3)); 
		System.out.println("Test 9:");
		this.dump(orient);

		assertTrue(orient.strikeInt() == 29);
		assertTrue(orient.dipInt() == 67);
		assertTrue(orient.azimuthInt() == 299); 
		assertTrue(orient.compassDirection().equalsIgnoreCase("NW")); 
	}
}
