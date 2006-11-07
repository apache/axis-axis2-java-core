Sample: MTOM (Message Transmission Optimization Mechanism)
=========================================================

Introduction:
============

This sample demonstrates the capabilities and power of MTOM support of AXIS2. In this sample the
user can send a file to the service.

Prerequisites
=============
Apache Ant 1.6.2 or later


Running the Sample:
===================
1. Use ant generate.service or just ant command in the AXIS2_DIST/sample/mtom/ to build the service.
2. Generated service gets copied to the AXIS2_DIST/repository/services automatically.
3. Run the AXIS2_DIST/bin/axis2server.{sh.bat} to start the standalone axis2 server. (Alternatively
  you can drop the sample to the services directory of a Axis2 server running in a servlet container)
4. Use ant run.client -Dfile "file to be send" -Ddest "destination file name" > to build and run the
 client.


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.

