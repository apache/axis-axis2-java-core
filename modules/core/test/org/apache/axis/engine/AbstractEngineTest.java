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

package org.apache.axis.engine;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.description.Parameter;
import org.apache.axis.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis.transport.TransportSender;

public class AbstractEngineTest extends TestCase {
    protected MessageContext mc;
    protected ArrayList executedHandlers = new ArrayList();
    protected AxisSystem engineRegistry;
    protected QName serviceName = new QName("axis/services/NullService");
    protected QName opearationName = new QName("NullOperation");
    protected AxisService service;

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

        public MessageContext invokeBusinessLogic(MessageContext inMessage,MessageContext outMessage)
            throws AxisFault {
            return inMessage;
        }

    }

    public class NullTransportSender implements TransportSender {
        public void cleanup() throws AxisFault {
        }

        public QName getName() {
            return null;
        }

        public Parameter getParameter(String name) {
            return null;
        }

        public void init(HandlerMetadata handlerdesc) {
        }

        public void invoke(MessageContext msgContext) throws AxisFault {
        }

        public void revoke(MessageContext msgContext) {
        }

    }

}
