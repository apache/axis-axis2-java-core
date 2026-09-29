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

package org.apache.axis2.json;

import junit.framework.TestCase;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.RequestSizeLimits;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.json.gson.GsonXMLStreamReader;
import org.apache.axis2.json.gsonh2.EnhancedGsonJsonBuilder;
import org.apache.axis2.json.moshi.MoshiXMLStreamReader;
import org.apache.axis2.json.moshih2.EnhancedMoshiJsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Every JSON builder must apply {@code jsonMaxRequestSize}. Streaming builders
 * are no exception: one string token is still read into memory whole, which is
 * what the body below is.
 */
public class JsonRequestSizeLimitTest extends TestCase {

    private static final String LIMIT = "64";

    private AxisConfiguration axisConfiguration;
    private MessageContext messageContext;

    protected void setUp() throws Exception {
        super.setUp();
        axisConfiguration = new AxisConfiguration();
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        messageContext = configurationContext.createMessageContext();
        messageContext.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
    }

    private void setLimit(String value) throws Exception {
        axisConfiguration.addParameter(new Parameter(RequestSizeLimits.JSON_MAX_REQUEST_SIZE, value));
    }

    private static InputStream body(int stringLength) throws Exception {
        StringBuilder sb = new StringBuilder("{\"a\":\"");
        for (int i = 0; i < stringLength; i++) {
            sb.append('A');
        }
        return new ByteArrayInputStream(sb.append("\"}").toString().getBytes("UTF-8"));
    }

    private static void assertExceedsCeiling(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getMessage() != null && c.getMessage().contains("exceeds the configured maximum")) {
                return;
            }
        }
        fail("Expected the request-size ceiling to be hit, got " + t);
    }

    public void testJsonCeilingHasADefault() {
        assertEquals(RequestSizeLimits.DEFAULT_JSON_MAX_REQUEST_SIZE,
                RequestSizeLimits.resolve(messageContext,
                        RequestSizeLimits.JSON_MAX_REQUEST_SIZE,
                        RequestSizeLimits.DEFAULT_JSON_MAX_REQUEST_SIZE));
    }

    public void testJsonOMBuilderIsBounded() throws Exception {
        setLimit(LIMIT);
        try {
            readJsonString(1000);
            fail("An over-sized body was read in full");
        } catch (Exception e) {
            assertExceedsCeiling(e);
        }
    }

    public void testJsonOMBuilderAcceptsABodyUnderTheCeiling() throws Exception {
        setLimit(LIMIT);
        assertEquals("{\"a\":\"AAAAAAAA\"}", readJsonString(8));
    }

    public void testJsonOMBuilderCeilingCanBeOptedOut() throws Exception {
        setLimit("-1");
        assertEquals(1000 + 8, readJsonString(1000).length());
    }

    /** Builds with JSONOMBuilder, then reads the body the way its data source does. */
    private String readJsonString(int stringLength) throws Exception {
        OMSourcedElement element = (OMSourcedElement) new JSONOMBuilder().processDocument(
                body(stringLength), JSONTestConstants.CONTENT_TYPE_MAPPED, messageContext);
        return (String) ((AbstractJSONDataSource) element.getDataSource()).getObject();
    }

    public void testGsonJsonBuilderIsBounded() throws Exception {
        setLimit(LIMIT);
        assertGsonReaderBounded(new org.apache.axis2.json.gson.JsonBuilder());
    }

    public void testEnhancedGsonJsonBuilderIsBounded() throws Exception {
        setLimit(LIMIT);
        assertGsonReaderBounded(new EnhancedGsonJsonBuilder());
    }

    public void testMoshiJsonBuilderIsBounded() throws Exception {
        setLimit(LIMIT);
        assertMoshiReaderBounded(new org.apache.axis2.json.moshi.JsonBuilder());
    }

    public void testEnhancedMoshiJsonBuilderIsBounded() throws Exception {
        setLimit(LIMIT);
        assertMoshiReaderBounded(new EnhancedMoshiJsonBuilder());
    }

    private void assertGsonReaderBounded(Builder builder) throws Exception {
        builder.processDocument(body(1000), "application/json", messageContext);
        GsonXMLStreamReader reader = (GsonXMLStreamReader)
                messageContext.getProperty(JsonConstant.GSON_XML_STREAM_READER);
        com.google.gson.stream.JsonReader jsonReader = reader.getJsonReader();
        try {
            jsonReader.beginObject();
            jsonReader.nextName();
            jsonReader.nextString();
            fail("An over-sized body was read in full");
        } catch (Exception e) {
            assertExceedsCeiling(e);
        }
    }

    private void assertMoshiReaderBounded(Builder builder) throws Exception {
        builder.processDocument(body(1000), "application/json", messageContext);
        MoshiXMLStreamReader reader = (MoshiXMLStreamReader)
                messageContext.getProperty(JsonConstant.MOSHI_XML_STREAM_READER);
        com.squareup.moshi.JsonReader jsonReader = reader.getJsonReader();
        try {
            jsonReader.beginObject();
            jsonReader.nextName();
            jsonReader.nextString();
            fail("An over-sized body was read in full");
        } catch (Exception e) {
            assertExceedsCeiling(e);
        }
    }
}
