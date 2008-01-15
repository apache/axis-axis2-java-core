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
package org.apache.axis2.jaxws.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;

import org.apache.axis2.jaxws.client.config.RespectBindingConfigurator;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.feature.ClientConfigurator;
import org.apache.axis2.jaxws.feature.ClientFramework;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.BindingProvider;

/**
 * Classes that would normally "implement javax.xml.ws.Binding"
 * should extend this class instead.
 */
public abstract class BindingImpl implements Binding {
    private static final ClientConfigurator RESPECT_BINDING_CONFIGURATOR =
        new RespectBindingConfigurator();

    // an unsorted list of handlers
    private List<Handler> handlers = null;

    protected String bindingId = null;

    private EndpointDescription endpointDesc;

    protected Set<String> roles = null;
    
    protected ClientFramework framework = null;

    protected static final String SOAP11_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    protected static final String SOAP12_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";

    public BindingImpl(EndpointDescription endpointDesc) {
        this.endpointDesc = endpointDesc;
        // client
        this.bindingId = endpointDesc.getClientBindingID();
        if (this.bindingId == null) {
            // server
            this.bindingId = endpointDesc.getBindingType();
        }
        
        framework = new ClientFramework();
        framework.addConfigurator(RespectBindingFeature.ID, RESPECT_BINDING_CONFIGURATOR);
    }

    public List<Handler> getHandlerChain() {
        if (handlers == null) {
            // non-null so client apps can manipulate
            handlers =
                    new HandlerResolverImpl(endpointDesc.getServiceDescription()).getHandlerChain(endpointDesc.getPortInfo());
        }
        return handlers;
    }

    public void setHandlerChain(List<Handler> list) {
        // handlers cannot be null so a client app can request and manipulate it
        if (list == null) {
            handlers = new ArrayList<Handler>(); // non-null, but rather
                                                    // empty
        } else {
            this.handlers = list;
        }
    }

    /**
     * @since JAX-WS 2.1
     */
    public String getBindingID() {
        return this.bindingId;
    }

    public void configure(MessageContext messageContext, BindingProvider provider) {
        framework.configure(messageContext, provider);
    }

    public WebServiceFeature getWebServiceFeature(String id) {
        return framework.getFeature(id);
    }

    public void setWebServiceFeatures(WebServiceFeature... features) {
        if (features != null) {
            for (WebServiceFeature feature : features) {
                framework.addFeature(feature);
            }
        }
    }
}
