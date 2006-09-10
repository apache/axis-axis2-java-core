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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.SimpleTokenStore;
import org.apache.rahas.TokenStorage;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.util.Loader;
import org.w3c.dom.Document;

import java.util.Vector;

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

    private MessageContext msgContext = null;

    private RampartPolicyData policyData = null;

    private WSSecHeader secHeader = null;

    private WSSConfig config = null;
    
    private int timeToLive = 300;
    
    private String timestampId;
    
    private Document document;
    
    private Vector encryptionParts;
    
    private Vector signatureParts;
    
    private Vector endorsedSignatureParts;
    
    private Vector signedEndorsedSignatureParts;

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
    
    private String secConvTokenId;
    
    
    /**
     * The service policy extracted from the message context.
     * If policy is specified in the RampartConfig <b>this</b> will take precedence
     */
    private Policy servicePolicy;

    private boolean isClientSide;
    
    private boolean sender;

    public RampartMessageData(MessageContext msgCtx, Document doc, boolean sender) throws RampartException {
        this.msgContext = msgCtx;
        this.document = doc;
        
        try {
            //Extract known properties from the msgCtx
            
            if(msgCtx.getProperty(KEY_WST_VERSION) != null) {
                this.wstVersion = TrustUtil.getWSTVersion((String)msgCtx.getProperty(KEY_WST_VERSION));
            }
            
            if(msgCtx.getProperty(KEY_WSSC_VERSION) != null) {
                this.secConvVersion = TrustUtil.getWSTVersion((String)msgCtx.getProperty(KEY_WSSC_VERSION));
            }
            
            //This is for a user to set policy in from the client
            if(msgCtx.getProperty(KEY_RAMPART_POLICY) != null) {
                this.servicePolicy = (Policy)msgCtx.getProperty(KEY_RAMPART_POLICY);
            }
            
            //If the policy is already available in the service, then use it
            if(msgCtx.getParameter(KEY_RAMPART_POLICY) != null) {
                this.servicePolicy = (Policy)msgCtx.getProperty(getPolicyKey(msgCtx));
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
            
        } catch (TrustException e) {
            throw new RampartException("errorInExtractingMsgProps", e);
        } catch (AxisFault e) {
            throw new RampartException("errorInExtractingMsgProps", e);
        }
        
    }
    
    /**
     * @return Returns the encryptionParts.
     */
    public Vector getEncryptionParts() {
        return encryptionParts;
    }

    /**
     * @param encryptionParts The encryptionParts to set.
     */
    public void setEncryptionParts(Vector encryptionParts) {
        this.encryptionParts = encryptionParts;
    }

    /**
     * @return Returns the endorsedSignatureParts.
     */
    public Vector getEndorsedSignatureParts() {
        return endorsedSignatureParts;
    }

    /**
     * @param endorsedSignatureParts The endorsedSignatureParts to set.
     */
    public void addEndorsedSignaturePart(String id) {
        if(this.endorsedSignatureParts == null) {
            this.endorsedSignatureParts = new Vector();
        }
        
        this.endorsedSignatureParts.add(id);
    }

    /**
     * @return Returns the signatureParts.
     */
    public Vector getSignatureParts() {
        return signatureParts;
    }

    /**
     * @param signatureParts The signatureParts to set.
     */
    public void addSignaturePart(String id) {
        if(this.signatureParts == null) {
            this.signatureParts = new Vector();
        }
        this.signatureParts.add(id);
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
        return secConvTokenId;
    }

    /**
     * @param secConvTokenId The secConvTokenId to set.
     */
    public void setSecConvTokenId(String secConvTokenId) {
        this.secConvTokenId = secConvTokenId;
    }

    /**
     * @return Returns the tokenStorage.
     */
    public TokenStorage getTokenStorage() throws RampartException {

        if(this.tokenStorage != null) {
            return this.tokenStorage;
        }

        TokenStorage storage = (TokenStorage) this.msgContext
                .getProperty(TokenStorage.TOKEN_STORAGE_KEY);

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
        }
        
        //Set the storage instance
        this.msgContext.getConfigurationContext().setProperty(
                TokenStorage.TOKEN_STORAGE_KEY, this.tokenStorage);
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
    public static String getPolicyKey(MessageContext msgCtx) {
        return RampartMessageData.KEY_RAMPART_POLICY
                + msgCtx.getAxisService().getName() + "{"
                + msgCtx.getAxisOperation().getName().getNamespaceURI()
                + "}" + msgCtx.getAxisOperation().getName().getLocalPart();
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

    /**
     * @return Returns the signedendorsedSignatureParts.
     */
    public Vector getSignedEndorsedSignatureParts() {
        return signedEndorsedSignatureParts;
    }

    public void addSignedEndorsedSignatureParts(String id) {
        if(this.signedEndorsedSignatureParts == null) {
            this.signedEndorsedSignatureParts = new Vector();
        }
        
        this.signedEndorsedSignatureParts.add(id);
    }

}
