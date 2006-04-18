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

package org.apache.axis2.security.rahas;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.security.WSDoAllReceiver;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.ws.security.WSSecurityEngine;

import javax.xml.namespace.QName;

/**
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class Receiver implements Handler {

    private static final long serialVersionUID = 8450183308062119444L;

    private HandlerDescription handlerDescription;

    public void invoke(MessageContext msgContext) throws AxisFault {
        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        
        try {
            if (Constants.RST_ACTON_SCT.equals(msgContext.getWSAAction())
                    || Constants.RSTR_ACTON_SCT.equals(msgContext
                            .getWSAAction())) {
                WSDoAllReceiver secReceiver = new WSDoAllReceiver();
                secReceiver.init(this.handlerDescription);
                secReceiver.invoke(msgContext);
                return;
            }
            
            // Parse the configuration
            RahasConfiguration config = RahasConfiguration.load(msgContext,
                    false);
            
            
            //check if there's an RSTR in the msg and process it if exists  
            SOAPEnvelope env = (SOAPEnvelope) config.getDocument().getDocumentElement();
            SOAPHeader header = env.getHeader();
            if (header != null
                    && header.getFirstChildWithName(new QName(Constants.WST_NS,
                            Constants.REQUEST_SECURITY_TOKEN_RESPONSE_LN)) != null) {
                OMElement elem = header.getFirstChildWithName(new QName(Constants.WST_NS,
                        Constants.REQUEST_SECURITY_TOKEN_RESPONSE_LN));
                Util.processRSTR(elem, config);
            }
            
            WSSecurityEngine secEngine = new WSSecurityEngine();
            secEngine.processSecurityHeader(config.getDocument(), null,
                    new RahasCallbackHandler(config), config
                            .getCrypto());

            //Convert back to llom since the inflow cannot use llom
            msgContext.setEnvelope(Axis2Util.getSOAPEnvelopeFromDOOMDocument(
                    config.getDocument(), false));
            
        } catch (Exception e) {
            if (e instanceof RahasException) {
                RahasException re = (RahasException) e;
                throw new AxisFault(re.getFaultString(), re.getFaultCode());
            } else {
                throw new AxisFault(e.getMessage());
            }
        } finally {
            DocumentBuilderFactoryImpl.setDOOMRequired(false);
            Axis2Util.useDOOM(false);
        }

    }

    public void cleanup() throws AxisFault {
    }

    public void init(HandlerDescription handlerdesc) {
        this.handlerDescription = handlerdesc;
    }

    public HandlerDescription getHandlerDesc() {
        return this.handlerDescription;
    }

    public QName getName() {
        return new QName("SecureConversation-Outflow handler");
    }

    public Parameter getParameter(String name) {
        return this.handlerDescription.getParameter(name);
    }
}
