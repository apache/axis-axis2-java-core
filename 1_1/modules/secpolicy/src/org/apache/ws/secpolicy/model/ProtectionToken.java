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

public class ProtectionToken extends AbstractSecurityAssertion implements TokenWrapper {
    
    private Token protectionToken;

    /**
     * @return Returns the protectionToken.
     */
    public Token getProtectionToken() {
        return protectionToken;
    }

    /**
     * @param protectionToken The protectionToken to set.
     */
    public void setProtectionToken(Token protectionToken) {
        this.protectionToken = protectionToken;
    }

    public void setToken(Token tok) {
        this.setProtectionToken(tok);
    }
    
    public QName getName() {
        return Constants.PROTECTION_TOKEN;
    }

    public PolicyComponent normalize() {
        /*
         *  ProtectionToken can not contain multiple values. Hence we consider it
         *  to always be in the normalized format.
         */
        return this;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localname = Constants.PROTECTION_TOKEN.getLocalPart();
        String namespaceURI = Constants.PROTECTION_TOKEN.getNamespaceURI();
        
        String prefix;
        
        String writerPrefix = writer.getPrefix(namespaceURI);
        if (writerPrefix == null) {
            prefix = Constants.PROTECTION_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
            
        } else {
            prefix = writerPrefix;
        }
        
        // <sp:ProtectionToken>
        writer.writeStartElement(prefix, localname, namespaceURI);
        
        if (writerPrefix == null) {
            // xmlns:sp=".."
            writer.writeNamespace(prefix, namespaceURI);
        }
        
        String policyLocalName = Constants.PROTECTION_TOKEN.getLocalPart();
        String policyNamespaceURI = Constants.PROTECTION_TOKEN.getNamespaceURI();
        
        String wspPrefix;
        
        String wspWriterPrefix = writer.getPrefix(policyNamespaceURI);
        
        if (wspWriterPrefix == null) {
            wspPrefix = Constants.PROTECTION_TOKEN.getPrefix();
            writer.setPrefix(wspPrefix, policyNamespaceURI);
        } else {
            wspPrefix = wspWriterPrefix;
        }
        
        // <wsp:Policy>
        writer.writeStartElement(wspPrefix, policyLocalName, policyNamespaceURI);
        
        if (wspWriterPrefix == null) {
            // xmlns:wsp=".."
            writer.writeNamespace(wspPrefix, policyNamespaceURI);
        }
        
        if (protectionToken == null) {
            throw new RuntimeException("ProtectionToken is not set");
        }
        
        protectionToken.serialize(writer);
        
        // </wsp:Policy>
        writer.writeEndElement();

        // </sp:ProtectionToken>
        writer.writeEndElement();
    }    
}
