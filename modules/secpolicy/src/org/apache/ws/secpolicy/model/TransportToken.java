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

import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class TransportToken extends AbstractSecurityAssertion implements TokenWrapper {

    private Token transportToken;
    
    /**
     * @return Returns the transportToken.
     */
    public Token getTransportToken() {
        return transportToken;
    }
    
    public QName getName() {
        return new QName(Constants.SP_NS, "TransportToken");
    }

    public boolean isOptional() {
        throw new UnsupportedOperationException();
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public short getType() {
        return org.apache.neethi.Constants.TYPE_ASSERTION;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        
        String localName = Constants.TRANSPORT_TOKEN.getLocalPart();
        String namespaceURI = Constants.TRANSPORT_TOKEN.getNamespaceURI();
        
        String prefix = writer.getPrefix(namespaceURI);
        if (prefix == null) {
            writer.setPrefix(prefix, namespaceURI);
        }
        
        // <sp:TransportToken>
        
        writer.writeStartElement(prefix, localName, namespaceURI);
        
        String wspPrefix = writer.getPrefix(Constants.POLICY.getNamespaceURI());
        if (wspPrefix == null) {
            writer.setPrefix(wspPrefix, Constants.POLICY.getNamespaceURI());
        }
        
        // <wsp:Policy>
        writer.writeStartElement(Constants.POLICY.getPrefix(), Constants.POLICY.getLocalPart(), Constants.POLICY.getNamespaceURI());
        
        // serialization of the token ..
        transportToken.serialize(writer);
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        
        writer.writeEndElement();
        // </sp:TransportToken>
    }

    /* (non-Javadoc)
     * @see org.apache.ws.secpolicy.model.TokenWrapper#setToken(org.apache.ws.secpolicy.model.Token)
     */
    public void setToken(Token tok) {
        this.transportToken = tok;
    }
    
    
}
