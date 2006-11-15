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

package org.apache.ws.secpolicy.model;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

public class Wss10 extends AbstractSecurityAssertion {
    
    private boolean mustSupportRefKeyIdentifier;
    private boolean MustSupportRefIssuerSerial;
    private boolean MustSupportRefExternalURI;
    private boolean MustSupportRefEmbeddedToken;
    
    /**
     * @return Returns the mustSupportRefEmbeddedToken.
     */
    public boolean isMustSupportRefEmbeddedToken() {
        return MustSupportRefEmbeddedToken;
    }
    /**
     * @param mustSupportRefEmbeddedToken The mustSupportRefEmbeddedToken to set.
     */
    public void setMustSupportRefEmbeddedToken(boolean mustSupportRefEmbeddedToken) {
        MustSupportRefEmbeddedToken = mustSupportRefEmbeddedToken;
    }
    /**
     * @return Returns the mustSupportRefExternalURI.
     */
    public boolean isMustSupportRefExternalURI() {
        return MustSupportRefExternalURI;
    }
    /**
     * @param mustSupportRefExternalURI The mustSupportRefExternalURI to set.
     */
    public void setMustSupportRefExternalURI(boolean mustSupportRefExternalURI) {
        MustSupportRefExternalURI = mustSupportRefExternalURI;
    }
    /**
     * @return Returns the mustSupportRefIssuerSerial.
     */
    public boolean isMustSupportRefIssuerSerial() {
        return MustSupportRefIssuerSerial;
    }
    /**
     * @param mustSupportRefIssuerSerial The mustSupportRefIssuerSerial to set.
     */
    public void setMustSupportRefIssuerSerial(boolean mustSupportRefIssuerSerial) {
        MustSupportRefIssuerSerial = mustSupportRefIssuerSerial;
    }
    /**
     * @return Returns the mustSupportRefKeyIdentifier.
     */
    public boolean isMustSupportRefKeyIdentifier() {
        return mustSupportRefKeyIdentifier;
    }
    /**
     * @param mustSupportRefKeyIdentifier The mustSupportRefKeyIdentifier to set.
     */
    public void setMustSupportRefKeyIdentifier(boolean mustSupportRefKeyIdentifier) {
        this.mustSupportRefKeyIdentifier = mustSupportRefKeyIdentifier;
    }
    
    public QName getName() {
        return Constants.WSS10;
    }
    
    public PolicyComponent normalize() {
        return this;
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localname = Constants.WSS10.getLocalPart();
        String namespaceURI = Constants.WSS10.getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = Constants.WSS10.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }

        // <sp:Wss10>
        writer.writeStartElement(prefix, localname, namespaceURI);
        
        // xmlns:sp=".."
        writer.writeNamespace(prefix, namespaceURI);
        
        String pPrefix = writer.getPrefix(Constants.POLICY.getNamespaceURI());
        if (pPrefix == null) {
            writer.setPrefix(Constants.POLICY.getPrefix(), Constants.POLICY.getNamespaceURI());
        }
        
        // <wsp:Policy>
        writer.writeStartElement(prefix, Constants.POLICY.getLocalPart(), Constants.POLICY.getNamespaceURI());
        
        if (isMustSupportRefKeyIdentifier()) {
            // <sp:MustSupportRefKeyIdentifier />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_REF_KEY_IDENTIFIER.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isMustSupportRefIssuerSerial()) {
            // <sp:MustSupportRefIssuerSerial />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_REF_ISSUER_SERIAL.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isMustSupportRefExternalURI()) {
            // <sp:MustSupportRefExternalURI />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_REF_EXTERNAL_URI.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isMustSupportRefEmbeddedToken()) {
            // <sp:MustSupportRefEmbeddedToken />
            writer.writeStartElement(prefix, Constants.MUST_SUPPORT_REF_EMBEDDED_TOKEN.getLocalPart(), namespaceURI);
            writer.writeEndElement();

            
        }
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        // </sp:Wss10>
        writer.writeEndElement();

    }
}
