Introduction
============

This sample demonstrates how use WSDL2Java generated code with Castor

Running of this sample assumes that you are running this within the extracted release folder.

Pre-Requisites
==============

None

Running The Sample
==================

Running the run.client runs the client/src/samples/databinding/StockClient.java class. You may use the command scripts to do so. You need to supply 2 parameters to the command- url and symbol.

 * ant run.client -Durl=http://localhost:8080/axis2/services/StockQuoteService -Dsymbol=IBM
   Succeeds with a Price of 234235.0. You will see "Price = 234235.0"  

When you call ant run.client with parameters, before running client/src/samples/databinding/StockClient.java class, it does the following as well:
 * Generate the stubs (for the client) from the WSDL
 * Compile the client classes
 * Create a Jar of the client classes and copy it to build/client/StockService-test-client.jar

Advanced Guide
==============
For more details kindly see doc/DataBindingSampleGuide.html