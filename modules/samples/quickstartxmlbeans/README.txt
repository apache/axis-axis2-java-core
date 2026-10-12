Axis2 Quick Start Guide- Sample 4 (XML Beans)
============================================

This sample contains source code for the Axis2 Quick Start Guide. For a more detailed
description on the source code, see the Axis2 Quick Start Guide documentation.

Introduction
============
In this sample, we are deploying an XMLBEANS generated service. The service
is tested using generated client stubs.


Pre-Requisites
==============

Apache Ant 1.10 or later

Building the Service
====================

Type "ant generate.service" from Axis2_HOME/modules/samples/quickstartxmlbeans
directory. Then deploy the 
AXIS2_HOME/modules/samples/quickstartxmlbeans/build/service/build/lib/StockQuoteService.aar


Running the Client
==================

type ant run.client in the AXIS2_HOME/modules/samples/quickstartxmlbeans directory

You will get following response
[java] 42.0
[java] price updated
[java] 42.32


Help
====
Please contact java-user list (java-user@axis.apache.org) if you have
any trouble running the sample.
