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
package org.apache.axis2.jaxws.server.endpoint;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.transport.http.HTTPWorkerFactory;
import org.apache.axis2.transport.http.server.SimpleHttpServer;
import org.apache.axis2.transport.http.server.WorkerFactory;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class EndpointImpl extends javax.xml.ws.Endpoint {

    private boolean published;
    private Object implementor;
    private EndpointDescription endpointDesc;
    private Binding binding;
    private SimpleHttpServer server;

    public EndpointImpl(Object o) {
        implementor = o;
        initialize();
    }

    public EndpointImpl(Object o, Binding bnd, EndpointDescription ed) {
        implementor = o;
        endpointDesc = ed;
        initialize();
    }

    private void initialize() {
        if (implementor == null) {
            throw ExceptionFactory.makeWebServiceException("The implementor object cannot be null");
        }
        
        // If we don't have the necessary metadata, let's go ahead and
        // create it.
        if (endpointDesc == null) {        
            ServiceDescription sd = DescriptionFactory.createServiceDescription(implementor.getClass());
            endpointDesc = sd.getEndpointDescriptions_AsCollection().iterator().next();
        }
        
        if (endpointDesc != null && binding == null) {
            binding = BindingUtils.createBinding(endpointDesc);
        }
        
        published = false;
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
        return published;
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
        ConfigurationContext ctx = endpointDesc.getServiceDescription().getAxisConfigContext();

        try {
            // For some reason the AxisService has not been added to the ConfigurationContext
            // at this point, so we need to do it for the service to be available.
            AxisService svc = endpointDesc.getAxisService();
            ctx.getAxisConfiguration().addService(svc);
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }        

        // Remove the default "axis2" context root.
        ctx.setContextRoot("/");
                    
        WorkerFactory wf = new HTTPWorkerFactory();

        try {
            server = new SimpleHttpServer(ctx, wf, 8080);  //TODO: Add a configurable port
            server.init();            
            server.start();
        } catch (IOException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }

        published = true;      
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
        try {
            server.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        T jaxwsEPR = null;
        
        

        return jaxwsEPR;
    }

    @Override
    public EndpointReference getEndpointReference(Element... arg0) {
        return getEndpointReference(W3CEndpointReference.class, arg0);
    }
}
