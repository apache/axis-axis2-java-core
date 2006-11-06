Introduction
============

This sample demonstrates how use WSDL2Java generated code with Castor

Running of this sample assumes that you are running this within the extracted release folder.

Pre-Requisites
==============

None

Before Running The Sample
=========================
 * Please create a directory named lib under the directory that contains this file.
 * Download latest stax-utils jar from
 https://stax-utils.dev.java.net/servlets/ProjectDocumentList?folderID=1106 and drop that into the
 new lib directory.
 * Download latest Castor jar from http://www.castor.org/download.html and drop that into the new
 lib directory

Running The Sample
==================

 *Deploying the Service*

    You need to create the stock service Web service and deploy it. Typing ant in the command prompt,
    will build the service against StockQuoteService.wsdl listed inside this (samples/databinding)
    folder and put it under repository/services of this release folder.

    You need to then startup the server to deploy the service. Goto bin folder and execute either
    axis2server.bat or axis2server.sh, depending on your platform.

 *Running the Client*

   Running the run.client runs the client/src/samples/databinding/StockClient.java class. You may use
   the command scripts to do so. You need to supply 2 parameters to the command- url and symbol.

     * ant run.client -Durl=http://localhost:8080/axis2/services/StockQuoteService -Dsymbol=IBM
     Succeeds with a Price of 99.0. You will see "Price = 99.0"

   When you call ant run.client with parameters, before running
   client/src/samples/databinding/StockClient.java class, it does the following as well:

      * Generate the stubs (for the client) from the WSDL
      * Compile the client classes
      * Create a Jar of the client classes and copy it to build/client/StockService-test-client.jar

Advanced Guide
==============
For more details kindly see doc/DataBindingSampleGuide.html