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

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The CallbackFuture implements the Axis2 <link>org.apache.axis2.client.async.Callback</link>
 * API and will get registered with the Axis2 engine to receive the asynchronous
 * callback responses.  This object is also responsible for taking the 
 * <link>java.util.concurrent.Executor</link> given to it by the JAX-WS client
 * and using that as the thread on which to deliver the async response the 
 * JAX-WS <link>javax.xml.ws.AsynchHandler</link>.
 */
public class CallbackFuture extends Callback {
    
    private static final Log log = LogFactory.getLog(CallbackFuture.class);
    private static final boolean debug = log.isDebugEnabled();
    
    private CallbackFutureTask cft;
    private Executor executor;
    private FutureTask task;
   
    @SuppressWarnings("unchecked")
    public CallbackFuture(AsyncResponse response, AsyncHandler handler, Executor exec) {
        cft = new CallbackFutureTask(response, handler);
        task = new FutureTask(cft);
        executor = exec;
    }
    
    public Future<?> getFutureTask() {
        return (Future<?>) task;
    }
    
    @Override
    public void onComplete(AsyncResult result) {
        if (debug) {
            log.debug("JAX-WS received the async response");
        }
        
        MessageContext response = null;
        try {
            response = AsyncUtils.createMessageContext(result);
        } catch (WebServiceException e) {
            cft.setError(e);
            if (debug) {
                log.debug("An error occured while processing the async response.  " + e.getMessage());
            }
        }
        
        if (response == null) {
            // TODO: throw an exception
        }
        
        cft.setMessageContext(response);
        execute();
    }

    @Override
    public void onError(Exception e) {
         cft.setError(e);
         execute();
    }
    
    private void execute() {
        if (log.isDebugEnabled()) {
            log.debug("Executor task starting to process async response");
        }
        
        if (executor != null) {
            executor.execute(task);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Executor task completed");
        }
    }    
}

class CallbackFutureTask implements Callable {
    
    private static final Log log = LogFactory.getLog(CallbackFutureTask.class);
    private static final boolean debug = log.isDebugEnabled();
    
    AsyncResponse response;
    MessageContext responseMsgCtx;
    AsyncHandler handler;
    Exception error;
    
    CallbackFutureTask(AsyncResponse r, AsyncHandler h) {
        response = r;
        handler = h;
    }
    
    void setMessageContext(MessageContext mc) {
        responseMsgCtx = mc;
    }
    
    void setError(Exception e) {
        error = e;
    }
    
    @SuppressWarnings("unchecked")
    public Object call() throws Exception {
        if (responseMsgCtx != null) {
            response.onComplete(responseMsgCtx);    
        }
        else if (error != null) {
            response.onError(error);
        }
        
        try {
            if (debug) {
                log.debug("Calling JAX-WS AsyncHandler with the Response object");
                log.debug("AyncHandler class: " + handler.getClass());
            }
            handler.handleResponse(response);    
        }
        catch (Throwable t) {
            if (debug) {
                log.debug("An error occured while invoking the callback object.");
                log.debug("Error: " + t.getMessage());
            }
        }
                
        return null;
    }
}