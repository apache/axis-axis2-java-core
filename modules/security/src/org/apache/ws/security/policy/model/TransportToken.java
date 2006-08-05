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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.Assertion;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.security.policy.Constants;


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
    
    public Iterator getOptions() {
        return transportTokens.iterator();
    }
    
    public void addOption(TransportToken transportToken) {
        transportTokens.add(transportToken);
    }

    public QName getName() {
        return new QName(Constants.SP_NS, "TransportToken");
    }

    public boolean isOptional() {
        // TODO Auto-generated method stub
        return false;
    }

    public PolicyComponent normalize() {
        // TODO Auto-generated method stub
        return null;
    }

    public short getType() {
        return Assertion.ASSERTION;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        // TODO Auto-generated method stub
        
    }
    
    
}
