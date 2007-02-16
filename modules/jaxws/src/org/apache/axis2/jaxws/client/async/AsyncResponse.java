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
package org.apache.axis2.jaxws.client.async;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.Response;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The AsyncResponse class is used to collect the response information from
 * Axis2 and deliver it to a JAX-WS client.  AsyncResponse implements the 
 * <link>javax.xml.ws.Response</link> API that is defined in the JAX-WS 2.0
 * specification.  The <code>Response</code> object will contain both the 
 * object that is returned as the response along with a <link>java.util.Map</link>
 * with the context information of the response.  
 */
public abstract class AsyncResponse implements Response {

    private static final Log log = LogFactory.getLog(AsyncResponse.class);
    
    private boolean cancelled;
    
    private Throwable fault;
    private MessageContext faultMessageContext;    
    private MessageContext response;
    
    private Map<String, Object> responseContext;
    
    private CountDownLatch latch;
    private boolean cacheValid = false;
    private Object cachedObject = null;
    
    protected AsyncResponse() {
        latch = new CountDownLatch(1);
    }
    
    protected void onError(Throwable flt, MessageContext faultCtx) {
        if (log.isDebugEnabled()) {
            log.debug("AsyncResponse received a fault.  Counting down latch.");
        }

        fault = flt;
        faultMessageContext = faultCtx;
        
        // Probably a good idea to invalidate the cache
        cacheValid = false;
        cachedObject = null;

        latch.countDown();
        if (log.isDebugEnabled()) {
            log.debug("New latch count = [" + latch.getCount() + "]");
        }
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
        latch.countDown();
        
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
        latch.await();
        
        Object obj = processResponse();
        return obj;
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (cancelled) {
            throw new CancellationException("The task was cancelled.");
        }
        
        // Wait for the response to come back
        if (log.isDebugEnabled()) {
            log.debug("Waiting for async response delivery with time out.");
            log.debug("timeout = " + timeout);
            log.debug("units   = " + unit);
        }
        latch.await(timeout, unit);
        
        // If the response still hasn't been returned, then we've timed out
        // and must throw a TimeoutException
        if (latch.getCount() > 0) {
            throw new TimeoutException("The client timed out while waiting for an asynchronous response");
        }
        
        Object obj = processResponse();
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
    
    private void initResponseContext() {
        responseContext = new HashMap<String, Object>();
    }
    
    private Object processResponse() throws ExecutionException {
        // If the fault object is not null, then we've received a fault message and 
        // we need to process it in one of a number of forms.
        if (fault != null) {
            if (log.isDebugEnabled()) {
                log.debug("A fault was found.  Starting to process fault response.");
            }
            Throwable t = processFaultResponse();
            // JAXWS 4.3.3 conformance bullet says to throw an ExecutionException from here
            throw new ExecutionException(t);
        }

        // If we don't have a fault, then we have to have a MessageContext for the response.
        if (response == null) {
            throw new ExecutionException(ExceptionFactory.makeWebServiceException("null response"));
        }
        
        // Avoid a reparse of the message. If we already retrived the object, return
        // it now.
        if (cacheValid) {
            if (log.isDebugEnabled()) {
                log.debug("Return object cached from last get()");
            }
            return cachedObject;
        }

        // TODO: Check the type of the object to make sure it corresponds with
        // the parameterized generic type.
        Object obj = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Unmarshalling the async response message.");
             }
             obj = getResponseValueObject(response);
             // Cache the object in case it is required again
             cacheValid = true;
             cachedObject = obj;      
        }
        catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while processing the response");
            }
            throw new ExecutionException(ExceptionFactory.makeWebServiceException(t));
        }

        if (log.isDebugEnabled() && obj != null) {
            log.debug("Unmarshalled response object of type: " + obj.getClass());
        }
        
        initResponseContext();
        
        return obj;
    }
    
    private Throwable processFaultResponse() {
        // A faultMessageContext means that there could possibly be a SOAPFault
        // on the MessateContext that we need to unmarshall.
        if (faultMessageContext != null) {
            Throwable t = getFaultResponse(faultMessageContext);
            if (t != null) {  
                return t;
            }
            else {
                return ExceptionFactory.makeWebServiceException(fault);
            }
        }
        else {
            return ExceptionFactory.makeWebServiceException(fault);
        }        
    }
    
    public abstract Object getResponseValueObject(MessageContext mc);
    
    public abstract Throwable getFaultResponse(MessageContext mc);

}
