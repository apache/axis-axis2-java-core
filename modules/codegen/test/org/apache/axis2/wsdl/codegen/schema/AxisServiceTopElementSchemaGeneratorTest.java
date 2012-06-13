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

package org.apache.axis2.wsdl.codegen.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.codegen.XMLSchemaTest;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.junit.Test;

public class AxisServiceTopElementSchemaGeneratorTest extends XMLSchemaTest {
    

    private AxisService service;
    private ArrayList<XmlSchema> schemas;
    private AxisServiceTopElementSchemaGenerator generator;
    private Map schemaMap;
    private Set topElements;

    @Override
    protected void setUp() throws Exception {
        service = new AxisService();
        schemas = new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemas);
        service.addSchema(schemas);
        generator = new AxisServiceTopElementSchemaGenerator(null);

        topElements = new HashSet();

        TopElement topElement;

        topElement = new TopElement(new QName("http://test.com", "testElement1"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com", "testElement2"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com", "testElement3"));
        topElement.setTypeQName(new QName(Constants.URI_2001_SCHEMA_XSD, "string"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com", "testElement4"));
        topElement.setTypeQName(new QName("http://test1.com", "testComplexType1"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com", "testElement5"));
        topElement.setTypeQName(new QName("http://test.com", "testComplexType2"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test.com", "testElement6"));
        topElement.setTypeQName(new QName("http://test2.com", "testComplexType2"));
        topElements.add(topElement);

        schemaMap = generator.getSchemaMap(topElements);
        generator.getXmlSchemaList(schemaMap);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        service = null;
        generator = null;
        schemaMap.clear();
        topElements = null;
        super.tearDown();
    }


    public void testSchemaGeneration() throws Exception {

        AxisServiceTopElementSchemaGenerator schemaGenerator = new AxisServiceTopElementSchemaGenerator(null);

        Set topElements = new HashSet();

        TopElement topElement;

        topElement = new TopElement(new QName("http://test.com","testElement1"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com","testElement2"));
        topElements.add(topElement);
        topElement = new TopElement(new QName("http://test1.com","testElement3"));
        topElement.setTypeQName(new QName(Constants.URI_2001_SCHEMA_XSD,"string"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com","testElement4"));
        topElement.setTypeQName(new QName("http://test1.com","testComplexType1"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test1.com","testElement5"));
        topElement.setTypeQName(new QName("http://test.com","testComplexType2"));
        topElements.add(topElement);

        topElement = new TopElement(new QName("http://test.com","testElement6"));
        topElement.setTypeQName(new QName("http://test2.com","testComplexType2"));
        topElements.add(topElement);

        Map schemaMap = schemaGenerator.getSchemaMap(topElements);
        schemaGenerator.getXmlSchemaList(schemaMap);

//        List xmlSchemaList = schemaGenerator.getXmlSchemaList(schemaMap);
//        for (Object aXmlSchemaList : xmlSchemaList) {
//            ((org.apache.ws.commons.schema.XmlSchema)aXmlSchemaList).write(System.out);
//        }
    }
    
    @Test
    public void testGetSchemaMap(){
        schemaMap = generator.getSchemaMap(topElements);
        org.apache.axis2.wsdl.codegen.schema.XmlSchema schema=(org.apache.axis2.wsdl.codegen.schema.XmlSchema)schemaMap.get("http://test.com");
        TopElement topElement = new TopElement(new QName("http://test.com", "testElement1"));
        assertTrue(schemaMap.size()==3);
        assertEquals(schema.getTargetNamespace(), "http://test.com");
    }
    
    @Test
    public void testGetTopElements() throws Exception {
        AxisOperation axisOperation = new AxisOperation() {

            @Override
            public void setRemainingPhasesInFlow(ArrayList list) {
            }

            @Override
            public void setPhasesOutFlow(ArrayList list) {
            }

            @Override
            public AxisService getAxisService() {
                return service;
            }

            @Override
            public void setPhasesOutFaultFlow(ArrayList list) {

            }

            @Override
            public void setPhasesInFaultFlow(ArrayList list) {
            }

            @Override
            public ArrayList getRemainingPhasesInFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesOutFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesOutFaultFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesInFaultFlow() {
                return null;
            }

            @Override
            public AxisMessage getMessage(String label) {
                return null;
            }

            @Override
            public OperationClient createClient(ServiceContext sc, Options options) {
                return null;
            }

            @Override
            public void addMessageContext(MessageContext msgContext, OperationContext opContext)
                    throws AxisFault {
            }

            @Override
            public void addMessage(AxisMessage message, String label) {
            }

            @Override
            public void addFaultMessageContext(MessageContext msgContext, OperationContext opContext)
                    throws AxisFault {

            }
        };
        axisOperation.setName(new QName("http://www.w3schools.com", "tset"));
        AxisMessage axisMessage = new AxisMessage();
        axisMessage.setParent(axisOperation);
        axisMessage.setElementQName(new QName("http://www.w3schools.com", "note"));
        axisOperation.addChild("message",axisMessage);
        service.addOperation(axisOperation);
        generator = new AxisServiceTopElementSchemaGenerator(service);
        Set set=generator.getTopElements();
        boolean found=false;
        TopElement element=null;        
        for (Object object : set) {
            element=(TopElement)object;
            if(element.getElementQName().equals(new QName("http://www.w3schools.com", "note"))){
                found=true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testGetSchemaElement() throws Exception {
        generator = new AxisServiceTopElementSchemaGenerator(service);
        XmlSchemaElement element=generator.getSchemaElement(new QName("http://www.w3schools.com","note"));
        assertNotNull(element);
        assertEquals(element.getName(), "note");
        
    }

    @Test
    public void testGetXmlSchemaList() throws Exception {
        Map map = generator.getSchemaMap(topElements);
        List xmlSchemaList = generator.getXmlSchemaList(map);
        int i = 0;
        for (Object object : xmlSchemaList) {
            i++;
            XmlSchema schema = (XmlSchema) object;
            // compare with initially generated ones
            String s = schemaToString(schema);
            s=s.replaceAll("<xsd:element name=\"testElement6\" type=\".*:testComplexType2\"/>",
                    "<xsd:element name=\"testElement6\" type=\"ns0:testComplexType2\"/>");
            
            s=s.replaceAll("<xsd:element name=\"testElement5\" type=\".*:testComplexType2\"/>",
                    "<xsd:element name=\"testElement5\" type=\"ns1:testComplexType2\"/>");
          
            String s1=readXMLfromSchemaFile(customDirectoryLocation + "generatedSchema" + i + ".xsd");
  
          assertNotNull(s);
         
       
        }
    }
    
    
    
}
