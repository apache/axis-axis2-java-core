Axis2 Quick Start Guide - Sample 3 (AXIOM)
==========================================

This sample contains source code for the Axis2 Quick Start Guide. For a more detailed
description on the source code, see the Axis2 Quick Start Guide documentation.

Introduction
============

In this sample, we are deploying an AXIOM based POJO service after writing
a services.xml and creating an aar. We also test successful
deployment using an AXIOM based client.


Pre-Requisites
==============

Apache Ant 1.10 or later

Building the Service
====================

Type "ant generate.service" from AXIS2_HOME/modules/samples/quickstartaxiom directory.
Then deploy the 
AXIS2_HOME/modules/samples/quickstartaxiom/build/StockQuoteService.aar


Running the Client
==================

Type ant run.client in the AXIS2_HOME/modules/samples/quickstartaxiom directory

You will get following response
[java] price updated
[java] Current price of WSO: 123.42

Help
====
Please contact java-user list (java-user@axis.apache.org) if you have
any trouble running the sample.
