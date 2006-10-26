********************************************************************************
**************************** Apache Rampart Samples ****************************
********************************************************************************

This is a set of Apache Rampart samples which uses configuraiton parameters 
to configure rampart.

Each "sampleX" directory contains :

    - client.axis2.xml - Client configuration
    - services.xml - Service configuration
    - src - Source of the sample
    - README.txt - you have to read this :-)

We use two parameters named "InflowSecurity" and "OutflowSecurity" within
these files to configure rampart.

01.) Rampart Engaged and no configuration
02.) UsernameToken authentication
03.) UsernameToken authentication with a plain text password
04.) Message integrity and non-repudiation with signature
05.) Encryption
06.) Sign and encrypt a messages
07.) Encrypt and sign messages
08.) Signing twice
09.) Encryption with a key known to both parties
10.) MTOM Optimizing base64 content in the secured message
11.) Dynamic configuration : Get rid of the config files ... let's use code!

You can use the ant build script provided here to run these samples.

Exmaple: Running sample - 01
    - Start two shell instnaces and change to the directory where this file is
    - To start the service: 
      $ ant service.01
    - To run client: 
      $ ant client.01

--------------------------------------------------------------------------------
NOTE: To view the messages exchanged
    - Change the "client.port" property in the "build.xml" to an available port
    	  E.g. : <property name="client.port" value="9080"/>
    	- Setup tcpmon (http://ws.apache.org/commons/tcpmon/) to listen on the above
    	  port and to point to port 8080 (value of the service.port property)