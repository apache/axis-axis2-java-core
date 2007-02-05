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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Message;
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
public class AsyncResponse implements Response {

    private static final Log log = LogFactory.getLog(AsyncResponse.class);
    private static final boolean debug = log.isDebugEnabled();
    
    private boolean cancelled;
    private MessageContext response;
    private Map<String, Object> responseContext;
    private Throwable fault;
    private CountDownLatch latch;
    private boolean cacheValid = false;
    private Object cachedObject = null;
    
    protected AsyncResponse() {
        latch = new CountDownLatch(1);
    }
    
    protected void onError(Throwable t) {
        if (debug) {
            log.debug("AsyncResponse received a fault.  Counting down latch.");
            log.debug("Fault type = " + t.getClass());
        }
        
        fault = t;
        latch.countDown();
        
        // Probably a good idea to invalidate the cache
        cacheValid = false;
        cachedObject = null;
        
        if (debug) {
            log.debug("New latch count = [" + latch.getCount() + "]");
        }
    }
    
    protected void onComplete(MessageContext mc) {
        if (debug) {
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
        
        if (debug) {
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
            if (debug) {
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
        if (debug) {
            log.debug("Waiting for async response delivery.");
        }
        latch.await();
        
        Object obj = processAsyncResponse(response);
        return obj;
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (cancelled) {
            throw new CancellationException("The task was cancelled.");
        }
        
        // Wait for the response to come back
        if (debug) {
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
        
        Object obj = processAsyncResponse(response);
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
    
    private boolean hasFault() {
        if (fault != null)
            return true;
        else
            return false;
    }
    
    private void initResponseContext() {
        responseContext = new HashMap<String, Object>();
    }
    
    private Object processAsyncResponse(MessageContext ctx) throws ExecutionException {
        if (hasFault()) {
            throw new ExecutionException(ExceptionFactory.makeWebServiceException(fault));
        }
        if (ctx == null) {
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
            if (debug) {
                log.debug("Unmarshalling the async response message.");
             }
             obj = getResponseValueObject(ctx);
             // Cache the object in case it is required again
             cacheValid = true;
             cachedObject = obj;      
        }
        catch (Throwable t) {
            if (debug) {
                log.debug("An error occurred while processing the response");
            }
            throw new ExecutionException(ExceptionFactory.makeWebServiceException(t));
        }

        if (debug && obj != null) {
            log.debug("Unmarshalled response object of type: " + obj.getClass());
        }
        
        initResponseContext();
        
        return obj;
    }
    
    /**
     * A default implementation of this method that returns the contents
     * of the message in the form of an XML String.  Subclasses should override
     * this to convert the response message into whatever format they require.
     * @param msg
     */
    protected Object getResponseValueObject(MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug("Demarshalling response message as a String");
        }
        Message msg = mc.getMessage();
        OMElement om = msg.getAsOMElement();
        return om.toString();
    }

}
