/*
* Copyright 2004,2005 The Apache Software Foundation.
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


package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;

/**
 * Interface Handler
 */
public interface Handler {

    /**
     * Method cleanup.
     */
    public void cleanup();

    /**
     * Method init.
     *
     * @param handlerdesc
     */
    public void init(HandlerDescription handlerdesc);

    /**
     * This method will be called on each registered handler when a message
     * needs to be processed.  If the message processing is paused by the
     * handler, then this method will be called again for the handler that
     * paused the processing once it is resumed.
     * 
     * This method may be called concurrently from multiple threads.
     * 
     * Handlers that want to determine the type of message that is to be
     * processed (e.g. response vs request, inbound vs. outbound, etc.) can
     * retrieve that information from the MessageContext via
     * MessageContext.getFLOW() and
     * MessageContext.getAxisOperation().getMessageExchangePattern() APIs.
     *
     * @param msgContext the <code>MessageContext</code> to process with this
     *                   <code>Handler</code>.
     * @throws AxisFault if the handler encounters an error
     */
    public void invoke(MessageContext msgContext) throws AxisFault;
    
    /**
     * This method will be called on each registered handler that had its
     * invoke(...) method called during the processing of the message, once
     * the message processing has completed.  During execution of the
     * flowComplete's, handlers are invoked in the opposite order that they
     * were invoked originally.
     * 
     * @param msgContext the <code>MessageContext</code> to process with this
     *                   <code>Handler</code>.
     */
    public void flowComplete(MessageContext msgContext);

    /**
     * Gets the HandlerDescription of a handler. This is used as an input to get phaseRule of a handler.
     *
     * @return Returns HandlerDescription.
     */
    public HandlerDescription getHandlerDesc();

    /**
     * Method getName.
     *
     * @return Returns String
     */
    public String getName();

    /**
     * Method getParameter.
     *
     * @param name
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name);
}
