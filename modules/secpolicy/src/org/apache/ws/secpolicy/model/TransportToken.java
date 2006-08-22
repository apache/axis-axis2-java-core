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
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.Assertion;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;


public class TransportToken implements Assertion {

    private Token transportToken;
    
    private ArrayList transportTokens = new ArrayList();

    /**
     * @return Returns the transportToken.
     */
    public Token getTransportToken() {
        return transportToken;
    }

    /**
     * @param transportToken The transportToken to set.
     */
    public void setTransportToken(Token transportToken) {
        this.transportToken = transportToken;
    }
    
    public Iterator getConfigurations() {
        return transportTokens.iterator();
    }
    
    public TransportToken getDefaultConfiguration() {
        if (transportTokens != null) {
            return (TransportToken) transportTokens.get(0);
        }
        return null;
    }
    
    public void addConfiguration(TransportToken transportToken) {
        transportTokens.add(transportToken);
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
        return Assertion.ASSERTION;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
        
    }
    
    
}
