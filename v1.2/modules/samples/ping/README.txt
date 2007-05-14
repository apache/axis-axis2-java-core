Sample - Pinging the service deployed in Axis2
==========================================

Introduction
============

In this sample, we are deploying an AXIOM based service after writing
a services.xml and creating an aar. We also test successful
deployment using an AXIOM based client which send serveral ping requests.
The client sends serveral ping requests, including service level requests.


Pre-Requisites
==============

Apache Ant 1.6.2 or later

Building the Service
====================

Type "ant generate.service" from Axis2_HOME/samples/pingingservices directory.
Then deploy the
Axis2_HOME/samples/pingingservices/build/PingService.aar


Running the Client
==================

Type ant run.client in the Axis2_HOME/samples/pingingservices directory

Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have
any trouble running the sample.