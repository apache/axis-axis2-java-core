Axis2 Dynamic client Sample
=========================


Building the Service
====================
* Move to "server" directory
* Run "mvn axis2:run" this will start Axis2 Simple HTTP server with above service. 



Running the Clients
===================

* Run each clients using a IDE or use following Maven commands. 

mvn exec:java -Dexec.mainClass="org.apache.axis2.examples.client.WSDL11DynamicClient"  -Dexec.classpathScope=compile
mvn exec:java -Dexec.mainClass="org.apache.axis2.examples.client.WSDL20DynamicClient"  -Dexec.classpathScope=compile
