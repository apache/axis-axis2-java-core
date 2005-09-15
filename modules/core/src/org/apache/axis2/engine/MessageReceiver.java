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
 * This one receives the Message, what is does is not of concern for Axis2 (litirally).
 * Any incomming message is hand over to the MessageReceiver, if the processing produce something 
 * or not as well as is there areany more SOAP Message to be sent or recived is up to the
 * Message Receiver to decide.
 */
public interface MessageReceiver {
    public void receive(MessageContext messgeCtx) throws AxisFault;
}
