/*
 * Copyright  2003-2005 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.rampart.samples.sample03;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import java.util.Vector;

public class SimpleService {

    MessageContext msgCtx;

    public void setOperationContext(OperationContext opContext)  throws AxisFault {
        this.msgCtx = opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
    }

    public String echo(String arg) {
        Vector results = null;
        if ((results = (Vector) msgCtx
                .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            System.out.println("No security results!!");
            throw new RuntimeException("No security results!!");
        } else {
            System.out.println("Number of results: " + results.size());
            for (int i = 0; i < results.size(); i++) {
                WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                Vector wsSecEngineResults = rResult.getResults();

                for (int j = 0; j < wsSecEngineResults.size(); j++) {
                    WSSecurityEngineResult wser = (WSSecurityEngineResult) wsSecEngineResults.get(j);
                    if (wser.getAction() == WSConstants.UT
                            && wser.getPrincipal() != null) {
                        
                        //Extract the principal
                        WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal)wser.getPrincipal();
                        
                        //Get user/pass
                        String user = principal.getName();
                        String passwd = principal.getPassword();
                        
                        //Authenticate
                        if("bob".equals(user) && "bobPW".equals(passwd)) {
                            //Authentication suceessful
                            return arg;
                        } else {
                            throw new RuntimeException("Authentication Faliure!!");
                        }
                        
                    }
                }
            }

            return arg;
        }

    }
}
