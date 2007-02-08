/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.axiom.om.util.StAXUtils;
import org.json.JSONException;

public class JSONDataSourceTest extends TestCase {

    public void testSerialize1() throws XMLStreamException {
        String jsonString = "{\"p\":{\"name\":{\"kamal\":{\"$\":\"yes\"},\"$\":\"innername\"},\"@pp\":\"value\"}}";
        InputStream jsonInputStream = new ByteArrayInputStream(jsonString.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JSONBadgerfishDataSource source = new JSONBadgerfishDataSource(readLocalName(jsonInputStream), "p");
        source.serialize(outStream, null);
        assertEquals(jsonString, new String(outStream.toByteArray()));
    }

    public void testSerialize2() throws XMLStreamException, IOException {
        String jsonString = "{\"p\":{\"name\":{\"kamal\":[{\"$\":\"yes\"},{\"$\":\"second\"}],\"$\":\"innername\"},\"@pp\":\"value\"}}";
        InputStream jsonInputStream = new ByteArrayInputStream(jsonString.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        JSONBadgerfishDataSource source = new JSONBadgerfishDataSource(readLocalName(jsonInputStream), "p");
        source.serialize(writer, null);
        writer.flush();
        assertEquals(jsonString, new String(outStream.toByteArray()));
    }

    public void testSerialize3() throws XMLStreamException, JSONException {
        String jsonString = "{\"p\":{\"@xmlns\":{\"bb\":\"http://other.nsb\",\"aa\":\"http://other.ns\",\"$\":\"http://def.ns\"},\"sam\":{\"$\":\"555\", \"@att\":\"lets\"}}}";
        InputStream jsonInputStream = new ByteArrayInputStream(jsonString.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(outStream);
        JSONBadgerfishDataSource source = new JSONBadgerfishDataSource(readLocalName(jsonInputStream), "p");
        source.serialize(writer);
        writer.flush();
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><p xmlns=\"http://def.ns\" xmlns:bb=\"http://other.nsb\" xmlns:aa=\"http://other.ns\"><sam att=\"lets\">555</sam></p>", new String(outStream.toByteArray()));
    }

    private InputStream readLocalName(InputStream in) {
        try {
            while ((char) in.read() != ':') {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }
}
