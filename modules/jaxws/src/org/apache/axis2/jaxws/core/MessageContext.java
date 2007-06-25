/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.core;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.util.MessageUtils;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * The <code>org.apache.axis2.jaxws.core.MessageContext</code> is an interface that extends the
 * JAX-WS 2.0 <code>javax.xml.ws.handler.MessageContext</code> defined in the spec.  This
 * encapsulates all of the functionality needed of the MessageContext for the other JAX-WS spec
 * pieces (the handlers for example) and also provides the needed bits of contextual information for
 * the rest of the JAX-WS implementation.
 * <p/>
 * Specifically, this is responsible for providing APIs so that the client and server implementation
 * portions can get to the Message, defined by the Message Model format and also any metadata that
 * is available.
 */
public class MessageContext {

    private InvocationContext invocationCtx;
    private org.apache.axis2.context.MessageContext axisMsgCtx;
    private Map<String, Object> properties;
    private EndpointDescription endpointDesc;
    private OperationDescription operationDesc;
    private QName operationName;    //FIXME: This should become the OperationDescription
    private Message message;
    private Mode mode;
    
    // TODO:  flag to set whether we delegate property setting up to the
    // axis2 options objecct or keep it local
    private boolean DELEGATE_TO_OPTIONS = true;
    
    /*
     * JAXWS runtime uses a request and response mc, but we need to know the pair.
     * We will use this mepCtx as a wrapper to the request and response message contexts
     * where the requestMC and responseMC have the same parent MEPContext to
     * preserve the relationship.
     */
    private MEPContext mepCtx;

    // If a local exception is thrown, the exception is placed on the message context.
    // It is not converted into a Message.
    private Throwable localException = null;

    public MessageContext() {
        axisMsgCtx = new org.apache.axis2.context.MessageContext();
        if (!DELEGATE_TO_OPTIONS) {
            properties = new HashMap<String, Object>();
        }
           
    }
    public MessageContext(org.apache.axis2.context.MessageContext mc) throws WebServiceException {
        if (!DELEGATE_TO_OPTIONS) {
            properties = new HashMap<String, Object>();
        }

        /*
         * Instead of creating a member MEPContext object every time, we will
         * rely on users of this MessageContext class to create a new
         * MEPContext and call setMEPContext(MEPContext)
         */
        
        if (mc != null) {
            axisMsgCtx = mc;
            message = MessageUtils.getMessageFromMessageContext(mc);
            if (message != null) {
                message.setMessageContext(this);
            }
        } else {
            axisMsgCtx = new org.apache.axis2.context.MessageContext();
        }
    }

    public InvocationContext getInvocationContext() {
        return invocationCtx;
    }

    public void setInvocationContext(InvocationContext ic) {
        invocationCtx = ic;
    }

    public Map<String, Object> getProperties() {
        if (DELEGATE_TO_OPTIONS) {
            return new ReadOnlyProperties(axisMsgCtx.getOptions().getProperties());
        }
        return properties;
    }
    
    public void setProperties(Map<String, Object> _properties) {
        if (DELEGATE_TO_OPTIONS) {
            axisMsgCtx.getOptions().setProperties(_properties);
        } else {
            getProperties().putAll(_properties);
        }
    }
    
    public Object getProperty(String key) {
        if (DELEGATE_TO_OPTIONS) {
            return axisMsgCtx.getOptions().getProperty(key);
        }
        return getProperties().get(key);
    }
    
    // acts like Map.put(key, value)
    public Object setProperty(String key, Object value) {
        if (DELEGATE_TO_OPTIONS) {
            Object retval = axisMsgCtx.getOptions().getProperty(key);
            axisMsgCtx.getOptions().setProperty(key, value);
            return retval;
        } else {
            return getProperties().put(key, value);
        }
    }

    public EndpointDescription getEndpointDescription() {
        return endpointDesc;
    }

    public void setEndpointDescription(EndpointDescription ed) {
        endpointDesc = ed;
    }

    public OperationDescription getOperationDescription() {
        return operationDesc;
    }

    public void setOperationDescription(OperationDescription od) {
        operationDesc = od;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode m) {
        mode = m;
    }

    //FIXME: This should become the OperationDescription
    public QName getOperationName() {
        return operationName;
    }

    //FIXME: This should become the OperationDescription
    public void setOperationName(QName op) {
        operationName = op;
    }

    public void setMessage(Message msg) {
        message = msg;
        msg.setMessageContext(this);
    }

    public Message getMessage() {
        return message;
    }

    public org.apache.axis2.context.MessageContext getAxisMessageContext() {
        return axisMsgCtx;
    }

    public ClassLoader getClassLoader() {
        AxisService svc = axisMsgCtx.getAxisService();
        if (svc != null)
            return svc.getClassLoader();
        else
            return null;
    }

    /**
     * Used to determine whether or not session state has been enabled.
     *
     * @return
     */
    public boolean isMaintainSession() {
        boolean maintainSession = false;

        Boolean value = (Boolean) getProperties().get(BindingProvider.SESSION_MAINTAIN_PROPERTY);
        if (value != null && value.booleanValue()) {
            maintainSession = true;
        }

        return maintainSession;
    }

    /**
     * The local exception is the Throwable object held on the Message from a problem that occurred
     * due to something other than the server.  In other words, no message ever travelled across
     * the wire.
     *
     * @return the Throwable object or null
     */
    public Throwable getLocalException() {
        return localException;
    }

    /**
     * The local exception is the Throwable object held on the Message from a problem that occurred
     * due to something other than the server.  In other words, no message ever travelled across the
     * wire.
     *
     * @param t
     * @see Throwable
     */
    public void setLocalException(Throwable t) {
        localException = t;
    }
    
    /**
     * Set the wrapper MEPContext.  Internally, this method also sets
     * the MEPContext's children so the pointer is bi-directional; you can
     * get the MEPContext from the MessageContext and vice-versa.
     * 
     * @param mepCtx
     */
    public void setMEPContext(MEPContext mepCtx) {
        if (this.mepCtx == null) {
            this.mepCtx = mepCtx;
            // and set parent's child pointer
            this.mepCtx.setResponseMessageContext(this);
        }
    }

    public MEPContext getMEPContext() {
        if (mepCtx == null) {
            setMEPContext(new MEPContext(this));
        }
        return mepCtx;
    }
    
    private class ReadOnlyProperties extends AbstractMap<String, Object> {
        
        private Map<String, Object> containedProps;
        
        public ReadOnlyProperties(Map containedProps) {
            this.containedProps = containedProps;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return new ReadOnlySet(containedProps.entrySet());
        }

        @Override
        public Set<String> keySet() {
            return new ReadOnlySet(containedProps.keySet());
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Object> values() {
            return new ReadOnlyCollection(containedProps.values());
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
}
