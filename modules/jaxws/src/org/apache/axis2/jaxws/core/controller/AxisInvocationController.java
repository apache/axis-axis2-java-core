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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.AxisCallback;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.impl.AsyncListenerWrapper;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The <tt>AxisInvocationController</tt> is a stateless entity used to
 * invoke the Axis2 client APIs.  All of the information that the 
 * AxisInvocationController needs should exist within the InvocatonContext
 * that is passed in.  
 * 
 * The request information is passed in within the InvocationContext.  The
 * AxisInvocationController assumes that there is a MessageContext within that
 * InvocationContext that is populated with all of the information that it
 * needs to invoke.  If not, an error will be returned.  Once the response 
 * comes back, the information for that response will be held inside of the
 * MessageContext representing the response, that exists in the 
 * InvocationContext.
 * 
 * The AxisInvocationController supports four different invocation patterns:
 * 
 * 1) synchronous - This is represented by the {@link #invoke(InvocationContext)}
 * method.  This is a blocking call to the Axis2 client.
 * 
 * 2) one-way - This is represented by the {@link #invokeOneWay(InvocationContext)}
 * method.  This is a one-way invocation that only returns errors related
 * to sending the message.  If an error occurs while processing, the client
 * will not be notified.
 * 
 * 3) asynchronous (callback) - {@link #invokeAsync(InvocationContext, AsyncHandler)}
 * 
 * 4) asynchronous (polling) - {@link #invokeAsync(InvocationContext)}
 */
public class AxisInvocationController implements InvocationController {
    
    private static Log log = LogFactory.getLog(AxisInvocationController.class);
    
    /**
     * Performs a synchronous (blocking) invocation of the client.
     * 
     * @param ic
     * @return
     */
    public InvocationContext invoke(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: synchronous");
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
        MessageContext responseMsgCtx = new MessageContext();
        ic.setResponseMessageContext(responseMsgCtx);
        
        ServiceClient client = ic.getServiceClient();        
        if (client != null) {
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            String targetUrl = (String) requestMsgCtx.getProperties().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            EndpointReference toEPR = new EndpointReference(targetUrl);
            client.getOptions().setTo(toEPR);
            
            // Get the SOAP Action (if needed)
            String soapAction = getSOAPAction(requestMsgCtx);
            client.getOptions().setAction(soapAction);
            
            // Use the ServiceClient to send the request.
            OMElement rspEnvelope = null;
            try {
                OMElement reqEnvelope = requestMsgCtx.getMessageAsOM();
                rspEnvelope = client.sendReceive(ServiceClient.ANON_OUT_IN_OP, reqEnvelope);
            } catch (AxisFault e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
            
            // Set the response message on the response MessageContext
            responseMsgCtx.setMessageAsOM(rspEnvelope);
        }

        return ic;
    }
    
    /**
     * Performs a one-way invocation of the client.  This is NOT a robust
     * invocation, so any fault that occurs during the processing of the request
     * will not be returned to the client.  Errors returned to the client are
     * problems that occurred during the sending of the message to the server.
     * 
     * @param ic
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
        
        ServiceClient client = ic.getServiceClient();        
        if (client != null) {
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            String targetUrl = (String) requestMsgCtx.getProperties().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            EndpointReference toEPR = new EndpointReference(targetUrl);
            client.getOptions().setTo(toEPR);
            
            // Get the SOAP Action (if needed)
            String soapAction = getSOAPAction(requestMsgCtx);
            client.getOptions().setAction(soapAction);
            
            // Use the ServiceClient to send the request.
            try {
                OMElement reqEnvelope = requestMsgCtx.getMessageAsOM();
                client.fireAndForget(ServiceClient.ANON_OUT_ONLY_OP, reqEnvelope);
            } catch (AxisFault e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }
        
        return;
    }
    
    /**
     * Performs an asynchronous (non-blocking) invocation of the client based 
     * on a callback model.  The AsyncHandler that is passed in is the callback
     * that the client programmer supplied when they invoked their JAX-WS
     * Dispatch or their SEI-based dynamic proxy.  
     * 
     * @param ic
     * @param callback
     * @return
     */
    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler callback) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: async (callback)");
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
        MessageContext responseMsgCtx = new MessageContext();
        ic.setResponseMessageContext(responseMsgCtx);
        
        ServiceClient client = ic.getServiceClient();        
        if (client != null) {
            // Get the target endpoint address and setup the TO endpoint 
            // reference.  This tells us where the request is going.
            String targetUrl = (String) requestMsgCtx.getProperties().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            EndpointReference toEPR = new EndpointReference(targetUrl);
            client.getOptions().setTo(toEPR);
            
            // Get the SOAP Action (if needed)
            String soapAction = getSOAPAction(requestMsgCtx);
            client.getOptions().setAction(soapAction);
            
            // Setup the client so that it knows whether the underlying call to
            // Axis2 knows whether or not to start a listening port for an
            // asynchronous response.
            Boolean useAsyncMep = (Boolean) requestMsgCtx.getProperties().get(Constants.USE_ASYNC_MEP);
            if((useAsyncMep != null && useAsyncMep.booleanValue()) 
                    || client.getOptions().isUseSeparateListener()) {
                client.getOptions().setUseSeparateListener(true);
                client.getOptions().setTransportInProtocol("http");
            }
            
            // There should be an AsyncListener that is configured and set on the
            // InvocationContext.  We must get this and use it to wait for the 
            // async response to come back.  The AxisCallback that is set on the 
            // AsyncListener is the callback that Axis2 will call when the response
            // has arrived.
            AsyncListener listener = ic.getAsyncListener();
            AxisCallback axisCallback = new AxisCallback();
            listener.setAxisCallback(axisCallback);
            
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
                throw ExceptionFactory.makeWebServiceException("Cannot call asynchronous invoke with null callback");
            }
            
            // Get the request message from the MessageContext and send it
            // using the ServiceClient API.
            OMElement reqEnvelope = requestMsgCtx.getMessageAsOM();
            try {
                client.sendReceiveNonBlocking(ServiceClient.ANON_OUT_IN_OP, reqEnvelope, axisCallback);
            } catch (AxisFault e) {
                e.printStackTrace();
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
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw ExceptionFactory.makeWebServiceException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw ExceptionFactory.makeWebServiceException(e.getMessage());
            }
            
            return wrapper;
        }

        return null;
    }
    
    /**
     * Performs an asynchronous (non-blocking) invocation of the client based 
     * on a polling model.  The Response object that is returned allows the 
     * client programmer to poll against it to see if a response has been sent
     * back by the server.
     * 
     * @param ic
     * @return
     */
    public Response invokeAsync(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: async (polling)");
        }
        return null;
    }
    
    /**
     * Creates the OperationClient instance that will be invoked upon.
     * 
     * This will be used instead of the ServiceClient as we mature the 
     * AxisInvocationController
     */
    /*
    private OperationClient createOperationClient(MessageContext mc) {
        OperationClient client = null;
        String operationName = mc.getOperationName();
        
        if (operationName != null) {
            AxisService service = mc.getMetadata();
            AxisOperation operation = service.getOperation(new QName(operationName));
            
            if (operation == null) {
                throw ExceptionFactory.makeWebServiceException("Operation not found.");
            }
            
            try {
                ServiceContext ctx = new ServiceContext(service, );
                client = operation.createClient(ctx, null);
                client.addMessageContext(null);
            }
            catch (AxisFault af) {
                throw new WebServiceException(af);
            }            
        }
        else {
            throw ExceptionFactory.makeWebServiceException("Operation name not set.");
        }
        
        return client;
    }
    */
    
    private String getSOAPAction(MessageContext ctx){
        Boolean useSoapAction = (Boolean) ctx.getProperties().get(BindingProvider.SOAPACTION_USE_PROPERTY);
        if(useSoapAction != null && useSoapAction.booleanValue()){
            return (String) ctx.getProperties().get(BindingProvider.SOAPACTION_URI_PROPERTY);
        }
        
        return null;
    }
}
