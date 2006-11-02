Introduction
============

This sample demonstrates how exceptions can be handled using WSDL faults, in other words, how to specify a WSDL fault in order to allow your service to communicate exception pathways to your clients. 

Running of this sample assumes that you are running this within the extracted release folder.

Pre-Requisites
==============

None

Running The Sample
==================

Running the ClientInvoke the client/src/example/BankClient.java class. You may use the command scripts to do so. You need to supply 3 parameters to the command- url, account and amount.

 * ant run.client -Durl=http://localhost:8080/axis2/services/BankService -Daccount=13 -Damt=400
   Throws AccountNotExistFaultMessageException. You will see "Account#13 does not exist"  
 * ant run.client -Durl=http://localhost:8080/axis2/services/BankService -Daccount=88 -Damt=1200
   Throws InsufficientFundsFaultMessageException. You will see "Account#88 has balance of 1000. It cannot support withdrawal   of 1200"  
 * ant run.client -Durl=http://localhost:8080/axis2/services/BankService -Daccount=88 -Damt=400
   Succeeds with a balance of 600. You will see "Balance = 600"  

When you call ant run.client with parameters, before running client/src/example/BankClient.java class, it does the following as well:
 * Generate the stubs (for the client) from the WSDL
 * Compile the client classes
 * Create a Jar of the client classes and copy it to build/client/BankService-test-client.jar

Advanced Guide
==============
For more details kindly see doc/FaultHandlingSampleGuide.html