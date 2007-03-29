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

import javax.xml.ws.WebServiceException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PollingFuture extends Callback {

    private static final Log log = LogFactory.getLog(PollingFuture.class);
    
    private AsyncResponse response;
    
    public PollingFuture(AsyncResponse ar) {
        response = ar;
    }
    
    @Override
    public void onComplete(AsyncResult result) {
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("JAX-WS async response listener received the response");
        }
        
        MessageContext responseMsgCtx = null;
        try {
            responseMsgCtx = AsyncUtils.createJAXWSMessageContext(result);
        } catch (WebServiceException e) {
            response.onError(e, null);
            if (debug) {
                log.debug("An error occured while processing the async response.  " + e.getMessage());
            }
        }
        
        if (response == null) {
            // TODO: throw an exception
        }
        
        response.onComplete(responseMsgCtx);
    }

    @Override
    public void onError(Exception e) {
        // If a SOAPFault was returned by the AxisEngine, the AxisFault
        // that is returned should have a MessageContext with it.  Use
        // this to unmarshall the fault included there.
        if (e.getClass().isAssignableFrom(AxisFault.class)) {
            AxisFault fault = (AxisFault) e;
            MessageContext faultMessageContext = null;
            try {
                faultMessageContext  = AsyncUtils.createJAXWSMessageContext(fault.getFaultMessageContext());                
            }
            catch (WebServiceException wse) {
                response.onError(wse, null);
            }
                    
            response.onError(e, faultMessageContext);
        }
        else {
            response.onError(e, null);
        }
    }

}

