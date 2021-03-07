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

package org.temp;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.custommonkey.xmlunit.Diff;
import org.junit.jupiter.api.Assertions;

import javax.xml.transform.stream.StreamSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public abstract class XMLSchemaTest {

    public final String XMLSchemaNameSpace = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    public final String CustomSchemaLocation = "src" + File.separator + "test"
            + File.separator + "schemas" + File.separator + "custom_schemas"
            + File.separator + "note.xsd";
    
    public final String customDirectoryLocation = "src" + File.separator + "test"
            + File.separator + "schemas" + File.separator + "custom_schemas"
            + File.separator;

    public final String SampleSchemasDirectory = "src" + File.separator + "test"
            + File.separator + "schemas" + File.separator + "custom_schemas"
            + File.separator;

    public final String MappingFileLocation = "src" + File.separator + "test" + File.separator
            + "schemas" + File.separator + "custom_schemas" + File.separator
            + "mapping.txt";

    public void assertSimilarXML(String XML1, String XML2) throws Exception {
        Diff myDiff = new Diff(XML1, XML2);
        assertTrue(myDiff.similar(), () -> "XML similar " + myDiff.toString());

    }

    public void assertIdenticalXML(String XML1, String XML2) throws Exception {
        Diff myDiff = new Diff(XML1, XML2);
        assertTrue(myDiff.identical(), () -> "XML similar " + myDiff.toString());

    }

    public String readXMLfromSchemaFile(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        schema.write(stream);
        is.close();
        return stream.toString();
    }
}
