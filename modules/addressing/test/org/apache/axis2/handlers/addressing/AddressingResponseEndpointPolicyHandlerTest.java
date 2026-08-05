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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.addressing.AddressingResponseEndpointPolicy;
import org.apache.axis2.description.Parameter;

/**
 * End-to-end coverage of the response-endpoint policy through the handler that
 * actually parses inbound WS-Addressing headers.
 *
 * <p>This exists because neither the unit tests for the policy itself nor the
 * reporter's proof-of-concept exercise the path an attacker actually has. The
 * PoC builds a MessageContext and calls {@code setReplyTo} in Java before
 * driving the transport sender, which no remote caller can do; the only way in
 * from outside is a {@code wsa:ReplyTo} header on an inbound message, which is
 * what these tests send.
 */
public class AddressingResponseEndpointPolicyHandlerTest extends TestCase {

    private AddressingInHandler handler;
    private ConfigurationContext configurationContext;

    protected void setUp() throws Exception {
        super.setUp();
        handler = new AddressingInHandler();
        configurationContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        // Decoupled responses are off by default; opt in so these tests reach the
        // address checks. testDecoupledReplyToRefusedByDefault covers the default.
        configurationContext.getAxisConfiguration().addParameter(
                new Parameter(AddressingResponseEndpointPolicy.ALLOW_NON_ANONYMOUS, "true"));
        // Only https ships as permitted; these cases use http addresses.
        configurationContext.getAxisConfiguration().addParameter(
                new Parameter(AddressingResponseEndpointPolicy.ALLOWED_SCHEMES_PARAMETER, "http,https"));
    }

    /**
     * Out of the box a non-anonymous ReplyTo is refused outright, whatever it
     * points at, so an inbound header cannot name an outbound destination.
     */
    public void testDecoupledReplyToRefusedByDefault() throws Exception {
        configurationContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        try {
            invokeWith("ReplyTo", "http://192.0.2.25/replies");
            fail("A non-anonymous ReplyTo should fault under the shipped default");
        } catch (AxisFault expected) {
            // expected
        }
    }

    /** Anonymous replies are unaffected, or ordinary in-out messaging breaks. */
    public void testAnonymousReplyToStillWorksByDefault() throws Exception {
        configurationContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        MessageContext mc = invokeWith("ReplyTo",
                org.apache.axis2.addressing.AddressingConstants.Final.WSA_ANONYMOUS_URL);
        assertTrue(mc.getReplyTo().hasAnonymousAddress());
    }

    /**
     * The cloud instance-metadata address is refused straight out of the box.
     */
    public void testMetadataReplyToIsRejected() throws Exception {
        try {
            invokeWith("ReplyTo", "http://169.254.169.254/latest/meta-data/");
            fail("A link-local ReplyTo should have faulted");
        } catch (AxisFault expected) {
            // expected
        }
    }

    /** The same applies to FaultTo, which fires on any request that faults. */
    public void testMetadataFaultToIsRejected() throws Exception {
        try {
            invokeWith("FaultTo", "http://169.254.169.254/latest/meta-data/");
            fail("A link-local FaultTo should have faulted");
        } catch (AxisFault expected) {
            // expected
        }
    }

    /** A genuine external reply endpoint still passes through untouched. */
    public void testOrdinaryReplyToIsAccepted() throws Exception {
        MessageContext mc = invokeWith("ReplyTo", "http://192.0.2.25/replies");
        assertEquals("http://192.0.2.25/replies", mc.getReplyTo().getAddress());
    }

    /**
     * With the strict posture enabled a private callback address is refused too,
     * which is the setting a deployment exposed to untrusted callers wants.
     */
    public void testPrivateReplyToRejectedUnderStrictPolicy() throws Exception {
        configurationContext.getAxisConfiguration().addParameter(
                new Parameter(AddressingResponseEndpointPolicy.BLOCK_PRIVATE_NETWORKS, "true"));
        try {
            invokeWith("ReplyTo", "http://10.1.2.3/internal");
            fail("A private ReplyTo should have faulted under the strict policy");
        } catch (AxisFault expected) {
            // expected
        }
    }

    /** Under the default policy that same private callback is allowed. */
    public void testPrivateReplyToAcceptedByDefault() throws Exception {
        MessageContext mc = invokeWith("ReplyTo", "http://10.1.2.3/internal");
        assertEquals("http://10.1.2.3/internal", mc.getReplyTo().getAddress());
    }

    /**
     * Build an inbound message carrying the given addressing header and run the
     * in-handler over it, exactly as the engine would for a remote request.
     */
    private MessageContext invokeWith(String headerName, String address) throws Exception {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();

        addAddressingHeader(factory, envelope, AddressingConstants.WSA_TO,
                "http://localhost/axis2/services/Target", false);
        addAddressingHeader(factory, envelope, AddressingConstants.WSA_ACTION,
                "urn:someAction", false);
        addAddressingHeader(factory, envelope, AddressingConstants.WSA_MESSAGE_ID,
                "urn:uuid:test-message-id", false);
        addAddressingHeader(factory, envelope, headerName, address, true);

        MessageContext mc = new MessageContext();
        mc.setConfigurationContext(configurationContext);
        mc.setServerSide(true);
        mc.setEnvelope(envelope);

        handler.invoke(mc);
        return mc;
    }

    /**
     * @param nested true for the EPR-valued headers, whose address sits in a
     *               child wsa:Address element rather than the header text
     */
    private void addAddressingHeader(SOAPFactory factory, SOAPEnvelope envelope,
                                     String name, String value, boolean nested) {
        SOAPHeaderBlock header = envelope.getHeader().addHeaderBlock(name,
                factory.createOMNamespace(AddressingConstants.Final.WSA_NAMESPACE, "wsa"));
        if (nested) {
            factory.createOMElement(AddressingConstants.EPR_ADDRESS,
                    factory.createOMNamespace(AddressingConstants.Final.WSA_NAMESPACE, "wsa"),
                    header).setText(value);
        } else {
            header.setText(value);
        }
    }
}
