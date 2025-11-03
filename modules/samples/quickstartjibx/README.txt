Axis2 Quick Start Guide- (JiBX)
======================================
This sample contains source code for the Axis2 Quick Start Guide. For a more detailed
description on the source code, see the Axis2 Quick Start Guide documentation.

Introduction
============
In this sample, we are deploying a JiBX generated service. The service
is tested using generated client stubs.

Pre-Requisites
==============

Apache Ant 1.6.2 or later

Building the Service
====================

Type "ant generate.service" from Axis2_HOME/samples/quickstartjibx
directory and then deploy the 
Axis2_HOME/samples/quickstartjibx/build/service/build/lib/StockQuoteService.aar

Running the Client
==================

type ant run.client in the Axis2_HOME/samples/quickstartadb directory

You will get following response
[java] 42.0
[java] price updated
[java] 42.35


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you
have any trouble running the sample.
