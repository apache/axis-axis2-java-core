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

public class RecipientToken extends AbstractSecurityAssertion implements TokenWrapper {
    
    private Token receipientToken;
   
    /**
     * @return Returns the receipientToken.
     */
    public Token getReceipientToken() {
        return receipientToken;
    }

    /**
     * @param receipientToken The receipientToken to set.
     */
    public void setReceipientToken(Token receipientToken) {
        this.receipientToken = receipientToken;
    }

    /* (non-Javadoc)
     * @see org.apache.ws.security.policy.TokenWrapper#setToken(org.apache.ws.security.policy.Token)
     */
    public void setToken(Token tok) {
        this.setReceipientToken(tok);
    }

    public QName getName() {
        return Constants.RECIPIENT_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String localName = Constants.RECIPIENT_TOKEN.getLocalPart();
        String namespaceURI = Constants.RECIPIENT_TOKEN.getNamespaceURI();

        String prefix = writer.getPrefix(namespaceURI);

        if (prefix == null) {
            prefix = Constants.RECIPIENT_TOKEN.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }
        
        // <sp:RecipientToken>
        writer.writeStartElement(prefix, localName, namespaceURI);
        
        String pPrefix = writer.getPrefix(Constants.POLICY.getNamespaceURI());
        if (pPrefix == null) {
            pPrefix = Constants.POLICY.getPrefix();
            writer.setPrefix(pPrefix, Constants.POLICY.getNamespaceURI());
        }
        
        // <wsp:Policy>
        writer.writeStartElement(pPrefix, Constants.POLICY.getLocalPart(), Constants.POLICY.getNamespaceURI());

        Token token = getReceipientToken();
        if (token == null) {
            throw new RuntimeException("RecipientToken doesn't contain any token assertions");
        }
        token.serialize(writer);
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        // </sp:RecipientToken>
        writer.writeEndElement();
    }    
}
