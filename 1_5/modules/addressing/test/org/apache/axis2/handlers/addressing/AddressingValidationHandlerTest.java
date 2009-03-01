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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.handlers.util.TestUtil;

import javax.xml.namespace.QName;

public class AddressingValidationHandlerTest extends TestCase implements AddressingConstants {
    AddressingInHandler inHandler = new AddressingInHandler();
    AddressingValidationHandler validationHandler = new AddressingValidationHandler();
    String addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
    String versionDirectory = "final";
    TestUtil testUtil = new TestUtil();

    protected MessageContext testMessageWithOmittedHeaders(String testName) throws Exception {
        return testAddressingMessage("omitted-header-messages", testName + "Message.xml");
    }

    protected MessageContext testAddressingMessage(String directory, String testName)
            throws Exception {
        String testfile = directory + "/" + versionDirectory + "/" + testName;

        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(ConfigurationContextFactory.createEmptyConfigurationContext());
        StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(testfile);
        mc.setEnvelope(omBuilder.getSOAPEnvelope());

        inHandler.invoke(mc);

        return mc;
    }

    public void testMessageWithOmittedMessageIDInOutMEP() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
        String messageID = messageContext.getOptions().getMessageId();

        assertNull("The message id is not null.", messageID);

        AxisOperation axisOperation = new InOutAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);

        try {
            validationHandler.invoke(messageContext);
            fail("An AxisFault should have been thrown due to the absence of a message id.");
        }
        catch (AxisFault af) {
            //Test passed.
        }
    }

    public void testMessageWithOmittedMessageIDInOnlyMEP() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
        String messageID = messageContext.getOptions().getMessageId();

        assertNull("The message id is not null.", messageID);

        AxisOperation axisOperation = new InOnlyAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);
        validationHandler.invoke(messageContext);
    }

    public void testMessageWithMessageIDInOutMEP() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noFrom");
        String messageID = messageContext.getOptions().getMessageId();

        assertNotNull("The message id is null.", messageID);

        AxisOperation axisOperation = new InOutAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);
        validationHandler.invoke(messageContext);
    }

    public void testInOutMessageWithOmittedMessageID() throws Exception {
        MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
        String messageID = messageContext.getOptions().getMessageId();

        assertNull("The message id is not null.", messageID);

        AxisOperation axisOperation = new InOutAxisOperation();
        messageContext.setAxisOperation(axisOperation);
        AxisService axisService = new AxisService();
        messageContext.setAxisService(axisService);

        try {
            validationHandler.invoke(messageContext);
        } catch (AxisFault axisFault) {
            // Confirm this is the correct fault
            assertEquals("Wrong fault code",
                         new QName(Final.WSA_NAMESPACE,
                                   Final.FAULT_ADDRESSING_HEADER_REQUIRED),
                         axisFault.getFaultCode());
            return;
        }
        fail("Validated message with missing message ID!");
    }
}
