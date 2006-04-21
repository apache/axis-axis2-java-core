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
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.security.WSDoAllSender;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.Token;
import org.apache.axis2.security.trust.TrustException;
import org.apache.axis2.security.trust.TrustUtil;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.security.cert.X509Certificate;

/**
 * Rahas outflow handler
 */
public class Sender implements Handler {

    private static final long serialVersionUID = 2041716475269157753L;
    
    private HandlerDescription handlerDescription;
    
    public void invoke(MessageContext msgContext) throws AxisFault {
        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        try {
            if(Constants.RST_ACTON_SCT.equals(msgContext.getWSAAction()) ||
                    Constants.RSTR_ACTON_SCT.equals(msgContext.getWSAAction())) {
                WSDoAllSender secSender = new WSDoAllSender();
                secSender.init(this.handlerDescription);
                secSender.processMessage(msgContext);
                return;
            }
            
            //Parse the rahas configuration
            RahasConfiguration config = RahasConfiguration.load(msgContext, true);
            msgContext.setEnvelope((SOAPEnvelope) config.getDocument()
                    .getDocumentElement());
            
            if(!config.getMsgCtx().isServerSide()) {
                if(config.getContextIdentifier() == null && !config.getMsgCtx().isServerSide()) {
    
                    String sts = config.getStsEPRAddress();
                    if(sts != null) {
                      //Use a security token service
                      STSRequester.issueRequest(config);
                    } else {
                        //Create an an SCT, include it in an RSTR 
                        // and add the RSTR to the header
                        this.createRSTR(config);
                    }
                    
                }
            }
            this.constructMessage(config);

        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof RahasException) {
                RahasException re = (RahasException)e;
                throw new AxisFault(re.getFaultString(), re.getFaultCode());
            } else {
                throw new AxisFault(e.getMessage());
            }
        } finally {
            DocumentBuilderFactoryImpl.setDOOMRequired(false);
        }
        
    }

    /**
     * Create the self created <code>wsc:SecurityContextToken</code> and 
     * add it to a <code>wst:RequestSecurityTokenResponse</code>.
     * 
     * This is called in the case where the security context establishment 
     * is done by one of the parties with out the use of an STS
     * and the creted SCT is sent across to the other party in an unsolicited 
     * <code>wst:RequestSecurityTokenResponse</code>
     * 
     * @param config
     * @throws Exception
     */
    private void createRSTR(RahasConfiguration config) throws Exception {
        
        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        Crypto crypto = Util.getCryptoInstace(config);
        String encryptionUser = config.getEncryptionUser();
        if(encryptionUser == null) {
            throw new RahasException("missingEncryptionUser");
        }
        X509Certificate cert = crypto.getCertificates(encryptionUser)[0];
        
        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        try {
            encrKeyBuilder.setUseThisCert(cert);
            encrKeyBuilder.prepare(config.getDocument(), crypto);
        } catch (WSSecurityException e) {
            throw new TrustException(
                    "errorInBuildingTheEncryptedKeyForPrincipal",
                    new String[] { cert.getSubjectDN().getName()});
        }
        
        SecurityContextToken sct = new SecurityContextToken(config.getDocument());
        config.resgisterContext(sct.getIdentifier());
        Token token = new Token(sct.getIdentifier(), (OMElement)sct.getElement());
        token.setSecret(encrKeyBuilder.getEphemeralKey());
        
        config.getTokenStore().add(token);
        
        SOAPEnvelope env = config.getMsgCtx().getEnvelope();

        SOAPHeader header = env.getHeader();
        if(header == null) {
            header = ((SOAPFactory)env.getOMFactory()).createSOAPHeader(env);
        }
        
        OMElement rstrElem = TrustUtil.createRequestSecurityTokenResponseElement(header);

        OMElement rstElem = TrustUtil.createRequestedSecurityTokenElement(rstrElem);
        
        rstElem.addChild((OMElement)sct.getElement());
        
        TrustUtil.createRequestedAttachedRef(rstrElem, "#" + sct.getID(),
                Constants.TOK_TYPE_SCT);

        TrustUtil.createRequestedUnattachedRef(rstrElem, sct.getIdentifier(),
                Constants.TOK_TYPE_SCT);
        
        Element encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
        Element bstElem = encrKeyBuilder.getBinarySecurityTokenElement();
        
        OMElement reqProofTok = TrustUtil.createRequestedProofTokenElement(rstrElem);

        if(bstElem != null) {
            reqProofTok.addChild((OMElement)bstElem);
        }
        
        reqProofTok.addChild((OMElement)encryptedKeyElem);
        
    }
    
    private void constructMessage(RahasConfiguration config) throws Exception {
        
        Crypto crypto = Util.getCryptoInstace(config);

        Document doc = config.getDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Token tempToken = config.getTokenStore().getToken(
                config.getContextIdentifier());
        byte[] tempSecret = tempToken.getSecret();

        SecurityContextToken sct = new SecurityContextToken((Element) doc
                .importNode((Element) tempToken.getToken(), true));

        // Derived key encryption
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        OMElement attachedReference = tempToken.getAttachedReference();
        if(attachedReference != null) {
            encrBuilder.setExternalKey(tempSecret, (Element) doc.importNode(
                    (Element) attachedReference, true));
        } else {
            String tokenId = sct.getID();
            encrBuilder.setExternalKey(tempSecret, tokenId);
        }
        encrBuilder.build(doc, crypto, secHeader);

        WSSecurityUtil.prependChildElement(doc, secHeader.getSecurityHeader(),
                sct.getElement(), false);
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
