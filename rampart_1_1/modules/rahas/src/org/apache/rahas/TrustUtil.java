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

package org.apache.rahas;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Iterator;

public class TrustUtil {
    private static final String WSSE_NAMESPACE_URI =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String WSSE_PREFIX = "wsse";
    private static final QName PROVIDER = new QName("provider");
    private static final QName NAME = new QName("name");

    /**
     * Create a wsse:Reference element with the given uri and the value type
     *
     * @param doc
     * @param refUri
     * @param refValueType
     * @return Element
     */
    public static Element createSecurityTokenReference(Document doc,
                                                       String refUri, String refValueType) {

        Reference ref = new Reference(doc);
        ref.setURI(refUri);
        if (refValueType != null) {
            ref.setValueType(refValueType);
        }
        SecurityTokenReference str = new SecurityTokenReference(doc);
        str.setReference(ref);

        return str.getElement();
    }

    public static OMElement
            createRequestSecurityTokenResponseElement(int version,
                                                      OMElement parent) throws TrustException {
        return createOMElement(parent,
                               getWSTNamespace(version),
                               RahasConstants.LocalNames.REQUEST_SECURITY_TOKEN_RESPONSE,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement
            createRequestSecurityTokenResponseCollectionElement(int version,
                                                                OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                               RahasConstants.LocalNames.
                                       REQUEST_SECURITY_TOKEN_RESPONSE_COLLECTION,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createRequestedSecurityTokenElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                               RahasConstants.IssuanceBindingLocalNames.REQUESTED_SECURITY_TOKEN,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createRequestSecurityTokenElement(int version) throws TrustException {
        String ns = getWSTNamespace(version);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        return fac.
                createOMElement(RahasConstants.LocalNames.REQUEST_SECURITY_TOKEN,
                                ns,
                                RahasConstants.WST_PREFIX);
    }

    public static OMElement createRequestedProofTokenElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                               RahasConstants.LocalNames.REQUESTED_PROOF_TOKEN,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createEntropyElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                               RahasConstants.IssuanceBindingLocalNames.ENTROPY,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createComputedKeyElement(int version,
                                                     OMElement parent) throws TrustException {
        return createOMElement(parent,
                               getWSTNamespace(version),
                               RahasConstants.IssuanceBindingLocalNames.COMPUTED_KEY,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createRequestTypeElement(int version,
                                                     OMElement parent,
                                                     String value) throws TrustException {
        String ns = getWSTNamespace(version);

        OMElement elem = createOMElement(parent,
                                         ns,
                                         RahasConstants.LocalNames.REQUEST_TYPE,
                                         RahasConstants.WST_PREFIX);

        if (RahasConstants.REQ_TYPE_ISSUE.equals(value)
            || RahasConstants.REQ_TYPE_CANCEL.equals(value)
            || RahasConstants.REQ_TYPE_RENEW.equals(value)
            || RahasConstants.REQ_TYPE_VALIDATE.equals(value)) {
            elem.setText(ns + value);
        } else {
            elem.setText(value);
        }

        return elem;
    }

    public static OMElement createTokenTypeElement(int version,
                                                   OMElement parent) throws TrustException {
        return createOMElement(parent,
                               getWSTNamespace(version),
                               RahasConstants.LocalNames.TOKEN_TYPE,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createBinarySecretElement(
            int version,
            OMElement parent,
            String type) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent, ns,
                                         RahasConstants.LocalNames.BINARY_SECRET,
                                         RahasConstants.WST_PREFIX);
        if (type != null) {
            elem.addAttribute(elem.getOMFactory().createOMAttribute(
                    RahasConstants.ATTR_TYPE, null, ns + type));
        }
        return elem;
    }

    public static OMElement createComputedKeyAlgorithm(int version,
                                                       OMElement parent,
                                                       String algoId) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent,
                                         ns,
                                         RahasConstants.IssuanceBindingLocalNames.COMPUTED_KEY_ALGO,
                                         RahasConstants.WST_PREFIX);
        elem.setText(ns + algoId);
        return elem;
    }

    public static OMElement
            createRequestedUnattachedRef(int version,
                                         OMElement parent,
                                         String refUri,
                                         String refValueType) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent, ns,
                                         RahasConstants.IssuanceBindingLocalNames.
                                                 REQUESTED_UNATTACHED_REFERENCE,
                                         RahasConstants.WST_PREFIX);
        elem.addChild((OMElement) createSecurityTokenReference(
                ((Element) parent).getOwnerDocument(), refUri, refValueType));
        return elem;
    }

    public static OMElement createRequestedAttachedRef(int version,
                                                       OMElement parent,
                                                       String refUri,
                                                       String refValueType) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent, ns,
                                         RahasConstants.IssuanceBindingLocalNames.
                                                 REQUESTED_ATTACHED_REFERENCE,
                                         RahasConstants.WST_PREFIX);
        elem.addChild((OMElement) createSecurityTokenReference(
                ((Element) parent).getOwnerDocument(), refUri, refValueType));
        return elem;
    }

    public static OMElement createKeySizeElement(int version,
                                                 OMElement parent,
                                                 int size) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement ksElem = createOMElement(parent, ns,
                                           RahasConstants.IssuanceBindingLocalNames.KEY_SIZE,
                                           RahasConstants.WST_PREFIX);
        ksElem.setText(Integer.toString(size));
        return ksElem;
    }

    public static OMElement createKeyTypeElement(int version,
                                                 OMElement parent,
                                                 String type) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement ktElem = createOMElement(parent, ns,
                                           RahasConstants.IssuanceBindingLocalNames.KEY_TYPE,
                                           RahasConstants.WST_PREFIX);
        if (RahasConstants.KEY_TYPE_BEARER.equals(type) ||
            RahasConstants.KEY_TYPE_PUBLIC_KEY.equals(type) ||
            RahasConstants.KEY_TYPE_SYMM_KEY.equals(type)) {
            ktElem.setText(ns + type);
        } else {
            ktElem.setText(type);
        }
        return ktElem;
    }

    public static OMElement
            createRequestedTokenCanceledElement(int version,
                                                OMElement parent) throws TrustException {
        return createOMElement(parent,
                               getWSTNamespace(version),
                               RahasConstants.CancelBindingLocalNames.REQUESTED_TOKEN_CANCELED,
                               RahasConstants.WST_PREFIX);
    }

    public static OMElement createLifetimeElement(int version,
                                                  OMElement parent,
                                                  String created,
                                                  String expires) throws TrustException {

        String ns = getWSTNamespace(version);

        OMElement ltElem = createOMElement(parent, ns,
                                           RahasConstants.IssuanceBindingLocalNames.LIFETIME,
                                           RahasConstants.WST_PREFIX);

        OMElement createdElem = createOMElement(ltElem, WSConstants.WSU_NS,
                                                WSConstants.CREATED_LN,
                                                WSConstants.WSU_PREFIX);
        createdElem.setText(created);

        OMElement expiresElem = createOMElement(ltElem, WSConstants.WSU_NS,
                                                WSConstants.EXPIRES_LN,
                                                WSConstants.WSU_PREFIX);
        expiresElem.setText(expires);

        return ltElem;
    }

    public static OMElement createLifetimeElement(int version,
                                                  OMElement parent,
                                                  long ttl) throws TrustException {

        Date creationTime = new Date();
        Date expirationTime = new Date();
        expirationTime.setTime(creationTime.getTime() + ttl);

        DateFormat zulu = new XmlSchemaDateFormat();

        return createLifetimeElement(version,
                                     parent,
                                     zulu.format(creationTime),
                                     zulu.format(expirationTime));
    }

    public static OMElement createAppliesToElement(OMElement parent,
                                                   String address, String addressingNs) {
        OMElement appliesToElem = createOMElement(parent,
                                                  RahasConstants.WSP_NS,
                                                  RahasConstants.IssuanceBindingLocalNames.
                                                          APPLIES_TO,
                                                  RahasConstants.WSP_PREFIX);

        OMElement eprElem = createOMElement(appliesToElem,
                                            addressingNs,
                                            "EndpointReference",
                                            AddressingConstants.WSA_DEFAULT_PREFIX);
        OMElement addressElem = createOMElement(eprElem, addressingNs,
                                                AddressingConstants.EPR_ADDRESS,
                                                AddressingConstants.WSA_DEFAULT_PREFIX);
        addressElem.setText(address);

        return appliesToElem;
    }

    public static String getActionValue(int version, String action) throws TrustException {
        if (RahasConstants.RST_ACTION_ISSUE.equals(action) ||
            RahasConstants.RST_ACTION_CANCEL.equals(action) ||
            RahasConstants.RST_ACTION_RENEW.equals(action) ||
            RahasConstants.RST_ACTION_VALIDATE.equals(action) ||
            RahasConstants.RST_ACTION_SCT.equals(action) ||
            RahasConstants.RSTR_ACTION_ISSUE.equals(action) ||
            RahasConstants.RSTR_ACTION_CANCEL.equals(action) ||
            RahasConstants.RSTR_ACTION_RENEW.equals(action) ||
            RahasConstants.RSTR_ACTION_VALIDATE.equals(action) ||
            RahasConstants.RSTR_ACTION_SCT.equals(action)) {

            return getWSTNamespace(version) + action;
        }
        return action;
    }

    /**
     * Create a new <code>SOAPEnvelope</code> of the same version as the
     * SOAPEnvelope in the given <code>MessageContext</code>
     *
     * @param nsUri
     * @return SOAPEnvelope
     */
    public static SOAPEnvelope createSOAPEnvelope(String nsUri) {
        if (nsUri != null
            && SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsUri)) {
            return DOOMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } else {
            return DOOMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
        }
    }


    private static OMElement createOMElement(OMElement parent,
                                             String ns,
                                             String ln,
                                             String prefix) {
        return parent.getOMFactory().createOMElement(new QName(ns, ln, prefix),
                                                     parent);
    }

    public static String getWSTNamespace(int version) throws TrustException {
        switch (version) {
            case RahasConstants.VERSION_05_02:
                return RahasConstants.WST_NS_05_02;
            case RahasConstants.VERSION_05_12:
                return RahasConstants.WST_NS_05_12;
            default:
                throw new TrustException("unsupportedWSTVersion");
        }
    }

    public static int getWSTVersion(String ns) throws TrustException {
        if (RahasConstants.WST_NS_05_02.equals(ns)) {
            return RahasConstants.VERSION_05_02;
        } else if (RahasConstants.WST_NS_05_12.equals(ns)) {
            return RahasConstants.VERSION_05_12;
        } else {
            throw new TrustException("unsupportedWSTVersion");
        }
    }

    /**
     * Returns the token store.
     * If the token store is aleady available in the configuration context then
     * fetch it and return it. If not create a new one, hook it up in the
     * configuration context and return it
     *
     * @param msgCtx
     * @return the token store
     */
    public static TokenStorage getTokenStore(MessageContext msgCtx) {
        ConfigurationContext configCtx = msgCtx.getConfigurationContext();
        TokenStorage storage =
                (TokenStorage) configCtx.getProperty(TokenStorage.TOKEN_STORAGE_KEY);
        if (storage == null) {
            storage = new SimpleTokenStore();
            configCtx.setProperty(TokenStorage.TOKEN_STORAGE_KEY, storage);
        }
        return storage;
    }


    /**
     * Create an ephemeral key
     *
     * @return The generated ephemeral key
     * @throws TrustException
     */
    protected byte[] generateEphemeralKey(int keySize) throws TrustException {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] temp = new byte[keySize / 8];
            random.nextBytes(temp);
            return temp;
        } catch (Exception e) {
            throw new TrustException("Error in creating the ephemeral key", e);
        }
    }

    /**
     * Create an ephemeral key
     *
     * @return The generated ephemeral key
     * @throws TrustException
     */
    protected byte[] generateEphemeralKey(byte[] reqEnt,
                                          byte[] respEnt,
                                          String algo,
                                          int keySize) throws TrustException {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] temp = new byte[keySize / 8];
            random.nextBytes(temp);
            return temp;
        } catch (Exception e) {
            throw new TrustException("Error in creating the ephemeral key", e);
        }
    }

    public static OMElement createCancelTargetElement(int version,
                                                      OMElement parent) throws TrustException {
        return createOMElement(parent,
                               getWSTNamespace(version),
                               RahasConstants.CancelBindingLocalNames.CANCEL_TARGET,
                               RahasConstants.WST_PREFIX);

    }

    public static OMElement createCancelRequest(String tokenId,
                                                int version) throws TrustException {
        /*
       <wst:RequestSecurityToken>
            <wst:RequestType>
            http://schemas.xmlsoap.org/ws/2005/02/trust/Cancel
            </wst:RequestType>
            <wst:CancelTarget>
                    <o:SecurityTokenReference
                         xmlns:o="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                      <o:Reference URI="urn:uuid:8e6a3a95-fd1b-4c24-96d4-28e875025ff7"
                                   ValueType="http://schemas.xmlsoap.org/ws/2005/02/sc/sct" />
                    </o:SecurityTokenReference>
            </wst:CancelTarget>
        </wst:RequestSecurityToken>
        */
        OMElement rst = TrustUtil.createRequestSecurityTokenElement(version);
        TrustUtil.createRequestTypeElement(version, rst, RahasConstants.REQ_TYPE_CANCEL);
        OMElement cancelTargetEle = TrustUtil.createCancelTargetElement(version, rst);
        OMFactory factory = rst.getOMFactory();
        OMElement secTokenRefEle =
                factory.createOMElement(RahasConstants.CancelBindingLocalNames.SECURITY_TOKEN_REF,
                                        WSSE_NAMESPACE_URI,
                                        WSSE_PREFIX);
        OMElement refEle =
                factory.createOMElement(RahasConstants.CancelBindingLocalNames.REFERENCE,
                                        WSSE_NAMESPACE_URI,
                                        WSSE_PREFIX);
        refEle.addAttribute(factory.createOMAttribute(RahasConstants.CancelBindingLocalNames.URI,
                                                      null, tokenId));
        secTokenRefEle.addChild(refEle);
        cancelTargetEle.addChild(secTokenRefEle);

        return rst;
    }

    public static Properties toProperties(OMElement cryptoElem) {
        Properties properties = new Properties();

        /*
           Process an element similar to this;

                <crypto provider="org.apache.ws.security.components.crypto.Merlin">
                    <property name="org.apache.ws.security.crypto.merlin.keystore.type">jks</property>
                    <property name="org.apache.ws.security.crypto.merlin.file">sts.jks</property>
                    <property name="org.apache.ws.security.crypto.merlin.keystore.password">password</property>
                </crypto>
        */
        for (Iterator propIter = cryptoElem.getChildElements(); propIter.hasNext();) {
            OMElement propElem = (OMElement) propIter.next();
            String name = propElem.getAttribute(NAME).getAttributeValue().trim();
            String value = propElem.getText().trim();
            properties.setProperty(name, value);
        }
        properties.setProperty("org.apache.ws.security.crypto.provider",
                               cryptoElem.getAttribute(PROVIDER).getAttributeValue().trim());
        return properties;
    }
}
