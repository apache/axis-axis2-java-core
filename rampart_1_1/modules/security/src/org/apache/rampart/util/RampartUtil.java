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

package org.apache.rampart.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.client.STSClient;
import org.apache.rampart.RampartException;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.IssuedToken;
import org.apache.ws.secpolicy.model.SecureConversationToken;
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.util.Loader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.crypto.KeyGenerator;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class RampartUtil {

    private static final String CRYPTO_PROVIDER = "org.apache.ws.security.crypto.provider";
    private static Log log = LogFactory.getLog(RampartUtil.class);
    

    public static CallbackHandler getPasswordCB(RampartMessageData rmd) throws RampartException {

        MessageContext msgContext = rmd.getMsgContext();
        RampartPolicyData rpd = rmd.getPolicyData();
        
        return getPasswordCB(msgContext, rpd);
    }

    /**
     * @param msgContext
     * @param rpd
     * @return The <code>CallbackHandler</code> instance
     * @throws RampartException
     */
    public static CallbackHandler getPasswordCB(MessageContext msgContext, RampartPolicyData rpd) throws RampartException {
        
        CallbackHandler cbHandler;

        if (rpd.getRampartConfig() != null && rpd.getRampartConfig().getPwCbClass() != null) {
            
            String cbHandlerClass = rpd.getRampartConfig().getPwCbClass();
            ClassLoader classLoader = msgContext.getAxisService().getClassLoader();
                
            log.debug("loading class : " + cbHandlerClass);
            
            Class cbClass;
            try {
                cbClass = Loader.loadClass(classLoader, cbHandlerClass);
            } catch (ClassNotFoundException e) {
                throw new RampartException("cannotLoadPWCBClass", 
                        new String[]{cbHandlerClass}, e);
            }
            try {
                cbHandler = (CallbackHandler) cbClass.newInstance();
            } catch (java.lang.Exception e) {
                throw new RampartException("cannotCreatePWCBInstance",
                        new String[]{cbHandlerClass}, e);
            }
        } else {
            cbHandler = (CallbackHandler) msgContext.getProperty(
                    WSHandlerConstants.PW_CALLBACK_REF);
            if(cbHandler == null) {
                Parameter param = msgContext.getParameter(
                        WSHandlerConstants.PW_CALLBACK_REF);
                cbHandler = (CallbackHandler)param.getValue();
            }
        }
        
        return cbHandler;
    }
    
    /**
     * Perform a callback to get a password.
     * <p/>
     * The called back function gets an indication why to provide a password:
     * to produce a UsernameToken, Signature, or a password (key) for a given
     * name.
     */
    public static WSPasswordCallback performCallback(CallbackHandler cbHandler,
                                               String username,
                                               int doAction)
            throws RampartException {

        WSPasswordCallback pwCb;
        int reason = 0;

        switch (doAction) {
        case WSConstants.UT:
        case WSConstants.UT_SIGN:
                reason = WSPasswordCallback.USERNAME_TOKEN;
                break;
            case WSConstants.SIGN:
                reason = WSPasswordCallback.SIGNATURE;
                break;
            case WSConstants.ENCR:
                reason = WSPasswordCallback.KEY_NAME;
                break;
        }
        pwCb = new WSPasswordCallback(username, reason);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = pwCb;
        /*
        * Call back the application to get the password
        */
        try {
            cbHandler.handle(callbacks);
        } catch (Exception e) {
            throw new RampartException("pwcbFailed", e);
        }
        return pwCb;
    }
    
    /**
     * Create the <code>Crypto</code> instance for encryption using information 
     * from the rampart configuration assertion
     * 
     * @param config
     * @return The <code>Crypto</code> instance to be used for encryption
     * @throws RampartException
     */
    public static Crypto getEncryptionCrypto(RampartConfig config, ClassLoader loader)
            throws RampartException {
        log.debug("Loading encryption crypto");
        
        if(config != null && config.getEncrCryptoConfig() != null) {
            CryptoConfig cryptoConfig = config.getEncrCryptoConfig();
            String provider = cryptoConfig.getProvider();
            log.debug("Usig provider: " + provider);
            Properties prop = cryptoConfig.getProp();
            prop.put(CRYPTO_PROVIDER, provider);
            return CryptoFactory.getInstance(prop, loader);
        } else {
            log.debug("Trying the signature crypto info");

            //Try using signature crypto infomation
            if(config != null && config.getSigCryptoConfig() != null) {
                CryptoConfig cryptoConfig = config.getSigCryptoConfig();
                String provider = cryptoConfig.getProvider();
                log.debug("Usig provider: " + provider);
                Properties prop = cryptoConfig.getProp();
                prop.put(CRYPTO_PROVIDER, provider);
                return CryptoFactory.getInstance(prop, loader);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Create the <code>Crypto</code> instance for signature using information 
     * from the rampart configuration assertion
     * 
     * @param config
     * @return The <code>Crypto</code> instance to be used for signature
     * @throws RampartException
     */
    public static Crypto getSignatureCrypto(RampartConfig config, ClassLoader loader)
            throws RampartException {
        log.debug("Loading Signature crypto");
        
        if(config != null && config.getSigCryptoConfig() != null) {
            CryptoConfig cryptoConfig = config.getSigCryptoConfig();
            String provider = cryptoConfig.getProvider();
            log.debug("Usig provider: " + provider);
            Properties prop = cryptoConfig.getProp();
            prop.put(CRYPTO_PROVIDER, provider);
            return CryptoFactory.getInstance(prop, loader);
        } else {
            return null;
        }
    }
    
    
    /**
     * figureout the key identifier of a give X509Token
     * @param token
     * @return The key identifier of a give X509Token
     * @throws RampartException
     */
    public static int getKeyIdentifier(X509Token token) throws RampartException {
        if (token.isRequireIssuerSerialReference()) {
            return WSConstants.ISSUER_SERIAL;
        } else if (token.isRequireThumbprintReference()) {
            return WSConstants.THUMBPRINT_IDENTIFIER;
        } else if (token.isRequireEmbeddedTokenReference()) {
            return WSConstants.BST_DIRECT_REFERENCE;
        } else {
            throw new RampartException(
                    "unknownKeyRefSpeficier");

        }
    }
    
    /**
     * Process a give issuer address element and return the address.
     * @param issuerAddress
     * @return The address of an issuer address element
     * @throws RampartException If the issuer address element is malformed.
     */
    public static String processIssuerAddress(OMElement issuerAddress) 
        throws RampartException {
        if(issuerAddress != null && issuerAddress.getText() != null && 
                !"".equals(issuerAddress.getText())) {
            return issuerAddress.getText().trim();
        } else {
            throw new RampartException("invalidIssuerAddress",
                    new String[] { issuerAddress.toString() });
        }
    }
    
    
    public static OMElement createRSTTempalteForSCT(int conversationVersion, 
            int wstVersion) throws RampartException {
        try {
            log.debug("Creating RSTTemplate for an SCT request");
            OMFactory fac = OMAbstractFactory.getOMFactory();
            
            OMNamespace wspNs = fac.createOMNamespace(Constants.SP_NS, "wsp");
            OMElement rstTempl = fac.createOMElement(
                    Constants.REQUEST_SECURITY_TOKEN_TEMPLATE.getLocalPart(),
                    wspNs);
            
            //Create TokenType element and set the value
            OMElement tokenTypeElem = TrustUtil.createTokenTypeElement(
                    wstVersion, rstTempl);
            String tokenType = ConversationConstants
                    .getWSCNs(conversationVersion)
                    + ConversationConstants.TOKEN_TYPE_SECURITY_CONTEXT_TOKEN;
            tokenTypeElem.setText(tokenType);
            
            return rstTempl;
        } catch (TrustException e) {
            throw new RampartException("errorCreatingRSTTemplateForSCT", e);
        } catch (ConversationException e) {
            throw new RampartException("errorCreatingRSTTemplateForSCT", e);
        }
    }
    

    public static int getTimeToLive(RampartMessageData messageData) {

        RampartConfig rampartConfig = messageData.getPolicyData().getRampartConfig();
        if(rampartConfig != null) {
        String ttl = rampartConfig.getTimestampTTL();
        int ttl_i = 0;
        if (ttl != null) {
            try {
                ttl_i = Integer.parseInt(ttl);
            } catch (NumberFormatException e) {
                ttl_i = messageData.getTimeToLive();
            }
        }
        if (ttl_i <= 0) {
            ttl_i = messageData.getTimeToLive();
        }
        return ttl_i;
        } else {
            return RampartConfig.DEFAULT_TIMESTAMP_TTL;
        }
    }
    
    /**
     * Obtain a security context token.
     * @param rmd
     * @param secConvTok
     * @return Return the SecurityContextidentifier of the token
     * @throws TrustException
     * @throws RampartException
     */
    public static String getSecConvToken(RampartMessageData rmd,
            SecureConversationToken secConvTok) throws TrustException,
            RampartException {
        String action = TrustUtil.getActionValue(
                rmd.getWstVersion(),
                RahasConstants.RST_ACTION_SCT);
        
        // Get sts epr
        OMElement issuerEpr = secConvTok.getIssuerEpr();
        String issuerEprAddress = rmd.getMsgContext().getTo().getAddress();
        if(issuerEpr != null) {
            issuerEprAddress = RampartUtil.processIssuerAddress(issuerEpr);
        }
        
        //Find SC version
        int conversationVersion = rmd.getSecConvVersion();
        
        OMElement rstTemplate = RampartUtil.createRSTTempalteForSCT(
                conversationVersion, 
                rmd.getWstVersion());
        
        Policy stsPolicy = null;

        //Try boot strap policy
        Policy bsPol = secConvTok.getBootstrapPolicy();
        
        if(bsPol != null) {
            log.debug("BootstrapPolicy found");
            bsPol.addAssertion(rmd.getPolicyData().getRampartConfig());
            stsPolicy = bsPol;
        } else {
            //No bootstrap policy use issuer policy
            log.debug("No bootstrap policy, using issuer policy");
            stsPolicy = rmd.getPolicyData().getIssuerPolicy();
        }
        
        String id = getToken(rmd, rstTemplate,
                issuerEprAddress, action, stsPolicy);
        
        log.debug("SecureConversationToken obtained: id=" + id);
        return id;
    }
    

    /**
     * Obtain an issued token.
     * @param rmd
     * @param issuedToken
     * @return The identifier of the issued token
     * @throws RampartException
     */
    public static String getIssuedToken(RampartMessageData rmd,
            IssuedToken issuedToken) throws RampartException {

        try {
            String action = TrustUtil.getActionValue(rmd.getWstVersion(),
                    RahasConstants.RST_ACTION_ISSUE);

            // Get sts epr
            String issuerEprAddress = RampartUtil.processIssuerAddress(issuedToken
                    .getIssuerEpr());

            OMElement rstTemplate = issuedToken.getRstTemplate();

            // Get STS policy
            Policy stsPolicy = rmd.getPolicyData().getIssuerPolicy();

            String id = getToken(rmd, rstTemplate, issuerEprAddress, action,
                    stsPolicy);

            log.debug("Issued token obtained: id=" + id);
            return id;
        } catch (TrustException e) {
            throw new RampartException("errorInObtainingToken", e);
        } 
    }
    
    /**
     * Request a token.
     * @param rmd
     * @param rstTemplate
     * @param issuerEpr
     * @param action
     * @param issuerPolicy
     * @return Return the identifier of the obtained token
     * @throws RampartException
     */
    public static String getToken(RampartMessageData rmd, OMElement rstTemplate,
            String issuerEpr, String action, Policy issuerPolicy) throws RampartException {

        try {
            Axis2Util.useDOOM(false);
            
            STSClient client = new STSClient(rmd.getMsgContext()
                    .getConfigurationContext());
            // Set request action
            client.setAction(action);
            
            client.setRstTemplate(rstTemplate);
    
            // Set crypto information
            Crypto crypto = RampartUtil.getSignatureCrypto(rmd.getPolicyData().getRampartConfig(), 
                    rmd.getMsgContext().getAxisService().getClassLoader());
            CallbackHandler cbh = RampartUtil.getPasswordCB(rmd);
            client.setCryptoInfo(crypto, cbh);
    
            // Get service policy
            Policy servicePolicy = rmd.getServicePolicy();
    
            // Get service epr
            String servceEprAddress = rmd.getMsgContext()
                    .getOptions().getTo().getAddress();
    
            //Make the request
            org.apache.rahas.Token rst = 
                client.requestSecurityToken(servicePolicy, 
                                            issuerEpr,
                                            issuerPolicy, 
                                            servceEprAddress);
            
            //Add the token to token storage
            rst.setState(Token.ISSUED);
            rmd.getTokenStorage().add(rst);
            Axis2Util.useDOOM(true);
            return rst.getId();
        } catch (Exception e) {
            throw new RampartException("errorInObtainingToken", e);
        }
    }

    public static String getSoapBodyId(SOAPEnvelope env) {
        return addWsuIdToElement(env.getBody());
    }
    
    public static String addWsuIdToElement(OMElement elem) {
        String id;
        OMAttribute idAttr = elem.getAttribute(new QName(WSConstants.WSU_NS, "Id"));
        if(idAttr != null) {
            id = idAttr.getAttributeValue();
        } else {
            //Add an id
            OMNamespace ns = elem.getOMFactory().createOMNamespace(
                    WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
            id = "Id-" + elem.hashCode();
            idAttr = elem.getOMFactory().createOMAttribute("Id", ns, id);
            elem.addAttribute(idAttr);
        }
        
        return id;
    }
    
    public static Element appendChildToSecHeader(RampartMessageData rmd,
            OMElement elem) {
        return appendChildToSecHeader(rmd, (Element)elem);
    }
    
    public static Element appendChildToSecHeader(RampartMessageData rmd,
            Element elem) {
        Element secHeaderElem = rmd.getSecHeader().getSecurityHeader();
        Node node = secHeaderElem.getOwnerDocument().importNode(
                        elem, true);
        return (Element)secHeaderElem.appendChild(node);
    }

    public static Element insertSiblingAfter(RampartMessageData rmd, Element child, Element sibling) {
        if(child == null) {
            return appendChildToSecHeader(rmd, sibling);
        } else {
            if(child.getOwnerDocument().equals(sibling.getOwnerDocument())) {
                ((OMElement)child).insertSiblingAfter((OMElement)sibling);
                return sibling;
            } else {
                Element newSib = (Element)child.getOwnerDocument().importNode(sibling, true);
                ((OMElement)child).insertSiblingAfter((OMElement)newSib);
                return newSib;
            }
        }
    }
    
    public static Element insertSiblingBefore(RampartMessageData rmd, Element child, Element sibling) {
        if(child == null) {
            return appendChildToSecHeader(rmd, sibling);
        } else {
            if(child.getOwnerDocument().equals(sibling.getOwnerDocument())) {
                ((OMElement)child).insertSiblingBefore((OMElement)sibling);
                return sibling;
            } else {
                Element newSib = (Element)child.getOwnerDocument().importNode(sibling, true);
                ((OMElement)child).insertSiblingBefore((OMElement)newSib);
                return newSib;
            }
        }
        
    }
    
    public static Vector getEncryptedParts(RampartMessageData rmd) {
        RampartPolicyData rpd =  rmd.getPolicyData();
        Vector parts = rpd.getEncryptedParts();
        SOAPEnvelope envelope = rmd.getMsgContext().getEnvelope();
        if(rpd.isEncryptBody()) {
            parts.add(new WSEncryptionPart(addWsuIdToElement(envelope.getBody()), "Content"));
        }
        
        return parts;
    }
    
    public static Vector getSignedParts(RampartMessageData rmd) {
        RampartPolicyData rpd =  rmd.getPolicyData();
        Vector parts = rpd.getSignedParts();
        SOAPEnvelope envelope = rmd
                            .getMsgContext().getEnvelope();
        if(rpd.isEntireHeadersAndBodySignatures()) {
            Iterator childElems = envelope.getHeader().getChildElements();
            while (childElems.hasNext()) {
                OMElement element = (OMElement) childElems.next();
                if(!element.getQName().equals(new QName(WSConstants.WSSE_NS, WSConstants.WSSE_LN)) &&
                        !element.getQName().equals(new QName(WSConstants.WSSE11_NS, WSConstants.WSSE_LN))) {
                    parts.add(new WSEncryptionPart(addWsuIdToElement(element)));
                }
            }
            parts.add(new WSEncryptionPart(addWsuIdToElement(envelope.getBody())));
            
        } else if(rpd.isEncryptBody()) {
            parts.add(new WSEncryptionPart(addWsuIdToElement(envelope.getBody())));
        }
        
        return parts;
    }
    
    public static KeyGenerator getEncryptionKeyGenerator(String symEncrAlgo) throws WSSecurityException {
        KeyGenerator keyGen;
        try {
            /*
             * Assume AES as default, so initialize it
             */
            keyGen = KeyGenerator.getInstance("AES");
            if (symEncrAlgo.equalsIgnoreCase(WSConstants.TRIPLE_DES)) {
                keyGen = KeyGenerator.getInstance("DESede");
            } else if (symEncrAlgo.equalsIgnoreCase(WSConstants.AES_128)) {
                keyGen.init(128);
            } else if (symEncrAlgo.equalsIgnoreCase(WSConstants.AES_192)) {
                keyGen.init(192);
            } else if (symEncrAlgo.equalsIgnoreCase(WSConstants.AES_256)) {
                keyGen.init(256);
            } else {
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new WSSecurityException(
                    WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e);
        }
        return keyGen;
    }
    
    /**
     * Creates the unique (reproducible) id for to hold the context identifier
     * of the message exchange.
     * @return Id to hold the context identifier in the message context
     */
    public static String getContextIdentifierKey(MessageContext msgContext) {
        return msgContext.getAxisService().getName();
    }
    
    
    /**
     * Returns the map of security context token identifiers
     * @return the map of security context token identifiers
     */
    public static Hashtable getContextMap(MessageContext msgContext) {
        //Fist check whether its there
        Object map = msgContext.getConfigurationContext().getProperty(
                ConversationConstants.KEY_CONTEXT_MAP);
        
        if(map == null) {
            //If not create a new one
            map = new Hashtable();
            //Set the map globally
            msgContext.getConfigurationContext().setProperty(
                    ConversationConstants.KEY_CONTEXT_MAP, map);
        }
        
        return (Hashtable)map;
    }
    
    public static boolean isTokenValid(RampartMessageData rmd, String id) throws RampartException {
        try {
            org.apache.rahas.Token token = rmd.getTokenStorage().getToken(id);
            return token!= null && token.getState() == org.apache.rahas.Token.ISSUED;
        } catch (TrustException e) {
            throw new RampartException("errorExtractingToken");
        } 
    }
    
    public static void setEncryptionUser(RampartMessageData rmd, WSSecEncryptedKey encrKeyBuilder) throws RampartException {
        RampartPolicyData rpd = rmd.getPolicyData();
        String encrUser = rpd.getRampartConfig().getEncryptionUser();
        if(encrUser == null || "".equals(encrUser)) {
            throw new RampartException("missingEncryptionUser");
        }
        if(encrUser.equals(WSHandlerConstants.USE_REQ_SIG_CERT)) {
            Object resultsObj = rmd.getMsgContext().getProperty(WSHandlerConstants.RECV_RESULTS);
            if(resultsObj != null) {
                encrKeyBuilder.setUseThisCert(getReqSigCert((Vector)resultsObj));
            } else {
                throw new RampartException("noSecurityResults");
            }
        } else {
            encrKeyBuilder.setUserInfo(encrUser);
        }
    }
    
    private static X509Certificate getReqSigCert(Vector results) {
        /*
        * Scan the results for a matching actor. Use results only if the
        * receiving Actor and the sending Actor match.
        */
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult =
                    (WSHandlerResult) results.get(i);

            Vector wsSecEngineResults = rResult.getResults();
            /*
            * Scan the results for the first Signature action. Use the
            * certificate of this Signature to set the certificate for the
            * encryption action :-).
            */
            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser =
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                if (wser.getAction() == WSConstants.SIGN) {
                    return wser.getCertificate();
                }
            }
        }
        
        return null;
    }
    
    public static String getRequestEncryptedKeyId(Vector results) {
        
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult =
                    (WSHandlerResult) results.get(i);

            Vector wsSecEngineResults = rResult.getResults();
            /*
            * Scan the results for the first Signature action. Use the
            * certificate of this Signature to set the certificate for the
            * encryption action :-).
            */
            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser =
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                if (wser.getAction() == WSConstants.ENCR && 
                        wser.getEncryptedKeyId() != null) {
                    return wser.getEncryptedKeyId();
                }
            }
        }
        
        return null;
    }
    
    public static byte[] getRequestEncryptedKeyValue(Vector results) {
        
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult =
                    (WSHandlerResult) results.get(i);

            Vector wsSecEngineResults = rResult.getResults();
            /*
            * Scan the results for the first Signature action. Use the
            * certificate of this Signature to set the certificate for the
            * encryption action :-).
            */
            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser =
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                if (wser.getAction() == WSConstants.ENCR && 
                        wser.getDecryptedKey() != null) {
                    return wser.getDecryptedKey();
                }
            }
        }
        
        return null;
    }
}
