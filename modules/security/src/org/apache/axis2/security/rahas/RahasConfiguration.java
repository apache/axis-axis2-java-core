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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.security.trust.SimpleTokenStore;
import org.apache.axis2.security.trust.TokenStorage;
import org.apache.axis2.security.util.Axis2Util;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;

import java.util.Hashtable;
import java.util.Properties;

/**
 * Configuration manager for Rahas
 */
public class RahasConfiguration {
    
    public final static String RAHAS_CONFIG = "rahas-configuration";
    
    public final static String SCOPE_SERVICE = "service";
    
    public final static String SCOPE_OPERATION = "operation";

    public final static QName SCOPE = new QName("scope");
    
    public final static QName STS_EPR_ADDRESS = new QName("stsEprAddress");
    
    public final static QName DERIVED_KEY_LENGTH = new QName("derivedKeyLength");
    
    public final static QName KEY_DERIVATION_ALGORITHM_CLASS = 
                              new QName("keyDerivationAlgorithmClass");
    
    public final static QName TOKEN_STORE_CLASS = new QName("tokenStoreClass");
    
    public final static QName CRYPTO_PROPERTIES_FILE = new QName(
            "cryptoProperties");
    
    public final static QName ENCRYPTION_USER = new QName("encryptionUser");
    
    public final static QName PW_CALLBACK_CLASS = new QName(
            WSHandlerConstants.PW_CALLBACK_CLASS);
    
    private String scope = SCOPE_SERVICE;
    
    private String stsEPRAddress;
    
    private String derivedKeyLength;
    
    private String keyDerivationAlgorithmClass;
    
    private Hashtable contextMap;
    
    private String tokenStoreClass;
    
    private TokenStorage tokenStore;

    private MessageContext msgCtx;
    
    private String contextIdentifier;
    
    /**
     * This is the properties of a particular <code>Crypto</code> impl
     * 
     * @see org.apache.ws.security.components.crypto.Crypto
     */
    private Properties cryptoProperties;
    
    /**
     * This is the <code>Crypto</code> impl class name.
     * 
     * This will ONLY be set via the message context as a property using 
     * <code>org.apache.axis2.security.rahas.RahasHandlerConstants#CRYPTO_PROPERTIES_KEY<code>. 
     * 
     * @see org.apache.ws.security.components.crypto.Crypto
     * @see org.apache.ws.security.components.crypto.Merlin
     */
    private String cryptoClassName;
    
    /**
     * This is the crypto properties file to be used
     * In this case the <code>Crypto</code> impl and its properties 
     * MUST be listed in this
     * @see org.apache.ws.security.components.crypto.CryptoFactory#getInstance(String)
     */
    private String cryptoPropertiesFile;
    
    private String passwordCallbackClass;
    
    /**
     * WSPasswordCallback handler reference
     */
    private CallbackHandler passwordCallbackRef;
    
    /**
     * Whether this configuration instance is created/used by the sender 
     * handler or not
     */
    private boolean sender;
    
    private Document doc;
    
    private Crypto crypto;
    
    private ClassLoader classLoader;
    
    private SecurityContextToken sct;
    
    private byte[] requesterEntropy;
    
    private byte[] issuerEntropy;
    
    private String encryptionUser;
    
    public static RahasConfiguration load(MessageContext msgCtx, boolean sender)
            throws Exception {
        Parameter param = msgCtx.getParameter(RAHAS_CONFIG);
        if(param == null) {
            param = (Parameter)msgCtx.getProperty(RAHAS_CONFIG);
        }
        if(param != null) {
            OMElement elem = param.getParameterElement();
            if (elem != null
                    && elem.getFirstElement() != null
                    && elem.getFirstElement().getLocalName().equals(
                            RAHAS_CONFIG)) {
                
                OMElement confElem = elem.getFirstElement();
                
                RahasConfiguration config = new RahasConfiguration();
                
                config.msgCtx = msgCtx;
                msgCtx.setProperty(RAHAS_CONFIG, config);
                
                config.scope = getStringValue(confElem.getFirstChildWithName(SCOPE));
                
                config.stsEPRAddress = getStringValue(confElem
                        .getFirstChildWithName(STS_EPR_ADDRESS));

                config.keyDerivationAlgorithmClass = getStringValue(confElem
                        .getFirstChildWithName(KEY_DERIVATION_ALGORITHM_CLASS));
                
                config.tokenStoreClass = getStringValue(confElem
                        .getFirstChildWithName(TOKEN_STORE_CLASS));
                
                config.cryptoPropertiesFile = getStringValue(confElem
                        .getFirstChildWithName(CRYPTO_PROPERTIES_FILE));

                config.passwordCallbackClass = getStringValue(confElem
                        .getFirstChildWithName(PW_CALLBACK_CLASS));
                
                config.encryptionUser = getStringValue(confElem
                        .getFirstChildWithName(ENCRYPTION_USER));
                
                //Get the action<->ctx-identifier map
                config.contextMap = (Hashtable) msgCtx
                        .getProperty(RahasHandlerConstants.CONTEXT_MAP_KEY);

                //Convert the Envelop to DOOM
                config.doc = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx.getEnvelope(), false);
                
                //Token store
                config.tokenStore = (TokenStorage) msgCtx
                        .getProperty(TokenStorage.TOKEN_STORAGE_KEY);
    
                // Context identifier
                if(sender) {
                    if(!msgCtx.isServerSide()) {
                        //Client side sender
                        if (config.scope.equals(RahasConfiguration.SCOPE_OPERATION)) {
                            // Operation scope
                            String action = msgCtx.getSoapAction();
                            config.contextIdentifier = (String) config.getContextMap()
                                    .get(action);
                        } else {
                            // Service scope
                            String serviceAddress = msgCtx.getTo().getAddress();
                            config.contextIdentifier = (String) config.getContextMap()
                                    .get(serviceAddress);
                        }
                        if(config.sct == null && config.contextIdentifier != null) {
                            OMElement tokElem = config.getTokenStore().getToken(config.contextIdentifier).getToken();
                            config.sct = new SecurityContextToken((Element)config.doc.importNode((Element)tokElem, true));
                        }
                        
                    } else {
                        //Server side sender
                        OperationContext opCtx = msgCtx.getOperationContext();
                        MessageContext inMsgCtx;
                        RahasConfiguration inConfig = null;
                        if(opCtx != null && (inMsgCtx = opCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)) != null) {
                            inConfig = (RahasConfiguration)inMsgCtx.getProperty(RAHAS_CONFIG);
                        }
                        if(inConfig != null && inConfig.contextIdentifier != null) {
                            config.contextIdentifier = inConfig.contextIdentifier;
                            config.tokenStore = inConfig.tokenStore;
                            OMElement token = config.tokenStore.getToken(config.contextIdentifier).getToken();
                            config.sct = new SecurityContextToken((Element)config.doc.importNode((Element)token, true));
                        } else {
                            throw new RahasException("canotFindContextIdentifier");
                        }
                        
                        config.setClassLoader(msgCtx.getAxisService().getClassLoader());
                    }
                }

                //Crypto properties
                config.cryptoProperties = (Properties)msgCtx
                        .getProperty(RahasHandlerConstants.CRYPTO_PROPERTIES_KEY);

                config.cryptoClassName = (String) msgCtx
                        .getProperty(RahasHandlerConstants.CRYPTO_CLASS_KEY);
                
                config.passwordCallbackRef = (CallbackHandler)msgCtx
                        .getProperty(WSHandlerConstants.PW_CALLBACK_REF);
                
                config.sender = sender;
                
                return config;
            } else {
                throw new RahasException("missingConfiguration",
                        new String[] { RAHAS_CONFIG });
            }
        } else {
            throw new RahasException("expectedParameterMissing",
                    new String[] { RAHAS_CONFIG });
        }
        
    }

    /**
     * @param elem
     * @throws RahasException
     */
    private static String getStringValue(OMElement elem) throws RahasException {
        if(elem != null) {
            String tempVal = elem.getText();
            return tempVal;
        }
        return null;
    }

    public Parameter getParameter() {
        Parameter param = new Parameter();
        OMElement element = this.getOMElement();
        OMElement paramElem = element.getOMFactory().createOMElement("parameter", null);
        paramElem.addAttribute("name", RahasConfiguration.RAHAS_CONFIG, null);
        paramElem.addChild(element);
        param.setParameterElement(paramElem);
        return param;
    }
    
    private OMElement getOMElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement elem = factory.createOMElement(RAHAS_CONFIG, null);
        if (this.scope != null) {
            OMElement tempElem = factory.createOMElement(SCOPE, elem);
            tempElem.setText(this.scope);
            elem.addChild(tempElem);
        }
        if (this.stsEPRAddress != null) {
            OMElement tempElem = factory.createOMElement(STS_EPR_ADDRESS, elem);
            tempElem.setText(this.stsEPRAddress);
            elem.addChild(tempElem);
        }
        if (this.derivedKeyLength != null) {
            OMElement tempElem = factory.createOMElement(DERIVED_KEY_LENGTH, elem);
            tempElem.setText(this.derivedKeyLength);
            elem.addChild(tempElem);
        }
        if (this.keyDerivationAlgorithmClass != null) {
            OMElement tempElem = factory.createOMElement(KEY_DERIVATION_ALGORITHM_CLASS, elem);
            tempElem.setText(this.keyDerivationAlgorithmClass);
            elem.addChild(tempElem);
        }
        if (this.passwordCallbackClass != null) {
            OMElement tempElem = factory.createOMElement(PW_CALLBACK_CLASS, elem);
            tempElem.setText(this.passwordCallbackClass);
            elem.addChild(tempElem);
        }
        if(this.cryptoPropertiesFile != null) {
            OMElement tempElem = factory.createOMElement(CRYPTO_PROPERTIES_FILE, elem);
            tempElem.setText(this.cryptoPropertiesFile);
            elem.addChild(tempElem);
        }
        if(this.encryptionUser != null) {
            OMElement tempElem = factory.createOMElement(ENCRYPTION_USER, elem);
            tempElem.setText(this.encryptionUser);
            elem.addChild(tempElem);
        }
        return elem;
    }
    
    
    protected void resgisterContext(String identifier) throws RahasException {
        this.contextIdentifier = identifier;
        
        if(this.scope.equals(SCOPE_OPERATION)) {
            String action = msgCtx.getSoapAction();
            if(action != null) {
                this.getContextMap().put(action, identifier);
            } else {
                throw new RahasException("missingWSAAction");
            }
        } else {
            String to = msgCtx.getTo().getAddress();
            if(to != null) {
                this.getContextMap().put(to, identifier);
            } else {
                throw new RahasException("missingWSATo");
            }
        }
        //TODO
        //this.contextMap
    }
    
    /**
     * @return Returns the scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * @return Returns the stsEPR.
     */
    public String getStsEPRAddress() {
        return stsEPRAddress;
    }

    /**
     * @return Returns the derivedKeyLength.
     */
    public String getDerivedKeyLength() {
        return derivedKeyLength;
    }

    /**
     * @return Returns the keyDerivationAlgorithmClass.
     */
    public String getKeyDerivationAlgorithmClass() {
        return keyDerivationAlgorithmClass;
    }

    /**
     * @param derivedKeyLength The derivedKeyLength to set.
     */
    public void setDerivedKeyLength(String derivedKeyLength) {
        this.derivedKeyLength = derivedKeyLength;
    }

    /**
     * @param keyDerivationAlgorithmClass The keyDerivationAlgorithmClass to set.
     */
    public void setKeyDerivationAlgorithmClass(String keyDerivationAlgorithmClass) {
        this.keyDerivationAlgorithmClass = keyDerivationAlgorithmClass;
    }

    /**
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @param stsEPRAddress The stsEPRAddress to set.
     */
    public void setStsEPRAddress(String stsEPRAddress) {
        this.stsEPRAddress = stsEPRAddress;
    }

    /**
     * @return Returns the contextMap.
     */
    protected Hashtable getContextMap() {
        if(contextMap == null) {
            contextMap = new Hashtable();
            
            //Context map should be global
            this.msgCtx.getConfigurationContext().setProperty(
                    RahasHandlerConstants.CONTEXT_MAP_KEY, contextMap);
        }
        
        return contextMap;
    }

    /**
     * @return Returns the tokenStore.
     */
    protected TokenStorage getTokenStore() throws Exception {
        if(this.tokenStore == null) {
            
            //First check the context hierarchy
            this.tokenStore = (TokenStorage) this.msgCtx
                    .getProperty(TokenStorage.TOKEN_STORAGE_KEY
                            + msgCtx.getWSAAction());
            if(this.tokenStore == null) {
                this.tokenStore = (TokenStorage) this.msgCtx
                .getProperty(TokenStorage.TOKEN_STORAGE_KEY
                        + msgCtx.getAxisService().getName()); 
            }
            
            //Create a new store
            if(this.tokenStore == null) {
                if(this.tokenStoreClass != null) {
                     this.tokenStore = (TokenStorage) Class
                            .forName(this.tokenStoreClass).newInstance();
                } else {
                    this.tokenStore = new SimpleTokenStore();
                }
            }
            
            if(SCOPE_SERVICE.equals(this.scope)) {
                this.msgCtx.getConfigurationContext().setProperty(
                        TokenStorage.TOKEN_STORAGE_KEY, this.tokenStore);
            } else {
                this.msgCtx.getConfigurationContext().setProperty(
                        TokenStorage.TOKEN_STORAGE_KEY, this.tokenStore);
            }
        }
        return tokenStore;
    }

    /**
     * @return Returns the tokenStoreClass.
     */
    public String getTokenStoreClass() {
        return tokenStoreClass;
    }

    /**
     * @return Returns the contextIdentifier.
     */
    protected String getContextIdentifier() {
        return contextIdentifier;
    }

    /**
     * @return Returns the cryptoProperties.
     */
    public Properties getCryptoProperties() {
        return cryptoProperties;
    }

    /**
     * @param cryptoProperties The cryptoProperties to set.
     */
    public void setCryptoProperties(Properties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    /**
     * @return Returns the msgCtx.
     */
    protected MessageContext getMsgCtx() {
        return msgCtx;
    }

    /**
     * @param tokenStoreClass The tokenStoreClass to set.
     */
    public void setTokenStoreClass(String tokenStoreClass) {
        this.tokenStoreClass = tokenStoreClass;
    }

    /**
     * @return Returns the cryptoPropertiesFile.
     */
    public String getCryptoPropertiesFile() {
        return cryptoPropertiesFile;
    }

    /**
     * @param cryptoPropertiesFile The cryptoPropertiesFile to set.
     */
    public void setCryptoPropertiesFile(String cryptoPropertiesFile) {
        this.cryptoPropertiesFile = cryptoPropertiesFile;
    }

    /**
     * @return Returns the cryptoClassName.
     */
    public String getCryptoClassName() {
        return cryptoClassName;
    }

    /**
     * @param cryptoClassName The cryptoClassName to set.
     */
    public void setCryptoClassName(String cryptoClassName) {
        this.cryptoClassName = cryptoClassName;
    }

    /**
     * @return Returns the sender.
     */
    protected boolean isSender() {
        return sender;
    }

    /**
     * @return Returns the doc.
     */
    protected Document getDocument() {
        return doc;
    }

    /**
     * @param doc The doc to set.
     */
    protected void setDocument(Document doc) {
        this.doc = doc;
    }

    /**
     * @return Returns the passwordCallbackClass.
     */
    public String getPasswordCallbackClass() {
        return passwordCallbackClass;
    }

    /**
     * @return Returns the passwordCallbackRef.
     */
    public CallbackHandler getPasswordCallbackRef() {
        return passwordCallbackRef;
    }

    /**
     * @return Returns the crypto.
     */
    protected Crypto getCrypto() {
        return crypto;
    }

    /**
     * @param crypto The crypto to set.
     */
    protected void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    /**
     * @return Returns the classLoader.
     */
    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @param classLoader The classLoader to set.
     */
    protected void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @param passwordCallbackClass The passwordCallbackClass to set.
     */
    public void setPasswordCallbackClass(String passwordCallbackClass) {
        this.passwordCallbackClass = passwordCallbackClass;
    }

    /**
     * @return Returns the issuerEntropy.
     */
    protected byte[] getIssuerEntropy() {
        return issuerEntropy;
    }

    /**
     * @param issuerEntropy The issuerEntropy to set.
     */
    protected void setIssuerEntropy(byte[] issuerEntropy) {
        this.issuerEntropy = issuerEntropy;
    }

    /**
     * @return Returns the requesterEntropy.
     */
    protected byte[] getRequesterEntropy() {
        return requesterEntropy;
    }

    /**
     * @param requesterEntropy The requesterEntropy to set.
     */
    protected void setRequesterEntropy(byte[] requesterEntropy) {
        this.requesterEntropy = requesterEntropy;
    }

    /**
     * @return Returns the encryptionUser.
     */
    public String getEncryptionUser() {
        return encryptionUser;
    }

    /**
     * @param encryptionUser The encryptionUser to set.
     */
    public void setEncryptionUser(String encryptionUser) {
        this.encryptionUser = encryptionUser;
    }
    
}
