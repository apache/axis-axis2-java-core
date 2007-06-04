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
package org.apache.axis2.jaxws.server.dispatcher;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.EndpointController;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivilegedExceptionAction;

/**
 * The ProviderDispatcher is used to invoke instances of a target endpoint that implement the {@link
 * javax.xml.ws.Provider} interface.
 * <p/>
 * The Provider<T> is a generic class, with certain restrictions on the parameterized type T.  This
 * implementation supports the following types:
 * <p/>
 * java.lang.String javax.activation.DataSource javax.xml.soap.SOAPMessage
 * javax.xml.transform.Source
 */
public class ProviderDispatcher extends JavaDispatcher {

    private static Log log = LogFactory.getLog(ProviderDispatcher.class);

    private BlockFactory blockFactory = null;
    private Class providerType = null;
    private Provider providerInstance = null;
    private Service.Mode providerServiceMode = null;
    private Message message = null;
    private Protocol messageProtocol;


    /**
     * Constructor
     *
     * @param _class
     * @param serviceInstance
     */
    public ProviderDispatcher(Class _class, Object serviceInstance) {
        super(_class, serviceInstance);
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.server.EndpointDispatcher#execute()
    */
    public MessageContext invoke(final MessageContext mc) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
        }

        providerInstance = getProviderInstance();

        // First we need to know what kind of Provider instance we're going
        // to be invoking against
        providerType = getProviderType();

        // REVIEW: This assumes there is only one endpoint description on the service.  Is that always the case?
        EndpointDescription endpointDesc = mc.getEndpointDescription();

        // Now that we know what kind of Provider we have, we can create the 
        // right type of Block for the request parameter data
        Object requestParamValue = null;
        Message message = mc.getMessage();
        if (message != null) {
            
            // Enable MTOM if indicated by the binding
            String bindingType = endpointDesc.getBindingType();
            if (bindingType != null) {
                if (bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                        bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                    message.setMTOMEnabled(true);
                }
            }

            // Save off the protocol info so we can use it when creating the response message.
            messageProtocol = message.getProtocol();
            // Determine what type blocks we want to create (String, Source, etc) based on Provider Type
            BlockFactory factory = createBlockFactory(providerType);


            providerServiceMode = endpointDesc.getServiceMode();

            if (providerServiceMode != null && providerServiceMode == Service.Mode.MESSAGE) {
                if (providerType.equals(SOAPMessage.class)) {
                    // We can get the SOAPMessage directly from the message itself
                    if (log.isDebugEnabled()) {
                        log.debug("Provider Type is SOAPMessage.");
                        log.debug("Number Message attachments=" + message.getAttachmentIDs().size());
                    }
                }

                requestParamValue = message.getValue(null, factory);
                if (requestParamValue == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "There are no elements to unmarshal.  ProviderDispatch will pass a null as input");
                    }
                }
            } else {
                // If it is not MESSAGE, then it is PAYLOAD (which is the default); only work with the body 
                Block block = message.getBodyBlock(null, factory);
                if (block != null) {
                    requestParamValue = block.getBusinessObject(true);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "No body blocks in SOAPMessage, Calling provider method with null input parameters");
                    }
                    requestParamValue = null;
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug(
                    "Provider Type = " + providerType + "; parameter type = " + requestParamValue);

        final Object input = providerType.cast(requestParamValue);
        if (input != null && log.isDebugEnabled()) {
            log.debug("Invoking Provider<" + providerType.getName() + "> with " +
                    "parameter of type " + input.getClass().getName());
        }
        if (input == null && log.isDebugEnabled()) {
            log.debug("Invoking Provider<" + providerType.getName() + "> with " +
                    "NULL input parameter");
        }

        // Invoke the actual Provider.invoke() method
        boolean faultThrown = false;
        XMLFault fault = null;
        Object responseParamValue = null;
        try {
            responseParamValue = (Object)org.apache.axis2.java.security.AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            return invokeProvider(mc, providerInstance, input);
                        }
                    });
        } catch (Exception e) {
            Throwable t = ClassUtils.getRootCause(e);
            faultThrown = true;
            fault = MethodMarshallerUtils.createXMLFaultFromSystemException(t);

            if (log.isDebugEnabled()) {
                log.debug("Marshal Throwable =" + e.getClass().getName());
                log.debug("  rootCause =" + t.getClass().getName());
                log.debug("  exception=" + t.toString());
            }
        }

        // Create the response MessageContext
        MessageContext responseMsgCtx = null;
        if (!EndpointController.isOneWay(mc.getAxisMessageContext())) {
            if (faultThrown) {
                // If a fault was thrown, we need to create a slightly different
                // MessageContext, than in the response path.
                Message responseMsg = createMessageFromValue(fault);
                responseMsgCtx = MessageContextUtils.createFaultMessageContext(mc);
                responseMsgCtx.setMessage(responseMsg);
            } else {
                Message responseMsg = createMessageFromValue(responseParamValue);

                // Enable MTOM if indicated by the binding
                String bindingType = endpointDesc.getBindingType();
                if (bindingType != null) {
                    if (bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                            bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                        responseMsg.setMTOMEnabled(true);
                    }
                }

                responseMsgCtx = MessageContextUtils.createResponseMessageContext(mc);
                responseMsgCtx.setMessage(responseMsg);
            }
        } else {
            // If we have a one-way operation, then we cannot create a MessageContext for the response.
            return null;
        }

        return responseMsgCtx;
    }

    protected Object invokeProvider(MessageContext ctx,
                                    Provider provider,
                                    Object input) throws Exception {
        return provider.invoke(input);
    }
    
    /**
     * Get the endpoint provider instance
     *
     * @return Provider
     * @throws Exception
     */
    public Provider getProvider() throws Exception {
        Provider p = getProviderInstance();
        setProvider(p);
        return p;
    }

    /**
     * Set the endpoint provider instance
     *
     * @param _provider
     */
    public void setProvider(Provider _provider) {
        this.providerInstance = _provider;
    }

    /**
     * Get the parameter for a given endpoint invocation
     *
     * @return
     * @throws Exception
     */
    public Message getMessage() throws Exception {
        return message;
    }

    /**
     * Set the parameter for a given endpoint invocation
     *
     * @param msg
     */
    public void setMessage(Message msg) {
        this.message = msg;
    }

    /*
    * Create a Message object out of the value object that was returned.
    */
    private Message createMessageFromValue(Object value) throws Exception {
        MessageFactory msgFactory =
                (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
        Message message = null;

        if (value != null) {
            BlockFactory factory = createBlockFactory(providerType);

            if (value instanceof XMLFault) {
                message = msgFactory.create(messageProtocol);
                message.setXMLFault((XMLFault)value);
            } else if (providerServiceMode != null && providerServiceMode == Service.Mode.MESSAGE) {
                // For MESSAGE mode, work with the entire message, Headers and Body
                // This is based on logic in org.apache.axis2.jaxws.client.XMLDispatch.createMessageFromBundle()
                if (value instanceof SOAPMessage) {
                    message = msgFactory.createFrom((SOAPMessage)value);
                } else {
                    Block block = factory.createFrom(value, null, null);
                    message = msgFactory.createFrom(block, null, messageProtocol);
                }
            } else {
                // PAYLOAD mode deals only with the body of the message.
                Block block = factory.createFrom(value, null, null);
                message = msgFactory.create(messageProtocol);
                message.setBodyBlock(block);
            }
        }

        if (message == null)
            // If we didn't create a message above (because there was no value), create one here
            message = msgFactory.create(messageProtocol);


        return message;
    }

    /*
      * Determine the Provider type for this instance
      */
    private Provider getProviderInstance() throws Exception {
        Class<?> clazz = getProviderType();

        if (!isValidProviderType(clazz)) {
            //TODO This will change once deployment code it in place
            throw new Exception(Messages.getMessage("InvalidProvider", clazz.getName()));
        }

        Provider provider = null;
        if (clazz == String.class) {
            provider = (Provider<String>)serviceInstance;
        } else if (clazz == Source.class) {
            provider = (Provider<Source>)serviceInstance;
        } else if (clazz == SOAPMessage.class) {
            provider = (Provider<SOAPMessage>)serviceInstance;
        } else if (clazz == JAXBContext.class) {
            provider = (Provider<JAXBContext>)serviceInstance;
        }

        if (provider == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("InvalidProviderCreate", clazz.getName()));
        }

        return provider;

    }

    /*
    * Get the provider type from a given implemention class instance
    */
    private Class<?> getProviderType() throws Exception {

        Class providerType = null;

        Type[] giTypes = serviceImplClass.getGenericInterfaces();
        for (Type giType : giTypes) {
            ParameterizedType paramType = null;
            try {
                paramType = (ParameterizedType)giType;
            } catch (ClassCastException e) {
                //TODO NLS
                throw new Exception(
                        "Provider based SEI Class has to implement javax.xml.ws.Provider as javax.xml.ws.Provider<String>, javax.xml.ws.Provider<SOAPMessage>, javax.xml.ws.Provider<Source> or javax.xml.ws.Provider<JAXBContext>");
            }
            Class interfaceName = (Class)paramType.getRawType();

            if (interfaceName == javax.xml.ws.Provider.class) {
                if (paramType.getActualTypeArguments().length > 1) {
                    //TODO NLS
                    throw new Exception(
                            "Provider cannot have more than one Generic Types defined as Per JAX-WS Specification");
                }
                providerType = (Class)paramType.getActualTypeArguments()[0];
            }
        }
        return providerType;
    }

    /*
    * Validate whether or not the Class passed in is a valid type of
    * javax.xml.ws.Provider<T>.  Per the JAX-WS 2.0 specification, the
    * parameterized type of a Provider can only be:
    *
    *   javax.xml.transform.Source
    *   javax.xml.soap.SOAPMessage
    *   javax.activation.DataSource
    *
    * We've also added support for String types which is NOT dictated
    * by the spec.
    */
    private boolean isValidProviderType(Class clazz) {
        boolean valid = clazz == String.class ||
                clazz == SOAPMessage.class ||
                clazz == Source.class ||
                clazz == DataSource.class;

        if (!valid) {
            if (log.isDebugEnabled()) {
                log.debug("Class " + clazz.getName() + " is not a valid Provider<T> type");
            }
        }

        return valid;
    }

    /*
    * Given a target class type for a payload, load the appropriate BlockFactory.
    */
    private BlockFactory createBlockFactory(Class type) {
        if (blockFactory != null)
            return blockFactory;

        if (type.equals(String.class)) {
            blockFactory = (XMLStringBlockFactory)FactoryRegistry.getFactory(
                    XMLStringBlockFactory.class);
        } else if (type.equals(Source.class)) {
            blockFactory = (SourceBlockFactory)FactoryRegistry.getFactory(
                    SourceBlockFactory.class);
        } else if (type.equals(SOAPMessage.class)) {
            blockFactory = (SOAPEnvelopeBlockFactory)FactoryRegistry.getFactory(
                    SOAPEnvelopeBlockFactory.class);
        } else {
            ExceptionFactory.makeWebServiceException("Unable to find BlockFactory " +
                    "for type: " + type.getClass().getName());
        }

        return blockFactory;
    }
    
}
