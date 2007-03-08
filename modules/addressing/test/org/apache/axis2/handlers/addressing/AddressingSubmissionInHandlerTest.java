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

package org.apache.axis2.handlers.addressing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingSubmissionInHandlerTest extends AddressingInHandlerTestBase {

    private Log log = LogFactory.getLog(getClass());

    /** @param testName  */
    public AddressingSubmissionInHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        inHandler = new AddressingSubmissionInHandler();
        addressingNamespace = AddressingConstants.Submission.WSA_NAMESPACE;
        versionDirectory = "submission";
        fromAddress = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
        secondRelationshipType = "axis2:some.custom.relationship";
    }

    public void testExtractAddressingInformationFromHeaders() {
        try {
            Options options = extractAddressingInformationFromHeaders();
            // Cannot check refparams in 2004/08 case as they can't be extracted until later
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedAction() {
        try {
            testMessageWithOmittedHeaders("noAction");
            fail("An AxisFault should have been thrown due to a missing Action header.");
        }
        catch (AxisFault af) {
            //test passed
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedFaultTo() {
        try {
            Options options = testMessageWithOmittedHeaders("noFaultTo");
            EndpointReference epr = options.getFaultTo();

            assertNull("The FaultTo endpoint reference is not null.", epr);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing FaultTo header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedFrom() {
        try {
            Options options = testMessageWithOmittedHeaders("noFrom");
            EndpointReference epr = options.getFrom();

            assertNull("The From endpoint reference is not null.", epr);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing From header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedMessageID() {
        try {
            testMessageWithOmittedHeaders("noMessageID");
            fail("An AxisFault should have been thrown due to a missing MessageID header.");
        }
        catch (AxisFault af) {
            //test passed
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedMessageIDReplyToAndFaultTo() {
        try {
            Options options = testMessageWithOmittedHeaders("noMessageIDNoReplyToNoFaultTo");
            String messageID = options.getMessageId();

            assertNull("The message id is not null.", messageID);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to missing MessageID, ReplyTo, and FaultTo headers.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedReplyTo() {
        try {
            Options options = testMessageWithOmittedHeaders("noReplyTo");
            EndpointReference epr = options.getReplyTo();
            String address = epr.getAddress();

            assertEquals("The address of the ReplyTo endpoint reference is not the none URI.",
                         AddressingConstants.Final.WSA_NONE_URI, address);
        }
        catch (AxisFault af) {
            af.printStackTrace();
            log.error(af.getMessage());
            fail("An unexpected AxisFault was thrown due to a missing ReplyTo header.");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }

    public void testMessageWithOmittedTo() {
        try {
            testMessageWithOmittedHeaders("noTo");
            fail("An AxisFault should have been thrown due to a missing To header.");
        }
        catch (AxisFault af) {
            //test passed
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            fail(" An Exception has occured " + e.getMessage());
        }
    }
}
