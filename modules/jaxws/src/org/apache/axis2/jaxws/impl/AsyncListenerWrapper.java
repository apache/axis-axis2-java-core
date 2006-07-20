/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.impl;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

/**
 * The AsyncListenerWrapper is what wraps the AsyncListener that waits for
 * the response.  The AsyncListenerWrapper controls the lifecycle of the 
 * listener, determining when it is started and stopped.
 * 
 * The wrapper is also the item that will call the users AsyncHandler that
 * they provided when they made the asynchronous call.
 */
public class AsyncListenerWrapper<T> extends FutureTask<T> implements Response<T> {

    private Map<String, Object> responseCtx;
    private AsyncHandler asyncHandler;
    
    public AsyncListenerWrapper(Callable<T> processor) {
        super(processor);
    }
    
    public void setAsyncHandler(AsyncHandler ah) {
        asyncHandler = ah;
    }
    
    public Map<String, Object> getContext() {
        return responseCtx;
    }
    
    protected void done() {
        super.done();
        
        if(!isCancelled()){
            if(asyncHandler != null){
                asyncHandler.handleResponse(this);
            }
        }
    }
}
