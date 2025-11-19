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
import org.apache.axis2.transport.h2.impl.httpclient5.H2TransportSender;

/**
 * Test to verify that the HTTP/2.0 PROTOCOL parameter validation fix works correctly.
 */
public class ProtocolValidationTest extends TestCase {

    public void testHTTP2ProtocolValidation() throws Exception {
        // Create minimal configuration context
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);

        // Create transport description with HTTP/2.0 protocol
        TransportOutDescription transportOut = new TransportOutDescription("h2");
        transportOut.addParameter(new Parameter("PROTOCOL", "HTTP/2.0"));

        // Create H2TransportSender
        H2TransportSender transportSender = new H2TransportSender();

        try {
            // This should NOT throw an exception after the fix
            transportSender.init(configContext, transportOut);

            // If we get here, the fix worked
            System.out.println("SUCCESS: H2TransportSender.init() accepted HTTP/2.0 protocol");

            // Verify the protocol parameter was preserved
            Parameter protocolParam = transportOut.getParameter("PROTOCOL");
            assertNotNull("Protocol parameter should not be null", protocolParam);
            assertEquals("Protocol parameter should be HTTP/2.0", "HTTP/2.0", protocolParam.getValue());

        } catch (AxisFault e) {
            // If we get the old error, the fix didn't work
            if (e.getMessage().contains("Can have values only HTTP/1.0 or HTTP/1.1")) {
                fail("FIX FAILED: Still getting protocol validation error: " + e.getMessage());
            } else {
                // Some other error - re-throw it
                throw e;
            }
        } finally {
            // Clean up
            try {
                transportSender.stop();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    public void testHTTP11StillWorks() throws Exception {
        // Verify that HTTP/1.1 still works after our changes
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);

        TransportOutDescription transportOut = new TransportOutDescription("h2");
        transportOut.addParameter(new Parameter("PROTOCOL", "HTTP/1.1"));

        H2TransportSender transportSender = new H2TransportSender();

        try {
            // This should work fine
            transportSender.init(configContext, transportOut);

            // Verify the protocol parameter was preserved
            Parameter protocolParam = transportOut.getParameter("PROTOCOL");
            assertNotNull("Protocol parameter should not be null", protocolParam);
            assertEquals("Protocol parameter should be HTTP/1.1", "HTTP/1.1", protocolParam.getValue());

        } finally {
            try {
                transportSender.stop();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
}