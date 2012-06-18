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

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;



public class ExtensionUtilityTest extends XMLSchemaTest {
    
    public void testInvoke() throws Exception{
        ArrayList<XmlSchema> schemas=new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemas);
        AxisService service=new AxisService();
        service.addSchema(schemas);
        CodeGenConfiguration configuration=new CodeGenConfiguration(new HashMap<String, CommandLineOption>());
        configuration.addAxisService(service);
        ExtensionUtility.invoke(configuration);
        TypeMapper mapper=configuration.getTypeMapper();
        assertEquals(mapper.getParameterName(new QName("//www.w3schools.com", "order")), "order");
     //   assertEquals(mapper., actual)
        ByteArrayOutputStream stream =new ByteArrayOutputStream();
        Document document=(Document)mapper.getTypeMappingObject(new QName("ColorType"));
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(document, null);
        schema.write(stream);
        //writeToFile(customDirectoryLocation+"generated.xsd", stream.toString());
        assertSimilarXML(stream.toString(), readXMLfromSchemaFile(customDirectoryLocation+"generated.xsd"));
 
        
    }

}
