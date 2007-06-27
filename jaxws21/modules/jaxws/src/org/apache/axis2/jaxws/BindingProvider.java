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

import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;
import org.apache.axis2.jaxws.addressing.factory.EndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceConverter;
import org.apache.axis2.jaxws.binding.BindingUtils;
import org.apache.axis2.jaxws.client.PropertyValidator;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.feature.WebServiceFeatureValidator;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

public class BindingProvider implements org.apache.axis2.jaxws.spi.BindingProvider {
    protected Map<String, Object> requestContext;

    protected Map<String, Object> responseContext;

    protected EndpointDescription endpointDesc;

    protected ServiceDelegate serviceDelegate;
    
    protected WebServiceFeatureValidator validator;
    
    //TODO: Is this the best place for this code?
    protected org.apache.axis2.addressing.EndpointReference epr;
    
    //TODO: Is this the best place for this code?
    protected String addressingNamespace;

    private Binding binding = null;
    
    private EndpointReferenceFactory eprFactory =
        (EndpointReferenceFactory) FactoryRegistry.getFactory(EndpointReferenceFactory.class);

    public BindingProvider(ServiceDelegate svcDelegate,
                           EndpointDescription epDesc,
                           org.apache.axis2.addressing.EndpointReference epr,
                           String addressingNamespace,
                           WebServiceFeature... features) {
        this.endpointDesc = epDesc;
        this.serviceDelegate = svcDelegate;
        this.epr = epr;
        this.addressingNamespace = addressingNamespace;
        this.validator = new WebServiceFeatureValidator(false, features);
        
        initialize();
    }

    /*
     * Initialize any objects needed by the BindingProvider
     */
    private void initialize() {
        requestContext = new ValidatingClientContext();
        responseContext = new ValidatingClientContext();
        
        // Setting standard property defaults for the request context
        requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.FALSE);
        requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        
        // Set the endpoint address
        String endpointAddress = (epr != null ) ? epr.getAddress() : endpointDesc.getEndpointAddress();        
        if (endpointAddress != null && !"".equals(endpointAddress)) {
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

    public EndpointReference getEndpointReference() {
        return getEndpointReference(W3CEndpointReference.class);
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
        T jaxwsEPR = null;
        String bindingID = endpointDesc.getClientBindingID();
        String addressingNamespace = eprFactory.getAddressingNamespace(clazz);
        
        if (BindingUtils.isSOAPBinding(bindingID)) {
            try {
                if (epr == null) {
                    String address = endpointDesc.getEndpointAddress();
                    org.apache.axis2.addressing.EndpointReference epr =
                        new org.apache.axis2.addressing.EndpointReference(address);
                    QName service = endpointDesc.getServiceQName();
                    QName port = endpointDesc.getPortQName();
                    URL wsdlURL = ((ServiceDescriptionWSDL) endpointDesc.getServiceDescription()).getWSDLLocation();
                    ServiceName serviceName = new ServiceName(service, port.getLocalPart());
                    WSDLLocation wsdlLocation = new WSDLLocation(port.getNamespaceURI(), wsdlURL.toString());
                    EndpointReferenceHelper.setServiceNameMetadata(epr, addressingNamespace, serviceName);
                    EndpointReferenceHelper.setWSDLLocationMetadata(epr, addressingNamespace, wsdlLocation);
                    this.epr = epr;
                    this.addressingNamespace = addressingNamespace;
                }
                else if (!addressingNamespace.equals(this.addressingNamespace)) {
                    String address = this.epr.getAddress();
                    org.apache.axis2.addressing.EndpointReference epr =
                        new org.apache.axis2.addressing.EndpointReference(address);
                    QName service = endpointDesc.getServiceQName();
                    QName port = endpointDesc.getPortQName();
                    URL wsdlURL = ((ServiceDescriptionWSDL) endpointDesc.getServiceDescription()).getWSDLLocation();
                    ServiceName serviceName = new ServiceName(service, port.getLocalPart());
                    WSDLLocation wsdlLocation = new WSDLLocation(port.getNamespaceURI(), wsdlURL.toString());
                    EndpointReferenceHelper.setServiceNameMetadata(epr, addressingNamespace, serviceName);
                    EndpointReferenceHelper.setWSDLLocationMetadata(epr, addressingNamespace, wsdlLocation);
                    EndpointReferenceHelper.transferReferenceParameters(this.epr, epr);
                    this.epr = epr;
                    this.addressingNamespace = addressingNamespace;                    
                }
            
                jaxwsEPR = clazz.cast(EndpointReferenceConverter.convertFromAxis2(epr, addressingNamespace));
            }
            catch (Exception e) {
                //TODO NLS enable.
                throw ExceptionFactory.makeWebServiceException("Error creating endpoint reference", e);
            }
        }
        else {
            //TODO NLS enable.
            throw new UnsupportedOperationException("This method is not supported with a binding of " + bindingID);
        }
        
        return jaxwsEPR;
    }
    
    public String getAddressingNamespace() {
        return addressingNamespace;
    }

    public org.apache.axis2.addressing.EndpointReference getAxis2EndpointReference() {
        return epr;
    }    

    public WebServiceFeatureValidator getWebServiceFeatureValidator() {
        return validator;
    }    
    
    /*
    * An inner class used to validate properties as they are set by the client.
    */
    class ValidatingClientContext extends Hashtable<String, Object> {
        /**
         * 
         */
        private static final long serialVersionUID = 3485112205801917858L;

        @Override
        public synchronized Object put(String key, Object value) {
            // super.put rightly throws a NullPointerException if key or value is null, so don't continue if that's the case
            if (value == null)
                return null;
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
