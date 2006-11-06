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

import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;

public class CallbackFuture extends Callback {
    
    private CallbackFutureTask cft;
    private Executor executor;
    private FutureTask task;
   
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
        cft.setResult(result);
        execute();
    }

    @Override
    public void onError(Exception e) {
         cft.setError(e);
         execute();
    }
    
    private void execute() {
        if (executor != null) {
            executor.execute(task);
        }
    }    
}

class CallbackFutureTask implements Callable {
    
    AsyncResponse response;
    AsyncResult result;
    AsyncHandler handler;
    Exception error;
    
    CallbackFutureTask(AsyncResponse r, AsyncHandler h) {
        response = r;
        handler = h;
    }
    
    void setResult(AsyncResult r) {
        result = r;
    }
    
    void setError(Exception e) {
        error = e;
    }
    
    public Object call() throws Exception {
        if (result != null) {
            response.onComplete(result);    
        }
        else if (error != null) {
            response.onError(error);
        }
        
        handler.handleResponse(response);        
        return null;
    }
}