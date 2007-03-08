/*
* Copyright 2006 The Apache Software Foundation.
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
package org.apache.axis2.addressing;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.context.MessageContext;

import javax.xml.namespace.QName;
import java.util.Map;

public class AddressingFaultsTest extends TestCase {

    /**
     * Test method for {@link AddressingFaultsHelper#triggerInvalidCardinalityFault(org.apache.axis2.context.MessageContext,
     * String)}.
     */
    public void testTriggerInvalidCardinalityFault() {
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                   AddressingConstants.Final.WSA_NAMESPACE);
        try {
            AddressingFaultsHelper.triggerInvalidCardinalityFault(messageContext, "Action");
            fail("Should have thrown exception");
        } catch (AxisFault af) {
            // Expected
            assertEquals(Final.FAULT_INVALID_HEADER_REASON, af.getMessage());
            assertEquals(new QName(Final.WSA_NAMESPACE, Final.FAULT_INVALID_HEADER),
                         af.getFaultCode());
            assertEquals("wsa:Action", ((Map) messageContext
                    .getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS)).get(
                    Final.FAULT_HEADER_PROB_HEADER_QNAME));
            assertEquals(Boolean.FALSE, messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
    }

    /** Test method for {@link AddressingFaultsHelper#triggerActionMismatchFault(org.apache.axis2.context.MessageContext)}. */
    public void testTriggerActionMismatchFault() {
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                   AddressingConstants.Final.WSA_NAMESPACE);
        try {
            AddressingFaultsHelper.triggerActionMismatchFault(messageContext);
            fail("Should have thrown exception");
        } catch (AxisFault af) {
            // Expected
            assertEquals(Final.FAULT_INVALID_HEADER_REASON, af.getMessage());
            assertEquals(new QName(Final.WSA_NAMESPACE, Final.FAULT_INVALID_HEADER),
                         af.getFaultCode());
            assertEquals("wsa:Action", ((Map) messageContext
                    .getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS)).get(
                    Final.FAULT_HEADER_PROB_HEADER_QNAME));
            assertEquals(Boolean.FALSE, messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
    }

    /**
     * Test method for {@link AddressingFaultsHelper#triggerOnlyAnonymousAddressSupportedFault(org.apache.axis2.context.MessageContext,
     * String)}.
     */
    public void testTriggerOnlyAnonymousAddressSupportedFault() {
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                   AddressingConstants.Final.WSA_NAMESPACE);
        try {
            AddressingFaultsHelper
                    .triggerOnlyAnonymousAddressSupportedFault(messageContext, "ReplyTo");
            fail("Should have thrown exception");
        } catch (AxisFault af) {
            // Expected
            assertEquals(Final.FAULT_INVALID_HEADER_REASON, af.getMessage());
            assertEquals(new QName(Final.WSA_NAMESPACE, Final.FAULT_INVALID_HEADER),
                         af.getFaultCode());
            assertEquals("wsa:ReplyTo", ((Map) messageContext
                    .getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS)).get(
                    Final.FAULT_HEADER_PROB_HEADER_QNAME));
            assertEquals(Boolean.FALSE, messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
    }

    /**
     * Test method for {@link AddressingFaultsHelper#triggerOnlyNonAnonymousAddressSupportedFault(org.apache.axis2.context.MessageContext,
     * String)}.
     */
    public void testTriggerOnlyNonAnonymousAddressSupportedFault() {
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                   AddressingConstants.Final.WSA_NAMESPACE);
        try {
            AddressingFaultsHelper
                    .triggerOnlyNonAnonymousAddressSupportedFault(messageContext, "ReplyTo");
            fail("Should have thrown exception");
        } catch (AxisFault af) {
            // Expected
            assertEquals(Final.FAULT_INVALID_HEADER_REASON, af.getMessage());
            assertEquals(new QName(Final.WSA_NAMESPACE, Final.FAULT_INVALID_HEADER),
                         af.getFaultCode());
            assertEquals("wsa:ReplyTo", ((Map) messageContext
                    .getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS)).get(
                    Final.FAULT_HEADER_PROB_HEADER_QNAME));
            assertEquals(Boolean.FALSE, messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
    }

    /**
     * Test method for {@link AddressingFaultsHelper#triggerMessageAddressingRequiredFault(org.apache.axis2.context.MessageContext,
     * String)}.
     */
    public void testTriggerMessageAddressingRequiredFault() {
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                   AddressingConstants.Final.WSA_NAMESPACE);
        try {
            AddressingFaultsHelper.triggerMessageAddressingRequiredFault(messageContext, "Action");
            fail("Should have thrown exception");
        } catch (AxisFault af) {
            // Expected
            assertEquals(Final.FAULT_ADDRESSING_HEADER_REQUIRED_REASON, af.getMessage());
            assertEquals(new QName(Final.WSA_NAMESPACE, Final.FAULT_ADDRESSING_HEADER_REQUIRED),
                         af.getFaultCode());
            assertEquals("wsa:Action", ((Map) messageContext
                    .getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS)).get(
                    Final.FAULT_HEADER_PROB_HEADER_QNAME));
            assertEquals(Boolean.FALSE, messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
    }

    /**
     * Test method for {@link AddressingFaultsHelper#triggerActionNotSupportedFault(org.apache.axis2.context.MessageContext,
     * String)}.
     */
    public void testTriggerActionNotSupportedFault() {
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                                   AddressingConstants.Final.WSA_NAMESPACE);
        try {
            AddressingFaultsHelper
                    .triggerActionNotSupportedFault(messageContext, "http://incorrect/action");
            fail("Should have thrown exception");
        } catch (AxisFault af) {
            // Expected
            assertEquals(AddressingConstants.FAULT_ACTION_NOT_SUPPORTED_REASON, af.getMessage());
            assertEquals(
                    new QName(Final.WSA_NAMESPACE, AddressingConstants.FAULT_ACTION_NOT_SUPPORTED),
                    af.getFaultCode());
            assertEquals("http://incorrect/action", ((Map) messageContext
                    .getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS)).get(
                    AddressingConstants.Final.FAULT_PROBLEM_ACTION_NAME));
            assertEquals(Boolean.FALSE, messageContext.getProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));
        }
    }

}
