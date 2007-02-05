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

package org.apache.rampart.conversation;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.SimpleTokenStore;
import org.apache.rahas.TokenStorage;
import org.apache.rampart.RampartException;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.util.Axis2Util;
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
 * Configuration manager for Ramapart-SecConv
 * @deprecated
 */
public class ConversationConfiguration {
    
    public final static String SC_CONFIG = "sc-configuration";
    
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

    private static final QName PROVIDE_ENTROPY = new QName("provideEntropy");
    
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
     * <code>org.apache.rampart.WSSHandlerConstants#CRYPTO_PROPERTIES_KEY<code>. 
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
    
    private String encryptionUser;
    
    private boolean provideEntropy;
    
    /**
     * WS-Trust version to use
     * Default is RahasConstants.VERSION_05_02
     */
    private int wstVersion = RahasConstants.VERSION_05_02;
    
    /**
     * Builds the configuration from an Axis2 parameter.
     * @param msgCtx
     * @param sender
     * @return If there is an Axis2 parameter available in the context
     * hierarchy or the configuration hierarchy then return the populated
     * <code>ConversationConfiguration</code> instance. If the parameter is not
     * found then  
     * @throws Exception
     */
    public static ConversationConfiguration load(MessageContext msgCtx, boolean sender)
            throws Exception {
        Parameter param = getParameter(msgCtx);
        if(param != null) {
            OMElement elem = param.getParameterElement();
            if (elem != null
                    && elem.getFirstElement() != null
                    && elem.getFirstElement().getLocalName().equals(
                            SC_CONFIG)) {
                
                OMElement confElem = elem.getFirstElement();
                
                ConversationConfiguration config = new ConversationConfiguration();
                
                config.msgCtx = msgCtx;
                msgCtx.setProperty(SC_CONFIG, config);
                
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
                
                config.provideEntropy = confElem
                        .getFirstChildWithName(PROVIDE_ENTROPY) != null;
                
                //Get the action<->ctx-identifier map
                config.contextMap = (Hashtable) msgCtx
                        .getProperty(WSSHandlerConstants.CONTEXT_MAP_KEY);

                //Convert the Envelop to DOOM
                config.doc = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx
                        .getEnvelope(), false);
                
                //Token store
                config.tokenStore = (TokenStorage) msgCtx
                        .getProperty(TokenStorage.TOKEN_STORAGE_KEY);
    
                // Context identifier
                if(sender) {
                    if(!msgCtx.isServerSide()) {
                        //Client side sender
                        if (config.scope.equals(ConversationConfiguration.SCOPE_OPERATION)) {
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
                        ConversationConfiguration inConfig = null;
                        if(opCtx != null && (inMsgCtx = opCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)) != null) {
                            inConfig = (ConversationConfiguration)inMsgCtx.getProperty(SC_CONFIG);
                        }
                        if(inConfig != null && inConfig.contextIdentifier != null) {
                            config.contextIdentifier = inConfig.contextIdentifier;
                            config.tokenStore = inConfig.tokenStore;
                            OMElement token = config.tokenStore.getToken(config.contextIdentifier).getToken();
                            config.sct = new SecurityContextToken((Element)config.doc.importNode((Element)token, true));
                        } else {
                            throw new RampartException("canotFindContextIdentifier");
                        }
                        
                        config.setClassLoader(msgCtx.getAxisService().getClassLoader());
                    }
                }

                //Crypto properties
                config.cryptoProperties = (Properties)msgCtx
                        .getProperty(WSSHandlerConstants.CRYPTO_PROPERTIES_KEY);

                config.cryptoClassName = (String) msgCtx
                        .getProperty(WSSHandlerConstants.CRYPTO_CLASS_KEY);
                
                config.passwordCallbackRef = (CallbackHandler)msgCtx
                        .getProperty(WSHandlerConstants.PW_CALLBACK_REF);
                
                config.sender = sender;
                
                return config;
            } else {
                throw new RampartException("missingConfiguration",
                        new String[] { SC_CONFIG });
            }
        } else {
            //If there's no configuration return null
            return null;
        }
        
    }

    /**
     * @param msgCtx
     * @return The configuration parameter from the given message context
     */
    public static Parameter getParameter(MessageContext msgCtx) {
        Parameter param = msgCtx.getParameter(SC_CONFIG);
        if(param == null) {
            param = (Parameter)msgCtx.getProperty(SC_CONFIG);
        }
        return param;
    }

    /**
     * @param elem
     * @throws RampartException
     */
    private static String getStringValue(OMElement elem) throws RampartException {
        if(elem != null) {
            return elem.getText();
        }
        return null;
    }

    /**
     * Generate the Axis2 parameter representing ConversationConfiguration
     * @return The Axis2 parameter representing ConversationConfiguration
     */
    public Parameter getParameter() {
        Parameter param = new Parameter();
        OMElement element = this.getOMElement();
        OMElement paramElem = element.getOMFactory().createOMElement("parameter", null);
        paramElem.addAttribute("name", ConversationConfiguration.SC_CONFIG, null);
        paramElem.addChild(element);
        param.setParameterElement(paramElem);
        return param;
    }
    
    private OMElement getOMElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement elem = factory.createOMElement(SC_CONFIG, null);
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
        if(this.provideEntropy) {
            factory.createOMElement(PROVIDE_ENTROPY, elem);
        }
        return elem;
    }
    
//    /**
//     * This registers the security context mapping ?e context identifier to 
//     * the wsa:Action/soapAction or the service address, depending on the scope.
//     * 
//     * @param identifier The security context identifier
//     * @throws RampartException 
//     *      If scope is "operation" and the wsa:Action is not available.
//     *      If scope is "service" and the wsa:To is missing.  
//     */
//    protected void resgisterContext(String identifier) throws RampartException {
//        this.contextIdentifier = identifier;
//        
//        if(this.scope.equals(SCOPE_OPERATION)) {
//            String action = msgCtx.getSoapAction();
//            if(action != null) {
//                this.getContextMap().put(action, identifier);
//            } else {
//                throw new RampartException("missingWSAAction");
//            }
//        } else {
//            String to = msgCtx.getTo().getAddress();
//            if(to != null) {
//                this.getContextMap().put(to, identifier);
//            } else {
//                throw new RampartException("missingWSATo");
//            }
//        }
//        //TODO
//        //this.contextMap
//    }
    
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
                    WSSHandlerConstants.CONTEXT_MAP_KEY, contextMap);
        }
        
        return contextMap;
    }

    /**
     * @return Returns the tokenStore.
     */
    public TokenStorage getTokenStore() throws Exception {
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
    public Document getDocument() {
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
     * @param passwordCallbackClass The passwordCallbackClass to set.
     */
    public void setPasswordCallbackClass(String passwordCallbackClass) {
        this.passwordCallbackClass = passwordCallbackClass;
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

    /**
     * @return Returns the provideEntropy.
     */
    public boolean isProvideEntropy() {
        return provideEntropy;
    }

    /**
     * @param provideEntropy The provideEntropy to set.
     */
    public void setProvideEntropy(boolean provideEntropy) {
        this.provideEntropy = provideEntropy;
    }

    /**
     * @return Returns the crypto.
     */
    public Crypto getCrypto() {
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
     * @return Returns the msgCtx.
     */
    public MessageContext getMsgCtx() {
        return msgCtx;
    }
    
    /**
     * @return Returns the contextIdentifier.
     */
    public String getContextIdentifier() {
        return contextIdentifier;
    }

    /**
     * @param contextIdentifier The contextIdentifier to set.
     */
    protected void setContextIdentifier(String contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
    }

    /**
     * @return Returns the wstVersion.
     */
    public int getWstVersion() {
        return wstVersion;
    }

    /**
     * @param wstVersion The wstVersion to set.
     */
    public void setWstVersion(int wstVersion) {
        this.wstVersion = wstVersion;
    }
}
