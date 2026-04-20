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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.factory.JsonConstant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link FieldFilteringMessageFormatter} with
 * {@link MoshiStreamingMessageFormatter} as the delegate.
 *
 * <p>Option C architecture: field filtering happens inside the Moshi
 * serialization layer via reflection-based selective field writing.
 * The streaming pipeline (Moshi → Okio → FlushingOutputStream) is
 * never broken — no capture buffer is used.</p>
 */
public class FieldFilteringMessageFormatterTest {

    private MessageContext outMsgContext;
    private OMOutputFormat outputFormat;
    private ByteArrayOutputStream outputStream;
    private FieldFilteringMessageFormatter formatter;

    @Before
    public void setUp() {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        outputFormat = new OMOutputFormat();
        outputStream = new ByteArrayOutputStream();

        outMsgContext = new MessageContext();
        outMsgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");

        formatter = new FieldFilteringMessageFormatter(
            new MoshiStreamingMessageFormatter());
    }

    @After
    public void tearDown() throws Exception {
        outputStream.close();
    }

    // ── Core filtering (through real streaming pipeline) ──────────────────

    @Test
    public void testFilterKeepsSelectedFields() throws Exception {
        setReturnObject(new PortfolioData("SUCCESS", 0.025, 0.157, 1));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("status", "variance"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue(response.getAsJsonObject().has("status"));
        Assert.assertTrue(response.getAsJsonObject().has("variance"));
        Assert.assertFalse("volatility should be filtered",
            response.getAsJsonObject().has("volatility"));
        Assert.assertFalse("calcTimeUs should be filtered",
            response.getAsJsonObject().has("calcTimeUs"));
    }

    @Test
    public void testFilterWithAllFieldsMatchesUnfiltered() throws Exception {
        PortfolioData data = new PortfolioData("SUCCESS", 0.025, 0.157, 1);
        setReturnObject(data);

        // Unfiltered baseline
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String unfiltered = outputStream.toString("UTF-8");

        // Filtered with ALL fields
        outputStream.reset();
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("status", "variance", "volatility", "calcTimeUs"));
        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String filtered = outputStream.toString("UTF-8");

        JsonElement expected = JsonParser.parseString(unfiltered);
        JsonElement actual = JsonParser.parseString(filtered);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFilterWithNoMatchingFieldsProducesEmptyResponse() throws Exception {
        setReturnObject(new PortfolioData("SUCCESS", 0.025, 0.157, 1));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("nonexistent"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertEquals("Empty response object", 0,
            response.getAsJsonObject().size());
    }

    @Test
    public void testFilterPreservesNestedObjects() throws Exception {
        setReturnObject(new NestedData("SUCCESS", new InnerData(42, "hello"), 99));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("nested"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue("nested should be kept",
            response.getAsJsonObject().has("nested"));
        Assert.assertFalse("status should be filtered",
            response.getAsJsonObject().has("status"));
        Assert.assertFalse("flat should be filtered",
            response.getAsJsonObject().has("flat"));
        // Verify nested structure intact
        Assert.assertEquals(42,
            response.getAsJsonObject().getAsJsonObject("nested")
                .get("value").getAsInt());
    }

    @Test
    public void testFilterPreservesArrayFields() throws Exception {
        setReturnObject(new ArrayData("SUCCESS", Arrays.asList(1, 2, 3), 3));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("items"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue("items should be kept",
            response.getAsJsonObject().has("items"));
        Assert.assertEquals(3,
            response.getAsJsonObject().getAsJsonArray("items").size());
        Assert.assertFalse("count should be filtered",
            response.getAsJsonObject().has("count"));
    }

    @Test
    public void testFilterPreservesNullValues() throws Exception {
        setReturnObject(new NullableData("SUCCESS", null, 42));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("status", "error"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue(response.getAsJsonObject().has("status"));
        Assert.assertTrue("null field should be kept",
            response.getAsJsonObject().has("error"));
        Assert.assertTrue(response.getAsJsonObject().get("error").isJsonNull());
        Assert.assertFalse("val should be filtered",
            response.getAsJsonObject().has("val"));
    }

    // ── Delegate passthrough tests ────────────────────────────────────────

    @Test
    public void testNoFilterDelegatesDirectly() throws Exception {
        setReturnObject(new PortfolioData("SUCCESS", 0.025, 0.157, 1));
        // No FIELD_FILTER set

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String result = outputStream.toString("UTF-8");

        Assert.assertTrue(result.contains("\"status\":\"SUCCESS\""));
        Assert.assertTrue(result.contains("\"variance\""));
        Assert.assertTrue(result.contains("\"volatility\""));
        Assert.assertTrue(result.contains("\"calcTimeUs\""));
    }

    @Test
    public void testFaultResponseNeverFiltered() throws Exception {
        outMsgContext.setProcessingFault(true);
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        var envelope = soapFactory.getDefaultEnvelope();
        envelope.getBody().addChild(TestHelper.createFaultElement());
        outMsgContext.setEnvelope(envelope);
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("nonexistent"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        String result = outputStream.toString("UTF-8");

        Assert.assertTrue("Fault should pass through",
            result.contains("Fault"));
        Assert.assertTrue(result.contains("faultcode"));
        Assert.assertTrue(result.contains("faultstring"));
    }

    // ── Inheritance and modifier edge cases ──────────────────────────────

    @Test
    public void testFilterIncludesInheritedFields() throws Exception {
        setReturnObject(new ChildData("SUCCESS", 0.025, "extra-info"));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("status", "extra"));  // status is inherited, extra is declared

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue("inherited 'status' should be included",
            response.getAsJsonObject().has("status"));
        Assert.assertTrue("declared 'extra' should be included",
            response.getAsJsonObject().has("extra"));
        Assert.assertFalse("inherited 'variance' should be filtered",
            response.getAsJsonObject().has("variance"));
    }

    @Test
    public void testFilterIncludesPrivateFields() throws Exception {
        setReturnObject(new PrivateFieldData("secret-val", 99));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("secret"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue("private 'secret' should be included",
            response.getAsJsonObject().has("secret"));
        Assert.assertEquals("secret-val",
            response.getAsJsonObject().get("secret").getAsString());
        Assert.assertFalse("private 'value' should be filtered",
            response.getAsJsonObject().has("value"));
    }

    @Test
    public void testStaticAndTransientFieldsNeverIncluded() throws Exception {
        setReturnObject(new StaticTransientData("SUCCESS", 42));
        outMsgContext.setProperty(JsonConstant.FIELD_FILTER,
            setOf("status", "value", "CONSTANT", "tempCache"));

        formatter.writeTo(outMsgContext, outputFormat, outputStream, false);
        JsonElement response = parseResponse();

        Assert.assertTrue("instance 'status' should be included",
            response.getAsJsonObject().has("status"));
        Assert.assertTrue("instance 'value' should be included",
            response.getAsJsonObject().has("value"));
        Assert.assertFalse("static 'CONSTANT' must never appear",
            response.getAsJsonObject().has("CONSTANT"));
        Assert.assertFalse("transient 'tempCache' must never appear",
            response.getAsJsonObject().has("tempCache"));
    }

    // ── parseFieldsCsv / parseFieldsFromUrl tests ─────────────────────────

    @Test
    public void testParseFieldsCsv() {
        Set<String> fields = FieldFilteringMessageFormatter.parseFieldsCsv(
            "status, variance , calcTimeUs");
        Assert.assertEquals(3, fields.size());
        Assert.assertTrue(fields.contains("status"));
        Assert.assertTrue(fields.contains("variance"));
        Assert.assertTrue(fields.contains("calcTimeUs"));
    }

    @Test
    public void testParseFieldsCsvEmpty() {
        Assert.assertTrue(
            FieldFilteringMessageFormatter.parseFieldsCsv("").isEmpty());
        Assert.assertTrue(
            FieldFilteringMessageFormatter.parseFieldsCsv(null).isEmpty());
    }

    @Test
    public void testParseFieldsFromUrl() {
        Set<String> fields = FieldFilteringMessageFormatter.parseFieldsFromUrl(
            "https://host/services/Svc?fields=status,variance&other=1");
        Assert.assertNotNull(fields);
        Assert.assertEquals(2, fields.size());
        Assert.assertTrue(fields.contains("status"));
        Assert.assertTrue(fields.contains("variance"));
    }

    @Test
    public void testParseFieldsFromUrlEncoded() {
        Set<String> fields = FieldFilteringMessageFormatter.parseFieldsFromUrl(
            "https://host/services/Svc?fields=first%20name,last%20name");
        Assert.assertNotNull(fields);
        Assert.assertEquals(2, fields.size());
        Assert.assertTrue(fields.contains("first name"));
        Assert.assertTrue(fields.contains("last name"));
    }

    @Test
    public void testParseFieldsCsvDuplicates() {
        Set<String> fields = FieldFilteringMessageFormatter.parseFieldsCsv(
            "status,variance,status");
        Assert.assertEquals("Duplicates should be deduplicated", 2, fields.size());
    }

    @Test
    public void testParseFieldsCsvEmptyParam() {
        // ?fields= with empty value
        Set<String> fields = FieldFilteringMessageFormatter.parseFieldsFromUrl(
            "https://host/services/Svc?fields=");
        Assert.assertNotNull(fields);
        Assert.assertTrue("Empty fields= should produce empty set", fields.isEmpty());
    }

    @Test
    public void testParseFieldsFromUrlNoParam() {
        Assert.assertNull(
            FieldFilteringMessageFormatter.parseFieldsFromUrl(
                "https://host/services/Svc?other=1"));
        Assert.assertNull(
            FieldFilteringMessageFormatter.parseFieldsFromUrl(
                "https://host/services/Svc"));
        Assert.assertNull(
            FieldFilteringMessageFormatter.parseFieldsFromUrl(null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void setReturnObject(Object obj) {
        outMsgContext.setProperty(JsonConstant.RETURN_OBJECT, obj);
        outMsgContext.setProperty(JsonConstant.RETURN_TYPE, obj.getClass());
    }

    private JsonElement parseResponse() {
        String json = outputStream.toString();
        JsonElement root = JsonParser.parseString(json);
        return root.getAsJsonObject().get("response");
    }

    private static Set<String> setOf(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    // ── Test POJOs ────────────────────────────────────────────────────────

    public static class PortfolioData {
        public String status;
        public double variance;
        public double volatility;
        public long calcTimeUs;
        public PortfolioData() {}
        public PortfolioData(String s, double v, double vol, long t) {
            status = s; variance = v; volatility = vol; calcTimeUs = t;
        }
    }

    public static class InnerData {
        public int value;
        public String label;
        public InnerData() {}
        public InnerData(int v, String l) { value = v; label = l; }
    }

    public static class NestedData {
        public String status;
        public InnerData nested;
        public int flat;
        public NestedData() {}
        public NestedData(String s, InnerData n, int f) {
            status = s; nested = n; flat = f;
        }
    }

    public static class ArrayData {
        public String status;
        public List<Integer> items;
        public int count;
        public ArrayData() {}
        public ArrayData(String s, List<Integer> i, int c) {
            status = s; items = i; count = c;
        }
    }

    public static class NullableData {
        public String status;
        public String error;
        public int val;
        public NullableData() {}
        public NullableData(String s, String e, int v) {
            status = s; error = e; val = v;
        }
    }

    /** POJO with private fields for setAccessible test. */
    public static class PrivateFieldData {
        private String secret;
        private int value;
        public PrivateFieldData() {}
        public PrivateFieldData(String s, int v) { secret = s; value = v; }
    }

    /** Base class for inheritance test. */
    public static class BaseData {
        public String status;
        public double variance;
        public BaseData() {}
        public BaseData(String s, double v) { status = s; variance = v; }
    }

    /** Child that adds a field; inherits status + variance from BaseData. */
    public static class ChildData extends BaseData {
        public String extra;
        public ChildData() {}
        public ChildData(String s, double v, String e) {
            super(s, v); extra = e;
        }
    }

    /** POJO with static and transient fields for modifier filtering test. */
    public static class StaticTransientData {
        public static final String CONSTANT = "should-never-serialize";
        public transient String tempCache = "should-never-serialize";
        public String status;
        public int value;
        public StaticTransientData() {}
        public StaticTransientData(String s, int v) { status = s; value = v; }
    }

    static class TestHelper {
        static org.apache.axiom.om.OMElement createFaultElement() {
            var factory = OMAbstractFactory.getOMFactory();
            var ns = factory.createOMNamespace(
                "http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
            var fault = factory.createOMElement("Fault", ns);
            var code = factory.createOMElement("faultcode", ns, fault);
            code.setText("soapenv:Server");
            var str = factory.createOMElement("faultstring", ns, fault);
            str.setText("test.Exception");
            var detail = factory.createOMElement("detail", ns, fault);
            detail.setText("filter test");
            return fault;
        }
    }
}
