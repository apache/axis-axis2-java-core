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

package org.apache.axis2.context;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.util.UUIDGenerator;

import javax.xml.namespace.QName;

public class OperationContextTest extends AbstractTestCase {

    private ConfigurationContext engineCtx = new ConfigurationContext(new AxisConfigurationImpl());

    public OperationContextTest(String arg0) {
        super(arg0);
    }

    public void testMEPfindingOnRelatesTO() throws Exception {

        AxisService serviceConfig = new AxisService(new QName("TempSC"));
        engineCtx.getAxisConfiguration().addService(serviceConfig);
       ServiceGroupContext sgc =  serviceConfig.getParent().getServiceGroupContext(engineCtx);

        ServiceContext sessionContext = sgc.getServiceContext("TempSC");
        MessageContext messageContext1 = this.getBasicMessageContext();

        messageContext1.setMessageID(
                UUIDGenerator.getUUID());
        AxisOperation axisOperation = new InOutAxisOperation(
                new QName("test"));
        OperationContext operationContext1 = axisOperation.findOperationContext(
                messageContext1, sessionContext);

        MessageContext messageContext2 = this.getBasicMessageContext();
        messageContext2.setMessageID(
                UUIDGenerator.getUUID());
        messageContext2.getMessageInformationHeaders().setRelatesTo(
                new RelatesTo(messageContext1.getMessageID()));
        OperationContext operationContext2 = axisOperation.findOperationContext(
                messageContext2, sessionContext);
        assertEquals(operationContext1, operationContext2);
    }

    public MessageContext getBasicMessageContext() throws AxisFault {

        return new MessageContext(engineCtx,
                new TransportInDescription(new QName("axis")),
                new TransportOutDescription(new QName("axis")));

    }

}
