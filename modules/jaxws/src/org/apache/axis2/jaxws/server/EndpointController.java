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
package org.apache.axis2.jaxws.server;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MEPContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultCode;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher;
import org.apache.axis2.jaxws.server.dispatcher.factory.EndpointDispatcherFactory;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.http.HTTPBinding;
import java.io.StringReader;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;

/**
 * The EndpointController is the server side equivalent to the InvocationController on the client
 * side.  It is an abstraction of the server side endpoint invocation that encapsulates all of the
 * Axis2 semantics.
 * <p/>
 * Like the InvocationController, this class is responsible for invoking the JAX-WS application
 * handler chain along with taking all of the provided information and setting up what's needed to
 * perform the actual invocation of the endpoint.
 */
public class EndpointController {

    private static final Log log = LogFactory.getLog(EndpointController.class);

    private static final String PARAM_SERVICE_CLASS = "ServiceClass";

    public EndpointController() {
        //do nothing
    }

    /**
     * This method is used to start the JAX-WS invocation of a target endpoint. It takes an
     * InvocationContext, which must have a MessageContext specied for the request.  Once the
     * invocation is complete, the information will be stored
     */
    public InvocationContext invoke(InvocationContext ic) {
        MessageContext requestMsgCtx = ic.getRequestMessageContext();

        String implClassName = getServiceImplClassName(requestMsgCtx);

        Class implClass = loadServiceImplClass(implClassName,
                                               requestMsgCtx.getClassLoader());

        EndpointDescription endpointDesc = getEndpointDescription(requestMsgCtx, implClass);
        requestMsgCtx.setEndpointDescription(endpointDesc);

        /*
         * TODO: review: make sure the handlers are set on the InvocationContext
         * This implementation of the JAXWS runtime does not use Endpoint, which
         * would normally be the place to initialize and store the handler list.
         * In lieu of that, we will have to intialize and store them on the 
         * InvocationContext.  also see the InvocationContextFactory.  On the client
         * side, the binding is not yet set when we call into that factory, so the
         * handler list doesn't get set on the InvocationContext object there.  Thus
         * we gotta do it here.
         * 
         * Since we're on the server, and there apparently is no Binding object
         * anywhere to be found...
         */
        if (ic.getHandlers() == null) {
            ic.setHandlers(new HandlerResolverImpl(endpointDesc).getHandlerChain(endpointDesc.getPortInfo()));
        }

        if (!bindingTypesMatch(requestMsgCtx, endpointDesc.getServiceDescription())) {
            Protocol protocol = requestMsgCtx.getMessage().getProtocol();
            // only if protocol is soap12 and MISmatches the endpoint do we halt processing
            if (protocol.equals(Protocol.soap12)) {
                ic.setResponseMessageContext(createMismatchFaultMsgCtx(requestMsgCtx,
                                                                       "Incoming SOAP message protocol is version 1.2, but endpoint is configured for SOAP 1.1"));
                return ic;
            } else if (protocol.equals(Protocol.soap11)) {
                // SOAP 1.1 message and SOAP 1.2 binding

                // The canSupport flag indicates that we can support this scenario.
                // Possible Examples of canSupport:  JAXB impl binding, JAXB Provider
                // Possible Example of !canSupport: Application handler usage, non-JAXB Provider
                // Initially I vote to hard code this as false.
                boolean canSupport = false;
                if (canSupport) {
                    // TODO: Okay, but we need to scrub the Message create code to make sure that the response message
                    // is always built from the receiver protocol...not the binding protocol
                } else {
                    ic.setResponseMessageContext(createMismatchFaultMsgCtx(requestMsgCtx,
                                                                           "Incoming SOAP message protocol is version 1.1, but endpoint is configured for SOAP 1.2.  This is not supported."));
                    return ic;
                }
            } else {
                ic.setResponseMessageContext(createMismatchFaultMsgCtx(requestMsgCtx,
                                                                       "Incoming message protocol does not match endpoint protocol."));
                return ic;
            }
        }

        MessageContext responseMsgContext = null;

        try {
            // Get the service instance.  This will run the @PostConstruct code.
            EndpointLifecycleManager elm = createEndpointlifecycleManager();
            Object serviceInstance = elm.createServiceInstance(requestMsgCtx, implClass);

            // The application handlers and dispatcher invoke will 
            // modify/destroy parts of the message.  Make sure to save
            // the request message if appropriate.
            saveRequestMessage(requestMsgCtx);

            // Invoke inbound application handlers.  It's safe to use the first object on the iterator because there is
            // always exactly one EndpointDescription on a server invoke
            boolean success =
                    HandlerInvokerUtils.invokeInboundHandlers(requestMsgCtx.getMEPContext(),
                                                              ic.getHandlers(),
                                                              HandlerChainProcessor.MEP.REQUEST,
                                                              isOneWay(requestMsgCtx.getAxisMessageContext()));

            if (success) {

                // Dispatch to the
                EndpointDispatcher dispatcher = getEndpointDispatcher(implClass, serviceInstance);
                try {
                    responseMsgContext = dispatcher.invoke(requestMsgCtx);
                } finally {
                    // Passed pivot point
                    requestMsgCtx.getMessage().setPostPivot();
                }

                // Invoke the outbound response handlers.
                // If the message is one way, we should not invoke the response handlers.  There is no response
                // MessageContext since a one way invocation is considered to have a "void" return.
                if (!isOneWay(requestMsgCtx.getAxisMessageContext())) {
                    responseMsgContext.setMEPContext(requestMsgCtx.getMEPContext());
                    
                    HandlerInvokerUtils.invokeOutboundHandlers(responseMsgContext.getMEPContext(),
                                                               ic.getHandlers(),
                                                               HandlerChainProcessor.MEP.RESPONSE,
                                                               false);
                }
            } else
            { // the inbound handler chain must have had a problem, and we've reversed directions
                responseMsgContext =
                        MessageContextUtils.createResponseMessageContext(requestMsgCtx);
                // since we've reversed directions, the message has "become a response message" (section 9.3.2.1, footnote superscript 2)
                responseMsgContext.setMessage(requestMsgCtx.getMessage());
            }

        } catch (Exception e) {
            // TODO for now, throw it.  We probably should try to make an XMLFault object and set it on the message
            throw ExceptionFactory.makeWebServiceException(e);
        } finally {
            restoreRequestMessage(requestMsgCtx);
        }

		// The response MessageContext should be set on the InvocationContext
		ic.setResponseMessageContext(responseMsgContext);

        return ic;
    }

    /*
      * Get the appropriate EndpointDispatcher for a given service endpoint.
      */
    protected EndpointDispatcher getEndpointDispatcher(Class serviceImplClass, Object serviceInstance)
            throws Exception {
        EndpointDispatcherFactory factory = 
            (EndpointDispatcherFactory)FactoryRegistry.getFactory(EndpointDispatcherFactory.class);        
        return factory.createEndpointDispatcher(serviceImplClass, serviceInstance);       
    }

    /*
      * Tries to load the implementation class that was specified for the
      * target endpoint
      */
    private Class loadServiceImplClass(String className, ClassLoader cl) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to load service impl class: " + className);
        }

        try {
            //TODO: What should be done if the supplied ClassLoader is null?
            Class _class = forName(className, true, cl);
            return _class;
            //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
            //does not extend Exception, so lets catch everything that extends Throwable
            //rather than just Exception.
        } catch (Throwable cnf) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "EndpointControllerErr4", className));
        }
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain private because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classloader);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }

    private String getServiceImplClassName(MessageContext mc) {
        // The PARAM_SERVICE_CLASS property that is set on the AxisService
        // will tell us what the service implementation class is.
        org.apache.axis2.context.MessageContext axisMsgContext = mc.getAxisMessageContext();
        AxisService as = axisMsgContext.getAxisService();
        Parameter param = as.getParameter(PARAM_SERVICE_CLASS);

        // If there was no implementation class, we should not go any further
        if (param == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "EndpointControllerErr2"));
        }

        String className = ((String)param.getValue()).trim();
        return className;
    }

    /*
    * Gets the ServiceDescription associated with the request that is currently
    * being processed.
    */
    private EndpointDescription getEndpointDescription(MessageContext mc, Class implClass) {
        AxisService axisSvc = mc.getAxisMessageContext().getAxisService();

        //Check to see if we've already created a ServiceDescription for this
        //service before trying to create a new one. 

        if (axisSvc.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER) != null) {
            Parameter param = axisSvc.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);

            EndpointDescription ed = (EndpointDescription)param.getValue();
            return ed;
        } else {
            // TODO: This is using a deprecated factory method to create the ServiceDescription.
            // The correct way to fix this is to create the ServiceDescriptions (and the AxisService
            // and associated descritpion hierahcy) at startup.  However, that is currently not done
            // in the Axis2 testing environment.  So, for testing, we create a Description hierachy
            // on the fly and attach the AxisService to it.  This should be changed to not used the
            // deprecated factory method.  HOWEVER doing so currently causes testcase failures in 
            // JAXWS and or Metadata
//            ServiceDescription sd = DescriptionFactory.createServiceDescription(implClass);
            ServiceDescription sd =
                    DescriptionFactory.createServiceDescriptionFromServiceImpl(implClass, axisSvc);
            EndpointDescription ed = sd.getEndpointDescriptions_AsCollection().iterator().next();
            return ed;
        }
    }

    private EndpointLifecycleManager createEndpointlifecycleManager() {
        EndpointLifecycleManagerFactory elmf = (EndpointLifecycleManagerFactory)FactoryRegistry
                .getFactory(EndpointLifecycleManagerFactory.class);
        return elmf.createEndpointLifecycleManager();
    }


    private boolean bindingTypesMatch(MessageContext requestMsgCtx,
                                      ServiceDescription serviceDesc) {
        // compare soap versions and respond appropriately under SOAP 1.2 Appendix 'A'
        Collection<EndpointDescription> eds = serviceDesc.getEndpointDescriptions_AsCollection();
        // dispatch endpoints do not have SEIs, so watch out for null or empty array
        if ((eds != null) && (eds.size() > 0)) {
            EndpointDescription ed = eds.iterator().next();
            Protocol protocol = requestMsgCtx.getMessage().getProtocol();
            String endpointBindingType = ed.getBindingType();
            if (protocol.equals(Protocol.soap11)) {
                return (SOAPBinding.SOAP11HTTP_BINDING.equalsIgnoreCase(endpointBindingType)) ||
                        (SOAPBinding.SOAP11HTTP_MTOM_BINDING.equalsIgnoreCase(endpointBindingType));
            } else if (protocol.equals(Protocol.soap12)) {
                return (SOAPBinding.SOAP12HTTP_BINDING.equalsIgnoreCase(endpointBindingType)) ||
                        (SOAPBinding.SOAP12HTTP_MTOM_BINDING.equalsIgnoreCase(endpointBindingType));
            } else if (protocol.equals(Protocol.rest)) {
                return HTTPBinding.HTTP_BINDING.equalsIgnoreCase(endpointBindingType);
            }
        }
        // safe to assume?
        return true;
    }

    private MessageContext createMismatchFaultMsgCtx(MessageContext requestMsgCtx,
                                                     String errorMsg) {
        try {
            XMLFault xmlfault =
                    new XMLFault(XMLFaultCode.VERSIONMISMATCH, new XMLFaultReason(errorMsg));
            Message msg = ((MessageFactory)FactoryRegistry.getFactory(MessageFactory.class))
                    .create(Protocol.soap11);  // always soap11 according to the spec
            msg.setXMLFault(xmlfault);
            MessageContext responseMsgCtx =
                    MessageContextUtils.createFaultMessageContext(requestMsgCtx);
            responseMsgCtx.setMessage(msg);
            return responseMsgCtx;
        } catch (XMLStreamException e) {
            // Need to fix this !   At least provide logging
            // TODO for now, throw it.  We probably should try to make an XMLFault object and set it on the message
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * Save the request message if indicated by the SAVE_REQUEST_MSG property
     *
     * @param requestMsgContext
     */
    private void saveRequestMessage(MessageContext requestMsgContext) {

        // TODO: TESTING...FORCE SAVING THE REQUEST MESSAGE
        // requestMsgContext.getAxisMessageContext().setProperty(Constants.SAVE_REQUEST_MSG, Boolean.TRUE);
        // END TESTING

        Boolean value = (Boolean)
                requestMsgContext.getAxisMessageContext().getProperty(Constants.SAVE_REQUEST_MSG);
        if (value != null && value == Boolean.TRUE) {
            // REVIEW: This does not properly account for attachments.
            Message m = requestMsgContext.getMessage();
            String savedMsg = m.getAsOMElement().toString();
            requestMsgContext.getAxisMessageContext()
                    .setProperty(Constants.SAVED_REQUEST_MSG_TEXT, savedMsg);
        }
    }

    /**
     * Restore the request message from the saved message text
     *
     * @param requestMsgContext
     */
    private void restoreRequestMessage(MessageContext requestMsgContext) {

        Boolean value = (Boolean)
                requestMsgContext.getAxisMessageContext().getProperty(Constants.SAVE_REQUEST_MSG);
        if (value != null && value == Boolean.TRUE) {
            // REVIEW: This does not properly account for attachments.
            String savedMsg = (String)requestMsgContext.getAxisMessageContext()
                    .getProperty(Constants.SAVED_REQUEST_MSG_TEXT);
            if (savedMsg != null && savedMsg.length() > 0) {
                try {
                    StringReader sr = new StringReader(savedMsg);
                    XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(sr);
                    MessageFactory mf = (MessageFactory)
                            FactoryRegistry.getFactory(MessageFactory.class);
                    Protocol protocol = requestMsgContext.getAxisMessageContext().isDoingREST() ?
                            Protocol.rest : null;
                    Message msg = mf.createFrom(xmlreader, protocol);
                    requestMsgContext.setMessage(msg);
                } catch (Throwable e) {
                    ExceptionFactory.makeWebServiceException(e);
                }
            }
        }

        // TESTING....SIMULATE A PERSIST OF THE REQUEST MESSAGE
        // String text = requestMsgContext.getMessage().getAsOMElement().toString();
        // System.out.println("Persist Message" + text);
        // END TESTING
    }

    /*
    * Determine if this is a one-way invocation or not.
    */
    public static boolean isOneWay(org.apache.axis2.context.MessageContext mc) {
        if (mc != null) {
            AxisOperation op = mc.getAxisOperation();
            String mep = op.getMessageExchangePattern();

            if (mep.equals(WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY) ||
                    mep.equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY) ||
                    mep.equals(WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY) ||
                    mep.equals(WSDL20_2006Constants.MEP_URI_IN_ONLY)||
                    mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY)||
                    mep.equals(WSDL2Constants.MEP_URI_IN_ONLY)) {
                return true;
            }
        }
        return false;
    }
}
