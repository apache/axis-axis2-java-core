Using WS-Security with Axis2
==============================================================================

This sample uses the WSS4J module with the addressing module to secure the messages as follows:

- First a Timestamp is added to the message and then the Timestamp and the WS-addressing headers are signed with the sender's private key.
- Then the message body is encrypted with the receiver's public key. 
- The CipherValue of the encrpted body is included as an MTOM part.

Setup the service
------------------------------------------------------------------------------
Please follow each of the following steps:
Note - These instructions assume that the Axis2.war is already deployed in Tomcat

1.) Download the rampart-SNAPSHOT.mar and addressing-SNAPSHOT.mar from 
	http://people.apache.org/repository/axis2/mars/
2.) To engage the rampart (WSS4J) module add the following line to axis2.xml in axis2/WEB-INF/conf/
	<module ref="rampart"/>
3.) Copy samples/security/SecureService.aar to axis2/WEB-INF/services/ directory
4.) Copy all jars other than the secUtil.jar in the samples/security/lib directory to axis2/WEB-INF/lib/
5.) Copy the downloaded rampart-SNAPSHOT.mar to Axis2/WEB-INF/modules/ directory
6.) Start Tomcat

Run the sample
------------------------------------------------------------------------------

Copy the downloaded rampart-SNAPSHOT.mar and addressing-SNAPSHOT.mar to samples/security/client_repo/modules/ directory

To run the sample client run the securitySample ant task in the ant build file available in the samples directory.
	$ ant securitySample

Then if everything goes well you will see the following output:

Build file: build.xml

securitySample:
     [java] Response: <example1:echo xmlns:example1="http://example1.org/example1"><example1:Text>Axis2 Echo String </example1:Text></example1:echo>
     [java] SecureService Invocation successful :-)

BUILD SUCCESSFUL
Total time: XX seconds

If you are using JDK 1.5, please download and copy xalan-2.7.0.jar to axis2/WEB-INF/lib/ and samples/security/lib.
xalan-2.7.0 - http://www.apache.org/dist/java-repository/xalan/jars/xalan-2.7.0.jar

If you want to see the signed and encrypted messages fireup tcpmon and change the securitySample ant task in the samples directory to set the required port number.
