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
import org.apache.axis2.context.MessageContext;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public class TrustUtil {
    
    /**
     * Create a wsse:Reference element with the given uri and the value type
     * @param doc
     * @param refUri
     * @param refValueType
     * @return
     */
    public static Element createSecurityTokenReference(Document doc,
            String refUri, String refValueType) {
        
        Reference ref = new Reference(doc);
        ref.setURI(refUri);
        if(refValueType != null) {
            ref.setValueType(refValueType);
        }
        SecurityTokenReference str = new SecurityTokenReference(doc);
        str.setReference(ref);
        
        return str.getElement();
    }
    
    public static OMElement createRequestSecurityTokenResponseElement(
            int version, 
            OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.REQUEST_SECURITY_TOKEN_RESPONSE_LN,
                RahasConstants.WST_PREFIX);
    }

    public static OMElement createRequestedSecurityTokenElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.REQUESTED_SECURITY_TOKEN_LN,
                RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createRequestSecurityTokenElement(
            int version) throws TrustException {
        String ns = getWSTNamespace(version);
        OMFactory fac = OMAbstractFactory.getOMFactory();
        return fac.createOMElement(RahasConstants.REQUEST_SECURITY_TOKEN_LN, ns, RahasConstants.WST_PREFIX);
    }

    public static OMElement createRequestedProofTokenElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.REQUESTED_PROOF_TOKEN_LN, RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createEntropyElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.ENTROPY_LN, RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createRequestTypeElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.REQUEST_TYPE_LN, RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createTokenTypeElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.TOKEN_TYPE_LN, RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createtTokenTypeElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.TOKEN_TYPE_LN, RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createBinarySecretElement(
            int version, 
            OMElement parent,
            String type) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent, ns,
                RahasConstants.BINARY_SECRET_LN, RahasConstants.WST_PREFIX);
        if(type != null) {
            elem.addAttribute(elem.getOMFactory().createOMAttribute(
                    RahasConstants.ATTR_TYPE, null, type));
        }
        return elem;
    }
    
    public static OMElement createRequestedUnattachedRef(
            int version, OMElement parent,
            String refUri, String refValueType) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent, ns,
                            RahasConstants.REQUESTED_UNATTACHED_REFERENCE_LN,
                            RahasConstants.WST_PREFIX);
        elem.addChild((OMElement) createSecurityTokenReference( 
                ((Element) parent).getOwnerDocument(), refUri, refValueType));
        return elem;
    }
    
    public static OMElement createRequestedAttachedRef(
            int version, OMElement parent,
            String refUri, String refValueType) throws TrustException {
        String ns = getWSTNamespace(version);
        OMElement elem = createOMElement(parent, ns,
                            RahasConstants.REQUESTED_ATTACHED_REFERENCE_LN,
                            RahasConstants.WST_PREFIX);
        elem.addChild((OMElement) createSecurityTokenReference(
                ((Element) parent).getOwnerDocument(), refUri, refValueType));
        return elem;
    }
    
    public static OMElement createKeySizeElement(
            int version, OMElement parent) throws TrustException {
        String ns = getWSTNamespace(version);
        return createOMElement(parent, ns,
                RahasConstants.KEY_SIZE_LN,
                RahasConstants.WST_PREFIX);
    }
    
    public static OMElement createLifetimeElement(
            int version, OMElement parent,
            String created, String expires) throws TrustException {
        
        String ns = getWSTNamespace(version);
        
        OMElement ltElem = createOMElement(parent, ns,
                RahasConstants.LIFETIME_LN,
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

    public static OMElement createAppliesToElement(OMElement parent) {
        return createOMElement(parent, RahasConstants.WSP_NS,
                RahasConstants.APPLIES_TO_LN,
                RahasConstants.WSP_PREFIX);
    }
    
    /**
     * Create a new <code>SOAPEnvelope</code> of the same version as the 
     * SOAPEnvelope in the given <code>MessageContext</code> 
     * @param msgCtx
     * @return
     */
    public static SOAPEnvelope createSOAPEnvelope(String nsUri) {
        if (nsUri != null
                && SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsUri)) {
            return DOOMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } else {
            return DOOMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
        }
    }

    
    private static OMElement createOMElement(OMElement parent, String ns,
            String ln, String prefix) {
        return parent.getOMFactory().createOMElement(new QName(ns, ln, prefix),
                parent);
    }
    
    public static String getWSTNamespace(int version) throws TrustException {
        switch (version){
            case RahasConstants.VERSION_05_02:
                return RahasConstants.WST_NS_05_02;
            case RahasConstants.VERSION_05_12:
                return RahasConstants.WST_NS_05_12;
            default:
                throw new TrustException("unsupportedWSTVersion");
        }
    }
    
    public static int getWSTVersion(String ns) {
        if(RahasConstants.WST_NS_05_02.equals(ns)) {
            return RahasConstants.VERSION_05_02;
        } else {
            return RahasConstants.VERSION_05_12;
        }
    }
    
    /**
     * Returns the token store.
     * If the token store is aleady available in the service context then
     * fetch it and return it. If not create a new one, hook it up in the 
     * service context and return it
     * @param msgCtx
     * @return
     */
    public static TokenStorage getTokenStore(MessageContext msgCtx) {
        String tempKey = TokenStorage.TOKEN_STORAGE_KEY
                                + msgCtx.getAxisService().getName();
        TokenStorage storage = (TokenStorage) msgCtx.getProperty(tempKey);
        if (storage == null) {
            storage = new SimpleTokenStore();
            msgCtx.getConfigurationContext().setProperty(tempKey, storage);
        }
        return storage;
    }
}
