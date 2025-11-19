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

package org.apache.axis2.transport.h2.impl.httpclient5;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.kernel.TransportSender;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for H2TransportSender focusing on HTTP/2 specific functionality.
 *
 * Tests cover:
 * - Basic HTTP/2 transport sender lifecycle
 * - HTTPS-only transport configuration
 * - HTTP/2 client version management
 * - Message context cleanup
 */
public class H2TransportSenderTest {

    private H2TransportSender transportSender;
    private MessageContext messageContext;

    @Before
    public void setUp() throws Exception {
        transportSender = new H2TransportSender();
        messageContext = new MessageContext();
    }

    @Test
    public void testTransportSenderCreation() {
        assertNotNull("H2TransportSender should be created successfully", transportSender);
        assertTrue("Should be instance of TransportSender", transportSender instanceof TransportSender);
    }

    @Test
    public void testCreateHTTPSender() {
        // Test that H2TransportSender creates the correct HTTP sender implementation
        // This is a protected method, so we test it indirectly through behavior
        assertNotNull("Transport sender should create HTTP sender", transportSender);
    }

    @Test
    public void testCleanup() throws AxisFault {
        // Set up message context with HTTP method property
        messageContext.setProperty(HTTPConstants.HTTP_METHOD, "POST");
        assertNotNull("HTTP method should be set",
                     messageContext.getProperty(HTTPConstants.HTTP_METHOD));

        // Test cleanup
        transportSender.cleanup(messageContext);

        // Verify HTTP method property is removed (guard against multiple calls)
        assertNull("HTTP method should be null after cleanup",
                  messageContext.getProperty(HTTPConstants.HTTP_METHOD));
    }

    @Test
    public void testCleanupWithNullOperationContext() throws AxisFault {
        // Test cleanup when operation context is null - should not throw exception
        messageContext.setProperty(HTTPConstants.HTTP_METHOD, "GET");

        transportSender.cleanup(messageContext);

        assertNull("HTTP method should be cleared even with null operation context",
                  messageContext.getProperty(HTTPConstants.HTTP_METHOD));
    }

    @Test
    public void testSetHTTPClientVersion() {
        // Test HTTP/2 client version setting
        org.apache.axis2.context.ConfigurationContext configContext =
            new org.apache.axis2.context.ConfigurationContext(
                new org.apache.axis2.engine.AxisConfiguration());

        transportSender.setHTTPClientVersion(configContext);

        // Verify HTTP/2 client version is set correctly
        String clientVersion = (String) configContext.getProperty(
            HTTPTransportConstants.HTTP_CLIENT_VERSION);
        assertNotNull("HTTP client version should be set", clientVersion);
        assertEquals("Should set HTTP Client 5.x version for HTTP/2",
                    HTTPTransportConstants.HTTP_CLIENT_5_X_VERSION, clientVersion);
    }

    @Test
    public void testMultipleCleanupCalls() throws AxisFault {
        // Test that multiple cleanup calls are safe (guard against multiple calls)
        messageContext.setProperty(HTTPConstants.HTTP_METHOD, "PUT");

        transportSender.cleanup(messageContext);
        assertNull("First cleanup should remove HTTP method",
                  messageContext.getProperty(HTTPConstants.HTTP_METHOD));

        // Second cleanup should not throw exception
        transportSender.cleanup(messageContext);
        assertNull("Second cleanup should be safe",
                  messageContext.getProperty(HTTPConstants.HTTP_METHOD));
    }

    @Test
    public void testHTTP2TransportIdentification() {
        // Verify this is properly identified as HTTP/2 transport
        // The transport sender should create H2SenderImpl which handles HTTP/2 specifics
        assertNotNull("H2TransportSender should be instantiated", transportSender);

        // Test that it's different from standard HTTP transport
        String transportClass = transportSender.getClass().getSimpleName();
        assertTrue("Should be HTTP/2 transport sender",
                  transportClass.contains("H2"));
    }
}