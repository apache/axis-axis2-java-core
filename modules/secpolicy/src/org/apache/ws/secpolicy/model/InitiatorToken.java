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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

public class InitiatorToken extends AbstractSecurityAssertion implements TokenWrapper {
    
    private ArrayList initiatorTokens = null;
    
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

    /* (non-Javadoc)
     * @see org.apache.ws.security.policy.TokenWrapper#setToken(org.apache.ws.security.policy.Token)
     */
    public void setToken(Token tok) {
        this.setInitiatorToken(tok);
    }
    
    public List getConfigurations() {
        return initiatorTokens;
    }
    
    public InitiatorToken getDefaultConfiguration() {
        if (initiatorTokens != null) {
            return (InitiatorToken) initiatorTokens.get(0);
        }
        return null;
    }
    
    public void addConfiguration(InitiatorToken initiatorToken) {
        
        if (initiatorTokens == null) {
           initiatorTokens = new ArrayList();
       }
       initiatorTokens.add(initiatorToken);
    }

    public QName getName() {
        return Constants.INITIATOR_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    
    
    
}
