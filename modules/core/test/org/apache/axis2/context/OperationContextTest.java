/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.context;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisFault;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public class OperationContextTest extends AbstractTestCase {

    private ConfigurationContext engineCtx = new ConfigurationContext(null);

    public OperationContextTest(String arg0) {
        super(arg0);
    }

    public void testMEPfindingOnRelatesTO() throws Exception {

        ServiceContext sessionContext = new ServiceContext(new ServiceDescription(), new ConfigurationContext(null));
        MessageContext messageContext1 = this.getBasicMessageContext();

        messageContext1.setMessageID(new Long(System.currentTimeMillis()).toString());
        OperationDescription axisOperation = new OperationDescription(new QName("test"));
        OperationContext operationContext1 = axisOperation.findOperationContext(messageContext1, sessionContext);

        MessageContext messageContext2 = this.getBasicMessageContext();
        messageContext2.setMessageID(new Long(System.currentTimeMillis()).toString());
        messageContext2.getMessageInformationHeaders().setRelatesTo(new RelatesTo(messageContext1.getMessageID()));
        OperationContext operationContext2 = axisOperation.findOperationContext(messageContext2, sessionContext);
        assertEquals(operationContext1, operationContext2);
    }

    public MessageContext getBasicMessageContext() throws AxisFault {

        return new MessageContext(engineCtx, new TransportInDescription(new QName("axis")), new TransportOutDescription(new QName("axis")));

    }

}
