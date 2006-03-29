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

import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.security.WSDoAllSender;
import org.apache.axis2.security.trust.Constants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;

/**
 * Rahas outflow handler
 */
public class Sender implements Handler {

    private static final long serialVersionUID = 2041716475269157753L;
    
    private HandlerDescription handlerDescription;
    
    public void invoke(MessageContext msgContext) throws AxisFault {
        
        try {
            System.out.println(msgContext.getWSAAction());
            if(Constants.RST_ACTON_SCT.equals(msgContext.getWSAAction())) {
                WSDoAllSender secSender = new WSDoAllSender();
                secSender.init(this.handlerDescription);
                secSender.invoke(msgContext);
                return;
            }
            
            //Parse the configuration
            RahasConfiguration config = RahasConfiguration.load(msgContext, true);

            if(config.getContextIdentifier() == null && config.getStsEPRAddress() != null) {

                String sts = config.getStsEPRAddress();
                if(sts != null) { 
                  //Use a security token service
                  STSRequester.issueRequest(config);
                } else {
                    //Create a token
                }
                
                
                
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
        
        Crypto crypto = Util.getCryptoInstace(config);
        
        Document doc = config.getDocument();
        
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        byte[] tempSecret = config.getTokenStore().getToken(
                config.getContextIdentifier()).getSecret();

        String tokenId = config.getSecurityContextToken().getID();

        // Derived key encryption
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_128);
        encrBuilder.setExternalKey(tempSecret, tokenId);
        encrBuilder.build(doc, crypto, secHeader);

        WSSecurityUtil.prependChildElement(doc, secHeader.getSecurityHeader(),
                config.getSecurityContextToken().getElement(), false);
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
