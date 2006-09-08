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
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.client.STSClient;
import org.apache.rampart.builder.TimestampBuilder;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.Axis2Util;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.IssuedToken;
import org.apache.ws.secpolicy.model.SecureConversationToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MessageBuilder {
    
    private static Log log = LogFactory.getLog(MessageBuilder.class);
    
    public void build(MessageContext msgCtx) throws WSSPolicyException,
            RampartException, WSSecurityException {
        

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
        
        Policy policy = null;
        /*
         * When creating the RampartMessageData instance we 
         * extract the service policy is set in the msgCtx.
         * If it is missing then try to obtain from the configuration files.
         */
        if(rmd.getServicePolicy() != null) {
            if(msgCtx.isServerSide()) {
                String policyXml = msgCtx.getEffectivePolicy().toString();
                policy = PolicyEngine.getPolicy(new ByteArrayInputStream(policyXml.getBytes()));
            } else {
                Parameter param = msgCtx.getParameter(RampartMessageData.KEY_RAMPART_POLICY);
                OMElement policyElem = param.getParameterElement().getFirstElement();
                policy = PolicyEngine.getPolicy(policyElem);
            }
        }
        
        
        List it = (List)policy.getAlternatives().next();
        
        RampartPolicyData policyData = RampartPolicyBuilder.build(it);

     
        rmd.setPolicyData(policyData);
        rmd.setSecHeader(secHeader);
        
        processEnvelope(msgCtx, rmd);
    }
    
    private void processEnvelope(MessageContext msgCtx, RampartMessageData rmd)
            throws RampartException, WSSecurityException {
        log.info("Before create Message assym....");

        RampartPolicyData rpd = rmd.getPolicyData();

        if(rpd.isIncludeTimestamp()) {
            
            log.debug("Adding a timestamp");
            
            TimestampBuilder tsBuilder = new TimestampBuilder();
            tsBuilder.build(rmd);
        }
        
        
        if(rpd.isSymmetricBinding()) {
            log.debug("Procesing symmentric binding: " +
                    "Setting up encryption token and signature token");
            //Setting up encryption token and signature token
            
            Token sigTok = rpd.getSignatureToken();
            Token encrTok = rpd.getEncryptionToken();
            if(sigTok instanceof IssuedToken) {
                
                log.debug("SignatureToken is an IssuedToken");
                
                if(rmd.getIssuedSignatureTokenId() == null) {
                    log.debug("No Issuedtoken found, requesting a new token");
                    
                    IssuedToken issuedToken = (IssuedToken)sigTok;
                    
                    try {
                        
                        String action = TrustUtil.getActionValue(rmd
                                .getWstVersion(),
                                RahasConstants.RST_ACTON_ISSUE);
                        
                        // Get sts epr
                        String issuerEprAddress = RampartUtil
                                .processIssuerAddress(issuedToken
                                        .getIssuerEpr());
                        
                        OMElement rstTemplate = issuedToken.getRstTemplate();
                        
                        String id = this.getToken(rmd, rstTemplate,
                                issuerEprAddress, action);
                        
                        log.debug("Issued token obtained: id=" + id);
                        
                        rmd.setIssuedSignatureTokenId(id);
                    } catch (TrustException e) {
                        throw new RampartException(e.getMessage(), e);
                    }
                    
                }
                
            } else if(sigTok instanceof SecureConversationToken) {
                
                log.debug("SignatureToken is a SecureConversationToken");
                
                if(rmd.getSecConvTokenId() == null) {
                
                    log.debug("No SecureConversationToken found, " +
                            "requesting a new token");
                    
                    SecureConversationToken secConvTok = 
                                        (SecureConversationToken) sigTok;
                    
                    try {
                        
                        String action = TrustUtil.getActionValue(
                                rmd.getWstVersion(),
                                RahasConstants.RST_ACTON_SCT);
                        
                        // Get sts epr
                        String issuerEprAddress = RampartUtil
                                .processIssuerAddress(secConvTok.getIssuerEpr());

                        //Find SC version
                        int conversationVersion = rmd.getSecConvVersion();
                        
                        OMElement rstTemplate = RampartUtil.createRSTTempalteForSCT(
                                conversationVersion, 
                                rmd.getWstVersion());
                        
                        String id = this.getToken(rmd, rstTemplate,
                                issuerEprAddress, action);
                        
                        log.debug("SecureConversationToken obtained: id=" + id);
                        
                        rmd.setSecConvTokenId(id);

                        
                    } catch (TrustException e) {
                        throw new RampartException(e.getMessage(), e);
                    }
                }
            }
            
            //If it was the ProtectionToken assertion then sigTok is the
            //same as encrTok
            if(sigTok.equals(encrTok) && sigTok instanceof IssuedToken) {
                
                log.debug("Symmetric binding uses a ProtectionToken, both" +
                        " SignatureToken and EncryptionToken are the same");
                
                rmd.setIssuedEncryptionTokenId(rmd.getIssuedEncryptionTokenId());
            } else {
                //Now we'll have to obtain the encryption token as well :-)
                //ASSUMPTION: SecureConversationToken is used as a 
                //ProtectionToken therfore we only have to process a issued 
                //token here
                
                log.debug("Obtaining the Encryption Token");
                if(rmd.getIssuedEncryptionTokenId() != null) {
                    
                    log.debug("EncrytionToken not alredy set");

                    IssuedToken issuedToken = (IssuedToken)encrTok;
                    
                    try {
                        
                        String action = TrustUtil.getActionValue(rmd
                                .getWstVersion(),
                                RahasConstants.RST_ACTON_ISSUE);
                        
                        // Get sts epr
                        String issuerEprAddress = RampartUtil
                                .processIssuerAddress(issuedToken
                                        .getIssuerEpr());
                        
                        OMElement rstTemplate = issuedToken.getRstTemplate();
                        
                        String id = this.getToken(rmd, rstTemplate,
                                issuerEprAddress, action);
                        
                        log.debug("Issued token obtained: id=" + id);
                        
                        rmd.setIssuedEncryptionTokenId(id);
                    } catch (TrustException e) {
                        throw new RampartException(e.getMessage(), e);
                    }

                    
                }
                
            }
            
        } else if(rpd.isTransportBinding()) {
            //TODO: Handle transport binding
            
        } else {
            //TODO Setup InitiatorToken and receipientToken
            
        }
    }
    
    
    private String getToken(RampartMessageData rmd, OMElement rstTemplate,
            String issuerEpr, String action) throws RampartException {

        try {
            
            STSClient client = new STSClient(rmd.getMsgContext()
                    .getConfigurationContext());
            // Set request action
            client.setAction(action);
            
            client.setRstTemplate(rstTemplate);
    
            // Set crypto information
            Crypto crypto = RampartUtil.getSignatureCrypto(rmd
                    .getPolicyData().getRampartConfig());
            CallbackHandler cbh = RampartUtil.getPasswordCB(rmd);
            client.setCryptoInfo(crypto, cbh);
    
            // Get service policy
            Policy servicePolicy = rmd.getServicePolicy();
    
            // Get STS policy
            Policy stsPolicy = rmd.getPolicyData()
                    .getRampartConfig().getTokenIssuerPolicy();
    
            // Get service epr
            String servceEprAddress = rmd.getMsgContext()
                    .getOptions().getTo().getAddress();
    
            // Request type
            String reqType = TrustUtil.getWSTNamespace(rmd
                    .getWstVersion())
                    + RahasConstants.REQ_TYPE_ISSUE;
            
            //Make the request
            org.apache.rahas.Token rst = 
                client.requestSecurityToken(servicePolicy, 
                                            issuerEpr,
                                            stsPolicy, 
                                            reqType, 
                                            servceEprAddress);
            
            //Add the token to token storage
            rmd.getTokenStorage().add(rst);
            
            return rst.getId();
        } catch (TrustException e) {
            throw new RampartException(e.getMessage(), e);
        }
    }
    
}
