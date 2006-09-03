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

package org.apache.rampart;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.rampart.builder.TimestampBuilder;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.Axis2Util;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.IssuedToken;
import org.apache.ws.secpolicy.model.SecureConversationToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import java.util.Iterator;

public class MessageBuilder {
    
    private static Log log = LogFactory.getLog(MessageBuilder.class);
    
    public void build(MessageContext msgCtx) throws WSSPolicyException, RampartException, WSSecurityException {
        
        //TODO: Get hold of the policy from the message context
        Policy policy = new Policy();
        Iterator it = (Iterator)policy.getAlternatives().next();
        
        RampartPolicyData policyData = RampartPolicyBuilder.build(it);
        
        processEnvelope(msgCtx, policyData);
    }
    
    private void processEnvelope(MessageContext msgCtx, RampartPolicyData rpd) throws RampartException, WSSecurityException {
        log.info("Before create Message assym....");

        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        
        /*
         * First get the SOAP envelope as document, then create a security
         * header and insert into the document (Envelope)
         */
        Document doc = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx.getEnvelope(), false);
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc
                .getDocumentElement());

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        RampartMessageData rmd = new RampartMessageData(msgCtx, doc);
        rmd.setPolicyData(rpd);
        rmd.setSecHeader(secHeader);
        
        if(rpd.isIncludeTimestamp()) {
            TimestampBuilder tsBuilder = new TimestampBuilder();
            tsBuilder.build(rmd);
        }
        
        
        if(rpd.isSymmetricBinding()) {
            //Setting up encryption token and signature token
            
            Token sigTok = rpd.getSignatureToken();
            Token encrTok = rpd.getEncryptionToken();
            
            if(sigTok instanceof IssuedToken) {
                if(rmd.getIssuedSignatureTokenId() == null) {
                    //TODO: Obtain the Token
                }
            } else if(sigTok instanceof SecureConversationToken) {
                if(rmd.getSecConvTokenId() == null) {

                    OMElement elem = ((SecureConversationToken) sigTok)
                            .getIssuerEpr();
                    
                    String issuerAddress = this.extractIssuerAddress(elem);
                    
                    issuerAddress = (issuerAddress != null) ? issuerAddress
                            : msgCtx.getOptions().getTo().getAddress();
                    
//                    String id = STSClient.getSct(rmd, issuerAddress,
//                            ((SecureConversationToken) sigTok)
//                                    .getBootstrapPolicy());
                }
            }
        } else if(!rpd.isSymmetricBinding() && !rpd.isTransportBinding()) {
            //TODO Setup InitiatorToken and receipientToken
            
        } else {
            //TODO: Handle transport binding
            
        }
    }
    
    /**
     * @param elem
     * @return
     */
    private String extractIssuerAddress(OMElement elem) {
        //Assuming the element is a wsa:Address element
        if(elem == null) {
            return null;
        }
        
        return elem.getText().trim();
    }


    
}
