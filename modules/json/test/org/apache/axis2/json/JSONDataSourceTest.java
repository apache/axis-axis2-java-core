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

import org.apache.axiom.om.util.StAXUtils;
import org.codehaus.jettison.json.JSONException;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

public class JSONDataSourceTest extends XMLTestCase {

    public void testMappedSerialize1() throws XMLStreamException {
        String jsonString = getMappedJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JSONDataSource source = getMappedDataSource(jsonString);
        source.serialize(outStream, null);
        assertEquals(jsonString, new String(outStream.toByteArray()));
    }

    public void testMappedSerialize2() throws XMLStreamException, IOException {
        String jsonString = getMappedJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        JSONDataSource source = getMappedDataSource(jsonString);
        source.serialize(writer, null);
        writer.flush();
        assertEquals(jsonString, new String(outStream.toByteArray()));

    }

    public void testMappedSerialize3() throws XMLStreamException {
        String jsonString = getMappedJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(outStream);
        JSONDataSource source = getMappedDataSource(jsonString);
        source.serialize(writer);
        writer.flush();
        assertEquals(
                "<?xml version='1.0' encoding='UTF-8'?><mapping><inner><first>test string one</first></inner><inner>test string two</inner><name>foo</name></mapping>",
                new String(outStream.toByteArray()));
    }

    public void testBadgerfishSerialize1() throws XMLStreamException {
        String jsonString = getBadgerfishJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JSONBadgerfishDataSource source = getBadgerfishDataSource(jsonString);
        source.serialize(outStream, null);
        assertEquals(jsonString, new String(outStream.toByteArray()));
    }

    public void testBadgerfishSerialize2() throws XMLStreamException, IOException {
        String jsonString = getBadgerfishJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        JSONBadgerfishDataSource source = getBadgerfishDataSource(jsonString);
        source.serialize(writer, null);
        writer.flush();
        assertEquals(jsonString, new String(outStream.toByteArray()));
    }

    public void testBadgerfishSerialize3() throws XMLStreamException, JSONException, IOException,
            ParserConfigurationException, SAXException {
        String jsonString = getBadgerfishJSONString();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(outStream);
        JSONBadgerfishDataSource source = getBadgerfishDataSource(jsonString);
        source.serialize(writer);
        writer.flush();
        assertXMLEqual(
                "<?xml version='1.0' encoding='UTF-8'?><p xmlns=\"http://def.ns\" xmlns:bb=\"http://other.nsb\" xmlns:aa=\"http://other.ns\"><sam att=\"lets\">555</sam></p>",
                new String(outStream.toByteArray()));
    }

    private JSONBadgerfishDataSource getBadgerfishDataSource(String jsonString) {
        InputStream jsonInputStream = new ByteArrayInputStream(jsonString.getBytes());
        return new JSONBadgerfishDataSource(readLocalName(jsonInputStream), "\"p\"");
    }

    private String getBadgerfishJSONString() {
        return "{\"p\":{\"@xmlns\":{\"bb\":\"http://other.nsb\",\"aa\":\"http://other.ns\",\"$\":\"http://def.ns\"},\"sam\":{\"$\":\"555\", \"@att\":\"lets\"}}}";
    }

    private JSONDataSource getMappedDataSource(String jsonString) {
        InputStream jsonInputStream = new ByteArrayInputStream(jsonString.getBytes());
        return new JSONDataSource(readLocalName(jsonInputStream), "\"mapping\"");
    }

    private String getMappedJSONString() {
        return "{\"mapping\":{\"inner\":[{\"first\":\"test string one\"},\"test string two\"],\"name\":\"foo\"}}";
    }

    private InputStream readLocalName(InputStream in) {
        try {
            while ((char)in.read() != ':') {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

}
