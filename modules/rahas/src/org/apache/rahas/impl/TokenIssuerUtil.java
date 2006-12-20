/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rahas.impl;

import java.security.SecureRandom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.RahasData;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.conversation.dkalgo.P_SHA1;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class TokenIssuerUtil {

    public final static String ENCRYPTED_KEY = "EncryptedKey";
    public final static String BINARY_SECRET = "BinarySecret";

    public static byte[] getSharedSecret(RahasData data,
                                         int keyComputation,
                                         int keySize) throws TrustException {

        boolean reqEntrPresent = data.getRequestEntropy() != null;

        try {
            if (reqEntrPresent &&
                keyComputation != SAMLTokenIssuerConfig.KeyComputation.KEY_COMP_USE_OWN_KEY) {
                //If there is requestor entropy and if the issuer is not
                //configured to use its own key

                if (keyComputation ==
                    SAMLTokenIssuerConfig.KeyComputation.KEY_COMP_PROVIDE_ENT) {
                    data.setResponseEntropy(WSSecurityUtil.generateNonce(keySize / 8));
                    P_SHA1 p_sha1 = new P_SHA1();
                    return p_sha1.createKey(data.getRequestEntropy(),
                                            data.getResponseEntropy(),
                                            0,
                                            keySize / 8);
                } else {
                    //If we reach this its expected to use the requestor's
                    //entropy
                    return data.getRequestEntropy();
                }
            } else { // need to use a generated key
                return generateEphemeralKey(keySize);
            }
        } catch (WSSecurityException e) {
            throw new TrustException("errorCreatingSymmKey", e);
        } catch (ConversationException e) {
            throw new TrustException("errorCreatingSymmKey", e);
        }
    }

    public static void handleRequestedProofToken(RahasData data,
                                                 int wstVersion,
                                                 AbstractIssuerConfig config,
                                                 OMElement rstrElem,
                                                 Token token,
                                                 Document doc) throws TrustException {
        OMElement reqProofTokElem =
                TrustUtil.createRequestedProofTokenElement(wstVersion, rstrElem);

        if (config.keyComputation == AbstractIssuerConfig.KeyComputation.KEY_COMP_PROVIDE_ENT
            && data.getRequestEntropy() != null) {
            //If we there's requestor entropy and its configured to provide
            //entropy then we have to set the entropy value and
            //set the RPT to include a ComputedKey element

            OMElement respEntrElem = TrustUtil.createEntropyElement(wstVersion, rstrElem);
            String entr = Base64.encode(data.getResponseEntropy());
            OMElement binSecElem = TrustUtil.createBinarySecretElement(wstVersion,
                                                            respEntrElem,
                                                            RahasConstants.BIN_SEC_TYPE_NONCE);
            binSecElem.setText(entr);

            OMElement compKeyElem =
                    TrustUtil.createComputedKeyElement(wstVersion, reqProofTokElem);
            compKeyElem.setText(data.getWstNs() + RahasConstants.COMPUTED_KEY_PSHA1);
        } else {
            if (TokenIssuerUtil.ENCRYPTED_KEY.equals(config.proofKeyType)) {
                WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
                Crypto crypto;
                if (config.cryptoPropertiesElement != null) { // crypto props defined as elements
                    crypto = CryptoFactory.getInstance(TrustUtil.toProperties(config.cryptoPropertiesElement),
                                                       data.getInMessageContext().
                                                               getAxisService().getClassLoader());
                } else { // crypto props defined in a properties file
                    crypto = CryptoFactory.getInstance(config.cryptoPropertiesFile,
                                                       data.getInMessageContext().
                                                               getAxisService().getClassLoader());
                }

                encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
                try {
                    encrKeyBuilder.setUseThisCert(data.getClientCert());
                    encrKeyBuilder.prepare(doc, crypto);
                } catch (WSSecurityException e) {
                    throw new TrustException("errorInBuildingTheEncryptedKeyForPrincipal",
                                             new String[]{data.
                                                     getClientCert().getSubjectDN().getName()});
                }
                Element encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
                Element bstElem = encrKeyBuilder.getBinarySecurityTokenElement();
                if (bstElem != null) {
                    reqProofTokElem.addChild((OMElement) bstElem);
                }

                reqProofTokElem.addChild((OMElement) encryptedKeyElem);

                token.setSecret(encrKeyBuilder.getEphemeralKey());
            } else if (TokenIssuerUtil.BINARY_SECRET.equals(config.proofKeyType)) {
                byte[] secret = TokenIssuerUtil.getSharedSecret(data,
                                                                config.keyComputation,
                                                                config.keySize);
                OMElement binSecElem = TrustUtil.createBinarySecretElement(wstVersion,
                                                                           reqProofTokElem,
                                                                           null);
                binSecElem.setText(Base64.encode(secret));
                token.setSecret(secret);
            } else {
                throw new IllegalArgumentException(config.proofKeyType);
            }
        }
    }

    private static byte[] generateEphemeralKey(int keySize) throws TrustException {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] temp = new byte[keySize / 8];
            random.nextBytes(temp);
            return temp;
        } catch (Exception e) {
            throw new TrustException("errorCreatingSymmKey", e);
        }
    }

}
