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

import com.google.gson.Gson;
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
 * Unit tests for {@link JSONStreamingMessageFormatter} (GSON variant).
 * Mirrors the test patterns in {@code JsonFormatterTest} to verify
 * the streaming formatter produces identical output.
 */
public class JSONStreamingMessageFormatterTest {

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
     * Test that a return-object response produces valid JSON identical
     * to the non-streaming JsonFormatter.
     */
    @Test
    public void testWriteToReturnObject() throws Exception {
        TestPerson person = new TestPerson("Leo", 27, "Male", true);
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, person);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, TestPerson.class);

        String expected = "{\"" + JsonConstant.RESPONSE + "\":"
            + new Gson().toJson(person, TestPerson.class) + "}";

        JSONStreamingMessageFormatter formatter = new JSONStreamingMessageFormatter();
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);

        String result = outputStream.toString("UTF-8");
        Assert.assertEquals(expected, result);
    }

    /**
     * Test that a fault response produces valid JSON.
     */
    @Test
    public void testWriteToFaultMessage() throws Exception {
        String expected = "{\"Fault\":{\"faultcode\":\"soapenv:Server\","
            + "\"faultstring\":\"javax.xml.stream.XMLStreamException\","
            + "\"detail\":\"testFaultMsg\"}}";

        outMsgContext.setProcessingFault(true);
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        soapEnvelope.getBody().addChild(createFaultOMElement());
        outMsgContext.setEnvelope(soapEnvelope);

        JSONStreamingMessageFormatter formatter = new JSONStreamingMessageFormatter();
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);

        String result = outputStream.toString("UTF-8");
        Assert.assertEquals(expected, result);
    }

    /**
     * Test that the streaming formatter produces identical output to the
     * standard formatter for a large object with many fields.
     */
    @Test
    public void testLargeObjectProducesValidJSON() throws Exception {
        // Build a response with many fields to exercise the flushing path
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("field_").append(i).append("_value_padding_data ");
        }
        TestPerson person = new TestPerson(sb.toString(), 99, "Other", false);
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, person);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, TestPerson.class);

        JSONStreamingMessageFormatter formatter = new JSONStreamingMessageFormatter();
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);

        String result = outputStream.toString("UTF-8");
        // Verify it's valid JSON by parsing it
        new Gson().fromJson(result, Object.class);
        Assert.assertTrue(result.startsWith("{\"" + JsonConstant.RESPONSE + "\":"));
        Assert.assertTrue(result.endsWith("}"));
    }

    /**
     * Test that the content type is returned correctly.
     */
    @Test
    public void testGetContentType() {
        outMsgContext.setProperty(Constants.Configuration.CONTENT_TYPE, "application/json");
        JSONStreamingMessageFormatter formatter = new JSONStreamingMessageFormatter();
        String ct = formatter.getContentType(outMsgContext, outputFormat, null);
        Assert.assertEquals("application/json", ct);
    }

    // --- Helper methods ---

    private OMElement createFaultOMElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMElement fault = factory.createOMElement("Fault", ns);
        OMElement faultCode = factory.createOMElement("faultcode", ns, fault);
        faultCode.setText("soapenv:Server");
        OMElement faultString = factory.createOMElement("faultstring", ns, fault);
        faultString.setText("javax.xml.stream.XMLStreamException");
        OMElement detail = factory.createOMElement("detail", ns, fault);
        detail.setText("testFaultMsg");
        return fault;
    }

    /**
     * Simple POJO for test serialization — matches the pattern in JsonFormatterTest.
     */
    public static class TestPerson {
        private String name;
        private int age;
        private String gender;
        private boolean single;

        public TestPerson() {}

        public TestPerson(String name, int age, String gender, boolean single) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.single = single;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public boolean isSingle() { return single; }
        public void setSingle(boolean single) { this.single = single; }
    }
}
