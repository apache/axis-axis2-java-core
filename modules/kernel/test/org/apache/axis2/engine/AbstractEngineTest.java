/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;

public class AbstractEngineTest extends TestCase {
    public AbstractEngineTest() {
    }

    public AbstractEngineTest(String arg0) {
        super(arg0);
    }

    public class NullMessageReceiver extends AbstractMessageReceiver {

        public void invokeBusinessLogic(MessageContext inMessage,
                                        MessageContext outMessage)
                throws AxisFault {

        }

        public final void invokeBusinessLogic(MessageContext msgContext) throws AxisFault {
            MessageContext outMsgContext = MessageContextBuilder.createOutMessageContext(msgContext);
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);

            invokeBusinessLogic(msgContext, outMsgContext);

            AxisEngine.send(outMsgContext);
        }
    }
}
