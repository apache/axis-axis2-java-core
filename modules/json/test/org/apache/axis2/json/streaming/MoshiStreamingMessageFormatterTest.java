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

package org.apache.axis2.json.streaming;

import com.squareup.moshi.Moshi;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.factory.JsonConstant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * Unit tests for {@link MoshiStreamingMessageFormatter}.
 * Mirrors the GSON streaming formatter tests to verify both
 * variants produce valid, equivalent output.
 */
public class MoshiStreamingMessageFormatterTest {

    private MessageContext outMsgContext;
    private OMOutputFormat outputFormat;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        outputFormat = new OMOutputFormat();
        outputStream = new ByteArrayOutputStream();

        outMsgContext = new MessageContext();
        outMsgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
    }

    @After
    public void tearDown() throws Exception {
        outputStream.close();
    }

    /**
     * Test that a return-object response produces valid JSON.
     */
    @Test
    public void testWriteToReturnObject() throws Exception {
        TestData data = new TestData("streaming-test", 42, true);
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, data);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, TestData.class);

        MoshiStreamingMessageFormatter formatter = new MoshiStreamingMessageFormatter();
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);

        String result = outputStream.toString("UTF-8");
        Assert.assertTrue("Response should start with {\"response\":",
            result.startsWith("{\"" + JsonConstant.RESPONSE + "\":"));
        Assert.assertTrue("Response should end with }",
            result.endsWith("}"));
        // Verify round-trip: parse the inner object back
        Assert.assertTrue(result.contains("\"label\":\"streaming-test\""));
        Assert.assertTrue(result.contains("\"count\":42"));
        Assert.assertTrue(result.contains("\"active\":true"));
    }

    /**
     * Test that a fault response produces valid JSON.
     */
    @Test
    public void testWriteToFaultMessage() throws Exception {
        String expected = "{\"Fault\":{\"faultcode\":\"soapenv:Server\","
            + "\"faultstring\":\"test.Exception\","
            + "\"detail\":\"moshi fault test\"}}";

        outMsgContext.setProcessingFault(true);
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        soapEnvelope.getBody().addChild(createFaultOMElement());
        outMsgContext.setEnvelope(soapEnvelope);

        MoshiStreamingMessageFormatter formatter = new MoshiStreamingMessageFormatter();
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);

        String result = outputStream.toString("UTF-8");
        Assert.assertEquals(expected, result);
    }

    /**
     * Test that a large object serializes as valid JSON through the
     * flushing stream without corruption.
     */
    @Test
    public void testLargeObjectProducesValidJSON() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("field_").append(i).append("_padding_data ");
        }
        TestData data = new TestData(sb.toString(), 99999, false);
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, data);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, TestData.class);

        MoshiStreamingMessageFormatter formatter = new MoshiStreamingMessageFormatter();
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);

        String result = outputStream.toString("UTF-8");
        // Verify it's parseable JSON
        Moshi moshi = new Moshi.Builder().build();
        moshi.adapter(Object.class).fromJson(result);
        Assert.assertTrue(result.startsWith("{\"" + JsonConstant.RESPONSE + "\":"));
    }

    /**
     * Test that the streaming formatter produces the same output as the
     * standard Moshi formatter for the same input.
     */
    @Test
    public void testOutputMatchesStandardFormatter() throws Exception {
        TestData data = new TestData("consistency-check", 7, true);
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, data);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, TestData.class);

        // Streaming formatter
        MoshiStreamingMessageFormatter streamingFormatter = new MoshiStreamingMessageFormatter();
        streamingFormatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String streamingResult = outputStream.toString("UTF-8");

        // Standard formatter
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        org.apache.axis2.json.moshi.JsonFormatter standardFormatter =
            new org.apache.axis2.json.moshi.JsonFormatter();
        standardFormatter.writeTo(outMsgContext, outputFormat, standardOutput, false);
        String standardResult = standardOutput.toString("UTF-8");

        Assert.assertEquals("Streaming and standard formatters should produce identical output",
            standardResult, streamingResult);
    }

    /**
     * Test content type passthrough.
     */
    @Test
    public void testGetContentType() {
        outMsgContext.setProperty(Constants.Configuration.CONTENT_TYPE, "application/json");
        MoshiStreamingMessageFormatter formatter = new MoshiStreamingMessageFormatter();
        String ct = formatter.getContentType(outMsgContext, outputFormat, null);
        Assert.assertEquals("application/json", ct);
    }

    // --- Helper methods ---

    private OMElement createFaultOMElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(
            "http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMElement fault = factory.createOMElement("Fault", ns);
        OMElement faultCode = factory.createOMElement("faultcode", ns, fault);
        faultCode.setText("soapenv:Server");
        OMElement faultString = factory.createOMElement("faultstring", ns, fault);
        faultString.setText("test.Exception");
        OMElement detail = factory.createOMElement("detail", ns, fault);
        detail.setText("moshi fault test");
        return fault;
    }

    /**
     * Simple POJO for test serialization.
     */
    public static class TestData {
        public String label;
        public int count;
        public boolean active;

        public TestData() {}

        public TestData(String label, int count, boolean active) {
            this.label = label;
            this.count = count;
            this.active = active;
        }
    }
}
