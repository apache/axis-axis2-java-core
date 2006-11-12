Axis2 Quick Start Guide- Sample 1
=================================

This sample contains source code for the xdocs/1_1/quickstartguide.html document found in 
the extracted Axis2 Documents Distribution. For a more detailed description on the 
source code kindly see this 'Axis2 Quick Start Guide' document.

Introduction
============
In this sample, we are deploying a pojo after writing a services.xml and
creating an aar. We also test the 2 methods using a browser.

Pre-Requisites
==============

Apache Ant 1.6.2 or later

Building the Service
====================

Type "ant generate.service" or just "ant" from Axis2_HOME/samples/quickstart directory 
and then deploy the Axis2_HOME/samples/quickstart/build/StockQuoteService.aar

Running the Client
==================
- From your browser, If you point to the following URL:
http://localhost:8080/axis2/rest/StockQuoteService/getPrice?symbol=IBM

You will get the following response:
<ns:getPriceResponse xmlns:ns="http://pojo.service.quickstart.samples/xsd">
<ns:return>42</ns:return></ns:getPriceResponse>

- If you invoke the update method like so:
http://localhost:8080/axis2/rest/StockQuoteService/update?symbol=IBM&price=100

and then execute the first getPrice url. You can see that the price got updated.

Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.