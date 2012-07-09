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

package org.apache.axis2.schema;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

public class SchemaCompilerTest extends XMLSchemaTest{
    
    private ArrayList<XmlSchema> schemas;
    private SchemaCompiler schemaCompiler;
    private HashMap<QName,String> processedElementMap;
    

    @Override
    protected void setUp() throws Exception {
        schemas=new ArrayList<XmlSchema>();       
        schemaCompiler=new SchemaCompiler(null);
    }

    @Override
    protected void tearDown() throws Exception {
        schemas=null;
        super.tearDown();
    }
    
    
    public void testCompileSchema() throws Exception{
        List<Integer> excludes = new ArrayList<Integer>();
        excludes.add(6);
        loadSampleSchemaFile(schemas, excludes);        
        Map map=schemaCompiler.getProcessedModelMap();
        schemaCompiler.compile(schemas);
        processedElementMap=schemaCompiler.getProcessedElementMap();
        map=schemaCompiler.getProcessedModelMap();
        Iterator iterator=map.values().iterator();
        int i=0;
        while (iterator.hasNext()) {
            i++;
            Document document= (Document) iterator.next();
            ByteArrayOutputStream  stream=new ByteArrayOutputStream();
            XmlSchemaCollection schemaCol = new XmlSchemaCollection();
            XmlSchema schema = schemaCol.read(document, null);
            schema.write(stream);
            assertSimilarXML(stream.toString(), readXMLfromSchemaFile(customDirectoryLocation+"generated"+i+".xsd"));
            
        }
        
    }
    
    public void testCompileSchemaForMixContent() throws Exception {
        schemas.add(loadSampleSchemaFile(String.valueOf(6)));
        Map map = schemaCompiler.getProcessedModelMap();
        try {
            schemaCompiler.compile(schemas);
            fail("Compiling sampleSchema6.xsd should throw SchemaCompilationException");
        } catch (SchemaCompilationException e) {
            assertTrue(e.getMessage().contains(
                    "SD complexType with mix content not supported in ADB"));
        }

    }

}
