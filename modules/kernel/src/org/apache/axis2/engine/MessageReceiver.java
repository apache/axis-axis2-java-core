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

/**
 * An instance of MessageReceiver can be setup to receive messages. The application logic has no impact
 * on the Axis Engine iself. It is upto the application logic to do whatever it needs. For e.g.
 * the MessageReceiver can handle a message, send a response back and/or send other messages.
 */
public interface MessageReceiver {
    public void receive(MessageContext messageCtx) throws AxisFault;
}
