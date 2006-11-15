/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.binding;

import java.net.URI;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;

/**
 * An implementation of the <link>javax.xml.ws.soap.SOAPBinding</link>
 * interface.  This is the default binding for JAX-WS, and will exist
 * for all Dispatch and Dynamic Proxy instances unless the XML/HTTP
 * Binding is explicitly specificied.
 */
public class SOAPBinding extends BindingImpl 
    implements javax.xml.ws.soap.SOAPBinding {

    private boolean mtomEnabled = false;
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.soap.SOAPBinding#getMessageFactory()
     */
    public MessageFactory getMessageFactory() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.soap.SOAPBinding#getRoles()
     */
    public Set<URI> getRoles() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.soap.SOAPBinding#getSOAPFactory()
     */
    public SOAPFactory getSOAPFactory() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.soap.SOAPBinding#isMTOMEnabled()
     */
    public boolean isMTOMEnabled() {
        return mtomEnabled;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.soap.SOAPBinding#setMTOMEnabled(boolean)
     */
    public void setMTOMEnabled(boolean flag) {
        mtomEnabled = flag;        
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.soap.SOAPBinding#setRoles(java.util.Set)
     */
    public void setRoles(Set<URI> set) {
        
    }

}
