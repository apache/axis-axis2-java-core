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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.SimpleTokenStore;
import org.apache.rahas.TokenStorage;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.rampart.util.Axis2Util;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.Loader;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;

import java.util.List;

public class RampartMessageData {
    
    /**
     * Axis2 parameter name to be used in the client's axis2 xml
     */
    public final static String KEY_RAMPART_POLICY = "rampartPolicy";
    
    /**
     * Key to hold the address of the issuer in the msg ctx.
     */
    public final static String KEY_ISSUER_ADDRESS = "issuerAddress";
    
    /**
     * Key to hold the WS-Trust version
     */
    public final static String KEY_WST_VERSION = "wstVersion";

    /**
     * Key to hold the WS-SecConv version
     */
    public final static String KEY_WSSC_VERSION = "wscVersion";

    public static final String KEY_SCT_ISSUER_POLICY = "sct-issuer-policy";
    
    private MessageContext msgContext = null;

    private RampartPolicyData policyData = null;

    private WSSecHeader secHeader = null;

    private WSSConfig config = null;
    
    private int timeToLive = 300;
    
    private String timestampId;
    
    private Document document;

    private TokenStorage tokenStorage;
    
    /**
     * WS-Trust version to use.
     * 
     * Pissible values:
     * RahasConstants.VERSION_05_02
     * RahasConstants.VERSION_05_12
     */
    
    private int wstVersion = RahasConstants.VERSION_05_02;
    
    private int secConvVersion = ConversationConstants.DEFAULT_VERSION;
    
    /*
     * IssuedTokens or SecurityContextTokens can be used
     * as the encryption token, signature token,
     */
    private String issuedEncryptionTokenId;
    
    private String issuedSignatureTokenId;
    
    /**
     * The service policy extracted from the message context.
     * If policy is specified in the RampartConfig <b>this</b> will take precedence
     */
    private Policy servicePolicy;

    private boolean isClientSide;
    
    private boolean sender;
    
    private ClassLoader customClassLoader;

    public RampartMessageData(MessageContext msgCtx, boolean sender) throws RampartException {
        
        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        
        this.msgContext = msgCtx;
        
        try {
            
            /*
             * First get the SOAP envelope as document, then create a security
             * header and insert into the document (Envelope)
             */
            this.document = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx.getEnvelope(), false);
            msgCtx.setEnvelope((SOAPEnvelope)this.document.getDocumentElement());
            
            //Extract known properties from the msgCtx
            
            if(msgCtx.getProperty(KEY_WST_VERSION) != null) {
                this.wstVersion = TrustUtil.getWSTVersion((String)msgCtx.getProperty(KEY_WST_VERSION));
            }
            
            if(msgCtx.getProperty(KEY_WSSC_VERSION) != null) {
                this.secConvVersion = TrustUtil.getWSTVersion((String)msgCtx.getProperty(KEY_WSSC_VERSION));
            }
            
            //If the policy is already available in the service, then use it
            
            String operationPolicyKey = getOperationPolicyKey(msgCtx);
            if(msgCtx.getProperty(operationPolicyKey) != null) {
                this.servicePolicy = (Policy)msgCtx.getProperty(operationPolicyKey);
            } 
            
            String svcPolicyKey = getServicePolicyKey(msgCtx);
            if(this.servicePolicy == null && msgCtx.getProperty(svcPolicyKey) != null) {
                this.servicePolicy = (Policy)msgCtx.getProperty(svcPolicyKey);
            }
            
            if(msgCtx.getProperty(KEY_RAMPART_POLICY) != null) {
                this.servicePolicy = (Policy)msgCtx.getProperty(KEY_RAMPART_POLICY);
            }
            
            /*
             * Init policy:
             * When creating the RampartMessageData instance we 
             * extract the service policy is set in the msgCtx.
             * If it is missing then try to obtain from the configuration files.
             */
            if(this.servicePolicy == null) {
                if(msgCtx.isServerSide()) {
                    this.servicePolicy = msgCtx.getEffectivePolicy();
                } else {
                    Parameter param = msgCtx.getParameter(RampartMessageData.KEY_RAMPART_POLICY);
                    if(param != null) {
                        OMElement policyElem = param.getParameterElement().getFirstElement();
                        this.servicePolicy = PolicyEngine.getPolicy(policyElem);
                    }

                    //Set the policy in the config ctx
                    msgCtx.getConfigurationContext().setProperty(
                            RampartMessageData.getOperationPolicyKey(msgCtx), this.servicePolicy);
                }
                
            }
            
            if(this.servicePolicy != null){
                List it = (List)this.servicePolicy.getAlternatives().next();

                //Process policy and build policy data
                this.policyData = RampartPolicyBuilder.build(it);
            }
            
            if(this.policyData != null) {
                //Check for RST and RSTR for an SCT
                RampartConfig rampartConfig = this.policyData.getRampartConfig();
                if((WSSHandlerConstants.RST_ACTON_SCT.equals(msgContext.getWSAAction())
                        || WSSHandlerConstants.RSTR_ACTON_SCT.equals(msgContext.getWSAAction())) &&
                        rampartConfig.getTokenIssuerPolicy() != null) {
                    
                    this.servicePolicy = rampartConfig.getTokenIssuerPolicy();
                    
                    /*
                     * Copy crypto info from the into the new issuer policy 
                     */
                    RampartConfig rc = new RampartConfig();
                    rc.setEncrCryptoConfig(rampartConfig.getEncrCryptoConfig());
                    rc.setSigCryptoConfig(rampartConfig.getSigCryptoConfig());
                    rc.setDecCryptoConfig(rampartConfig.getDecCryptoConfig());
                    rc.setUser(rampartConfig.getUser());
                    rc.setEncryptionUser(rampartConfig.getEncryptionUser());
                    rc.setPwCbClass(rampartConfig.getPwCbClass());
                    
                    this.servicePolicy.addAssertion(rc);
                    
                    List it = (List)this.servicePolicy.getAlternatives().next();
    
                    //Process policy and build policy data
                    this.policyData = RampartPolicyBuilder.build(it);
                }
            }
            
            this.isClientSide = !msgCtx.isServerSide();
            this.sender = sender;
            
            if(!this.isClientSide && this.sender) {
                //Get hold of the incoming msg ctx
                OperationContext opCtx = this.msgContext.getOperationContext();
                MessageContext inMsgCtx;
                if (opCtx != null
                        && (inMsgCtx = opCtx
                                .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)) != null) {
                    msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, 
                            inMsgCtx.getProperty(WSHandlerConstants.RECV_RESULTS));
                }
            }
            
            this.config = WSSConfig.getDefaultWSConfig();

            this.customClassLoader = msgCtx.getAxisService().getClassLoader();
            
            this.secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(this.document);
            
        } catch (TrustException e) {
            throw new RampartException("errorInExtractingMsgProps", e);
        } catch (AxisFault e) {
            throw new RampartException("errorInExtractingMsgProps", e);
        } catch (WSSPolicyException e) {
            throw new RampartException("errorInExtractingMsgProps", e);
        } catch (WSSecurityException e) {
            throw new RampartException("errorInExtractingMsgProps", e);
        }
        
    }

    /**
     * @return Returns the document.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document The document to set.
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @return Returns the timeToLive.
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    /**
     * @param timeToLive The timeToLive to set.
     */
    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * @return Returns the config.
     */
    public WSSConfig getConfig() {
        return config;
    }

    /**
     * @param config
     *            The config to set.
     */
    public void setConfig(WSSConfig config) {
        this.config = config;
    }

    /**
     * @return Returns the msgContext.
     */
    public MessageContext getMsgContext() {
        return msgContext;
    }

    /**
     * @param msgContext
     *            The msgContext to set.
     */
    public void setMsgContext(MessageContext msgContext) {
        this.msgContext = msgContext;
    }

    /**
     * @return Returns the policyData.
     */
    public RampartPolicyData getPolicyData() {
        return policyData;
    }

    /**
     * @param policyData
     *            The policyData to set.
     */
    public void setPolicyData(RampartPolicyData policyData) throws RampartException {
        this.policyData = policyData;
        
        try {
            //if client side then check whether sig conf enabled 
            //and get hold of the stored signature values
            if(this.isClientSide && !this.sender && policyData.isSignatureConfirmation()) {
                OperationContext opCtx = msgContext.getOperationContext();
                MessageContext outMsgCtx = opCtx
                        .getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                msgContext.setProperty(WSHandlerConstants.SEND_SIGV, outMsgCtx
                        .getProperty(WSHandlerConstants.SEND_SIGV));
            }
        } catch (AxisFault e) {
            throw new RampartException("errorGettingSignatureValuesForSigconf", e);
        }
    }

    /**
     * @return Returns the secHeader.
     */
    public WSSecHeader getSecHeader() {
        return secHeader;
    }

    /**
     * @param secHeader
     *            The secHeader to set.
     */
    public void setSecHeader(WSSecHeader secHeader) {
        this.secHeader = secHeader;
    }

    /**
     * @return Returns the issuedEncryptionTokenId.
     */
    public String getIssuedEncryptionTokenId() {
        return issuedEncryptionTokenId;
    }

    /**
     * @param issuedEncryptionTokenId The issuedEncryptionTokenId to set.
     */
    public void setIssuedEncryptionTokenId(String issuedEncryptionTokenId) {
        this.issuedEncryptionTokenId = issuedEncryptionTokenId;
    }

    /**
     * @return Returns the issuedSignatureTokenId.
     */
    public String getIssuedSignatureTokenId() {
        return issuedSignatureTokenId;
    }

    /**
     * @param issuedSignatureTokenId The issuedSignatureTokenId to set.
     */
    public void setIssuedSignatureTokenId(String issuedSignatureTokenId) {
        this.issuedSignatureTokenId = issuedSignatureTokenId;
    }

    /**
     * @return Returns the secConvTokenId.
     */
    public String getSecConvTokenId() {
        String contextIdentifierKey = RampartUtil.getContextIdentifierKey(this.msgContext);
        return (String) RampartUtil.getContextMap(this.msgContext).get(contextIdentifierKey);
    }

    /**
     * @param secConvTokenId The secConvTokenId to set.
     */
    public void setSecConvTokenId(String secConvTokenId) {
        String contextIdentifierKey = RampartUtil.getContextIdentifierKey(this.msgContext);
        RampartUtil.getContextMap(this.msgContext).put(
                                                    contextIdentifierKey,
                                                    secConvTokenId);
    }


    
    /**
     * @return Returns the tokenStorage.
     */
    public TokenStorage getTokenStorage() throws RampartException {

        if(this.tokenStorage != null) {
            return this.tokenStorage;
        }

        TokenStorage storage = (TokenStorage) this.msgContext
                .getConfigurationContext().getProperty(
                        TokenStorage.TOKEN_STORAGE_KEY);

        if (storage != null) {
            this.tokenStorage = storage;
        } else {

            String storageClass = this.policyData.getRampartConfig()
                    .getTokenStoreClass();
    
            if (storageClass != null) {
                Class stClass = null;
                try {
                    stClass = Loader.loadClass(msgContext.getAxisService()
                            .getClassLoader(), storageClass);
                } catch (ClassNotFoundException e) {
                    throw new RampartException(
                            "WSHandler: cannot load token storage class: "
                                    + storageClass, e);
                }
                try {
                    this.tokenStorage = (TokenStorage) stClass.newInstance();
                } catch (java.lang.Exception e) {
                    throw new RampartException(
                            "Cannot create instance of token storage: "
                                    + storageClass, e);
                }
            } else {
                this.tokenStorage = new SimpleTokenStore();
                
            }
            
//            /Set the storage instance
            this.msgContext.getConfigurationContext().setProperty(
                    TokenStorage.TOKEN_STORAGE_KEY, this.tokenStorage);
        }
        
        
        return tokenStorage;
    }

    /**
     * @param tokenStorage The tokenStorage to set.
     */
    public void setTokenStorage(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    /**
     * @return Returns the wstVerion.
     */
    public int getWstVersion() {
        return wstVersion;
    }

    /**
     * @param wstVerion The wstVerion to set.
     */
    public void setWstVersion(int wstVerion) {
        this.wstVersion = wstVerion;
    }

    /**
     * @return Returns the secConvVersion.
     */
    public int getSecConvVersion() {
        return secConvVersion;
    }

    /**
     * @return Returns the servicePolicy.
     */
    public Policy getServicePolicy() {
        return servicePolicy;
    }

    /**
     * @param servicePolicy The servicePolicy to set.
     */
    public void setServicePolicy(Policy servicePolicy) {
        this.servicePolicy = servicePolicy;
    }
    
    /**
     * @param msgCtx
     * @return
     */
    public static String getOperationPolicyKey(MessageContext msgCtx) {
        if(msgCtx.getAxisOperation() != null) {
            return createPolicyKey(msgCtx.getAxisService().getName(), 
                                msgCtx.getAxisOperation().getName());
            
        }
        return null;
    }

    public static String getServicePolicyKey(MessageContext msgCtx) {
        return  createPolicyKey(msgCtx.getAxisService().getName(), null);
    }
    
    public static String createPolicyKey(String service, QName operation) {
        if(operation != null) {
            return RampartMessageData.KEY_RAMPART_POLICY + service
                    + "{" + operation.getNamespaceURI() + "}"
                    + operation.getLocalPart();
        } else {
            return RampartMessageData.KEY_RAMPART_POLICY + service;
        }
    }
    
    /**
     * @return Returns the timestampId.
     */
    public String getTimestampId() {
        return timestampId;
    }

    /**
     * @param timestampId The timestampId to set.
     */
    public void setTimestampId(String timestampId) {
        this.timestampId = timestampId;
    }

    /**
     * @return Returns the isClientSide.
     */
    public boolean isClientSide() {
        return isClientSide;
    }

    public ClassLoader getCustomClassLoader() {
        return customClassLoader;
    }
}
