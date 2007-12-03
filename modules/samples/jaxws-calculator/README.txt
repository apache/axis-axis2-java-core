Sample: JAXWS-Calculator
====================================

Introduction
============

This is an example JAXWS Web service. It shows how to expose the methods of a class as a JAXWS Web
service using Aixs2.


Building the Service
====================

To build the sample service, type: mvn clan install

This will build the jaxws-calculator.aar in the target directory and copy it to the
<AXIS2_HOME>/repository/services directory.

You can start the Axis2 server by running either axis2server.bat (on Windows) or axis2server.sh
(on Linux)that are located in <AXIS2_HOME>/bin directory.

The WSDL for this service should be viewable at:

http://<yourhost>:<yourport>/axis2/services/CalculatorService?wsdl
(e.g. http://localhost:8080/axis2/services/CalculatorService?wsdl)


Running the Client
==================

Rest like invocation, 
Open the browser and invoke the service 
EX : http://localhost:8080/axis2/services/CalculatorService/add?value1=1&value2=34

generate the client stubs pointing to the wsdl and invoke the service with appropriate client

Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.

Improvements
============
Make this sample run possible with ant 


