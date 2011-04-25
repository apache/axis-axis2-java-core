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
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.transport.http.HTTPWorkerFactory;
import org.apache.axis2.transport.http.server.SimpleHttpServer;
import org.apache.axis2.transport.http.server.WorkerFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.EndpointContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class EndpointImpl extends javax.xml.ws.Endpoint {

    private boolean published;
    private Object implementor;
    private EndpointDescription endpointDesc;
    private Binding binding;
    private SimpleHttpServer server;
    private List<Source> metadata;
    private Map<String, Object> properties;
    private Executor executor;
    private EndpointContext endpointCntx;

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
        	
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("initErr"));
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
        return this.metadata;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.Endpoint#setMetadata(java.util.List)
    */
    public void setMetadata(List<Source> list) {
        this.metadata = list;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.Endpoint#getProperties()
    */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.Endpoint#setProperties(java.util.Map)
    */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
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
        return this.executor;
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
     * @see javax.xml.ws.Endpoint#setEndpointContext(javax.xml.ws.EndpointContext)
     */
    public void setEndpointContext(EndpointContext ctxt) {
         this.endpointCntx = ctxt;
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
        if (isPublishDisabled()) {
            throw new UnsupportedOperationException("Endpoint publish not allowed in managed environment");
        }

    }

    /**
     * Answer if the Endpoint.publish methods have been disabled.  Per JSR-109 Section 5.3.3, the use of 
     * Endpoint.publish in a managed environment is non portable, and a managed environment may choose
     * to disable dynamic publishing of endpoints.  The default is that publishing is NOT disabled, unless
     * the property is set to true.  
     * @return true if publishing of enpdoints is disabled, false otherwise.  False is the default.
     */
    private boolean isPublishDisabled() {
        boolean publishDisabled = false;
        if (endpointDesc != null) {
            ConfigurationContext cfgCtx = endpointDesc.getServiceDescription().getAxisConfigContext();
            AxisConfiguration axisConfig = cfgCtx.getAxisConfiguration();
            Parameter parameter = axisConfig.getParameter(org.apache.axis2.jaxws.Constants.DISABLE_ENDPOINT_PUBLISH_METHODS);
            String flagValue = null;
            if (parameter != null) {
                flagValue = (String) parameter.getValue();
            }
    
            if (flagValue != null) {
                if ("false".equalsIgnoreCase(flagValue)) {
                    publishDisabled = false;
                } else if ("true".equalsIgnoreCase(flagValue)) {
                    publishDisabled = true;
                }
            }
        }
        return publishDisabled;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#publish(java.lang.String)
     */
    public void publish(String s) {
        if (isPublishDisabled()) {
            throw new UnsupportedOperationException("Endpoint publish not allowed in managed environment");
        }
        int port = -1;
        String address = s;
        try {
            URI uri = new URI(s);
            port = uri.getPort();
        } catch (URISyntaxException e) {
        }
        // Default to 8080
        if(port == -1){
            port = 8080;
            address = s + ":" + port;
        }
        ConfigurationContext ctx = endpointDesc.getServiceDescription().getAxisConfigContext();
        if (endpointDesc.getEndpointAddress() == null)
            endpointDesc.setEndpointAddress(address);
        
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
            server = new SimpleHttpServer(ctx, wf, port);
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
        this.executor = executor;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.Endpoint#stop()
     */
    public void stop() {
        try {
            if(server != null) {
                server.destroy();
            }
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    @Override
    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        if (!isPublished()) {
            throw new WebServiceException("Endpoint is not published");
        }
        
        if (!BindingUtils.isSOAPBinding(binding.getBindingID())) {
            throw new UnsupportedOperationException("This method is unsupported for the binding: " + binding.getBindingID());
        }
        
        EndpointReference jaxwsEPR = null;
        String addressingNamespace = EndpointReferenceUtils.getAddressingNamespace(clazz);
        String address = endpointDesc.getEndpointAddress();
        QName serviceName = endpointDesc.getServiceQName();
        QName portName = endpointDesc.getPortQName();

        String wsdlLocation = null;
        if (metadata != null) {
            Source wsdlSource = metadata.get(0);
            if (wsdlSource != null) {   
                wsdlLocation = wsdlSource.getSystemId();
            }
        }
        
        org.apache.axis2.addressing.EndpointReference axis2EPR =
        	EndpointReferenceUtils.createAxis2EndpointReference(address, serviceName, portName, wsdlLocation, addressingNamespace);
        
        try {
            EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters);
            jaxwsEPR = EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("endpointRefCreationError"), e);
        }

        return clazz.cast(jaxwsEPR);
    }

    @Override
    public EndpointReference getEndpointReference(Element... referenceParameters) {
        return getEndpointReference(W3CEndpointReference.class, referenceParameters);
    }
}
