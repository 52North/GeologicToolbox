![](https://52north.org/wp-content/uploads/2016/06/logo-main.png) 
# 52n GeologicToolbox
Tools for geologic data access, analysis, and 3D visualization

## Project Idea
The objective of the Geologic Toolbox project is to provide software tools which 
bring geologic data into the GIS world. As a first step, a collection of functions 
to import geologic layer models and borehole information into ESRI's ArcGIS Pro 
environment is provided. Furthermore, additional Java implementations which run 
independently from ArcGIS Pro can be used. We expect that the offered toolbox 
functionality will grow soon in the near future. A draft version of a White Paper 
describing the idea behind the project can be found 
[here](https://www.hs-bochum.de/fileadmin/public/Die-BO_Fachbereiche/fb_g/veroeffentlichungen/Schmidt/52N_GeologicToolbox_White_Paper_draft3.pdf). 

## Functionality
### 52n GeologicToolbox for ArcGIS Pro
Currently, this functionality is available as ArcGIS Pro tools.
- Import of GOCAD TSurf data (optionally including color codes)
- Import of DUDE TIN files (RAG-specific format)
- Import of models in VTK format
- Import of borehole data in BIF2 format (RAG-specific format)
- Cross-section generation (on rasterized triangle meshes only)
- Extraction of topological model boundaries
- Surface-layer intersection check utility (prototype implementation)
- Voxel-element generation between surfaces (prototype implementations)

It is planned to add more functionality in the near future.

### 52n GeologicToolbox for Java 
Based on the 52n Triturus framework (see https://github.com/52North/triturus) 
additionally some Java helpers are provided. Currently, this functionality has 
been realized: 
- Checking of GOCAD project files
- Access to GOCAD TSurf data
- Transformation of surface data into simple ASCII formats, e.g. VTK (e.g., for use in [ParaView](https://www.paraview.org/),
X3D, or Wavefront OBJ
- Orientation analysis tools (incl. Clar notation support)
- Simple HTML5/WebGL-based visualization (via X3DOM) 

## License information
This program is free software; you can redistribute it and/or modify it under the 
terms of the Apache License version 2.0. For further information please refer to 
'LICENSE'-file.

## Software Installation 
### GeologicToolbox for ArcGIS Pro
To install the very latest toolbox version for ArcGIS Pro, follow these steps:
1. Copy the required files to your local disk: /arcgispro/bin/GeologicToolbox.tbx 
and all Python source files from /arcgispro/src. Note: Alternatively, just check 
out this repository https://github.com/52North/GeologicToolbox.git with a suitable 
Git client (e.g., TortoiseGit).
2. Start up ArcGIS Pro and call the 'Add Toolbox' command. Then select the file 
GeologicToolbox.tbx. The GeologicToolbox.tbx is now available in the catalog, 
under "Project", "Toolboxes". It contains the different tools as scripts.
3. The scripts still have to be imported individually inside ArcGIS Pro. To do this, open the properties in the context menu
of a script. Here you have to check the box "Import script".
That's all!

### GeologicToolbox for 52n Triturus (for Java Developers)
If you are a Java programmer and want to program your own applications based on
the toolbox's Java packages, here the "quick start" instructions are given:
1. Install Git, e.g. Git for Windows (https://gitforwindows.org/) and TortoiseGit 
(see https://tortoisegit.org/).
2. Check out the 52n Triturus source code, e.g. by starting TortoiseGit and giving 
the repository name: https://github.com/52North/triturus
3. Check out the toolbox's source-code, repository name: https://github.com/52North/GeologicToolbox.git
4. Be sure to have a proper JDK installed, e.g. JDK 8 (download via 
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
5. Set up your Java project using your favorite IDE, e.g. the Eclipse IDE 
(download via http://www.eclipse.org). Use the folder triturus\src for your Java 
source files and do not forget to include the Triturus project. (Using the Eclipse
IDE, the latter can be done by adding the Triturus project to the build path with
help of the "Properties..." dialog of the GeologicToolbox project.)   
6. Compile and/or modify the Java source code in your IDE! (To check whether 
everything has been installed properly, just compile the class 
org.n52.v3d.triturus.geologic.examples.HelloWorld).

## Contributing
Please find information for contributing to the project in the separate 
[CONTRIBUTE.md](CONTRIBUTE.md).

## Project Information
A draft version of the GeologicToolbox White Paper can be found 
[here](https://www.hs-bochum.de/fileadmin/public/Die-BO_Fachbereiche/fb_g/veroeffentlichungen/Schmidt/52N_GeologicToolbox_White_Paper_draft3.pdf). 
We first presented the project at GeoBremen 2017; find our abstract 
[here](https://www.hs-bochum.de/fileadmin/public/Die-BO_Fachbereiche/fb_g/veroeffentlichungen/Schmidt/GeologicToolbox-Abstract-GeoBremen.pdf). In the near future we will provide more information about this project, e.g. in the German [user group "3D Geology and GIS"](https://www.esri.de/gis-community/anwendergruppen). 

## Support and Contact
If you encounter any issues with the software or if you would like to see certain 
functionality added, let us know at:
- Benno Schmidt, Bochum University of Applied Sciences, Geovisualization Lab 
(benno.schmidt@hs-bochum.de)
- Holger Lipke, ESRI Deutschland GmbH (h.lipke@esri.de)
- Johannes Ruban (johannes.ruban@hs-bochum.de)
