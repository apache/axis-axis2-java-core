/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.core.MessageContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author rott
 * 
 * BaseMessageContext is the base class for the two handler message contexts:
 * SoapMessageContext and LogicalMessageContext.  It delegates everything up to
 * the MEPContext, which itself delegates to the requestMC or responseMC, as
 * appropriate.
 * 
 */
public class BaseMessageContext implements javax.xml.ws.handler.MessageContext {

    protected MessageContext messageCtx;
    
    /**
     * @param messageCtx
     */
    protected BaseMessageContext(MessageContext messageCtx) {
        this.messageCtx = messageCtx;
        
        // Install an an AttachmentsAdapter between the 
        // jaxws attachment standard properties and the
        // MessageContext Attachments implementation.
        AttachmentsAdapter.install(messageCtx);
        TransportHeadersAdapter.install(messageCtx);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        messageCtx.getMEPContext().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return messageCtx.getMEPContext().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return messageCtx.getMEPContext().containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return messageCtx.getMEPContext().entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return messageCtx.getMEPContext().get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return messageCtx.getMEPContext().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return messageCtx.getMEPContext().keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(String key, Object value) {
        return messageCtx.getMEPContext().put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends Object> t) {
        messageCtx.getMEPContext().putAll(t);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        return messageCtx.getMEPContext().remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return messageCtx.getMEPContext().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<Object> values() {
        return messageCtx.getMEPContext().values();
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.handler.MessageContext#getScope(java.lang.String)
     */
    public Scope getScope(String s) {
        return messageCtx.getMEPContext().getScope(s);
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.handler.MessageContext#setScope(java.lang.String, javax.xml.ws.handler.MessageContext.Scope)
     */
    public void setScope(String s, Scope scope) {
        messageCtx.getMEPContext().setScope(s, scope);
    }

}
