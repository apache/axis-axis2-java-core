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
package org.apache.axis2.jaxws;

import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.client.PropertyValidator;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.HandlerResolver;

import java.util.Hashtable;
import java.util.Map;

public class BindingProvider implements org.apache.axis2.jaxws.spi.BindingProvider {

    protected Map<String, Object> requestContext;

    protected Map<String, Object> responseContext;

    protected EndpointDescription endpointDesc;

    protected ServiceDelegate serviceDelegate;

    private Binding binding = null;

    public BindingProvider(ServiceDelegate svcDelegate, EndpointDescription epDesc) {
        endpointDesc = epDesc;
        serviceDelegate = svcDelegate;

        initialize();
    }

    /*
     * Initialize any objects needed by the BindingProvider
     */
    private void initialize() {
        requestContext = new ValidatingClientContext();
        responseContext = new ValidatingClientContext();

        // Setting standard property defaults for the request context
        requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, new Boolean(false));
        requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, new Boolean(true));

        // Set the endpoint address
        String endpointAddress = endpointDesc.getEndpointAddress();
        if (endpointAddress != null) {
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        }
        
        // JAXWS 9.2.1.1 requires that we go ahead and create the binding object
        // so we can also set the handlerchain
        if (binding == null) {
            binding = BindingUtils.createBinding(endpointDesc);
            
            // TODO should we allow the ServiceDelegate to figure out the default handlerresolver?  Probably yes, since a client app may look for one there.
            HandlerResolver handlerResolver =
                    serviceDelegate.getHandlerResolver() != null ? serviceDelegate.getHandlerResolver()
                            : new HandlerResolverImpl(endpointDesc);
            binding.setHandlerChain(handlerResolver.getHandlerChain(endpointDesc.getPortInfo()));
        }

    }

    public ServiceDelegate getServiceDelegate() {
        return serviceDelegate;
    }

    public EndpointDescription getEndpointDescription() {
        return endpointDesc;
    }

    public Binding getBinding() {
        return binding;
    }
    
    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public Map<String, Object> getRequestContext() {
        return requestContext;
    }

    public Map<String, Object> getResponseContext() {
        return responseContext;
    }

    /**
     * Check for maintain session state enablement either in the
     * MessageContext.isMaintainSession() or in the ServiceContext properties.
     * 
     * @param mc
     * @param ic
     */
    protected void checkMaintainSessionState(MessageContext mc, InvocationContext ic) {
        Map<String, Object> properties = ic.getServiceClient().getServiceContext().getProperties();
        boolean bValue = false;

        if (properties != null
            && properties
                         .containsKey(javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY)) {
            bValue = (Boolean) properties
                .get(javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY);
        }
        if (mc.isMaintainSession() || bValue == true) {
            setupSessionContext(properties);
        }
    }

    /*
    * Ensure that the next request context contains the session value returned
    * from previous request
    */
    protected void setupSessionContext(Map<String, Object> properties) {
        String sessionKey = null;
        String sessionValue = null;

        if (properties == null) {
            return;
        }

        if (properties.containsKey(HTTPConstants.HEADER_LOCATION)) {
            sessionKey = HTTPConstants.HEADER_LOCATION;
            sessionValue = (String)properties.get(sessionKey);
            if (sessionValue != null && !"".equals(sessionValue)) {
                requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, sessionValue);
            }
        } else if (properties.containsKey(HTTPConstants.HEADER_COOKIE)) {
            sessionKey = HTTPConstants.HEADER_COOKIE;
            sessionValue = (String)properties.get(sessionKey);
            if (sessionValue != null && !"".equals(sessionValue)) {
                requestContext.put(HTTPConstants.COOKIE_STRING, sessionValue);
            }
        } else if (properties.containsKey(HTTPConstants.HEADER_COOKIE2)) {
            sessionKey = HTTPConstants.HEADER_COOKIE2;
            sessionValue = (String)properties.get(sessionKey);
            if (sessionValue != null && !"".equals(sessionValue)) {
                requestContext.put(HTTPConstants.COOKIE_STRING, sessionValue);
            }
        } else {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("NoMaintainSessionProperty"));
        }

        if (sessionValue == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("NullValueForMaintainSessionProperty", sessionKey));
        }
    }

    /**
     * Returns a boolean value representing whether or not a SOAPAction header should be sent with
     * the request.
     */
    protected boolean useSoapAction() {
        //TODO: Add some bit of validation for this property so that we know
        // it is actually a Boolean and not a String.
        Boolean use = (Boolean)requestContext.get(BindingProvider.SOAPACTION_USE_PROPERTY);
        if (use != null) {
            if (use.booleanValue()) {
                return true;
            } else {
                return false;
            }
        } else {
            // If the value is not set, then just default to sending a SOAPAction
            return true;
        }
    }

    /*
    * An inner class used to validate properties as they are set by the client.
    */
    class ValidatingClientContext extends Hashtable<String, Object> {

        @Override
        public synchronized Object put(String key, Object value) {
            if (PropertyValidator.validate(key, value)) {
                return super.put(key, value);
            } else {
                throw ExceptionFactory.makeWebServiceException(
                        Messages.getMessage("invalidPropValue", key, value.getClass().getName(),
                                            PropertyValidator.getExpectedValue(key).getName()));
            }
        }
    }
}
