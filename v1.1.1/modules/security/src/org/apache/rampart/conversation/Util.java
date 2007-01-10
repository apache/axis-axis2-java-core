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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.Base64;
import org.apache.axis2.util.Loader;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;
import org.apache.rampart.RampartException;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import java.util.Vector;

public class Util {

    /**
     * Returns the crypto instance of this configuration. If one is not
     * availabale then it will try to create a <code>Crypto</code> instance
     * using available configuration information and will set it as the
     * <code>Crypto</code> instance of the configuration.
     *
     * @param config
     * @return The crypto instance of this configuration
     * @throws RahasException
     */
    public static Crypto getCryptoInstace(ConversationConfiguration config)
            throws RampartException {
        if (config.getCrypto() != null) {
            return config.getCrypto();
        } else {
            Crypto crypto = null;
            if (config.getCryptoClassName() != null
                && config.getCryptoProperties() != null) {
                crypto = CryptoFactory.getInstance(config.getCryptoClassName(),
                                                   config.getCryptoProperties());
            } else if (config.getCryptoPropertiesFile() != null) {
                if (config.getClassLoader() != null) {
                    crypto = CryptoFactory
                            .getInstance(config.getCryptoPropertiesFile(),
                                         config.getClassLoader());
                } else {
                    crypto = CryptoFactory.getInstance(config
                            .getCryptoPropertiesFile());
                }
            } else {
                throw new RampartException("cannotCrateCryptoInstance");
            }
            config.setCrypto(crypto);
            return crypto;
        }
    }

    public static void processRSTR(OMElement rstr, ConversationConfiguration config)
            throws Exception {
        // Extract the SecurityContextToken

        String ns = null;

        OMElement rstElem =
                rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_02,
                                                     RahasConstants.IssuanceBindingLocalNames.
                                                             REQUESTED_SECURITY_TOKEN));
        if (rstElem != null) {
            ns = RahasConstants.WST_NS_05_02;
        } else {
            //At this point we certainthe version is the WS-SX version
            rstElem =
                    rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_12,
                                                         RahasConstants.IssuanceBindingLocalNames.
                                                                 REQUESTED_SECURITY_TOKEN));
            ns = RahasConstants.WST_NS_05_12;
        }
        Token token = null;
        if (rstElem != null) {
            OMElement sctElem = rstElem.getFirstElement();
            if (sctElem != null) {
                SecurityContextToken sct = new SecurityContextToken(
                        (Element) sctElem);
                token = new Token(sct.getIdentifier(), sctElem, rstr
                        .getFirstChildWithName(new QName(ns,
                                                         RahasConstants.IssuanceBindingLocalNames.
                                                                 LIFETIME)));
                resgisterContext(sct.getIdentifier(), config);
            } else {
                throw new RampartException("sctMissingInResponse");
            }
        } else {
            throw new TrustException("reqestedSecTokMissing");
        }

        // Process RequestedProofToken and extract the secret
        byte[] secret = null;
        OMElement rpt = rstr.getFirstChildWithName(new QName(ns,
                                                             RahasConstants.LocalNames.
                                                                     REQUESTED_PROOF_TOKEN));
        if (rpt != null) {
            OMElement elem = rpt.getFirstElement();

            if (WSConstants.ENC_KEY_LN.equals(elem.getLocalName())
                && WSConstants.ENC_NS.equals(elem.getNamespace().getNamespaceURI())) {
                // Handle the xenc:EncryptedKey case
                EncryptedKeyProcessor processor = new EncryptedKeyProcessor();
                processor.handleToken((Element) elem, null, Util
                        .getCryptoInstace(config),
                                      getCallbackHandlerInstance(config), null, new Vector(),
                                      null);
                secret = processor.getDecryptedBytes();
            } else if (RahasConstants.LocalNames.BINARY_SECRET.equals(elem.getLocalName()) &&
                       RahasConstants.WST_NS_05_02.equals(elem.getNamespace().getNamespaceURI()))
            {
                // Handle the wst:BinarySecret case
                secret = Base64.decode(elem.getText());
            } else {
                throw new TrustException("notSupported", new String[]{"{"
                                                                      + elem.getNamespace().getNamespaceURI() + "}"
                                                                      + elem.getLocalName()});
            }
        } else {
            throw new TrustException("rptMissing");
        }

        // Check for attached ref
        OMElement reqAttElem =
                rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_02,
                                                     RahasConstants.IssuanceBindingLocalNames.
                                                             REQUESTED_ATTACHED_REFERENCE));
        OMElement reqAttRef = reqAttElem == null ? null : reqAttElem
                .getFirstElement();

        OMElement reqUnattElem =
                rstr.getFirstChildWithName(new QName(RahasConstants.WST_NS_05_02,
                                                     RahasConstants.IssuanceBindingLocalNames.
                                                             REQUESTED_UNATTACHED_REFERENCE));
        OMElement reqUnattRef = reqUnattElem == null ? null : reqUnattElem
                .getFirstElement();

        token.setAttachedReference(reqAttRef);
        token.setUnattachedReference(reqUnattRef);
        token.setSecret(secret);
        config.getTokenStore().add(token);
    }

    private static CallbackHandler getCallbackHandlerInstance(
            ConversationConfiguration config) throws Exception {
        if (config.getPasswordCallbackRef() != null) {
            return config.getPasswordCallbackRef();
        } else if (config.getPasswordCallbackClass() != null) {
            if (config.getClassLoader() != null) {
                Class clazz = Loader.loadClass(config.getClassLoader(), config
                        .getPasswordCallbackClass());
                return (CallbackHandler) clazz.newInstance();
            } else {
                Class clazz = Loader.loadClass(config
                        .getPasswordCallbackClass());
                return (CallbackHandler) clazz.newInstance();
            }
        } else {
            throw new RampartException("noInfoForCBhandler");
        }
    }

    /**
     * This registers the security context mapping ?e context identifier to
     * the wsa:Action/soapAction or the service address, depending on the scope.
     *
     * @param identifier The security context identifier
     * @param config     The ConversationConfiguration instance
     * @throws RampartException If scope is "operation" and the wsa:Action is not available.
     *                          If scope is "service" and the wsa:To is missing.
     */
    public static void resgisterContext(String identifier, ConversationConfiguration config) throws RampartException {
        config.setContextIdentifier(identifier);

        if (config.getScope().equals(ConversationConfiguration.SCOPE_OPERATION)) {
            String action = config.getMsgCtx().getSoapAction();
            if (action != null) {
                config.getContextMap().put(action, identifier);
            } else {
                throw new RampartException("missingWSAAction");
            }
        } else {
            String to = config.getMsgCtx().getTo().getAddress();
            if (to != null) {
                config.getContextMap().put(to, identifier);
            } else {
                throw new RampartException("missingWSATo");
            }
        }
        //TODO
        //this.contextMap
    }

}
