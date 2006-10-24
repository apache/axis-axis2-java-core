/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.axis2.oasis.ping;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.wsdl.WSDLConstants;
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
public class PingPortSkeleton{
    /**
     * Auto generated method signature
     *
     * @param param0
     */
    public PingResponseDocument Ping
            (PingDocument param0) {
        Vector results = null;
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        if ((results =
                (Vector) msgCtx.getProperty(WSHandlerConstants.RECV_RESULTS))
                == null) {
            System.out.println("No security results!!");
            throw new RuntimeException("No security results!!");
        } else {
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

}
    