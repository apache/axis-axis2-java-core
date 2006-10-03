/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.core.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.AxisCallback;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.impl.AsyncListenerWrapper;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.attachments.AttachmentUtils;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.impl.AttachmentImpl;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The <tt>AxisInvocationController</tt> is an implementation of the 
 * {@link org.apache.axis2.jaxws.core.controller.InvocationController}
 * interface.  This implemenation uses the Axis2 engine to drive the
 * request to the target service.  
 * 
 * For more information on how to invoke this class, please see the 
 * InvocationController interface comments.
 */
public class AxisInvocationController implements InvocationController {
    
    private static Log log = LogFactory.getLog(AxisInvocationController.class);
    
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invoke(org.apache.axis2.jaxws.core.InvocationContext)
     */
    public InvocationContext invoke(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: synchronous");
        }
        
        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }
        
        // Get the request MessageContext
        MessageContext requestMsgCtx = ic.getRequestMessageContext();
        MessageContext responseMsgCtx = null;
        
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        // TODO: Fix this logic once AxisService is created via annoations and not just WSDL
        //       If ServiceDesc.axisService is null, then we created an Annon Service and operations in 
        //       ServiceDelegate.getServiceClient(), and that is what the service client points to.
        //       Therefore, we need to use the annonymous operation name in that case, so the anonymous service client will find 
        //       the anonymous AxisOperation on that service.  
        //       This means the ServiceDesc was not build with WSDL, and so there are no Axis objects attached to them
        //       i.e the OperationDesc.axisOperation == null
        QName operationName = getOperationNameToUse(requestMsgCtx, ServiceClient.ANON_OUT_IN_OP);

        // TODO: Will the ServiceClient stick around on the InvocationContext
        // or will we need some other mechanism of creating this?
        // Try to create an OperationClient from the passed in ServiceClient
        ServiceClient svcClient = ic.getServiceClient();
        OperationClient opClient = createOperationClient(svcClient, operationName);
        
        setupProperties(requestMsgCtx, opClient.getOptions());
        
        if (opClient != null) {
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            String targetUrl = (String) requestMsgCtx.getProperties().get(
                    BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            EndpointReference toEPR = new EndpointReference(targetUrl);
            opClient.getOptions().setTo(toEPR);
            
            // Get the SOAP Action (if needed)
            String soapAction = configureSOAPAction(requestMsgCtx);
            opClient.getOptions().setAction(soapAction);
            
            // Use the OperationClient to send the request and put the contents
            // of the response in the response MessageContext.
            try {
                // The MessageContext will contain a Message object with the
                // contents that need to be sent.  We need to get those contents
                // in a form that Axis2 can consume them, an AXIOM SOAPEnvelope.
                Message requestMsg = requestMsgCtx.getMessage();
                SOAPEnvelope requestOM = (SOAPEnvelope) requestMsg.getAsOMElement();
                
                org.apache.axis2.context.MessageContext axisRequestMsgCtx = 
                    requestMsgCtx.getAxisMessageContext();
                axisRequestMsgCtx.setEnvelope(requestOM);
                
                // For now, just take all of the properties that were in the 
                // JAX-WS MessageContext, and set them on the Axis2 MessageContext.
                axisRequestMsgCtx.setProperty(AbstractContext.COPY_PROPERTIES,
                    Boolean.TRUE);
                Map props = axisRequestMsgCtx.getOptions().getProperties();
                props.putAll(requestMsgCtx.getProperties());
                
                axisRequestMsgCtx.getOptions().setProperties(props);
                if (log.isDebugEnabled()) {
                    log.debug("Properties: " + axisRequestMsgCtx.getProperties().toString());
                }
                
                // Setting the ServiceContext will create the association between 
                // the OperationClient it's MessageContexts and the 
                // AxisService/AxisOperation that they are tied to.
                axisRequestMsgCtx.setServiceContext(svcClient.getServiceContext());

                // Set the Axis2 request MessageContext
                opClient.addMessageContext(axisRequestMsgCtx);

                //This assumes that we are on the ultimate execution thread
                ThreadContextMigratorUtil.performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
                opClient.execute(true);
                ThreadContextMigratorUtil.performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
                
                // Collect the response MessageContext and envelope
                org.apache.axis2.context.MessageContext axisResponseMsgCtx = 
                    opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                
                //This assumes that we are on the ultimate execution thread
                ThreadContextMigratorUtil.performMigrationToThread(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);
                
                SOAPEnvelope rspEnvelope = axisResponseMsgCtx.getEnvelope();
                Message responseMsg = null;
                if (rspEnvelope != null) {
                    responseMsg = createMessageFromOM(rspEnvelope);
                }
                
                // Setup the response MessageContext
                responseMsgCtx = new MessageContext(axisResponseMsgCtx);
                responseMsgCtx.setMessage(responseMsg);
                
                //FIXME: This should be revisited when we re-work the MTOM support.
                //This destroys performance by forcing a double pass through the message.
                //If attachments are found, we must find all of the OMText nodes and 
                //replace them with <xop:include> elements so they can be processed
                //correctly by JAXB.
                if (axisResponseMsgCtx.getProperty(MTOMConstants.ATTACHMENTS) != null) { 
                    Message response = responseMsgCtx.getMessage();
                    response.setMTOMEnabled(true);
                    
                    ArrayList<OMText> binaryNodes = AttachmentUtils.findBinaryNodes(
                            axisResponseMsgCtx.getEnvelope());
                    if (binaryNodes != null) {
                        Iterator<OMText> itr = binaryNodes.iterator();
                        while (itr.hasNext()) {
                            OMText node = itr.next();
                            OMElement xop = AttachmentUtils.makeXopElement(node);
                            node.getParent().addChild(xop);
                            node.detach();
                            
                            Attachment a = new AttachmentImpl((DataHandler) node.getDataHandler(), 
                                    node.getContentID());
                            response.addAttachment(a);
                        }
                    }
                }
            } catch (AxisFault e) {
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (MessageException e) { 
                throw ExceptionFactory.makeWebServiceException(e);
            }
            
            // Set the response MessageContext on the InvocationContext
            ic.setResponseMessageContext(responseMsgCtx);
        }

        return ic;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeOneWay(org.apache.axis2.jaxws.core.InvocationContext)
     */
    public void invokeOneWay(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: one-way");
        }
        
        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException("Cannot invoke; InvocationContext was null");
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException("Cannot invoke; request MessageContext was null");
        }
        
        // Setup the MessageContext for the response
        MessageContext requestMsgCtx = ic.getRequestMessageContext();
        
        //TODO: Need to figure out whether or not we need to create the response
        //MessageContext here and whether or not handlers need to be run for 
        //one-way invocations.
        MessageContext responseMsgCtx = new MessageContext();
        ic.setResponseMessageContext(responseMsgCtx);
        
        // Try to create an OperationClient from the passed in ServiceClient
        ServiceClient svcClient = ic.getServiceClient();

        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        // TODO: Fix this logic once AxisService is created via annoations and not just WSDL
        //       If ServiceDesc.axisService is null, then we created an Annon Service and operations in 
        //       ServiceDelegate.getServiceClient(), and that is what the service client points to.
        //       Therefore, we need to use the annonymous operation name in that case, so the anonymous service client will find 
        //       the anonymous AxisOperation on that service.  
        //       This means the ServiceDesc was not build with WSDL, and so there are no Axis objects attached to them
        //       i.e the OperationDesc.axisOperation == null
        QName operationName = getOperationNameToUse(requestMsgCtx, ServiceClient.ANON_OUT_ONLY_OP);

        OperationClient opClient = createOperationClient(svcClient, operationName);
        
        setupProperties(requestMsgCtx, opClient.getOptions());
        
        if (opClient != null) {
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            String targetUrl = (String) requestMsgCtx.getProperties().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            EndpointReference toEPR = new EndpointReference(targetUrl);
            opClient.getOptions().setTo(toEPR);
            
            // Get the SOAP Action (if needed)
            String soapAction = configureSOAPAction(requestMsgCtx);
            opClient.getOptions().setAction(soapAction);
            
            // Use the OperationClient to send the request.
            Message responseMsg = null;
            org.apache.axis2.context.MessageContext axisResponseMsgCtx = null;
            try {
                // We need to prepare the contents of the Message from the request 
                // MessageContext to be sent
                Message requestMsg = requestMsgCtx.getMessage();
                SOAPEnvelope requestOM = (SOAPEnvelope) requestMsg.getAsOMElement();
                
                org.apache.axis2.context.MessageContext axisRequestMsgCtx = 
                    requestMsgCtx.getAxisMessageContext();
                
                // For now, just take all of the properties that were in the 
                // JAX-WS MessageContext, and set them on the Axis2 MessageContext.
                axisRequestMsgCtx.setProperty(AbstractContext.COPY_PROPERTIES,
                    Boolean.TRUE);
                Map props = axisRequestMsgCtx.getOptions().getProperties();
                props.putAll(requestMsgCtx.getProperties());
                axisRequestMsgCtx.getOptions().setProperties(props);
                if (log.isDebugEnabled()) {
                    log.debug("Properties: " + axisRequestMsgCtx.getProperties().toString());
                }
                
                axisRequestMsgCtx.setEnvelope(requestOM);
                
                // Setting the ServiceContext will create the association between 
                // the OperationClient it's MessageContexts and the 
                // AxisService/AxisOperation that they are tied to.
                axisRequestMsgCtx.setServiceContext(svcClient.getServiceContext());
                opClient.addMessageContext(axisRequestMsgCtx);

                //This assumes that we are on the ultimate execution thread
                ThreadContextMigratorUtil.performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
                opClient.execute(true);
                ThreadContextMigratorUtil.performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
            } catch (AxisFault e) {
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (MessageException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }
        
        return;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeAsync(org.apache.axis2.jaxws.core.InvocationContext, javax.xml.ws.AsyncHandler)
     */
    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler callback) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: async (callback)");
        }

        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr3"));
        }
        
        // Setup the MessageContext for the response
        MessageContext requestMsgCtx = ic.getRequestMessageContext();
        
        ServiceClient svcClient = ic.getServiceClient();
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        // TODO: Fix this logic once AxisService is created via annoations and not just WSDL
        //       If ServiceDesc.axisService is null, then we created an Annon Service and operations in 
        //       ServiceDelegate.getServiceClient(), and that is what the service client points to.
        //       Therefore, we need to use the annonymous operation name in that case, so the anonymous service client will find 
        //       the anonymous AxisOperation on that service.  
        //       This means the ServiceDesc was not build with WSDL, and so there are no Axis objects attached to them
        //       i.e the OperationDesc.axisOperation == null
        QName operationName = getOperationNameToUse(requestMsgCtx, ServiceClient.ANON_OUT_IN_OP);

        OperationClient opClient = createOperationClient(svcClient, operationName);
        
        setupProperties(requestMsgCtx, opClient.getOptions());
        
        if (opClient != null) {
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            String targetUrl = (String) requestMsgCtx.getProperties().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            EndpointReference toEPR = new EndpointReference(targetUrl);
            opClient.getOptions().setTo(toEPR);
            
            // Get the SOAP Action (if needed)
            String soapAction = configureSOAPAction(requestMsgCtx);
            opClient.getOptions().setAction(soapAction);
            
            // Setup the client so that it knows whether the underlying call to
            // Axis2 knows whether or not to start a listening port for an
            // asynchronous response.
            Boolean useAsyncMep = (Boolean) requestMsgCtx.getProperties().get(Constants.USE_ASYNC_MEP);
            if((useAsyncMep != null && useAsyncMep.booleanValue()) 
                    || opClient.getOptions().isUseSeparateListener()) {
                opClient.getOptions().setUseSeparateListener(true);
                opClient.getOptions().setTransportInProtocol("http");
                // Setup the response callback receiver to receive the async response
                // This logic is based on org.apache.axis2.client.ServiceClient.sendReceiveNonBlocking(...)
                AxisOperation op = opClient.getOperationContext().getAxisOperation();
                MessageReceiver messageReceiver = op.getMessageReceiver();
                if (messageReceiver == null || !(messageReceiver instanceof CallbackReceiver))
                    op.setMessageReceiver(new CallbackReceiver());
            }
            
            // There should be an AsyncListener that is configured and set on the
            // InvocationContext.  We must get this and use it to wait for the 
            // async response to come back.  The AxisCallback that is set on the 
            // AsyncListener is the callback that Axis2 will call when the response
            // has arrived.
            AsyncListener listener = ic.getAsyncListener();
            AxisCallback axisCallback = new AxisCallback();
            listener.setAxisCallback(axisCallback);
            listener.setInvocationContext(ic);
            
            // Once the AsyncListener is configured, we must include that in an 
            // AsyncListenerWrapper.  The wrapper is what will handle the lifecycle 
            // of the listener and determine when it's started and stopped.
            AsyncListenerWrapper<?> wrapper = new AsyncListenerWrapper<Object>(listener);

            // Inside of the wrapper we must set the callback that the JAX-WS
            // client programmer provided.  This is the user object that we 
            // must call back on once we've done everything we need to do at
            // the JAX-WS layer.
            if(callback != null){
                wrapper.setAsyncHandler(callback);
            }
            else {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr4"));
            }
            
            try {
                // Get the request message from the MessageContext and send it
                // using the OperationClient API.
                Message requestMsg = requestMsgCtx.getMessage();
                SOAPEnvelope requestOM = (SOAPEnvelope) requestMsg.getAsOMElement();
                
                // The envelope must be set on the Axis2 MessageContext as a
                // OM-based SOAPEnvelope
                org.apache.axis2.context.MessageContext axisRequestMsgCtx = 
                    requestMsgCtx.getAxisMessageContext();
                axisRequestMsgCtx.setEnvelope(requestOM);
                
                // For now, just take all of the properties that were in the 
                // JAX-WS MessageContext, and set them on the Axis2 MessageContext.
                axisRequestMsgCtx.setProperty(AbstractContext.COPY_PROPERTIES,
                    Boolean.TRUE);
                Map props = axisRequestMsgCtx.getOptions().getProperties();
                props.putAll(requestMsgCtx.getProperties());
                axisRequestMsgCtx.getOptions().setProperties(props);
                if (log.isDebugEnabled()) {
                    log.debug("Properties: " + axisRequestMsgCtx.getProperties().toString());
                }
                
                // Setting the ServiceContext will create the association between 
                // the OperationClient it's MessageContexts and the 
                // AxisService/AxisOperation that they are tied to.
                axisRequestMsgCtx.setServiceContext(svcClient.getServiceContext());
                opClient.addMessageContext(axisRequestMsgCtx);
                opClient.setCallback(axisCallback);
                ThreadContextMigratorUtil.performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
                opClient.execute(false);
                ThreadContextMigratorUtil.performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
            } catch (AxisFault e) {
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (MessageException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
            
            // Now that the request has been sent, start the listener thread so that it can
            // catch the async response.
            // TODO: Need to determine whether this should be done BEFORE or AFTER
            // we send the request.  My guess is before though.
            try {
                // TODO:Need to figure out where we get the Executor from
                // Can't be from the MessageContext, but should maybe be 
                // set somewhere accessible.
                // FIXME: This should NOT be an ExecutorService, but should just
                // be a plain old Executor.
                ExecutorService exec = (ExecutorService) ic.getExecutor();
                Future<?> future = exec.submit(wrapper);
                future.get();
                //TODO temp fix to resolve async callback hang.
                exec.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw ExceptionFactory.makeWebServiceException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw ExceptionFactory.makeWebServiceException(e);
            }
            
            return wrapper;
        }
        
        return null;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.core.controller.InvocationController#invokeAsync(org.apache.axis2.jaxws.core.InvocationContext)
     */
    public Response invokeAsync(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: async (polling)");
        }
        
        throw ExceptionFactory.makeWebServiceException(Messages.getMessage("AsyncPollingNotSupported"));
    }
    
    /**
     * Returns the SOAPAction that should be used for the invocation.  This
     * method will get the information from the MessageContext passed in
     * either from :
     * 
     * a) the JAX-WS properties available on the MessageContext or
     * b) the WSDL configuration information available from the MessageContext 
     * 
     * @param ctx
     * @return
     */
    private String configureSOAPAction(MessageContext ctx) {
        //TODO: Need to get SOAPAction information from the WSDL config
        
        //TODO: Need to determine what the story is with using the SOAPAction
        // declared in the WSDL.  If the property says not to use it, but it's
        // listed in the WSDL, do we still include it?  Do we include it if
        // the property is not even set?
        Boolean useSoapAction = (Boolean) ctx.getProperties().get(BindingProvider.SOAPACTION_USE_PROPERTY);
        if(useSoapAction != null && useSoapAction.booleanValue()){
            return (String) ctx.getProperties().get(BindingProvider.SOAPACTION_URI_PROPERTY);
        }
        
        return null;
    }
    
    /**
     * Use the provided ServiceClient instance to create an OperationClient identified 
     * by the operation QName provided.
     * 
     * @param sc
     * @param operation
     * @return
     */
    private OperationClient createOperationClient(ServiceClient sc, QName operation) {
        if (sc == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICCreateOpClientErr1"));
        }
        if (operation == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICCreateOpClientErr2"));
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Creating OperationClient for operation: " + operation);
        }
        
        try {
            OperationClient client = sc.createClient(operation);
            return client;
        } catch (AxisFault e) {
            //TODO: NLS and ExceptionFactory
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    private Message createMessageFromOM(OMElement om) throws MessageException {
        try {
            MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
            Message msg = mf.createFrom(om);
            return msg;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    /*
     * TODO: This is a first pass at filtering the properties that are set on the 
     * RequestContext.  Right now it's called during the invoke, but needs to be 
     * moved over to when the property is set.  This should not be in the path
     * of performance.
     */
    private void setupProperties(MessageContext mc, Options ops) {
        Map<String, Object> properties = mc.getProperties();
        for (Iterator<String> it = properties.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            Object value = properties.get(key);
            ops.setProperty(key, value);
        }
        
        // Enable MTOM
        Message msg = mc.getMessage();
        if (msg.isMTOMEnabled()) {
            ops.setProperty(Configuration.ENABLE_MTOM, "true");
        }
    }
    
    // TODO: This method may need to be removed or refactored when the ServiceDescription can
    //       create the AxisService hierachy using annotations.  Currently the AxisService hierachy is
    //       only created under a ServiceDescription if WSDL is supplied
    private static QName getOperationNameToUse(MessageContext requestMsgCtx, QName defaultOpName) {
        // We need the qname of the operation being invoked to know which 
        // AxisOperation the OperationClient should be based on.
        // Note that the OperationDesc is only set through use of the Proxy. Dispatch
        // clients do not use operations, so the operationDesc will be null.  In this
        // case an anonymous AxisService with anoymouns AxisOperations for the supported
        // MEPs will be created; and it is that anonymous operation name which needs to
        // be specified
        // TODO: Fix this logic once AxisService is created via annoations and not just WSDL
        //       If ServiceDesc.axisService is null (which currently happens if no WSDL 
        //       was supplied when the ServiceDesc is created), then we created an annonymous Service and operations in 
        //       ServiceDelegate.getServiceClient(), and that is what the service client used in the inovke points to.
        //       Therefore, we need to use the annonymous operation name in that case, so the anonymous service client will find 
        //       the anonymous AxisOperation on that service.  
        //       The check below for this case is that the AxisOperation is null, since all Axis description
        //       objects (including AxisOperation) are only created if WSDL was use to create the ServiceDesc.
        //       This will probably need to change once Axis descriptions are also created from annotions.
        QName operationName = null;
        OperationDescription opDesc = requestMsgCtx.getOperationDescription();
        if (opDesc != null && opDesc.getAxisOperation() != null)
            operationName = opDesc.getName();
        else 
            operationName = defaultOpName;
        return operationName;
    }
}
