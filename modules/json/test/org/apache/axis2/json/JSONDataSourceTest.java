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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.codehaus.jettison.json.JSONException;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

public class JSONDataSourceTest extends XMLTestCase {

    public void testMappedSerialize1() throws Exception {
        String jsonString = getMappedJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JSONDataSource source = getMappedDataSource(jsonString);
        source.serialize(outStream, new OMOutputFormat());
        assertXMLEqual("<mapping><inner><first>test string one</first></inner><inner>test string two</inner><name>foo</name></mapping>",
                outStream.toString("utf-8"));
    }

    public void testMappedSerialize2() throws Exception {
        String jsonString = getMappedJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        JSONDataSource source = getMappedDataSource(jsonString);
        source.serialize(writer, new OMOutputFormat());
        writer.flush();
        assertXMLEqual("<mapping><inner><first>test string one</first></inner><inner>test string two</inner><name>foo</name></mapping>",
                outStream.toString("utf-8"));
    }

    public void testMappedSerialize3() throws Exception {
        String jsonString = getMappedJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(outStream);
        JSONDataSource source = getMappedDataSource(jsonString);
        source.serialize(writer);
        writer.flush();
        assertXMLEqual("<mapping><inner><first>test string one</first></inner><inner>test string two</inner><name>foo</name></mapping>",
                outStream.toString("utf-8"));
    }

    public void testBadgerfishSerialize1() throws Exception {
        String jsonString = getBadgerfishJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JSONBadgerfishDataSource source = getBadgerfishDataSource(jsonString);
        source.serialize(outStream, new OMOutputFormat());
        assertXMLEqual("<p xmlns=\"http://def.ns\" xmlns:bb=\"http://other.nsb\" xmlns:aa=\"http://other.ns\"><sam att=\"lets\">555</sam></p>",
                outStream.toString("utf-8"));
    }

    public void testBadgerfishSerialize2() throws Exception {
        String jsonString = getBadgerfishJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        JSONBadgerfishDataSource source = getBadgerfishDataSource(jsonString);
        source.serialize(writer, new OMOutputFormat());
        writer.flush();
        assertXMLEqual("<p xmlns=\"http://def.ns\" xmlns:bb=\"http://other.nsb\" xmlns:aa=\"http://other.ns\"><sam att=\"lets\">555</sam></p>",
                outStream.toString("utf-8"));
    }

    public void testBadgerfishSerialize3() throws XMLStreamException, JSONException, IOException,
            ParserConfigurationException, SAXException {
        String jsonString = getBadgerfishJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(outStream);
        JSONBadgerfishDataSource source = getBadgerfishDataSource(jsonString);
        source.serialize(writer);
        writer.flush();
        assertXMLEqual("<p xmlns=\"http://def.ns\" xmlns:bb=\"http://other.nsb\" xmlns:aa=\"http://other.ns\"><sam att=\"lets\">555</sam></p>",
                outStream.toString("utf-8"));
    }

    private JSONBadgerfishDataSource getBadgerfishDataSource(String jsonString) {
        return new JSONBadgerfishDataSource(new StringReader(jsonString));
    }

    private String getBadgerfishJSONString() {
        return "{\"p\":{\"@xmlns\":{\"bb\":\"http://other.nsb\",\"aa\":\"http://other.ns\",\"$\":\"http://def.ns\"},\"sam\":{\"$\":\"555\", \"@att\":\"lets\"}}}";
    }

    private JSONDataSource getMappedDataSource(String jsonString) {
        MessageContext messageContext = new MessageContext();
        messageContext.setAxisService(new AxisService());
        return new JSONDataSource(new StringReader(jsonString), messageContext);
    }

    private String getMappedJSONString() {
        return "{\"mapping\":{\"inner\":[{\"first\":\"test string one\"},\"test string two\"],\"name\":\"foo\"}}";
    }
}
