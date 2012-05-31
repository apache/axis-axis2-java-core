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

package org.apache.axis2.description.java2wsdl;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public abstract class XMLSchemaTest extends TestCase {

    public final String XMLSchemaNameSpace = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    public final String CustomSchemaLocation = "test-resources" + File.separator + "schemas"
            + File.separator + "custom_schemas" + File.separator + "note.xsd";

    public final String SampleSchemasDirectory = "test-resources" + File.separator + "schemas"
            + File.separator + "custom_schemas" + File.separator;

    public final String MappingFileLocation = "test-resources" + File.separator + "schemas"
            + File.separator + "mapping_files" + File.separator + "mapping1.txt";

    public void assertSimilarXML(String XML1, String XML2) throws Exception {
        Diff myDiff = new Diff(XML1, XML2);
        assertTrue("XML similar " + myDiff.toString(), myDiff.similar());

    }

    public void assertIdenticalXML(String XML1, String XML2) throws Exception {
        Diff myDiff = new Diff(XML1, XML2);
        assertTrue("XML similar " + myDiff.toString(), myDiff.identical());

    }

    public void loadSampleSchemaFile(ArrayList<XmlSchema> schemas) throws Exception {
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        File file = null;
        int i = 1;

        file = new File(SampleSchemasDirectory + "sampleSchema" + i + ".xsd");
        while (file.exists()) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            Document doc = documentBuilderFactory.newDocumentBuilder().parse(file);
            schemas.add(xmlSchemaCollection.read(doc, null));
            i++;
            file = new File(SampleSchemasDirectory + "sampleSchema" + i + ".xsd");
        }

    }

}
