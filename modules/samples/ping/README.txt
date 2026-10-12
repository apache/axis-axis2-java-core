Sample - Pinging the service deployed in Axis2
==========================================

Introduction
============

In this sample, we are deploying an AXIOM based service after writing
a services.xml and creating an aar. We also test successful
deployment using an AXIOM based client which send several ping requests.
The client sends several ping requests, including service level requests.


Pre-Requisites
==============

This sample is not included in the binary distribution. To run it, copy this directory
into AXIS2_HOME/modules/samples/ of an extracted Axis2 binary distribution and run ant
from there (the build resolves AXIS2_HOME as ../../..).


Apache Ant 1.10 or later

Building the Service
====================

Type "ant generate.service" from AXIS2_HOME/modules/samples/ping directory,
to generate the service and deploy it into 
AXIS2_HOME/repository/services


Running the Client
==================

Type ant run.client in the Axis2_HOME/modules/samples/ping directory

Note
====
Sometimes, if you're having trouble running the client successfully, 
It may be necessary to clean the services repository before you generate the service, deploy it
and run the client. (i.e. delete services created from previous samples.)


Help
====
Please contact java-user list (java-user@axis.apache.org) if you have
any trouble running the sample.