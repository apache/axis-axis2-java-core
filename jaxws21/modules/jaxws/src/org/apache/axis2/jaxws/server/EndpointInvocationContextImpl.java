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
package org.apache.axis2.jaxws.server;

import org.apache.axis2.jaxws.core.InvocationContextImpl;
import org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher;

public class EndpointInvocationContextImpl extends InvocationContextImpl
    implements EndpointInvocationContext {

    private EndpointCallback callback;
    private EndpointDispatcher dispatcher;
    private Boolean oneWay;
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointInvocationContext#getCallback()
     */
    public EndpointCallback getCallback() {
        return callback;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointInvocationContext#getDispatcher()
     */
    public EndpointDispatcher getDispatcher() {
        return dispatcher;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointInvocationContext#setCallback(org.apache.axis2.jaxws.server.EndpointCallback)
     */
    public void setCallback(EndpointCallback cb) {
        callback = cb;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointInvocationContext#setEndpointDispatcher(org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher)
     */
    public void setEndpointDispatcher(EndpointDispatcher ed) {
        dispatcher = ed;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointInvocationContext#isOneWay()
     */
    public boolean isOneWay() {
        if (oneWay != null) {
            return oneWay.booleanValue();    
        }
        else {
            // TODO: We can add some code to derive it here 
            // should we see fit.
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointInvocationContext#setIsOneWay(boolean)
     */
    public void setIsOneWay(boolean value) {
        oneWay = value;
    }

}
