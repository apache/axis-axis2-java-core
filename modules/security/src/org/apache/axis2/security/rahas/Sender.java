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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.security.trust.Token;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSecurityContextToken;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;

import java.security.SecureRandom;

/**
 * Rahas outflow handler
 */
public class Sender implements Handler {

    private static final long serialVersionUID = 2041716475269157753L;
    
    private HandlerDescription handlerDescription;
    
    public void invoke(MessageContext msgContext) throws AxisFault {
        
        try {
            //Parse the configuration
            RahasConfiguration config = RahasConfiguration.load(msgContext, true);
            

            if(config.getContextIdentifier() != null) {
                
            } else {
                this.constructMessage(config);
            }
            
            
            String sts = config.getStsEPRAddress();
            
            if(sts != null) { //Use a security token service
                
            }
            
            
            
        } catch (Exception e) {
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
    
    private void constructMessage(RahasConfiguration config) throws Exception {
        
        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        
        Crypto crypto = null;
        if (config.getCryptoClassName() != null) {
            //we can let the crypto properties be null since there can be a 
            //crypto impl that doesn't use any expernal properties
            crypto = CryptoFactory.getInstance(config.getCryptoClassName(),
                    config.getCryptoProperties());
        } else if (config.getCryptoPropertiesFile() != null) {
            crypto = CryptoFactory
                    .getInstance(config.getCryptoPropertiesFile());
        }
        
        //convert the envelope to DOOM
        Document doc = Axis2Util.getDocumentFromSOAPEnvelope(config.getMsgCtx()
                .getEnvelope(), false);
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        WSSecSecurityContextToken sctBuilder = new WSSecSecurityContextToken();
        sctBuilder.prepare(doc, crypto);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] tempSecret = new byte[16];
        random.nextBytes(tempSecret);

        String tokenId = sctBuilder.getSctId();

        // Derived key encryption
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(tempSecret, tokenId);
        encrBuilder.build(doc, crypto, secHeader);

        sctBuilder.prependSCTElementToHeader(doc, secHeader);
        
        Token tok = new Token(sctBuilder.getIdentifier(), (OMElement) sctBuilder
                .getSct().getElement());
        
        tok.setSecret(tempSecret);
        
        config.getTokenStore().add(tok);
        
        
        
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
