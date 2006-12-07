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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
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
    
    private boolean cancelled;
    private Object responseObj;
    private MessageContext response;
    private Map<String, Object> responseContext;
    private Throwable fault;
    private CountDownLatch latch;
    
    protected AsyncResponse() {
        latch = new CountDownLatch(1);
    }
    
    protected void onError(Throwable t) {
        fault = t;
        latch.countDown();
    }
    
    protected void onComplete(MessageContext mc) {
        response = mc;
        latch.countDown();
    }
    
    //-------------------------------------
    // javax.xml.ws.Response APIs
    //-------------------------------------
    
    public boolean cancel(boolean mayInterruptIfRunning) {
        // If the task has been cancelled or has completed, then we must
        // return false because the call failed.
        // If the task has NOT been cancelled or completed, then we must
        // set the appropriate flags and not allow the task to continue.

        // TODO: Do we actually need to do some level of interrupt on the
        // processing in the get() call?  If so, how?  
        if (!cancelled || !(latch.getCount() == 0)) {
            return false;
        }
        else {
            //TODO: Implement the actual cancellation.
            return false;
        }
    }

    public Object get() throws InterruptedException, ExecutionException {
        // Wait for the response to come back
        latch.await();
        
        if (hasFault()) {
            throw new ExecutionException(fault);
        }
        if (response == null) {
            WebServiceException wse = new WebServiceException("null response");
            throw new ExecutionException(wse);
        }
        
        // TODO: Check the type of the object to make sure it corresponds with
        // the parameterized generic type.
        if (responseObj == null) {
            if (log.isDebugEnabled()) {
                log.debug("Demarshalling the async response message");
            }
            responseObj = getResponseValueObject(response);
        }

        return responseObj;
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // Wait for the response to come back
        latch.await(timeout, unit);
        
        if (hasFault()) {
            throw new ExecutionException(fault);
        }
        if (response == null) {
            WebServiceException wse = new WebServiceException("null response");
            throw new ExecutionException(wse);
        }
        
        // TODO: Check the type of the object to make sure it corresponds with
        // the parameterized generic type.
        if (responseObj == null) {
            if (log.isDebugEnabled()) {
                log.debug("Demarshalling the async response message");
            }
            responseObj = getResponseValueObject(response);
        }

        return responseObj;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return (latch.getCount() == 0);
    }

    public Map getContext() {
        if (responseContext == null) {
            responseContext = new HashMap<String, Object>();
        }
        return responseContext;
    }
    
    private boolean hasFault() {
        if (fault != null)
            return true;
        else
            return false;
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
        try {
            Message msg = mc.getMessage();
            OMElement om = msg.getAsOMElement();
            return om.toString();
        } catch (MessageException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

}
