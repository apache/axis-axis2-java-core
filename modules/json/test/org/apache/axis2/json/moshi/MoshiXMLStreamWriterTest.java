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

package org.apache.axis2.json.moshi;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
import okio.BufferedSink;
import okio.Okio;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;


public class MoshiXMLStreamWriterTest {
    private String jsonString;

    @Test
    public void testMoshiXMLStreamWriter() throws Exception {
        jsonString = "{\"response\":{\"return\":{\"name\":\"kate\",\"age\":\"35\",\"gender\":\"female\"}}}";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Moshi moshi = new Moshi.Builder().add(Date.class, new Rfc3339DateJsonAdapter()).build();
        JsonAdapter<Object> adapter = moshi.adapter(Object.class);
        BufferedSink sink = Okio.buffer(Okio.sink(baos));
        JsonWriter jsonWriter = JsonWriter.of(sink);

        String fileName = "test-resources/custom_schema/testSchema_1.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is));
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);
        QName elementQName = new QName("http://www.w3schools.com", "response");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());

        MoshiXMLStreamWriter moshiXMLStreamWriter = new MoshiXMLStreamWriter(jsonWriter, elementQName, schemaList, configCtxt);
        OMElement omElement = getResponseOMElement();
        moshiXMLStreamWriter.writeStartDocument();
        omElement.serialize(moshiXMLStreamWriter);
        moshiXMLStreamWriter.writeEndDocument();

        String actualString = baos.toString();
        sink.close();
        Assert.assertEquals(jsonString, actualString);
    }


    private OMElement getResponseOMElement() {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omFactory.createOMNamespace("", "");

        OMElement response = omFactory.createOMElement("response", ns);
        OMElement ret = omFactory.createOMElement("return", ns);
        OMElement name = omFactory.createOMElement("name", ns);
        name.setText("kate");
        OMElement age = omFactory.createOMElement("age", ns);
        age.setText("35");
        OMElement gender = omFactory.createOMElement("gender", ns);
        gender.setText("female");
        ret.addChild(name);
        ret.addChild(age);
        ret.addChild(gender);
        response.addChild(ret);
        return response;
    }
}
