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
package org.apache.axis2.dispatchers;

import junit.framework.TestCase;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;

public class AddressingBasedDispatcherTest extends TestCase {

    public void testValidateActionFlag() throws Exception {
        MessageContext mc = new MessageContext();
        
        //Indicate that the AddressingInHandler has run.
        mc.setProperty(AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED, Boolean.TRUE);

        // Tell validation handler NOT to check action dispatch
        mc.setProperty(AddressingConstants.ADDR_VALIDATE_ACTION, Boolean.FALSE);

        // Even though this message has an action that will not dispatch to an
        // AxisOperation, this shouldn't throw a fault.
        AddressingBasedDispatcher dispatcher = new AddressingBasedDispatcher();
        dispatcher.invoke(mc);
    }

    /**
     * A RelatesTo that correlates to no operation context must not stop the service
     * being bound from the To here, in the Addressing phase. Otherwise the Dispatch
     * phase binds it from the same To after the Security phase has run with no
     * service, and that service's security handlers never see the message.
     */
    public void testUncorrelatedRelatesToStillDispatchesOnTo() throws Exception {
        ConfigurationContext cc = ConfigurationContextFactory.createEmptyConfigurationContext();
        cc.getAxisConfiguration().addService(new AxisService("Service1"));
        MessageContext mc = cc.createMessageContext();
        mc.setTo(new EndpointReference("http://127.0.0.1:8080/axis2/services/Service1"));
        mc.addRelatesTo(new RelatesTo("urn:uuid:no-such-operation-context"));

        new AddressingBasedDispatcher().invoke(mc);

        assertNotNull("the service must be bound from the To", mc.getAxisService());
        assertEquals("Service1", mc.getAxisService().getName());
    }
}
