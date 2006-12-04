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

public class InitiatorToken extends AbstractSecurityAssertion implements TokenWrapper {
    
    private Token initiatorToken;

    /**
     * @return Returns the initiatorToken.
     */
    public Token getInitiatorToken() {
        return initiatorToken;
    }

    /**
     * @param initiatorToken The initiatorToken to set.
     */
    public void setInitiatorToken(Token initiatorToken) {
        this.initiatorToken = initiatorToken;
    }

    public void setToken(Token tok) {
        this.setInitiatorToken(tok);
    }
    
    public QName getName() {
        return Constants.INITIATOR_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localName = Constants.INITIATOR_TOKEN.getLocalPart();
        String namespaceURI = Constants.INITIATOR_TOKEN.getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);

        if (prefix == null) {
            prefix = Constants.INITIATOR_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }
        
        // <sp:InitiatorToken>
        writer.writeStartElement(prefix, localName, namespaceURI);
        
        String pPrefix = writer.getPrefix(Constants.POLICY.getNamespaceURI());
        if (pPrefix == null) {
            pPrefix = Constants.POLICY.getPrefix();
            writer.setPrefix(pPrefix, Constants.POLICY.getNamespaceURI());
        }
        
        // <wsp:Policy>
        writer.writeStartElement(pPrefix, Constants.POLICY.getLocalPart(), Constants.POLICY.getNamespaceURI());

        Token token = getInitiatorToken();
        if (token == null) {
            throw new RuntimeException("InitiatorToken doesn't contain any token assertions");
        }
        token.serialize(writer);
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        // </sp:InitiatorToken>
        writer.writeEndElement();
    }
}
