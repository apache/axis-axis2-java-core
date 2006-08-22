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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#getName()
     */
    public QName getName() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.Assertion#normalize()
     */
    public PolicyComponent normalize() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.neethi.PolicyComponent#serialize(javax.xml.stream.XMLStreamWriter)
     */
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }
    
    
}
