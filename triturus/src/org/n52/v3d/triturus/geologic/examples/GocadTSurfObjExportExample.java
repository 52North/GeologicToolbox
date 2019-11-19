package org.n52.v3d.triturus.geologic.examples;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.geologic.importers.IoGocadTSurfReader;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINFeature;
import org.n52.v3d.triturus.gisimplm.IoTINWriter;

/**
 * Geologic Toolbox example application: Reads a GOCAD TSurf TIN and writes 
 * it to an OBJ-File.
 * 
 * @author Jatzek
 */
public class GocadTSurfObjExportExample
{

	private final String
 		inFilename = "data/simple_test.ts",
		outFilename = "data/simple_test.obj"; 
	
	public static void main(String args[]) {
		new GocadTSurfObjExportExample().run();
	}
	
	public void run() 
	{ 
		try {
			// Read first TSurf model from GOCAD data file...
			IoGocadTSurfReader reader = new IoGocadTSurfReader();
			GmSimpleTINFeature surf = reader.read(inFilename).get(0);
			// ... and generate OBJ output:
			IoTINWriter writer = new IoTINWriter(IoTINWriter.OBJ);
			writer.writeToFile(surf, outFilename);
			System.out.println("Wrote the file \"" + outFilename + "\".");
		}
		catch (T3dException e) {
			e.printStackTrace();
		}
	}

}
