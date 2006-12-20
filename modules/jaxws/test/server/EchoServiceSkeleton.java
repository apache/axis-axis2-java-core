/**
 * EchoServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT Apr 09, 2006 (10:20:36 CDT)
 */
package server;

/**
 *  EchoServiceSkeleton java skeleton for the axisService
 */
public class EchoServiceSkeleton {

    /**
     * Auto generated method signature
     * @param param0
     */
    public  server.EchoStringResponse echoString(server.EchoString input) {
        System.out.println(">> Entering method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        System.out.println(">> Endpoint received input [" + input.getInput() + "]");
        System.out.println(">> Returning string [ECHO:" + input.getInput() + "]");
        System.out.println("<< Done with method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        EchoStringResponse output = new EchoStringResponse();
        output.setEchoStringReturn("ECHO:" + input.getInput());
        return output;
    }
}
    