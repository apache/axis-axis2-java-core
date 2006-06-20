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

import org.apache.axis2.jaxws.AxisCallback;
import org.apache.axis2.jaxws.param.Parameter;


/**
 * The AsyncResponseProcessor is responsible for collecting the async response
 * from Axis2 and saving the value of the response.  This is should be called
 * from within the Executor configured by the JAX-WS Service (or ServiceDelegate).
 */
public class AsyncResponseProcessor implements Callable {

    private AxisCallback axisCallback;
    private Mode mode;
    private Parameter param;
    
    public AsyncResponseProcessor(AxisCallback cb) {
        axisCallback = cb;
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    //TODO: Need to re-work Parameter usage so that we're not using the same instance.
    //Should be based off of parameter types so that we're not possibly contaminating a 
    //previous object.
    public void setParameter(Parameter p) {
        param = p;
    }
    
    /**
     * This method will be called to collect the async response from Axis2.  
     */
    public Object call() throws Exception {
        if (axisCallback != null) {
            while (!axisCallback.isComplete()) {
                //System.out.println(">> AsyncResponseProcessor - waiting for response");
                //TODO: The wait period should probably be configurable
                Thread.sleep(1000);
            }
            
            //System.out.println(">> AsyncResponseProcessor - response received, processing");
            param.fromEnvelope(mode, axisCallback.getSOAPEnvelope());
            return param.getValue();            
        }
        
        return null;
    }
}
