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

package org.apache.axis2.json.factory;

import com.google.gson.JsonParser;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.json.gsonh2.EnhancedGsonJsonFormatter;
import org.apache.axis2.json.moshih2.EnhancedMoshiJsonFormatter;
import org.apache.axis2.json.streaming.JSONStreamingMessageFormatter;
import org.apache.axis2.json.streaming.MoshiStreamingMessageFormatter;
import org.apache.axis2.kernel.MessageFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * The {@code disableJSONHtmlEncoding} parameter turns off the HTML content encoding
 * of String values in every JSON formatter; without it the encoding stays on.
 */
public class JsonHtmlEncodingTest {

    private static final String RAW = "AT&T <b>bold</b>";
    private static final String ENCODED = "AT&amp;T &lt;b&gt;bold&lt;/b&gt;";

    public static class Payload {
        public String text;
        public int count;

        public Payload(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    private static MessageContext context(boolean disabledGlobally) throws Exception {
        MessageContext ctx = new MessageContext();
        ctx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
        ctx.setProperty(JsonConstant.RETURN_OBJECT, new Payload(RAW, 7));
        ctx.setProperty(JsonConstant.RETURN_TYPE, Payload.class);
        AxisConfiguration axisConfig = new AxisConfiguration();
        if (disabledGlobally) {
            axisConfig.addParameter(new Parameter(JsonConstant.DISABLE_JSON_HTML_ENCODING, "true"));
        }
        ctx.setConfigurationContext(new ConfigurationContext(axisConfig));
        return ctx;
    }

    private static String textOf(MessageFormatter formatter, MessageContext ctx) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        formatter.writeTo(ctx, new OMOutputFormat(), out, false);
        String json = out.toString("UTF-8");
        return JsonParser.parseString(json).getAsJsonObject()
                .getAsJsonObject(JsonConstant.RESPONSE).get("text").getAsString();
    }

    private static MessageFormatter[] formatters() {
        return new MessageFormatter[] {
                new org.apache.axis2.json.gson.JsonFormatter(),
                new org.apache.axis2.json.moshi.JsonFormatter(),
                new JSONStreamingMessageFormatter(),
                new MoshiStreamingMessageFormatter(),
                new EnhancedGsonJsonFormatter(),
                new EnhancedMoshiJsonFormatter()
        };
    }

    @Test
    public void encodesByDefault() throws Exception {
        for (MessageFormatter formatter : formatters()) {
            Assert.assertEquals(formatter.getClass().getName(), ENCODED,
                    textOf(formatter, context(false)));
        }
    }

    @Test
    public void parameterTurnsEncodingOff() throws Exception {
        for (MessageFormatter formatter : formatters()) {
            Assert.assertEquals(formatter.getClass().getName(), RAW,
                    textOf(formatter, context(true)));
        }
    }

    @Test
    public void parameterOnTheServiceAlone() throws Exception {
        MessageContext ctx = context(false);
        AxisService service = new AxisService("plainTextService");
        service.addParameter(new Parameter(JsonConstant.DISABLE_JSON_HTML_ENCODING, "true"));
        ctx.setAxisService(service);
        Assert.assertEquals(RAW, textOf(new MoshiStreamingMessageFormatter(), ctx));
        Assert.assertEquals(RAW, textOf(new org.apache.axis2.json.gson.JsonFormatter(), ctx));
    }

    @Test
    public void fieldFilteringHonoursTheParameter() throws Exception {
        MessageContext on = context(false);
        on.setProperty(JsonConstant.FIELD_FILTER, new LinkedHashSet<String>(Arrays.asList("text")));
        Assert.assertEquals(ENCODED, textOf(new MoshiStreamingMessageFormatter(), on));
        Assert.assertEquals(ENCODED, textOf(new JSONStreamingMessageFormatter(), on));

        MessageContext off = context(true);
        off.setProperty(JsonConstant.FIELD_FILTER, new LinkedHashSet<String>(Arrays.asList("text")));
        Assert.assertEquals(RAW, textOf(new MoshiStreamingMessageFormatter(), off));
        Assert.assertEquals(RAW, textOf(new JSONStreamingMessageFormatter(), off));
    }

    @Test
    public void nullContextKeepsEncodingOn() {
        Assert.assertFalse(JsonHtmlEncoding.isDisabled(null));
        Assert.assertFalse(JsonHtmlEncoding.isDisabled(new MessageContext()));
    }
}
