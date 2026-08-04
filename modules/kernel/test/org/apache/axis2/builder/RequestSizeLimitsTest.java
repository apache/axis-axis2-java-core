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

package org.apache.axis2.builder;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Unit tests for the request-body ceilings applied by the message builders that
 * read the transport stream directly.
 */
public class RequestSizeLimitsTest extends TestCase {

    private AxisConfiguration axisConfiguration;
    private MessageContext messageContext;

    protected void setUp() throws Exception {
        super.setUp();
        axisConfiguration = new AxisConfiguration();
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        messageContext = configurationContext.createMessageContext();
    }

    public void testDefaultsApplyWhenUnconfigured() {
        assertEquals(RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE,
                RequestSizeLimits.resolve(messageContext,
                        RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                        RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE));
    }

    public void testConfiguredValueOverridesDefault() throws Exception {
        axisConfiguration.addParameter(
                new Parameter(RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE, "4096"));
        assertEquals(4096L, RequestSizeLimits.resolve(messageContext,
                RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE));
    }

    /** A negative value is the documented way back to the old unbounded behaviour. */
    public void testNegativeValueMeansUnlimited() throws Exception {
        axisConfiguration.addParameter(
                new Parameter(RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE, "-1"));
        assertEquals(RequestSizeLimits.UNLIMITED, RequestSizeLimits.resolve(messageContext,
                RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE));
    }

    /** A typo in axis2.xml must not silently remove the ceiling. */
    public void testGarbageValueFallsBackToTheDefault() throws Exception {
        axisConfiguration.addParameter(
                new Parameter(RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE, "not-a-number"));
        assertEquals(RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE,
                RequestSizeLimits.resolve(messageContext,
                        RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                        RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE));
    }

    public void testBoundedStreamPassesBodiesWithinTheLimit() throws Exception {
        InputStream in = BoundedInputStream.wrap(
                new ByteArrayInputStream(new byte[512]), 1024);
        assertEquals(512, readFully(in));
    }

    public void testBoundedStreamRejectsAnOversizedBody() {
        InputStream in = BoundedInputStream.wrap(
                new ByteArrayInputStream(new byte[4096]), 1024);
        try {
            readFully(in);
            fail("Reading past the ceiling should have failed");
        } catch (IOException expected) {
            assertTrue("The failure should name the limit",
                    expected.getMessage().contains("1024"));
        }
    }

    /**
     * Failing rather than reporting end-of-stream matters: a truncated body would
     * otherwise be parsed as if it were the whole message.
     */
    public void testBoundedStreamFailsRatherThanTruncatingByteAtATime() {
        InputStream in = BoundedInputStream.wrap(new ByteArrayInputStream(new byte[8]), 4);
        try {
            for (int i = 0; i < 8; i++) {
                in.read();
            }
            fail("Reading past the ceiling should have failed");
        } catch (IOException expected) {
            // expected
        }
    }

    public void testUnlimitedLeavesTheStreamUnwrapped() {
        InputStream original = new ByteArrayInputStream(new byte[8]);
        assertSame(original, BoundedInputStream.wrap(original, RequestSizeLimits.UNLIMITED));
    }

    private int readFully(InputStream in) throws IOException {
        byte[] buffer = new byte[256];
        int total = 0;
        int n;
        while ((n = in.read(buffer, 0, buffer.length)) != -1) {
            total += n;
        }
        return total;
    }
}
