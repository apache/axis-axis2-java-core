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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Base64;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.RahasData;
import org.apache.rahas.Token;
import org.apache.rahas.TokenIssuer;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.SecureRandom;

public class SCTIssuer implements TokenIssuer {

    public final static String ENCRYPTED_KEY = "EncryptedKey";

    public final static String COMPUTED_KEY = "ComputedKey";

    public final static String BINARY_SECRET = "BinarySecret";

    private String configFile;

    private OMElement configElement;

    private String configParamName;

    /**
     * Issue a SecuritycontextToken based on the wsse:Signature or
     * wsse:UsernameToken
     * 
     * This will support returning the SecurityContextToken with the following
     * types of wst:RequestedProof tokens:
     * <ul>
     * <li>xenc:EncryptedKey</li>
     * <li>wst:ComputedKey</li>
     * <li>wst:BinarySecret (for secure transport)</li>
     * </ul>
     */
    public SOAPEnvelope issue(RahasData data) throws TrustException {

        SCTIssuerConfig config = null;
        if (this.configElement != null) {
            config = SCTIssuerConfig
                    .load(configElement
                            .getFirstChildWithName(SCTIssuerConfig.SCT_ISSUER_CONFIG));
        }

        // Look for the file
        if (config == null && this.configFile != null) {
            config = SCTIssuerConfig.load(this.configFile);
        }

        // Look for the param
        if (config == null && this.configParamName != null) {
            Parameter param = data.getInMessageContext().getParameter(this.configParamName);
            if (param != null && param.getParameterElement() != null) {
                config = SCTIssuerConfig.load(param.getParameterElement()
                        .getFirstChildWithName(
                                SCTIssuerConfig.SCT_ISSUER_CONFIG));
            } else {
                throw new TrustException("expectedParameterMissing",
                        new String[] { this.configParamName });
            }
        }

        if (config == null) {
            throw new TrustException("missingConfiguration",
                    new String[] { SCTIssuerConfig.SCT_ISSUER_CONFIG
                            .getLocalPart() });
        }

        if (ENCRYPTED_KEY.equals(config.proofTokenType)) {
            SOAPEnvelope responseEnv = this.doEncryptedKey(config,data);
            return responseEnv;
        } else if (BINARY_SECRET.equals(config.proofTokenType)) {
            SOAPEnvelope responseEnv = this.doBinarySecret(config, data);
            return responseEnv;
        } else if (COMPUTED_KEY.equals(config.proofTokenType)) {
            // TODO
            throw new UnsupportedOperationException("TODO");
        } else {
            // TODO
            throw new UnsupportedOperationException("TODO: Default");
        }
    }

    private SOAPEnvelope doBinarySecret(SCTIssuerConfig config, RahasData data)
            throws TrustException {

        SOAPEnvelope env = TrustUtil.createSOAPEnvelope(data.getSoapNs());
        int wstVersion = data.getVersion();
        
        // Get the document
        Document doc = ((Element) env).getOwnerDocument();

        SecurityContextToken sct = new SecurityContextToken(doc);

        OMElement rstrElem = TrustUtil
                .createRequestSecurityTokenResponseElement(wstVersion, env
                        .getBody());

        OMElement rstElem = TrustUtil.createRequestedSecurityTokenElement(
                wstVersion, rstrElem);

        rstElem.addChild((OMElement) sct.getElement());

        if (config.addRequestedAttachedRef) {
            if (wstVersion == RahasConstants.VERSION_05_02) {
                TrustUtil.createRequestedAttachedRef(wstVersion, rstrElem, "#"
                        + sct.getID(), RahasConstants.V_05_02.TOK_TYPE_SCT);
            } else {
                TrustUtil.createRequestedAttachedRef(wstVersion, rstrElem, "#"
                        + sct.getID(), RahasConstants.V_05_12.TOK_TYPE_SCT);
            }
        }

        if (config.addRequestedUnattachedRef) {
            if (wstVersion == RahasConstants.VERSION_05_02) {
                TrustUtil.createRequestedUnattachedRef(wstVersion, rstrElem,
                        sct.getIdentifier(),
                        RahasConstants.V_05_02.TOK_TYPE_SCT);
            } else {
                TrustUtil.createRequestedUnattachedRef(wstVersion, rstrElem,
                        sct.getIdentifier(),
                        RahasConstants.V_05_12.TOK_TYPE_SCT);
            }
        }

        OMElement reqProofTok = TrustUtil.createRequestedProofTokenElement(
                wstVersion, rstrElem);

        OMElement binSecElem = TrustUtil.createBinarySecretElement(wstVersion,
                reqProofTok, null);

        byte[] secret = this.generateEphemeralKey();
        binSecElem.setText(Base64.encode(secret));

        // Store the tokens
        Token sctToken = new Token(sct.getIdentifier(), (OMElement) sct
                .getElement());
        sctToken.setSecret(secret);
        TrustUtil.getTokenStore(data.getInMessageContext()).add(sctToken);

        return env;
    }

    private SOAPEnvelope doEncryptedKey(SCTIssuerConfig config, RahasData data)
            throws TrustException {

        int wstVersion = data.getVersion();
        
        SOAPEnvelope env = TrustUtil.createSOAPEnvelope(data.getSoapNs());
        // Get the document
        Document doc = ((Element) env).getOwnerDocument();

        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        Crypto crypto = CryptoFactory.getInstance(config.cryptoPropertiesFile,
                data.getInMessageContext().getAxisService().getClassLoader());

        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        try {
            encrKeyBuilder.setUseThisCert(data.getClientCert());
            encrKeyBuilder.prepare(doc, crypto);
        } catch (WSSecurityException e) {
            throw new TrustException(
                    "errorInBuildingTheEncryptedKeyForPrincipal",
                    new String[] { data.getClientCert().getSubjectDN()
                            .getName() });
        }

        SecurityContextToken sct = new SecurityContextToken(doc);

        OMElement rstrElem = TrustUtil
                .createRequestSecurityTokenResponseElement(wstVersion, env
                        .getBody());

        OMElement rstElem = TrustUtil.createRequestedSecurityTokenElement(
                wstVersion, rstrElem);

        rstElem.addChild((OMElement) sct.getElement());

        if (config.addRequestedAttachedRef) {
            if (wstVersion == RahasConstants.VERSION_05_02) {
                TrustUtil.createRequestedAttachedRef(wstVersion, rstrElem, "#"
                        + sct.getID(), RahasConstants.V_05_02.TOK_TYPE_SCT);
            } else {
                TrustUtil.createRequestedAttachedRef(wstVersion, rstrElem, "#"
                        + sct.getID(), RahasConstants.V_05_12.TOK_TYPE_SCT);
            }
        }

        if (config.addRequestedUnattachedRef) {
            if (wstVersion == RahasConstants.VERSION_05_02) {
                TrustUtil.createRequestedUnattachedRef(wstVersion, rstrElem,
                        sct.getIdentifier(),
                        RahasConstants.V_05_02.TOK_TYPE_SCT);
            } else {
                TrustUtil.createRequestedUnattachedRef(wstVersion, rstrElem,
                        sct.getIdentifier(),
                        RahasConstants.V_05_12.TOK_TYPE_SCT);
            }
        }

        Element encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
        Element bstElem = encrKeyBuilder.getBinarySecurityTokenElement();

        OMElement reqProofTok = TrustUtil.createRequestedProofTokenElement(
                wstVersion, rstrElem);

        if (bstElem != null) {
            reqProofTok.addChild((OMElement) bstElem);
        }

        reqProofTok.addChild((OMElement) encryptedKeyElem);

        // Store the tokens
        Token sctToken = new Token(sct.getIdentifier(), (OMElement) sct
                .getElement());
        sctToken.setSecret(encrKeyBuilder.getEphemeralKey());
        TrustUtil.getTokenStore(data.getInMessageContext()).add(sctToken);

        return env;
    }

    public String getResponseAction(RahasData data) throws TrustException {
        if (RahasConstants.WST_NS_05_02.equals(data.getRstElement()
                .getNamespace().getNamespaceURI())) {
            return RahasConstants.V_05_02.RSTR_ACTON_SCT;
        } else {
            return RahasConstants.V_05_12.RSTR_ACTON_SCT;
        }
    }

    /**
     * @see org.apache.rahas.TokenIssuer#setConfigurationFile(java.lang.String)
     */
    public void setConfigurationFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * @see org.apache.rahas.TokenIssuer#setConfigurationElement(java.lang.String)
     */
    public void setConfigurationElement(OMElement configElement) {
        this.configElement = configElement;
    }

    /**
     * Create an ephemeral key
     * 
     * @return
     * @throws WSSecurityException
     */
    private byte[] generateEphemeralKey() throws TrustException {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] temp = new byte[16];
            random.nextBytes(temp);
            return temp;
        } catch (Exception e) {
            throw new TrustException("errorCreatingSymmKey", e);
        }
    }

    public void setConfigurationParamName(String configParamName) {
        this.configParamName = configParamName;
    }

}
