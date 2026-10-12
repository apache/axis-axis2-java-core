Sample: HTTPS Sample
===============

Introduction
============

This sample demonstrate use of HTTPS transport in both server and client sides. Jetty Maven plug-in used as 
the web server and keytool-maven-plugin is used to generate secure key for the sample further same generated 
key is used in client side too. This sample does not try to introduce secure key management best practices, 
but in real world scenarios it's recommended to follow standard key management practices.   

NOTE: httpsService still uses the old org.mortbay maven-jetty-plugin 6.1.26 (Jetty 6, javax
Servlet 2.5), which cannot run the Jakarta-based Axis2 2.x webapp, so "mvn jetty:run" below
does not currently work. Until the sample moves to a Jakarta EE 10 container (for example
jetty-ee10-maven-plugin), deploy the built war to a Jakarta Servlet 6.0 container such as
Tomcat 11 with an HTTPS connector on port 8443.


Pre-Requisites
==============

Apache Maven 3.6.0+ and JDK 17 or later



Running the Sample Service
=========================

1.) In a command line move to "modules/samples/transport/https-sample/httpsService" directory  and run " mvn clean jetty:run"

2.) You should able to see following message on console. 

     "XXXX-XX-XX XX:XX:XX. XXX:INFO::Started SslSocketConnector@0.0.0.0:8443"

3.) Try to access WSDL file through the following URL, in some browsers you have to force to accepts the server certificate. 

     https://localhost:8443/services/SimpleService?wsdl


Running the Sample Client
=========================

1.) In a another command window move to this directory "modules/samples/transport/https-sample/httpsClient".

2.) Run following command.

   "mvn package exec:java -Dexec.mainClass="org.apache.axis2.examples.httpsclient.SimpleServiceClient"  -Dexec.classpathScope=runtime"


3.) You should able to see the response as follows. 

<ns:helloServiceResponse xmlns:ns="http://httpsservice.examples.axis2.apache.org"><ns:return>Hello World </ns:return></ns:helloServiceResponse>



Help
====
Please contact java-user list (java-user@axis.apache.org) if you have any trouble running the sample.
