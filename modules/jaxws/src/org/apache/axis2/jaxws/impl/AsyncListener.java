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

import java.util.concurrent.Callable;

import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.AxisCallback;
import org.apache.axis2.jaxws.core.MessageContext;


/**
 * The AsyncListener is responsible for holding the callback that was passed
 * to the Axis2 client and waiting for that async response to come back.  Once 
 * the response comes back, the AsyncListener will perform whatever function is
 * needed by the JAX-WS layer before returning.
 * 
 * This class implements the Callable interface and is what will be called by
 * the execute() method of the client's Executor.
 */
public class AsyncListener implements Callable {

    protected AxisCallback axisCallback;
    protected Mode mode;
    
    public AsyncListener() {
        //do nothing
    }
    
    public AsyncListener(AxisCallback cb) {
        axisCallback = cb;
    }
    
    public void setAxisCallback(AxisCallback cb) {
        axisCallback = cb;
    }
    
    //TODO: This will probably be removed or at least made a little more 
    //clear since it's only the Dispatch that's concerned with the mode.
    public void setMode(Mode m) {
        mode = m;
    }
    
    /**
     * This method will be called to collect the async response from Axis2.  
     */
    public Object call() throws Exception {
        if (axisCallback != null) {
            while (!axisCallback.isComplete()) {
                //TODO: The wait period should probably be configurable
                Thread.sleep(1000);
            }
            
            MessageContext responseMsgCtx = axisCallback.getResponseMessageContext();
            Object responseObj = getResponseValueObject(responseMsgCtx);
            return responseObj;            
        }
        else {
            //throw an Exception
        }
        
        return null;
    }
    
    /**
     * A default implementation of this method that returns the contents
     * of the message in the form of an XML String.  Subclasses should override
     * this to convert the response message into whatever format they require.
     * @param msg
     */
    protected Object getResponseValueObject(MessageContext mc) {
        OMElement msg = mc.getMessageAsOM();
        return msg.toString();
    }
}
