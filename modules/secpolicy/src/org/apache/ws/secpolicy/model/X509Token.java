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

public class X509Token extends Token {

    private boolean requireKeyIdentifierReference;
    
    private boolean requireIssuerSerialReference;
    
    private boolean requireEmbeddedTokenReference;
    
    private boolean requireThumbprintReference;
    
    private String tokenVersionAndType;
    
    /**
     * @return Returns the requireEmbeddedTokenReference.
     */
    public boolean isRequireEmbeddedTokenReference() {
        return requireEmbeddedTokenReference;
    }

    /**
     * @param requireEmbeddedTokenReference The requireEmbeddedTokenReference to set.
     */
    public void setRequireEmbeddedTokenReference(
            boolean requireEmbeddedTokenReference) {
        this.requireEmbeddedTokenReference = requireEmbeddedTokenReference;
    }

    /**
     * @return Returns the requireIssuerSerialReference.
     */
    public boolean isRequireIssuerSerialReference() {
        return requireIssuerSerialReference;
    }

    /**
     * @param requireIssuerSerialReference The requireIssuerSerialReference to set.
     */
    public void setRequireIssuerSerialReference(boolean requireIssuerSerialReference) {
        this.requireIssuerSerialReference = requireIssuerSerialReference;
    }

    /**
     * @return Returns the requireKeyIdentifierReference.
     */
    public boolean isRequireKeyIdentifierReference() {
        return requireKeyIdentifierReference;
    }

    /**
     * @param requireKeyIdentifierReference The requireKeyIdentifierReference to set.
     */
    public void setRequireKeyIdentifierReference(
            boolean requireKeyIdentifierReference) {
        this.requireKeyIdentifierReference = requireKeyIdentifierReference;
    }

    /**
     * @return Returns the requireThumbprintReference.
     */
    public boolean isRequireThumbprintReference() {
        return requireThumbprintReference;
    }

    /**
     * @param requireThumbprintReference The requireThumbprintReference to set.
     */
    public void setRequireThumbprintReference(boolean requireThumbprintReference) {
        this.requireThumbprintReference = requireThumbprintReference;
    }

    /**
     * @return Returns the tokenVersionAndType.
     */
    public String getTokenVersionAndType() {
        return tokenVersionAndType;
    }

    /**
     * @param tokenVersionAndType The tokenVersionAndType to set.
     */
    public void setTokenVersionAndType(String tokenVersionAndType) {
        this.tokenVersionAndType = tokenVersionAndType;
    }

    public QName getName() {
        return Constants.X509_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localName = Constants.X509_TOKEN.getLocalPart();
        String namespaceURI = Constants.X509_TOKEN.getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);

        if (prefix == null) {
            prefix = Constants.X509_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }
            
        // <sp:X509Token> 
        writer.writeStartElement(prefix, localName, namespaceURI);
        
        String inclusion = getInclusion();
        
        if (inclusion != null) {
            writer.writeAttribute(prefix, namespaceURI, Constants.INCLUDE_TOKEN
                    .getLocalPart(), inclusion);
        }
        
        
        String pPrefix = writer.getPrefix(Constants.POLICY.getNamespaceURI());
        if (pPrefix == null) {
            pPrefix = Constants.POLICY.getPrefix();
            writer.setPrefix(pPrefix, Constants.POLICY.getNamespaceURI());
        }
        
        // <wsp:Policy>
        writer.writeStartElement(pPrefix, Constants.POLICY.getLocalPart(), Constants.POLICY.getNamespaceURI());
        
        if (isRequireKeyIdentifierReference()) {
            // <sp:RequireKeyIdentifierReference />
            writer.writeStartElement(prefix, Constants.REQUIRE_KEY_IDENTIFIRE_REFERENCE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isRequireIssuerSerialReference()) {
            // <sp:RequireIssuerSerialReference />
            writer.writeStartElement(prefix, Constants.REQUIRE_ISSUER_SERIAL_REFERENCE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isRequireEmbeddedTokenReference()) {
            // <sp:RequireEmbeddedTokenReference />
            writer.writeStartElement(prefix, Constants.REQUIRE_EMBEDDED_TOKEN_REFERENCE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isRequireThumbprintReference()) {
            // <sp:RequireThumbprintReference />
            writer.writeStartElement(prefix, Constants.REQUIRE_THUMBPRINT_REFERENCE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (tokenVersionAndType != null) {
            // <sp:WssX509V1Token10 /> | ..
            writer.writeStartElement(prefix, tokenVersionAndType, namespaceURI);
            writer.writeEndElement();
        }
        
        if(isDerivedKeys()) {
            // <sp:RequireDerivedKeys/>
            writer.writeStartElement(prefix, Constants.REQUIRE_DERIVED_KEYS.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        // </sp:X509Token>
        writer.writeEndElement();
    }
       
}
