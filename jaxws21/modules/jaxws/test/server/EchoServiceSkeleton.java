/**
 * EchoServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT Apr 09, 2006 (10:20:36 CDT)
 */
package server;

import org.apache.axis2.jaxws.TestLogger;

/**
 *  EchoServiceSkeleton java skeleton for the axisService
 */
public class EchoServiceSkeleton {

    /**
     * Auto generated method signature
     * @param param0
     */
    public  server.EchoStringResponse echoString(server.EchoString input) {
        TestLogger.logger
                .debug(">> Entering method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        TestLogger.logger.debug(">> Endpoint received input [" + input.getInput() + "]");
        TestLogger.logger.debug(">> Returning string [ECHO:" + input.getInput() + "]");
        TestLogger.logger
                .debug("<< Done with method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        EchoStringResponse output = new EchoStringResponse();
        output.setEchoStringReturn("ECHO:" + input.getInput());
        return output;
    }
}
    