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

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;

public class AbstractEngineTest extends TestCase {
//    protected ArrayList executedHandlers = new ArrayList();
//    protected AxisConfiguration engineRegistry;
//    protected QName serviceName = new QName("axis/services/NullService");
//    protected QName opearationName = new QName("NullOperation");
//    protected ServiceDescription service;

    public AbstractEngineTest() {
    }

    public AbstractEngineTest(String arg0) {
        super(arg0);
    }

    //    public class TempHandler extends AbstractHandler {
    //        private Integer index;
    //        private boolean pause = false;
    //        public TempHandler(int index, boolean pause) {
    //            this.index = new Integer(index);
    //            this.pause = pause;
    //        }
    //        public TempHandler(int index) {
    //            this.index = new Integer(index);
    //        }
    //
    //        public void invoke(MessageContext msgContext) throws AxisFault {
    //            executedHandlers.add(index);
    //            if (pause) {
    //                msgContext.setPaused(true);
    //            }
    //        }
    //
    //    }

    public class NullMessageReceiver extends AbstractInOutSyncMessageReceiver {

        public void invokeBusinessLogic(
            MessageContext inMessage,
            MessageContext outMessage)
            throws AxisFault {
            
        }
    }
}
