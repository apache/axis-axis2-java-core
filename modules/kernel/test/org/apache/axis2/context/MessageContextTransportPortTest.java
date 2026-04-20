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
package org.apache.axis2.context;

import junit.framework.TestCase;

/**
 * AXIS2-5762: Verify transport port constants and property round-trip.
 */
public class MessageContextTransportPortTest extends TestCase {

    public void testConstantsDefined() {
        assertEquals("TRANSPORT_LOCAL_PORT", MessageContext.TRANSPORT_LOCAL_PORT);
        assertEquals("TRANSPORT_REMOTE_PORT", MessageContext.TRANSPORT_REMOTE_PORT);
    }

    public void testLocalPortRoundTrip() {
        MessageContext mc = new MessageContext();
        assertNull(mc.getProperty(MessageContext.TRANSPORT_LOCAL_PORT));

        mc.setProperty(MessageContext.TRANSPORT_LOCAL_PORT, 8443);
        assertEquals(8443, mc.getProperty(MessageContext.TRANSPORT_LOCAL_PORT));
    }

    public void testRemotePortRoundTrip() {
        MessageContext mc = new MessageContext();
        assertNull(mc.getProperty(MessageContext.TRANSPORT_REMOTE_PORT));

        mc.setProperty(MessageContext.TRANSPORT_REMOTE_PORT, 52431);
        assertEquals(52431, mc.getProperty(MessageContext.TRANSPORT_REMOTE_PORT));
    }

    public void testBothPortsIndependent() {
        MessageContext mc = new MessageContext();
        mc.setProperty(MessageContext.TRANSPORT_LOCAL_PORT, 8080);
        mc.setProperty(MessageContext.TRANSPORT_REMOTE_PORT, 49152);

        assertEquals(8080, mc.getProperty(MessageContext.TRANSPORT_LOCAL_PORT));
        assertEquals(49152, mc.getProperty(MessageContext.TRANSPORT_REMOTE_PORT));
    }
}
