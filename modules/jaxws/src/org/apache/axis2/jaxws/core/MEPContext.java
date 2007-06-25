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
package org.apache.axis2.jaxws.core;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.message.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The <tt>MEPContext</tt> is the version of the MessageContext
 * that will be given to application handlers as the handler list 
 * is traversed.  It is only to be used by application handlers.
 * 
 * The MEPContext object is constructed using a non-null request
 * context.  Once the request has been fully processed in the JAX-WS engine,
 * the response context should be set on this.  Since the response context
 * is always last, it takes priority in all MEPContext methods.
 * 
 */
public class MEPContext implements javax.xml.ws.handler.MessageContext {

    protected MessageContext requestMC;
    protected MessageContext responseMC;
    
    private Map<String, Scope> scopes;  // APPLICATION or HANDLER scope for properties
    
    /*
     * Flag to indicate whether we're being called from a handler or an application
     * (endpoint or client).  Users of MEPContext should use the 'is' and
     * 'set' appropriately for this flag.  The most likely scenario is to set the
     * flag to true after the server-side inbound handlers are complete.
     * 
     * TODO, all methods should use this flag to check for access rights
     */
    private boolean ApplicationAccessLocked = false;

    /*
     * Ideally this would be "protected", but we want the junit tests to see it.
     */
    public MEPContext(MessageContext requestMsgCtx) {
        this.requestMC = requestMsgCtx;
        scopes = new HashMap<String, Scope>();
        // make sure the MessageContext points back to this
        requestMsgCtx.setMEPContext(this);
    }
    
    public EndpointDescription getEndpointDesc() {
        if (responseMC != null) {
            return responseMC.getEndpointDescription();
        }
        return requestMC.getEndpointDescription();
    }

    public MessageContext getRequestMessageContext() {
        return requestMC;
    }
    
    public MessageContext getResponseMessageContext() {
        return responseMC;
    }
    
    public MessageContext getMessageContext() {
        if (responseMC != null) {
            return responseMC;
        }
        return requestMC;
    }
    
    protected void setResponseMessageContext(MessageContext responseMC) {
        // TODO does ApplicationAccessLocked mean anything here? -- method is protected, so probably not
        this.responseMC = responseMC;
        // if callers are being careful, the responseMC should not be set
        // until the engine is done invoking the endpoint, on both server and
        // client side.  At that point, we can start allowing callers access
        // to HANDLER scoped properties again.  Set the flag:
        ApplicationAccessLocked = false;
    }
    
    public void setMessage(Message msg) {
        if (responseMC != null) {
            responseMC.setMessage(msg);
        }
        else {
            requestMC.setMessage(msg);
        }
    }
    
    public Scope getScope(String s) {
        if (scopes.get(s) == null) {
            // JAX-WS default 9.4.1.  However, we try to set the scope for
            // every incoming property to HANDLER.  If a property is coming from
            // the axis2 Options bag, we want those to be APPLICATION scoped.
            return Scope.APPLICATION;
        }
        return scopes.get(s);
    }

    public void setScope(String s, Scope scope) {
        // TODO review next two lines
        if (isApplicationAccessLocked()) {  // endpoints are not allowed to change property scope.  They should all be APPLICATION scoped anyway
            return;
        }
        scopes.put(s, scope);
    }

    //--------------------------------------------------
    // java.util.Map methods
    //--------------------------------------------------

    public void clear() {
        // TODO review
        if (isApplicationAccessLocked()) {  // endpoints are allowed to clear APPLICATION scoped properties only
            Map<String, Object> appScopedProps = getApplicationScopedProperties();
            for(Iterator it = appScopedProps.keySet().iterator(); it.hasNext();) {
                String key = (String)it.next();
                remove(key);
                // TODO also remove Scope setting for "key"?  How?
            }
            return;
        }
        // yes, clear both
        if (responseMC != null) {
            responseMC.getProperties().clear();
        }
        requestMC.getProperties().clear();
    }

    public boolean containsKey(Object key) {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().containsKey(key);
        }
        if (responseMC != null) {
            boolean containsKey = responseMC.getProperties().containsKey(key) || requestMC.getProperties().containsKey(key);
            if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
                return containsKey;
            }
        }
        if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
            return requestMC.getProperties().containsKey(key);
        }
        return false;
    }

    public boolean containsValue(Object value) {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().containsValue(value);
        }
        if (responseMC != null) {
            return responseMC.getProperties().containsValue(value) || requestMC.getProperties().containsValue(value);
        }
        return requestMC.getProperties().containsValue(value);
    }

    public Set entrySet() {
        // TODO should check ApplicationAccessLocked flag
        // and return only APPLICATION scoped properties if true
        if (isApplicationAccessLocked()) {
            return new ReadOnlySet(getApplicationScopedProperties().entrySet());
        }
        Properties tempProps = new Properties();
        tempProps.putAll(requestMC.getProperties());
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return new ReadOnlySet(tempProps.entrySet());
    }

    public Object get(Object key) {
        if (responseMC != null) {
            if (responseMC.getProperties().get(key) != null) {
                if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
                    return responseMC.getProperties().get(key);
                }
            }
        }
        if ((getScope((String)key) == Scope.APPLICATION) || (!isApplicationAccessLocked())) {
            return requestMC.getProperties().get(key);
        }
        return null;
    }

    public boolean isEmpty() {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().isEmpty();
        }
        if (responseMC != null) {
            return requestMC.getProperties().isEmpty() && requestMC.getProperties().isEmpty();
        }
        return requestMC.getProperties().isEmpty();
    }

    public Set keySet() {
        if (isApplicationAccessLocked()) {
            return new ReadOnlySet(getApplicationScopedProperties().keySet());
        }
        Properties tempProps = new Properties();
        tempProps.putAll(requestMC.getProperties());
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return new ReadOnlySet(tempProps.keySet());
    }

    public Object put(String key, Object value) {
        // TODO careful:  endpoints may overwrite pre-existing key/value pairs.
        // Those key/value pairs may already have a scope attached to them, which
        // means an endpoint could "put" a property that is wrongly scoped
        if (scopes.get(key) == null) {  // check the scopes object directly, not through getScope()!!
            setScope(key, Scope.HANDLER);
        }
        if (requestMC.getProperties().containsKey(key)) {
            return requestMC.setProperty(key, value);
        }
        if (responseMC != null) {
            return responseMC.setProperty(key, value);
        }
        return requestMC.setProperty(key, value);
    }

    public void putAll(Map t) {
        // TODO similar problem as "put"
        for(Iterator it = t.entrySet().iterator(); it.hasNext();) {
            Entry<String, Object> entry = (Entry)it.next();
            if (getScope(entry.getKey()) == null) {
                setScope(entry.getKey(), Scope.HANDLER);
            }
        }
        if (responseMC != null) {
            responseMC.setProperties(t);
        }
        else {
            requestMC.setProperties(t);
        }
    }

    public Object remove(Object key) {
        // check ApplicationAccessLocked flag and prevent removal of HANDLER scoped props
        if (isApplicationAccessLocked()) {
            if (getScope((String)key).equals(Scope.HANDLER)) {
                return null;
            }
        }
        
        // yes, remove from both and return the right object
        Object retVal = null;
        if (responseMC != null) {
            retVal = responseMC.getProperties().remove(key);
        }
        if (retVal == null) {
            return requestMC.getProperties().remove(key);
        }
        else {
            requestMC.getProperties().remove(key);
        }
        return retVal;
    }

    public int size() {
        if (isApplicationAccessLocked()) {
            return getApplicationScopedProperties().size();
        }
        Properties tempProps = new Properties();
        tempProps.putAll(requestMC.getProperties());
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return tempProps.size();
    }

    public ReadOnlyCollection values() {
        if (isApplicationAccessLocked()) {
            return new ReadOnlyCollection(getApplicationScopedProperties().values());
        }
        Properties tempProps = new Properties();
        tempProps.putAll(requestMC.getProperties());
        if (responseMC != null) {
            tempProps.putAll(responseMC.getProperties());
        }
        return new ReadOnlyCollection(tempProps.values());
    }

    public Message getMessageObject() {
        // TODO does ApplicationAccessLocked apply here?
        if (responseMC != null) {
            return responseMC.getMessage();
        }
        return requestMC.getMessage();
    }
    
    public boolean isApplicationAccessLocked() {
        // since MEPContext is both a wrapper and a subclass, we need to be careful to set it only on the wrapper object:
        if (this == requestMC.getMEPContext()) {  // object compare, I am the wrapper object
            return ApplicationAccessLocked;
        }
        if (responseMC == null) {
            return requestMC.getMEPContext().isApplicationAccessLocked();
        }
        else {
            return responseMC.getMEPContext().isApplicationAccessLocked() || requestMC.getMEPContext().isApplicationAccessLocked();
        }
    }

    public void setApplicationAccessLocked(boolean applicationAccessLocked) {
        // since MEPContext is both a wrapper and a subclass, we need to be careful to set it only on the wrapper object:
        if (this == requestMC.getMEPContext()) {  // object compare, I am the wrapper object
            ApplicationAccessLocked = applicationAccessLocked;
        }
        else {
            requestMC.getMEPContext().setApplicationAccessLocked(applicationAccessLocked);
        }
            
    }
    
    /**
     * The returned tempMap should be used as a read-only map as changes to it will
     * not propogate into the requestMC or responseMC
     * 
     * Watch out for infinite loop if you call another method in this class that uses this method.
     * 
     * @return
     */
    public Map<String, Object> getApplicationScopedProperties() {
        Map<String, Object> tempMap = new HashMap<String, Object>();
        // better performance:
        if (!scopes.containsValue(Scope.APPLICATION)) {
            return tempMap;
        }
        for(Iterator it = requestMC.getProperties().keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            if ((getScope(key).equals(Scope.APPLICATION) && (requestMC.getProperties().containsKey(key)))) {
                tempMap.put(key, get(key));
            }
        }
        if (responseMC != null) {
            for(Iterator it = responseMC.getProperties().keySet().iterator(); it.hasNext();) {
                String key = (String)it.next();
                if ((getScope(key).equals(Scope.APPLICATION) && (responseMC.getProperties().containsKey(key)))) {
                    tempMap.put(key, get(key));
                }
            }
        }
        return tempMap;
    }
    
    
    /*
     * nested classes to be used to enforce read-only Collection, Set, and Iterator for MEPContext
     */
    
    class ReadOnlyCollection implements Collection {
        
        private Collection containedCollection;
        
        private ReadOnlyCollection(Collection containedCollection) {
            this.containedCollection = containedCollection;
        }
        
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return containedCollection.contains(o);
        }

        public boolean containsAll(Collection c) {
            return containedCollection.containsAll(c);
        }

        public boolean isEmpty() {
            return containedCollection.isEmpty();
        }

        public Iterator iterator() {
            return new ReadOnlyIterator(containedCollection.iterator());
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return containedCollection.size();
        }

        public Object[] toArray() {
            return containedCollection.toArray();
        }

        public Object[] toArray(Object[] a) {
            return containedCollection.toArray(a);
        }

    }
    
    class ReadOnlyIterator implements Iterator {
        
        private Iterator containedIterator;
        
        private ReadOnlyIterator(Iterator containedIterator) {
            this.containedIterator = containedIterator;
        }
        
        // override remove() to make this Iterator class read-only
        
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return containedIterator.hasNext();
        }

        public Object next() {
            return containedIterator.next();
        }
    }
    
    class ReadOnlySet implements Set {

        private Set containedSet;
        
        private ReadOnlySet(Set containedSet) {
            this.containedSet = containedSet;
        }
        
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return containedSet.contains(o);
        }

        public boolean containsAll(Collection c) {
            return containedSet.containsAll(c);
        }

        public boolean isEmpty() {
            return containedSet.isEmpty();
        }

        public Iterator iterator() {
            return new ReadOnlyIterator(containedSet.iterator());
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return containedSet.size();
        }

        public Object[] toArray() {
            return containedSet.toArray();
        }

        public Object[] toArray(Object[] a) {
            return containedSet.toArray(a);
        }
        
    }
    
}
