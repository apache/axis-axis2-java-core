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
package org.apache.axis2.jaxws.handler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.axis2.jaxws.core.MessageContext;

/**
 * The <tt>ProtectedMessageContext</tt> is the version of the MessageContext
 * that will be given to application handlers as the handler list 
 * is traversed.  Access to internal properties is limited by the protections
 * built into this context.
 * 
 */
public class ProtectedMessageContext implements javax.xml.ws.handler.MessageContext {
    
    private MessageContext msgContext;

    public ProtectedMessageContext() {
        //do nothing
    }
    
    public ProtectedMessageContext(MessageContext mc) {
        msgContext = mc;
    }
    
    public Scope getScope(String s) {
        return null;
    }

    public void setScope(String s, Scope scope) {
        
    }
    
    //--------------------------------------------------
    // java.util.Map methods
    //--------------------------------------------------
    
    public void clear() {
        msgContext.getProperties().clear();
    }

    public boolean containsKey(Object key) {
        return msgContext.getProperties().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return msgContext.getProperties().containsValue(value);
    }

    public Set entrySet() {
        return msgContext.getProperties().entrySet();
    }

    public Object get(Object key) {
        return msgContext.getProperties().get(key);
    }

    public boolean isEmpty() {
        return msgContext.getProperties().isEmpty();
    }

    public Set keySet() {
        return msgContext.getProperties().keySet();
    }

    public Object put(String key, Object value) {
        return msgContext.getProperties().put(key, value);
    }

    public void putAll(Map t) {
        msgContext.getProperties().putAll(t);        
    }

    public Object remove(Object key) {
        return msgContext.getProperties().remove(key);
    }

    public int size() {
        return msgContext.getProperties().size();
    }

    public Collection values() {
        return msgContext.getProperties().values();
    }
}
