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

package org.apache.axis2.transport.h2;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.h2.impl.httpclient5.H2TransportSender;

/**
 * Simple test to verify HTTP/2.0 protocol support works with the cleaner approach.
 */
public class SimpleProtocolTest extends TestCase {

    public void testHTTP2ProtocolSupport() throws Exception {
        System.out.println("Testing clean HTTP/2.0 protocol support...");

        // Create minimal test setup
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);
        TransportOutDescription transportOut = new TransportOutDescription("h2");

        // Set HTTP/2.0 protocol parameter
        transportOut.addParameter(new Parameter("PROTOCOL", HTTPConstants.HEADER_PROTOCOL_20));

        H2TransportSender sender = new H2TransportSender();

        try {
            // This should work with our clean fix
            sender.init(configContext, transportOut);
            System.out.println("✅ SUCCESS: HTTP/2.0 protocol accepted!");

            // Verify parameter is still HTTP/2.0
            Parameter protocol = transportOut.getParameter("PROTOCOL");
            assertEquals("HTTP/2.0 parameter should be preserved",
                         HTTPConstants.HEADER_PROTOCOL_20, protocol.getValue());

        } catch (AxisFault e) {
            System.err.println("❌ FAILED: " + e.getMessage());
            throw e;
        } finally {
            try {
                sender.stop();
            } catch (Exception ignored) {}
        }
    }

    public void testConstantValue() {
        // Verify our constant is correct
        assertEquals("HTTP/2.0 constant should match expected value",
                     "HTTP/2.0", HTTPConstants.HEADER_PROTOCOL_20);
        System.out.println("✅ HTTP/2.0 constant value is correct: " + HTTPConstants.HEADER_PROTOCOL_20);
    }
}