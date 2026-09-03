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
package org.apache.axis2.transport.jms;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;

/**
 * A JMS endpoint reference carries the JNDI environment used to resolve its
 * destination, so where the endpoint reference came from the caller — a decoupled
 * WS-Addressing response — a <code>java.naming.*</code> parameter would let that
 * caller choose which broker the server connects to. These tests pin the refusal,
 * and pin that it applies only to that case: a client-side send addresses whatever
 * provider the application asked for.
 *
 * <p>No broker is needed; the refusal happens before any connection is attempted.
 */
public class JMSDecoupledResponseEnvironmentTest extends TestCase {

    /** An attacker-chosen broker, smuggled in as a reply address. */
    private static final String EPR_WITH_ENVIRONMENT =
            "jms:/ReplyQueue"
            + "?java.naming.factory.initial=org.apache.activemq.jndi.ActiveMQInitialContextFactory"
            + "&java.naming.provider.url=tcp://attacker.example.com:61616";

    private static final String REFUSAL = "supplies the JNDI environment parameter";

    private MessageContext decoupledResponse() {
        MessageContext msgCtx = new MessageContext();
        msgCtx.setServerSide(true);
        msgCtx.setTo(new EndpointReference("jms:/ReplyQueue"));
        return msgCtx;
    }

    private MessageContext clientRequest() {
        MessageContext msgCtx = new MessageContext();
        msgCtx.setServerSide(false);
        msgCtx.setTo(new EndpointReference("jms:/ReplyQueue"));
        return msgCtx;
    }

    public void testDecoupledResponseRefusesCallerSuppliedJndiEnvironment() throws Exception {
        try {
            new JMSSender().sendMessage(decoupledResponse(), EPR_WITH_ENVIRONMENT, null);
            fail("a caller-supplied JNDI environment must not be honoured on a reply");
        } catch (AxisFault expected) {
            assertTrue("refused for the right reason, was: " + expected.getMessage(),
                    expected.getMessage().contains(REFUSAL));
            assertTrue("the offending parameter should be named, was: " + expected.getMessage(),
                    expected.getMessage().contains("java.naming.factory.initial")
                            || expected.getMessage().contains("java.naming.provider.url"));
        }
    }

    /**
     * The scope control. This send fails too — there is no such broker — but it must
     * not fail as a refusal, or the restriction would be blanket rather than aimed at
     * caller-nominated destinations.
     */
    public void testClientRequestIsNotRefusedForCarryingAnEnvironment() throws Exception {
        try {
            new JMSSender().sendMessage(clientRequest(), EPR_WITH_ENVIRONMENT, null);
        } catch (Exception whateverItWas) {
            // It will fail -- there is no broker and the sender is not initialised --
            // but it must get far enough to fail for one of those reasons.
            assertRefusalAbsent("a client-side send must not hit the decoupled-response "
                    + "refusal", whateverItWas);
        }
    }

    /** A reply naming only a destination, with no environment, is untouched. */
    public void testDecoupledResponseWithoutEnvironmentIsNotRefused() throws Exception {
        try {
            new JMSSender().sendMessage(decoupledResponse(), "jms:/ReplyQueue", null);
        } catch (Exception whateverItWas) {
            assertRefusalAbsent("naming a destination is allowed", whateverItWas);
        }
    }

    private static void assertRefusalAbsent(String what, Exception thrown) {
        String message = thrown.getMessage();
        assertFalse(what + ", was: " + thrown.getClass().getSimpleName() + ": " + message,
                message != null && message.contains(REFUSAL));
    }
}
