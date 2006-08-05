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

package org.apache.ws.security.policy.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.security.policy.Constants;

public class ProtectionToken extends AbstractSecurityAssertion implements TokenWrapper {
    
    private Token protectionToken;
    
    private List protectionTokens = new ArrayList();

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
    
    public Iterator getOptions() {
        return protectionTokens.iterator();
    }
    
    public void addOption(ProtectionToken protectionToken) {
        protectionTokens.add(protectionToken);
    }

    public QName getName() {
        return Constants.PROTECTION_TOKEN;
    }

    public PolicyComponent normalize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO Auto-generated method stub
        
    }
    
    
}
