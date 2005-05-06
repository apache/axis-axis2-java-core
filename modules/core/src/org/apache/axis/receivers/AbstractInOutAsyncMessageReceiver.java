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
package org.apache.axis.receivers;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is takes care of the IN-OUT sync MEP in the server side
 */
public abstract class AbstractInOutAsyncMessageReceiver
    extends AbstractMessageReceiver {
        protected Log log = LogFactory.getLog(getClass());
    public abstract void invokeBusinessLogic(MessageContext inMessage,ServerCallback callback)
        throws AxisFault;

    public final void recieve(final MessageContext messgeCtx) throws AxisFault {
        final ServerCallback callback = new ServerCallback() {
            public void handleResult(MessageContext result)throws AxisFault {
                MessageSender sender =
                    new MessageSender(messgeCtx.getOperationContext().getServiceContext().getEngineContext());
                sender.send(messgeCtx);
            }
            public void handleFault(AxisFault fault)throws AxisFault{
                AxisEngine engine = new AxisEngine(messgeCtx.getOperationContext().getServiceContext().getEngineContext());
                engine.handleFault(messgeCtx,fault);
            }
        };
        Runnable theadedTask = new Runnable() {
            public void run() {
                try {
                    invokeBusinessLogic(messgeCtx,callback);
                } catch (AxisFault e) {
                    log.error(e);
                }
            }
        };
        (new Thread(theadedTask)).start();
    }

}
