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


package org.apache.axis2.receivers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;

/**
 * This is the Absract IN-OUT MEP MessageReceiver. The
 * protected abstract methods are only for the sake of breaking down the logic
 */
public abstract class AbstractInOutSyncMessageReceiver extends AbstractMessageReceiver {
    public abstract void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage)
            throws AxisFault;

    public final void receive(MessageContext msgContext) throws AxisFault {
        MessageContext outMsgContext = MessageContextBuilder.createOutMessageContext(msgContext);
        outMsgContext.getOperationContext().addMessageContext(outMsgContext);

        ThreadContextDescriptor tc = setThreadContext(msgContext);
        try {
            invokeBusinessLogic(msgContext, outMsgContext);
        } finally {
            restoreThreadContext(tc);
        }

        AxisEngine engine =
                new AxisEngine(
                        msgContext.getConfigurationContext());

        engine.send(outMsgContext);
    }
}
