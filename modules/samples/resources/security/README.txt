Using WS-Security with Axis2
==============================================================================

This sample uses the WSS4J module with the addressing module to secure the messages as follows:

- First a Timestamp is added to the message and then the Timestamp and the WS-addressing headers are signed with the sender's private key.
- The the message body is encrypted with the receiver's public key. 
- The CipherValue of the encrpted body is included as an MTOM part.

Setup the service
------------------------------------------------------------------------------
Please follow each of the following steps:

1.) To engage the security (WSS4J) module add the following line to axis2.xml in axis
	<module ref="security"/>
2.) Copy samples/security/SecureService.aar to axis2/WEB-INF/services/ directory
3.) Copy samples/security/secUtil.jar to axis2/WEB-INF/lib/
4.) Start Tomcat

Run the sample
------------------------------------------------------------------------------
To run the sample client run the securitySample ant task in the ant build file available in the samples directory.
	$ ant securitySample

Then if everything goes well you will see the following output:

Build file: build.xml

securitySample:
     [java] Response: <example1:echo xmlns:example1="http://example1.org/example1"><example1:Text>Axis2 Echo String </example1:Text></example1:echo>
     [java] SecureService Invocation successful :-)

BUILD SUCCESSFUL
Total time: XX seconds

If you want to see the signed and encrypted messages fireup tcpmon and change the securitySample ant task in the samples directory to set the required port number.
