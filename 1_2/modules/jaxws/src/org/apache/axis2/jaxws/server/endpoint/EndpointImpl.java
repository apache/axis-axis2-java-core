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
package org.apache.axis2.jaxws.server.endpoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;

import org.apache.axis2.jaxws.binding.BindingImpl;
import org.apache.axis2.jaxws.description.EndpointDescription;

public class EndpointImpl extends javax.xml.ws.Endpoint {

    private Object implementor;
    private EndpointDescription endpointDesc;
    private Binding binding;
    
    public EndpointImpl(Object o) {
        implementor = o;
        initialize();
    }
    
    public EndpointImpl(Object o, EndpointDescription ed) {
        implementor = o;
        endpointDesc = ed;
        initialize();
    }
    
    private void initialize() {
        if (endpointDesc != null) {
            binding = new BindingImpl(endpointDesc.getBindingType());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#getMetadata()
     */
    public List<Source> getMetadata() {
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#setMetadata(java.util.List)
     */
    public void setMetadata(List<Source> list) {
        return;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#getProperties()
     */
    public Map<String, Object> getProperties() {
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#setProperties(java.util.Map)
     */
    public void setProperties(Map<String, Object> properties) {
        return;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#getBinding()
     */
    public Binding getBinding() {
        return binding;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#getExecutor()
     */
    public Executor getExecutor() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#getImplementor()
     */
    public Object getImplementor() {
        return implementor;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#isPublished()
     */
    public boolean isPublished() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#publish(java.lang.Object)
     */
    public void publish(Object obj) {
       
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#publish(java.lang.String)
     */
    public void publish(String s) {
        
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#setExecutor(java.util.concurrent.Executor)
     */
    public void setExecutor(Executor executor) {
        
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#stop()
     */
    public void stop() {
        
    }

}
