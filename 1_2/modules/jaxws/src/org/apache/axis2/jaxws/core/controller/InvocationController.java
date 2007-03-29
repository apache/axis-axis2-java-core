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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The <tt>InvocationController</tt> is an abstract implementation modeling
 * the invocation of a target web service.  All of the information that the 
 * InvocationController needs should exist within the InvocatonContext
 * that is passed in to the various invoke methods.  
 * 
 * The request information is passed in within the InvocationContext.  The
 * InvocationController assumes that there is a MessageContext within that
 * InvocationContext that is populated with all of the information that it
 * needs to invoke.  If not, an error will be returned.  Once the response 
 * comes back, the information for that response will be held inside of the
 * MessageContext representing the response, that exists in the 
 * InvocationContext.
 * 
 * The InvocationController supports four different invocation patterns:
 * 
 * 1) synchronous - This is represented by the {@link #invoke(InvocationContext)}
 * method.  This is a blocking, request/response call to the web service.
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
public abstract class InvocationController {
    
    private static final Log log = LogFactory.getLog(InvocationController.class);
    
    /**
     * Performs a synchronous (blocking) invocation of a target service.  The 
     * InvocationContext passed in should contain a valid MessageContext 
     * containing the properties and message to be sent for the request.  The
     * response contents will be processed and placed in the InvocationContext
     * as well.
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
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }
        
        MessageContext request = ic.getRequestMessageContext();
        MessageContext response = null;

        request.getProperties().put(Constants.INVOCATION_PATTERN, InvocationPattern.SYNC);
        
        // Invoke outbound handlers.
        // TODO uncomment, and get the EndpointDescription from the request context, which should soon be available
        boolean success = true; //HandlerInvokerUtils.invokeOutboundHandlers(request, request.getEndpointDescription(), HandlerChainProcessor.MEP.REQUEST, false);

        if (success) {
        	prepareRequest(request);
        	response = doInvoke(request);
        	prepareResponse(response);
        
        	// Invoke inbound handlers.
        	// TODO uncomment, and get the EndpointDescription from the request context, which should soon be available
        	//HandlerInvokerUtils.invokeInboundHandlers(response, request.getEndpointDescription(), HandlerChainProcessor.MEP.RESPONSE, false);
        } else { // the outbound handler chain must have had a problem, and we've reversed directions
        	response = MessageContextUtils.createResponseMessageContext(request);
        	// since we've reversed directions, the message has "become a response message" (section 9.3.2.1, footnote superscript 2)
        	response.setMessage(request.getMessage());
        }
        ic.setResponseMessageContext(response);
        return ic;
    }
    
    protected abstract MessageContext doInvoke(MessageContext request);
    
    /**
     * Performs a one-way invocation of the client.  This is SHOULD NOT be a 
     * robust invocation, so any fault that occurs during the processing of 
     * the request will not be returned to the client.  Errors returned to the
     * client are problems that occurred during the sending of the message to 
     * the server.
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
            throw ExceptionFactory.makeWebServiceException("ICErr1");
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException("ICErr2");
        }
        
        MessageContext request = ic.getRequestMessageContext();
        request.getProperties().put(Constants.INVOCATION_PATTERN, InvocationPattern.ONEWAY);

        // Invoke outbound handlers.
        // TODO uncomment, and get the EndpointDescription from the request context, which should soon be available
        boolean success = true; //HandlerInvokerUtils.invokeOutboundHandlers(request, request.getEndpointDescription(), HandlerChainProcessor.MEP.REQUEST, false);

        if (success) {
        	prepareRequest(request);
            doInvokeOneWay(request);
        }
        return;
    }
    
    protected abstract void doInvokeOneWay(MessageContext mc);
    
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
    public Response invokeAsync(InvocationContext ic) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: asynchronous(polling)");
        }
        
        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }
        
        MessageContext request = ic.getRequestMessageContext();
        request.getProperties().put(Constants.INVOCATION_PATTERN, InvocationPattern.ASYNC_POLLING);

        Response resp = null;
        
        // Invoke outbound handlers.
        // TODO uncomment, and get the EndpointDescription from the request context, which should soon be available
        boolean success = true; //HandlerInvokerUtils.invokeOutboundHandlers(request, request.getEndpointDescription(), HandlerChainProcessor.MEP.REQUEST, false);
        if (success) {
        	prepareRequest(request);
        	resp = doInvokeAsync(request);
        } else { // the outbound handler chain must have had a problem, and we've reversed directions
        	// since we've reversed directions, the message has "become a response message" (section 9.3.2.1, footnote superscript 2)

        	// TODO we know the message is a fault message, we should
        	// convert it to an exception and throw it.
        	// something like:
        	
        	//throw new AxisFault(request.getMessage());
        }

        return resp;
    }
    
    public abstract Response doInvokeAsync(MessageContext mc);
    
    /**
     * Performs an asynchronous (non-blocking) invocation of the client based 
     * on a polling model.  The Response object that is returned allows the 
     * client programmer to poll against it to see if a response has been sent
     * back by the server.
     * 
     * @param ic
     * @return
     */
    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler) {
        if (log.isDebugEnabled()) {
            log.debug("Invocation pattern: asynchronous(callback)");
        }
        
        // Check to make sure we at least have a valid InvocationContext
        // and request MessageContext
        if (ic == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr1"));
        }
        if (ic.getRequestMessageContext() == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ICErr2"));
        }
        if ((ic.getExecutor() != null) && (ic.getExecutor() instanceof ExecutorService))
        {
            ExecutorService es = (ExecutorService) ic.getExecutor();
            if (es.isShutdown())
            {
                // the executor service is shutdown and won't accept new tasks
                // so return an error back to the client
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("ExecutorShutdown"));
            }
        }

        MessageContext request = ic.getRequestMessageContext();
        request.getProperties().put(Constants.INVOCATION_PATTERN, InvocationPattern.ASYNC_CALLBACK);

        Future<?> future = null;
        
        // Invoke outbound handlers.
        // TODO uncomment, and get the EndpointDescription from the request context, which should soon be available
        boolean success = true; //HandlerInvokerUtils.invokeOutboundHandlers(request, request.getEndpointDescription(), HandlerChainProcessor.MEP.REQUEST, false);
        if (success) {
        	prepareRequest(request);
        	future = doInvokeAsync(request, asyncHandler);
        } else { // the outbound handler chain must have had a problem, and we've reversed directions
        	// since we've reversed directions, the message has "become a response message" (section 9.3.2.1, footnote superscript 2)

        	// TODO we know the message is a fault message, we should
        	// convert it to an exception and throw it.
        	// something like:

        	//throw new AxisFault(request.getMessage());
        }
        return future;        
    }
    
    public abstract Future<?> doInvokeAsync(MessageContext mc, AsyncHandler asyncHandler);
    
    /**
     * Abstract method that must be implemented by whoever is providing
     * the specific client binding.  Once this is called, everything that
     * is needed to invoke the operation must be available in the 
     * MessageContext.
     * @param mc
     */
    protected abstract void prepareRequest(MessageContext mc);
    
    /**
     * Abstract method that must be implemented by whoever is providing
     * the specific client binding.  This is called after the response has 
     * come back and allows the client binding to put whatever info it has
     * in the response MessageContext.
     * @param mc
     */
    protected abstract void prepareResponse(MessageContext mc);
    
}
