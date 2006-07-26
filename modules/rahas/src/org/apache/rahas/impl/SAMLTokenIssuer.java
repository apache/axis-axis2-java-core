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

package org.apache.rahas.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TokenIssuer;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.EncryptionConstants;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAttribute;
import org.opensaml.SAMLAttributeStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLStatement;
import org.opensaml.SAMLSubject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

/**
 * Issuer to issue SAMl tokens
 */
public class SAMLTokenIssuer implements TokenIssuer {
    
    private String configParamName;
    private OMElement configElement;
    private String configFile;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#issue(org.apache.axiom.om.OMElement,
     *      org.apache.axis2.context.MessageContext)
     */
    public SOAPEnvelope issue(OMElement request, MessageContext inMsgCtx)
            throws TrustException {


        /*
         * User can be identifier using a UsernameToken or a certificate
         *  - If a certificate is found then we use that to 
         *      - identify the user and 
         *      - encrypt the response (if required)
         *  - If a UsernameToken is found then we will not be encrypting the 
         *    response 
         */
        
        //Flag to identify whether we found a cert or not
        Principal principal = null;
        X509Certificate clientCert = null;
        
        Vector results = null;
        if ((results = (Vector) inMsgCtx
                .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            throw new TrustException(TrustException.REQUEST_FAILED);
        } else {

            for (int i = 0; i < results.size(); i++) {
                WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                Vector wsSecEngineResults = rResult.getResults();

                for (int j = 0; j < wsSecEngineResults.size(); j++) {
                    WSSecurityEngineResult wser = 
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                    if (wser.getAction() == WSConstants.SIGN
                            && wser.getPrincipal() != null) {
                        clientCert = wser.getCertificate();
                        principal = wser.getPrincipal();
                    } else if(wser.getAction() == WSConstants.UT
                            && wser.getPrincipal() != null){
                        principal = wser.getPrincipal();
                    }
                }
            }
            //If the principal is missing
            if(principal == null) {
                throw new TrustException(TrustException.REQUEST_FAILED);
            }
        }
        
        SAMLTokenIssuerConfig config = null;
        if(this.configElement != null) {
            config = SAMLTokenIssuerConfig
                    .load(configElement
                            .getFirstChildWithName(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG));
        } 

        //Look for the file
        if(config == null && this.configFile != null) {
            config = SAMLTokenIssuerConfig.load(this.configFile);
        }
        
        //Look for the param
        if(config == null && this.configParamName != null) {
            Parameter param = inMsgCtx.getParameter(this.configParamName);
            if(param != null && param.getParameterElement() != null) {
                config = SAMLTokenIssuerConfig.load(param.getParameterElement()
                        .getFirstChildWithName(
                                SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG));
            } else {
                throw new TrustException("expectedParameterMissing",
                        new String[] { this.configParamName });
            }
        }
        

        //Set the DOM impl to DOOM
        DocumentBuilderFactoryImpl.setDOOMRequired(true);

        SOAPEnvelope env = TrustUtil.createSOAPEnvelope(inMsgCtx.getEnvelope()
                .getNamespace().getNamespaceURI());

        Crypto crypto = CryptoFactory.getInstance(config.cryptoPropFile,
                inMsgCtx.getAxisService().getClassLoader());
        
        //Creation and expiration times
        Date creationTime = new Date();
        Date expirationTime = new Date();
        expirationTime.setTime(creationTime.getTime() + config.ttl);
        
        // Get the document
        Document doc = ((Element) env).getOwnerDocument();
        
        //Get the key size and create a new byte array of that size
        int keySize = TrustUtil.findKeySize(request);
        
        keySize = (keySize == -1) ? config.keySize : keySize;
        
        byte[] secret = new byte[keySize/8]; 
        
        /*
         * Find the KeyType
         * If the KeyType is SymmetricKey or PublicKey, issue a SAML HoK 
         * assertion.
         *      - In the case of the PublicKey, in coming security header 
         *      MUST contain a certificate (maybe via signature)
         *      
         * If the KeyType is Bearer then issue a Bearer assertion
         * 
         * If the key type is missing we will issue a HoK asserstion
         */ 
        
        String keyType = TrustUtil.findKeyType(request);
        String appliesToAddress = this.getServiceAddress(request);
        
        SAMLAssertion assertion = null;
        
        if(keyType == null) {
            throw new TrustException(TrustException.INVALID_REQUEST, new String[]{"Requested KeyType is missing"});
        }
        
        if(keyType.endsWith(RahasConstants.KEY_TYPE_SYMM_KEY) || 
                         keyType.endsWith(RahasConstants.KEY_TYPE_SYMM_KEY)) {
            assertion = createHoKAssertion(config, request, doc, crypto,
                    creationTime, expirationTime, keyType, secret);
        } else  if(keyType.endsWith(RahasConstants.KEY_TYPE_BEARER)) {
            //TODO Create bearer token
        } else {
            throw new TrustException("unsupportedKeyType");
        }
        
        OMElement rstrElem = null; 
        
        int version = TrustUtil.getWSTVersion(request.getNamespace().getNamespaceURI());
        
        if(RahasConstants.VERSION_05_02 == version) {
            rstrElem = TrustUtil
                .createRequestSecurityTokenResponseElement(version, env.getBody());
        } else {
            OMElement rstrcElem = TrustUtil
                    .createRequestSecurityTokenResponseCollectionElement(
                            version, env.getBody());
            
            rstrElem = TrustUtil
                .createRequestSecurityTokenResponseElement(version, rstrcElem);
        }
        
        TrustUtil.createtTokenTypeElement(version, rstrElem).setText(
                RahasConstants.TOK_TYPE_SAML_10);

        
        TrustUtil.createKeySizeElement(version, rstrElem, keySize);
        
        if (config.addRequestedAttachedRef) {
            TrustUtil.createRequestedAttachedRef(version, rstrElem, "#"
                    + assertion.getId(), RahasConstants.TOK_TYPE_SAML_10);
        }

        if (config.addRequestedUnattachedRef) {
            TrustUtil.createRequestedUnattachedRef(version, rstrElem, assertion
                    .getId(), RahasConstants.TOK_TYPE_SAML_10);
        }

        if(appliesToAddress != null) {
            TrustUtil.createAppliesToElement(rstrElem, appliesToAddress);
        }
        
        // Use GMT time in milliseconds
        DateFormat zulu = new XmlSchemaDateFormat();

        // Add the Lifetime element
        TrustUtil.createLifetimeElement(version, rstrElem, zulu
                .format(creationTime), zulu.format(expirationTime));
        
        //Create the RequestedSecurityToken element and add the SAML token to it
        OMElement reqSecTokenElem = TrustUtil
                .createRequestedSecurityTokenElement(version, rstrElem);
        try {
            Node tempNode = assertion.toDOM();
            reqSecTokenElem.addChild((OMNode) ((Element) rstrElem)
                    .getOwnerDocument().importNode(tempNode, true));

            // Store the token
            Token sctToken = new Token(assertion.getId(), (OMElement) assertion
                    .toDOM());
            // At this point we definitely have the secret
            // Otherwise it should fail with an exception earlier
            sctToken.setSecret(secret);
            TrustUtil.getTokenStore(inMsgCtx).add(sctToken);
            
        } catch (SAMLException e) {
            throw new TrustException("samlConverstionError", e);
        }

        //Add the RequestedProofToken
        OMElement reqProofTokElem = TrustUtil
                .createRequestedProofTokenElement(version, rstrElem);
        OMElement binSecElem = TrustUtil.createBinarySecretElement(version,
                reqProofTokElem, null);
        binSecElem.setText(Base64.encode(secret));
        
        // Unet the DOM impl to DOOM
        DocumentBuilderFactoryImpl.setDOOMRequired(false);
        return env;
    }
    

    /**
     * Uses the <code>wst:AppliesTo</code> to figure out the certificate to 
     * encrypt the secret in the SAML token 
     * @param request
     * @param config
     * @param crypto
     * @throws WSSecurityException
     * @return
     */
    private X509Certificate getServiceCert(OMElement request,
            SAMLTokenIssuerConfig config, Crypto crypto)
            throws WSSecurityException, TrustException {

        String address = this.getServiceAddress(request);
        
        if(address != null && !"".equals(address)) {
            String alias = (String)config.trustedServices.get(address);;
            return (X509Certificate)crypto.getCertificates(alias)[0];
        } else {
            //Return the STS cert
            return (X509Certificate)crypto.getCertificates(config.issuerKeyAlias)[0];
        }
        
    }

    
    private String getServiceAddress(OMElement request) throws TrustException {
        OMElement appliesToElem = request.getFirstChildWithName(
                new QName(RahasConstants.WSP_NS, RahasConstants.APPLIES_TO_LN));
        if(appliesToElem != null) {
            OMElement eprElem = appliesToElem.getFirstChildWithName(new QName(
                    RahasConstants.WSA_NS, RahasConstants.ENDPOINT_REFERENCE));
            if (eprElem != null) {
                OMElement addrElem = eprElem.getFirstChildWithName(new QName(
                        RahasConstants.WSA_NS, RahasConstants.ADDRESS));
                if (addrElem != null && addrElem.getText() != null && !"".equals(addrElem.getText().trim())) {
                    return addrElem.getText().trim();
                } else {
                    throw new TrustException("samlInvalidAppliesToElem");
                }
            } else {
                throw new TrustException("samlInvalidAppliesToElem");
            }
        }
        //If the AppliesTo element is missing
        return null;
    }
    
    private SAMLAssertion createHoKAssertion(SAMLTokenIssuerConfig config,
            OMElement request, Document doc, Crypto crypto, Date creationTime,
            Date expirationTime, String keyType, byte[] secret)
            throws TrustException {
        
        Element encryptedKeyElem = null;
        X509Certificate serviceCert = null;
        try {
            
            //Get ApliesTo to figureout which service to issue the token for
            serviceCert = getServiceCert(request, config, crypto);

            //Ceate the encrypted key
            WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
    
            //Use thumbprint id
            encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);

            //SEt the encryption cert
            encrKeyBuilder.setUseThisCert(serviceCert);
            
            //set keysize
            encrKeyBuilder.setKeySize(secret.length*8);
            
            //Set key encryption algo
            encrKeyBuilder.setKeyEncAlgo(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
            
            //Build
            encrKeyBuilder.prepare(doc, crypto);
            
            //Extract the base64 encoded secret value
            System.arraycopy(encrKeyBuilder.getEphemeralKey(), 0, secret, 0, secret.length);
            
            //Extract the Encryptedkey DOM element 
            encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
        } catch (WSSecurityException e) {
            throw new TrustException(
                    "errorInBuildingTheEncryptedKeyForPrincipal",
                    new String[] { serviceCert.getSubjectDN().getName()}, e);
        }
        return this.createAssertion(doc, encryptedKeyElem, 
                config, crypto, creationTime, expirationTime);
    }
    /**
     * Create the SAML assertion with the secret held in an 
     * <code>xenc:EncryptedKey</code>
     * @param doc
     * @param keyInfoContent
     * @param config
     * @param crypto
     * @param notBefore
     * @param notAfter
     * @return
     * @throws TrustException
     */
    private SAMLAssertion createAssertion(Document doc, 
                Element keyInfoContent, 
                SAMLTokenIssuerConfig config, 
                Crypto crypto,
                Date notBefore,
                Date notAfter) throws  TrustException {
        try {
            String[] confirmationMethods = new String[]{SAMLSubject.CONF_HOLDER_KEY};
            
            Element keyInfoElem = doc.createElementNS(WSConstants.SIG_NS, "KeyInfo");
            ((OMElement)keyInfoContent).declareNamespace(WSConstants.SIG_NS, WSConstants.SIG_PREFIX);
            ((OMElement)keyInfoContent).declareNamespace(WSConstants.ENC_NS, WSConstants.ENC_PREFIX);
            
            keyInfoElem.appendChild(keyInfoContent);
            
            SAMLSubject subject = new SAMLSubject(null, 
                    Arrays.asList(confirmationMethods),
                    null,
                    keyInfoElem);
            
            SAMLAttribute attribute = new SAMLAttribute("Name", 
                    "https://rahas.apache.org/saml/attrns", 
                    null, -1, Arrays.asList(new String[]{"Colombo/Rahas"}));
            SAMLAttributeStatement attrStmt = new SAMLAttributeStatement(
                    subject, Arrays.asList(new SAMLAttribute[] { attribute }));
            
            SAMLStatement[] statements = {attrStmt};
            
            SAMLAssertion assertion = new SAMLAssertion(config.issuerName, notBefore,
                    notAfter, null, null, Arrays.asList(statements));
            
            //sign the assertion
            X509Certificate[] issuerCerts =
                crypto.getCertificates(config.issuerKeyAlias);

            String sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA;
            String pubKeyAlgo =
                    issuerCerts[0].getPublicKey().getAlgorithm();
            if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_DSA;
            }
            java.security.Key issuerPK =
                    crypto.getPrivateKey(config.issuerKeyAlias,
                            config.issuerKeyPassword);
            assertion.sign(sigAlgo, issuerPK, Arrays.asList(issuerCerts));
            
            
            return assertion;
        } catch (Exception e) {
            throw new TrustException("samlAssertionCreationError", e);
        }
    }

    

    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#getResponseAction(org.apache.axiom.om.OMElement,
     *      org.apache.axis2.context.MessageContext)
     */
    public String getResponseAction(OMElement request, MessageContext inMsgCtx)
            throws TrustException {
        if(RahasConstants.WST_NS_05_02.equals(request.getNamespace().getNamespaceURI())) {
            return RahasConstants.V_05_02.RSTR_ACTON_ISSUE;
        } else {
            return RahasConstants.V_05_12.RSTR_ACTON_ISSUE;    
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#setConfigurationFile(java.lang.String)
     */
    public void setConfigurationFile(String configFile) {
        // TODO TODO SAMLTokenIssuer setConfigurationFile

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#setConfigurationElement(org.apache.axiom.om.OMElement)
     */
    public void setConfigurationElement(OMElement configElement) {
        // TODO TODO SAMLTokenIssuer setConfigurationElement
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#setConfigurationParamName(java.lang.String)
     */
    public void setConfigurationParamName(String configParamName) {
        this.configParamName = configParamName;
    }

}
