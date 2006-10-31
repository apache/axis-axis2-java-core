Sample for MTOM
===============

Overview:
---------

This sample demonstrates the capabilities and power of MTOM support of 
AXIS2. In this sample the user can send a file to the service.

Running the Sample:
-------------------
1. Please make sure to set the AXIS2_HOME environment variable to point to the 
AXIS2_DIST.
1. Use <ant service> command in the AXIS2_DIST/sample/mtom/ to build the service.
2. Generated service gets copied to the AXIS2_DIST/repository/services automatically.
Run the AXIS2_DIST/bin/axis2server.{sh.bat} to start the standalone axis2 server. 
(Alternatively you can drop the sample to the services directory of a Axis2 server
running in a servlet container)
4. Use <ant client -Dfile "file to be send" -Ddest "destination file name" > to 
build and run the client.

Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble 
running the sample.

