/*
 * Copyright 2007 The Apache Software Foundation.
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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.handlers.util.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

public class AddressingValidationHandlerTest extends TestCase {
    private Log log = LogFactory.getLog(getClass());
    AddressingInHandler inHandler = new AddressingFinalInHandler();
    AddressingValidationHandler validationHandler = new AddressingValidationHandler();
    String addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
    String versionDirectory = "final";
    TestUtil testUtil = new TestUtil();

    protected void basicExtractAddressingInformationFromHeaders(String testMessagePath,
                                                                MessageContext mc)
            throws Exception {
        StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder(testMessagePath);
        SOAPHeader header = ((SOAPEnvelope) omBuilder.getDocumentElement()).getHeader();
        ArrayList addressingHeaderBlocks = header.getHeaderBlocksWithNSURI(addressingNamespace);
        inHandler.extractAddressingInformation(header, mc, addressingHeaderBlocks,
                                               addressingNamespace);
    }

    protected MessageContext testMessageWithOmittedHeaders(String testName) throws Exception {
        String testfile =
                "omitted-header-messages/" + versionDirectory + "/" + testName + "Message.xml";

        MessageContext mc = new MessageContext();
        basicExtractAddressingInformationFromHeaders(testfile, mc);

        return mc;
    }

    public void testMessageWithOmittedMessageIDInOutMEP() {
        try {
            MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
            String messageID = messageContext.getOptions().getMessageId();

            assertNull("The message id is not null.", messageID);

            messageContext.setProperty(AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED,
                                       Boolean.TRUE);
            messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                       AddressingConstants.Final.WSA_NAMESPACE);
            AxisOperation axisOperation = new InOutAxisOperation();
            messageContext.setAxisOperation(axisOperation);
            AxisService axisService = new AxisService();
            messageContext.setAxisService(axisService);
            validationHandler.invoke(messageContext);
            fail("An AxisFault should have been thrown due to the absence of a message id.");
        }
        catch (AxisFault af) {
            //Test passed.
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedMessageIDInOnlyMEP() {
        try {
            MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
            String messageID = messageContext.getOptions().getMessageId();

            assertNull("The message id is not null.", messageID);

            messageContext.setProperty(AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED,
                                       Boolean.TRUE);
            messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                       AddressingConstants.Final.WSA_NAMESPACE);
            AxisOperation axisOperation = new InOnlyAxisOperation();
            messageContext.setAxisOperation(axisOperation);
            AxisService axisService = new AxisService();
            messageContext.setAxisService(axisService);
            validationHandler.invoke(messageContext);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing MessageID header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithMessageIDInOutMEP() {
        try {
            MessageContext messageContext = testMessageWithOmittedHeaders("noFrom");
            String messageID = messageContext.getOptions().getMessageId();

            assertNotNull("The message id is null.", messageID);

            messageContext.setProperty(AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED,
                                       Boolean.TRUE);
            messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                       AddressingConstants.Final.WSA_NAMESPACE);
            AxisOperation axisOperation = new InOutAxisOperation();
            messageContext.setAxisOperation(axisOperation);
            AxisService axisService = new AxisService();
            messageContext.setAxisService(axisService);
            validationHandler.invoke(messageContext);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedMessageID200408() {
        try {
            MessageContext messageContext = testMessageWithOmittedHeaders("noMessageID");
            String messageID = messageContext.getOptions().getMessageId();

            assertNull("The message id is not null.", messageID);

            messageContext.setProperty(AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED,
                                       Boolean.TRUE);
            messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                       AddressingConstants.Submission.WSA_NAMESPACE);
            AxisOperation axisOperation = new InOutAxisOperation();
            messageContext.setAxisOperation(axisOperation);
            AxisService axisService = new AxisService();
            messageContext.setAxisService(axisService);
            validationHandler.invoke(messageContext);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing MessageID header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }
}
