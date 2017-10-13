# GeologicToolbox
Tools for geologic data access and visualization

## Project Idea
The objective of the Geologic toolbox project is to provide software tools which bring geologic data into the GIS world. 
As a first step, a collection of functions to import geologic layer models and borehole information into ESRI's ArcGIS Pro environment
is provided. 

## Functionality
- Import of GOCAD TSurf data (optionally including color codes)
- Import of DUDE TIN files (RAG-specific format)
- Import of borehole data in BIF2 format (RAG-specific format)
- Cross-section generation 
- Surface-layer intersection check utility (prototype implementation)

Currently, this functionality is available as ArcGIS Pro tools. In the future, more fuctionality will be added. 

## License information
This program is free software; you can redistribute it and/or modify it under the terms of the Apache License version 2.0. For further information please refer to 'LICENSE'-file.

## Software Installation 
To install the toolbox for ArcGIS Pro, follow these steps:
1. Copy the required files to your local disk: /arcgispro/bin/SurfaceTools.tbx and all Python source files from /arcgispro/src. Note: Alternatively, just check out this repository https://github.com/bogeo/GeologicToolbox.git with a suitable Git client (e.g., TortoiseGit).
2. Start up ArcGIS Pro and call the 'Add Toolbox' command. Then select the file SurfaceTools.tbx.
3. Inside ArcGIS Pro, select all the downloaded Python files (.py) under 'Tool Properties'. 
That's all!

## Contributing
Please find information for contributing to the project in the separate [CONTRIBUTE.md](CONTRIBUTE.md).

## Project Information
We just presented the project at GeoBremen 2017; find our abstract here: https://www.hochschule-bochum.de/fileadmin/media/fb_v/prof_schmidt/Geologic_Toolbox_Abstract_GeoBremen.pdf
In the near future we will provide more information about this project. 

## Support and Contact
If you encounter any issues with the software or if you would like to see certain functionality added, let us know at:
- Johannes Ruban (johannes.ruban@hs-bochum.de)
- Benno Schmidt (benno.schmidt@hs-bochum.de)
- Holger Lipke (h.lipke@esri.de)
