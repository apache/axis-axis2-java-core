package org.apache.axis2.oasis.ping;

import org.apache.axis2.context.MessageContext;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.xmlsoap.ping.PingDocument;
import org.xmlsoap.ping.PingResponse;
import org.xmlsoap.ping.PingResponseDocument;

import java.util.Vector;

/**
 * Auto generated java skeleton for the service by the Axis code generator
 */
public class PingPortSkeleton {

    private MessageContext mc;

    public void init(MessageContext mc) {
        this.mc = mc;
    }

    /**
     * Auto generated method signature
     *
     * @param param0
     */
    public PingResponseDocument Ping
            (PingDocument param0) {
        Vector results = null;
        if ((results =
                (Vector) mc.getProperty(WSHandlerConstants.RECV_RESULTS))
                == null) {
            System.out.println("No security results!!");
        }
        System.out.println("Number of results: " + results.size());
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult =
                    (WSHandlerResult) results.get(i);
            Vector wsSecEngineResults = rResult.getResults();

            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser =
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                if (wser.getAction() != WSConstants.ENCR && wser.getPrincipal() != null) {
                    System.out.println(wser.getPrincipal().getName());
                }
            }
        }
        PingResponseDocument response = PingResponseDocument.Factory.newInstance();
        PingResponse pingRes = response.addNewPingResponse();
        pingRes.setText("Response: " + param0.getPing().getText());
        return response;
    }

}
    