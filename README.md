# 52n GeologicToolbox
Tools for geologic data access, analysis, and 3D visualization

## Project Idea
The objective of the Geologic Toolbox project is to provide software tools which bring geologic data into the GIS world. As a first step, a collection of functions to import geologic layer models and borehole information into ESRI's ArcGIS Pro environment is provided. Furthermore, additional Java implementations which run independently from ArcGIS Pro can be used. We expect that the offered toolbox functionality will grow soon in the near future.    

## Functionality
### 52n GeologicToolbox for ArcGIS Pro
Currently, this functionality is available as ArcGIS Pro tools.  
- Import of GOCAD TSurf data (optionally including color codes)
- Import of DUDE TIN files (RAG-specific format)
- Import of borehole data in BIF2 format (RAG-specific format)
- Cross-section generation 
- Surface-layer intersection check utility (prototype implementation)
- Voxel-element generation between surfaces (prototype implementation)

In the near future, more functionality will be added.

### Java helpers
Based on the 52n Triturus framework (see https://github.com/52North/triturus) addionally some Java helpers are provided. Currently, this functionality has been realized: 
- Checking of GOCAD project files 
- Access to GOCAD TSurf data
- Simple HTML5/WebGL-based visualization (via X3DOM) 

## License information
This program is free software; you can redistribute it and/or modify it under the terms of the Apache License version 2.0. For further information please refer to 'LICENSE'-file.

## Software Installation 
### GeologicToolbox for ArcGIS Pro
To install the toolbox for ArcGIS Pro, follow these steps:
1. Copy the required files to your local disk: /arcgispro/bin/SurfaceTools.tbx and all Python source files from /arcgispro/src. Note: Alternatively, just check out this repository https://github.com/bogeo/GeologicToolbox.git with a suitable Git client (e.g., TortoiseGit).
2. Start up ArcGIS Pro and call the 'Add Toolbox' command. Then select the file SurfaceTools.tbx.
3. Inside ArcGIS Pro, select all the downloaded Python files (.py) under 'Tool Properties'. 
That's all!

### Java helpers (for Java Developers)
Here the "quick start" instructions are given:
1. Install Git, e.g. Git for Windows (https://gitforwindows.org/) and TortoiseGit (see https://tortoisegit.org/).
2. Check out the 52n Triturus source code, e.g. by starting TortoiseGit and giving the repository name: https://github.com/52North/triturus
3. Check out the toolbox's source-code, repository name: https://github.com/52North/GeologicToolbox.git
4. Be sure to have a proper JDK installed, e.g. JDK 8 (download via http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
5. Set up your Java project using your favorite IDE, e.g. the Eclipse IDE (download via http://www.eclipse.org). Use the folder triturus\src for your Java source files and do not forget to include the Triturus project.    
6. Compile and/or modify the Java source code in your IDE!

## Contributing
Please find information for contributing to the project in the separate [CONTRIBUTE.md](CONTRIBUTE.md).

## Project Information
We first presented the project at GeoBremen 2017; find our abstract [here](https://www.hs-bochum.de/fileadmin/public/Die-BO_Fachbereiche/fb_g/veroeffentlichungen/Schmidt/GeologicToolbox-Abstract-GeoBremen.pdf). In the near future we will provide more information about this project, e.g. in the German [user group "3D Geology and GIS"](https://www.esri.de/gis-community/anwendergruppen). 

## Support and Contact
If you encounter any issues with the software or if you would like to see certain functionality added, let us know at:
- Benno Schmidt, Bochum University of Applied Sciences, Geovisualization Lab (benno.schmidt@hs-bochum.de)
- Johannes Ruban, Bochum University of Applied Sciences, Department of Geodesy (johannes.ruban@hs-bochum.de)
- Holger Lipke, ESRI Deutschland GmbH (h.lipke@esri.de)
