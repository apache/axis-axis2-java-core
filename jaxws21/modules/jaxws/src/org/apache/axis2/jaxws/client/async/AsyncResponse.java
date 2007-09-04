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
package org.apache.axis2.jaxws.client.async;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.HandlerChainProcessor;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;
import org.apache.axis2.jaxws.handler.TransportHeadersAdapter;
import org.apache.axis2.jaxws.message.attachments.AttachmentUtils;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * The AsyncResponse class is used to collect the response information from Axis2 and deliver it to
 * a JAX-WS client.  AsyncResponse implements the <link>javax.xml.ws.Response</link> API that is
 * defined in the JAX-WS 2.0 specification.  The <code>Response</code> object will contain both the
 * object that is returned as the response along with a <link>java.util.Map</link> with the context
 * information of the response.
 */
public abstract class AsyncResponse implements Response {

    private static final Log log = LogFactory.getLog(AsyncResponse.class);

    private boolean cancelled;

    private Throwable fault;
    private MessageContext faultMessageContext;
    private MessageContext response;

    private EndpointDescription endpointDescription;
    private Map<String, Object> responseContext;

    /* 
     * CountDownLatch is used to track whether we've received and
     * processed the async response.  For example, the client app
     * could be polling on 30 second intervals, and we don't receive
     * the async response until the 1:15 mark.  In that case, the
     * first few polls calling the .get() would hit the latch.await()
     * which blocks the thread if the latch count > 0
     */
    private CountDownLatch latch;
    private boolean cacheValid = false;
    private Object cachedObject = null;

    // we need to ensure the classloader used under onComplete (where the response object is unmarshalled) is
    // the same classloader as the one used by the client app, otherwise we'll get a strange ClassCastException
    // This object is just a cache object.
    private ClassLoader classLoader = null;
    
    // the object to be returned
    private Object obj = null;
    // we need to save an exception if processResponse fails
    private ExecutionException savedException = null;
    
    protected AsyncResponse(EndpointDescription ed) {
        endpointDescription = ed;
        latch = new CountDownLatch(1);
    }

    protected void onError(Throwable flt, MessageContext mc, ClassLoader cl) {
        setThreadClassLoader(cl);
        onError(flt, mc);
        ClassLoader origClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        setThreadClassLoader(origClassLoader);
    }
    
    protected void onError(Throwable flt, MessageContext faultCtx) {
        if (log.isDebugEnabled()) {
            log.debug("AsyncResponse received a fault.  Counting down latch.");
        }

        fault = flt;
        faultMessageContext = faultCtx;
        faultMessageContext.setEndpointDescription(endpointDescription);

        // Probably a good idea to invalidate the cache
        cacheValid = false;
        cachedObject = null;

        Throwable t = processFaultResponse();
        
        if (log.isDebugEnabled()) {
            log.debug("New latch count = [" + latch.getCount() + "]");
        }
        
        // JAXWS 4.3.3 conformance bullet says to throw an ExecutionException from here
        savedException = new ExecutionException(t);
    }
    
    private void setThreadClassLoader(final ClassLoader cl) {
        if (this.classLoader != null) {
            if (!this.classLoader.getClass().equals(cl.getClass())) {
                throw ExceptionFactory.makeWebServiceException("Attemping to use ClassLoader of type " + cl.getClass().toString() +
                                                               ", which is incompatible with current ClassLoader of type " +
                                                               this.classLoader.getClass().toString());
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Setting up the thread's ClassLoader");
                log.debug(cl.toString());
            }
            this.classLoader = cl;
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(cl);
                    return null;
                }
            });
        }
    }
    
    protected void onComplete(MessageContext mc, ClassLoader cl) {
        setThreadClassLoader(cl);
        onComplete(mc);
        ClassLoader origClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        setThreadClassLoader(origClassLoader);
    }

    protected void onComplete(MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug("AsyncResponse received a MessageContext. Counting down latch.");
        }

        // A new message context invalidates the cached object retrieved
        // during the last get()
        if (response != mc) {
            cachedObject = null;
            cacheValid = false;
        }

        response = mc;
        response.setEndpointDescription(endpointDescription);
        
        // Check for cached attachment file(s) if attachments exist. 
        if(response.getAxisMessageContext().getAttachmentMap() != null){
        	AttachmentUtils.findCachedAttachment(response.getAxisMessageContext().getAttachmentMap());
        }
        
        /*
         * TODO: review?
         * We need to process the response right when we get it, instead of
         * caching it away for processing when the client poller calls .get().
         * Reason for this is that some platforms (or web containers) will close
         * down their threads immediately after "dropping off" the async response.
         * If those threads disappear, the underlying input stream object may also
         * disappear, thus causing a NullPointerException later when we try to .get().
         * The NPE would manifest itself way down in the parser.
         */
        try {
            obj = processResponse();
        } catch (ExecutionException e) {
            savedException = e;
        }

        if (log.isDebugEnabled()) {
            log.debug("New latch count = [" + latch.getCount() + "]");
        }
    }

    //-------------------------------------
    // javax.xml.ws.Response APIs
    //-------------------------------------

    public boolean cancel(boolean mayInterruptIfRunning) {
        // The task cannot be cancelled if it has already been cancelled
        // before or if it has already completed.
        if (cancelled || latch.getCount() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Cancellation attempt failed.");
            }
            return false;
        }

        cancelled = true;
        return cancelled;
    }

    public Object get() throws InterruptedException, ExecutionException {
        if (cancelled) {
            throw new CancellationException("The task was cancelled.");
        }

        // Wait for the response to come back
        if (log.isDebugEnabled()) {
            log.debug("Waiting for async response delivery.");
        }
        
        // If latch count > 0, it means we have not yet received
        // and processed the async response, and must block the
        // thread.
        latch.await();

        if (savedException != null) {
            throw savedException;
        }
        
        return obj;
    }

    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (cancelled) {
            throw new CancellationException("The task was cancelled.");
        }

        // Wait for the response to come back
        if (log.isDebugEnabled()) {
            log.debug("Waiting for async response delivery with time out.");
            log.debug("timeout = " + timeout);
            log.debug("units   = " + unit);
        }
        
        // latch.await will only block if its count is > 0
        latch.await(timeout, unit);

        if (savedException != null) {
            throw savedException;
        }
        
        // If the response still hasn't been returned, then we've timed out
        // and must throw a TimeoutException
        if (latch.getCount() > 0) {
            throw new TimeoutException(
                    "The client timed out while waiting for an asynchronous response");
        }

        return obj;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return (latch.getCount() == 0);
    }

    public Map getContext() {
        return responseContext;
    }

    private Object processResponse() throws ExecutionException {
        /*
         * note the latch.countDown() here.  We have to make sure the countdown
         * occurs everywhere we might leave the method, which could be a return
         * or throw.
         */
    	
        // If we don't have a fault, then we have to have a MessageContext for the response.
        if (response == null) {
        	latch.countDown();
            throw new ExecutionException(ExceptionFactory.makeWebServiceException("null response"));
        }

        // Avoid a reparse of the message. If we already retrived the object, return
        // it now.
        if (cacheValid) {
            if (log.isDebugEnabled()) {
                log.debug("Return object cached from last get()");
            }
            latch.countDown();
            return cachedObject;
        }

        Object obj = null;
        try {
            // TODO: IMPORTANT: this is the right call here, but beware that the messagecontext may be turned into
            // a fault context with a fault message.  We need to check for this and, if necessary, make an exception and throw it.
            // Invoke inbound handlers.
            TransportHeadersAdapter.install(response);
            AttachmentsAdapter.install(response);
            HandlerInvokerUtils.invokeInboundHandlers(response.getMEPContext(),
                                                      response.getInvocationContext().getHandlers(),
                                                      HandlerChainProcessor.MEP.RESPONSE,
                                                      false);

            // TODO: Check the type of the object to make sure it corresponds with
            // the parameterized generic type.
            if (log.isDebugEnabled()) {
                log.debug("Unmarshalling the async response message.");
            }
            
            obj = getResponseValueObject(response);
            // Cache the object in case it is required again
            cacheValid = true;
            cachedObject = obj;
            
            if (log.isDebugEnabled() && obj != null) {
                log.debug("Unmarshalled response object of type: " + obj.getClass());
            }

            responseContext = new HashMap<String, Object>();

            // Migrate the properties from the response MessageContext back
            // to the client response context bag.
            ApplicationContextMigratorUtil.performMigrationFromMessageContext(Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                                                                              responseContext,
                                                                              response);
            latch.countDown();
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while processing the response");
            }
            latch.countDown();
            throw new ExecutionException(ExceptionFactory.makeWebServiceException(t));
        }

        return obj;
    }

    private Throwable processFaultResponse() {
        // A faultMessageContext means that there could possibly be a SOAPFault
        // on the MessageContext that we need to unmarshall.
        if (faultMessageContext != null) {
        	Throwable throwable = null;
            // it is possible the message could be null.  For example, if we gave the proxy a bad endpoint address.
            // If it is the case that the message is null, there's no sense running through the handlers.
            if (faultMessageContext.getMessage() != null) {
                // The adapters are intentionally NOT installed here.  They cause unit test failures
                // TransportHeadersAdapter.install(faultMessageContext);
                // AttachmentsAdapter.install(faultMessageContext);
            	try {
                    // Invoke inbound handlers.
                    HandlerInvokerUtils.invokeInboundHandlers(faultMessageContext.getMEPContext(),
                                                          faultMessageContext.getInvocationContext()
                                                                             .getHandlers(),
                                                          HandlerChainProcessor.MEP.RESPONSE,
                                                          false);
            	} catch (Throwable t) {
            		throwable = t;
            	}
            }
            if (throwable == null) {
                throwable = getFaultResponse(faultMessageContext);
            }
            latch.countDown();
            if (throwable != null) {
                return throwable;
            } else {
                return ExceptionFactory.makeWebServiceException(fault);
            }
        } else {
        	latch.countDown();
            return ExceptionFactory.makeWebServiceException(fault);
        }
    }

    public abstract Object getResponseValueObject(MessageContext mc);

    public abstract Throwable getFaultResponse(MessageContext mc);

}
