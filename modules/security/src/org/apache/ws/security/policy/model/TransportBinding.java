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
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.security.policy.Constants;

public class TransportBinding extends Binding {
    
    private TransportToken transportToken;
    
    private List transportBindings;
    
    /**
     * @return Returns the transportToken.
     */
    public TransportToken getTransportToken() {
        return transportToken;
    }

    /**
     * @param transportToken The transportToken to set.
     */
    public void setTransportToken(TransportToken transportToken) {
        this.transportToken = transportToken;
    }
    
    public List getOptions() {
        return transportBindings;
    }
    
    public void addOption(TransportBinding transportBinding) {
        if (transportBindings == null) {
            transportBindings = new ArrayList();
        }
        transportBindings.add(transportBinding);
    }

    public QName getName() {
        return Constants.TRANSPORT_BINDING;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

}
