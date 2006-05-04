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

package org.apache.axis2.security.trust;

import org.apache.axiom.om.OMElement;
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
            OMElement parent) {
        return createOMElement(parent,Constants.WST_NS,
                Constants.REQUEST_SECURITY_TOKEN_RESPONSE_LN,
                Constants.WST_PREFIX);
    }

    public static OMElement createRequestedSecurityTokenElement(OMElement parent) {
        return createOMElement(parent,Constants.WST_NS,
                Constants.REQUESTED_SECURITY_TOKEN_LN,
                Constants.WST_PREFIX);
    }

    public static OMElement createRequestedProofTokenElement(OMElement parent) {
        return createOMElement(parent, Constants.WST_NS,
                Constants.REQUESTED_PROOF_TOKEN_LN, Constants.WST_PREFIX);
    }
    
    public static OMElement createEntropyElement(OMElement parent) {
        return createOMElement(parent, Constants.WST_NS,
                Constants.ENTROPY, Constants.WST_PREFIX);
    }
    
    public static OMElement createBinarySecretElement(OMElement parent,
            String type) {
        OMElement elem = createOMElement(parent, Constants.WST_NS,
                Constants.BINARY_SECRET, Constants.WST_PREFIX);
        if(type != null) {
            elem.addAttribute(elem.getOMFactory().createOMAttribute(
                    Constants.ATTR_TYPE, null, type));
        }
        return elem;
    }
    
    public static OMElement createRequestedUnattachedRef(OMElement parent,
            String refUri, String refValueType) {
        OMElement elem = createOMElement(parent, Constants.WST_NS,
                            Constants.REQUESTED_UNATTACHED_REFERENCE,
                            Constants.WST_PREFIX);
        elem.addChild((OMElement) createSecurityTokenReference(
                ((Element) parent).getOwnerDocument(), refUri, refValueType));
        return elem;
    }
    
    public static OMElement createRequestedAttachedRef(OMElement parent,
            String refUri, String refValueType) {
        OMElement elem = createOMElement(parent, Constants.WST_NS,
                            Constants.REQUESTED_ATTACHED_REFERENCE,
                            Constants.WST_PREFIX);
        elem.addChild((OMElement) createSecurityTokenReference(
                ((Element) parent).getOwnerDocument(), refUri, refValueType));
        return elem;
    }
    
    public static OMElement createKeySizeElement(OMElement parent) {
        return createOMElement(parent, Constants.WST_NS,
                Constants.KEY_SIZE,
                Constants.WST_PREFIX);
    }
    
    private static OMElement createOMElement(OMElement parent, String ns,
            String ln, String prefix) {
        return parent.getOMFactory().createOMElement(new QName(ns, ln, prefix),
                parent);
    }
}
