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
1. Use ant generate.service or just the ant command alone in the Axis2_HOME/sample/mtom/ to build the service.
2. Generated service gets copied to the Axis2_HOME/repository/services automatically.
3. Run the Axis2_HOME/bin/axis2server.{sh.bat} to start the standalone axis2 server. (Alternatively
  you can drop the sample into the services directory of a Axis2 server running in a servlet container)
4. Use ant generate.client to build the client.
5. Use ant run.client -Dfile "file to be sent" -Ddest "destination file name" to run the client.


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.

