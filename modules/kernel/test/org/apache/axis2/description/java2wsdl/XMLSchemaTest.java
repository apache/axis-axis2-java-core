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

import junit.framework.TestCase;

import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import javax.xml.transform.stream.StreamSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class XMLSchemaTest extends TestCase {

    public final String XMLSchemaNameSpace = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    public final String CustomSchemaLocation = "test-resources"
            + File.separator + "schemas" + File.separator + "custom_schemas"
            + File.separator + "note.xsd";
    
    public final String customDirectoryLocation = "test-resources"
            + File.separator + "schemas" + File.separator + "custom_schemas"
            + File.separator;

    public final String SampleSchemasDirectory = "test-resources"
            + File.separator + "schemas" + File.separator + "custom_schemas"
            + File.separator;

    public final String MappingFileLocation = "test-resources" + File.separator
            + "schemas" + File.separator + "mapping_files" + File.separator
            + "mapping1.txt";

    public void assertSimilarXML(String expected, String actual) throws Exception {
        boolean ignoreWhitespace = XMLUnit.getIgnoreWhitespace();
        XMLUnit.setIgnoreWhitespace(true);
        try {
            Diff myDiff = new Diff(expected, actual);
            assertTrue("XML similar " + myDiff.toString(), myDiff.similar());
        } finally {
            XMLUnit.setIgnoreWhitespace(ignoreWhitespace);
        }
    }

    public void loadSampleSchemaFile(ArrayList<XmlSchema> schemas) throws Exception{
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        File file = null;
        int i = 1;
       
            file = new File(SampleSchemasDirectory + "sampleSchema" + i
                    + ".xsd");
            while (file.exists()) {
                InputStream is = new FileInputStream(file);
                XmlSchemaCollection schemaCol = new XmlSchemaCollection();
                XmlSchema schema = schemaCol.read(new StreamSource(is));
                schemas.add(schema);
                i++;
                file = new File(SampleSchemasDirectory + "sampleSchema" + i
                        + ".xsd");
            }
       
    }
    
    public XmlSchema loadSingleSchemaFile(int i) throws Exception{
        File file = new File(SampleSchemasDirectory + "sampleSchema" + i
                + ".xsd");
        InputStream is = new FileInputStream(file);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is));
        return schema;
    }
    
    public String readFile(String fileName) throws Exception {
        File file = new File(fileName);
        char[] buffer = null;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        buffer = new char[(int) file.length()];
        int i = 0;
        int c = bufferedReader.read();
        while (c != -1) {
            buffer[i++] = (char) c;
            c = bufferedReader.read(); 
        }
        return new String(buffer);
    }

    public String readXMLfromSchemaFile(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        schema.write(stream);
        return stream.toString();
    }
    
   
    public String readWSDLFromFile(String path) throws Exception {
        File file=new File(path);
        XMLPrettyPrinter.prettify(file);    //this is used to correct unnecessary formatting in the file
        return readFile(path);
    }
    
    public void writeToFile(String path,String data) throws Exception{
        FileWriter fileWriter=new FileWriter(new File(path));
        fileWriter.write(data);
        fileWriter.flush();
        fileWriter.close();        
    }

}
