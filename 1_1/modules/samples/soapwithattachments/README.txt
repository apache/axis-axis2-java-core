Sample for MTOM
===============

Overview:
---------

This sample demonstrates the capabilities and power of Soap with Attachment
support and the Attachmet API of AXIS2. In this sample the user can upload a 
file to the service. The service is written and deployed as a simple POJO and 
it uses the Attachment API to retrieve the received attachment. Client is written 
using Operation Client API of Axis2.

Running the Sample:
-------------------
1. Use ant generate.service or ant command in the AXIS2_DIST/sample/mtom/ to build the service.
2. Generated service gets copied to the AXIS2_DIST/repository/services automatically.
Run the AXIS2_DIST/bin/axis2server.{sh.bat} to start the standalone axis2 server. 
(Alternatively you can drop the sample to the services directory of a Axis2 server
running in a servlet container)
4. Use ant run.client -Dfile "file to be send" -Ddest "destination file name" > to 
build and run the client.

Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble 
running the sample.

