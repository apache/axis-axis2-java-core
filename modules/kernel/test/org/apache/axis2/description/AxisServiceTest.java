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

package org.apache.axis2.description;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.java2wsdl.XMLSchemaTest;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class AxisServiceTest extends XMLSchemaTest {
    public static final String PARAM_NAME = "CustomParameter";
    public static final Object PARAM_VALUE = new Object();
    private AxisService service;
    private ArrayList<XmlSchema> sampleSchemas;

    class MyObserver implements ParameterObserver {
        public boolean gotIt = false;

        public void parameterChanged(String name, Object value) {
            if (PARAM_NAME.equals(name)) {
                assertEquals("Wrong value", PARAM_VALUE, value);
                gotIt = true;
            }
        }
    }
    
    

    @Override
    protected void setUp() throws Exception {
        service = new AxisService();
        sampleSchemas = new ArrayList<XmlSchema>();
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        service = null;
        sampleSchemas = null;
        super.tearDown();
    }



    public void testAddMessageElementQNameToOperationMappingBasic() {
        
        AxisOperation op1 = new InOnlyAxisOperation();
        QName opName = new QName("foo");
        
        // test registering the same operation multiple times
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));        
    }
    
    public void testAddMessageElementQNameToOperationMappingOverloading() {
        
        AxisOperation op1 = new InOnlyAxisOperation();
        AxisOperation op2 = new InOnlyAxisOperation();
        AxisOperation op3 = new InOnlyAxisOperation();
        QName opName = new QName("foo");
        
        // test registering different operations under the same opName
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op2);
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op3);
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));       
    }

    public void testParameterObserver() throws Exception {

        MyObserver observer = new MyObserver();
        service.addParameterObserver(observer);
        service.addParameter(PARAM_NAME, PARAM_VALUE);
        assertTrue("Didn't get notification", observer.gotIt);
    }
     
    /**
     * Simple test to ensure that Parameters marked as Transient
     * are not persisted.
     * @throws Exception
     */
    public void testTransientParameters() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Parameter p1 = new Parameter("NORMAL", "Hello World");
        Parameter p2 = new Parameter("TRANSIENT", "Hello World");
        p2.setTransient(true);
        
        // The header in an object output is 4 bytes
        final int HEADER_LENGTH = 4;
        
        // Make sure that non-transient value is written
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        p1.writeExternal(oos);
        oos.flush();
        oos.close();
        int length1 = baos.toByteArray().length;
        assertTrue( length1 > HEADER_LENGTH);
        
        // Make sure the transient value is not written
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        p2.writeExternal(oos);
        oos.flush();
        oos.close();
        int length2 = baos.toByteArray().length;
        assertTrue( length2 <= HEADER_LENGTH);
        
        
    }
    
    /**
     * Simple test to make verify that the MessageContext listener
     * is invoked when a ServiceContext is attached to the MessageContext
     * @throws Exception
     */
    public void testMessageContextListener() throws Exception {
        
        AxisConfiguration ac = new AxisConfiguration();
        ConfigurationContext cc = new ConfigurationContext(ac);
        
        // Create a dummy AxisService
        AxisService service = new AxisService();
        service.setName("dummy");
        
        AxisServiceGroup asg = new AxisServiceGroup();
        asg.addService(service);
        
        // Attach a ServiceContextListener
        // The ServiceContextListener will copy sample information from 
        // the ServiceContext onto the MessageContext
        service.addMessageContextListener(new MyMessageContextListener());
        
        // Create a Dummy ServiceContext
        ServiceGroupContext sgc = new ServiceGroupContext(cc, asg);
        ServiceContext sc = sgc.getServiceContext(service);
        sc.setProperty("SERVICE_PROPERTY", "SUCCESSFUL");
        
        // Create a MessageContext
        MessageContext mc = new MessageContext();
        
        // Attach the ServiceContext and MessageContext.
        // This will trigger the MyServiceContextListener.attachEvent
        mc.setServiceContext(sc);
        
        // Verify success
        assertTrue("SUCCESSFUL".equals(mc.getProperty("MESSAGE_PROPERTY")));
    }
    
    public void testOperationActionMapping() throws Exception {                
        AxisOperation op1 = new InOutAxisOperation();
        AxisOperation op2 = new InOutAxisOperation();
        op2.addParameter(DeploymentConstants.TAG_ALLOWOVERRIDE, "true");
        AxisOperation op3 = new InOutAxisOperation();
        
        service.mapActionToOperation("testaction1", op1);
        assertEquals(service.getOperationByAction("testaction1"), op1);
        //Test duplicate registration with same operation
        service.mapActionToOperation("testaction1", op1);
        assertEquals(service.getOperationByAction("testaction1"), op1);
        //Test duplicate registration with different operation and allowOverride
        service.mapActionToOperation("testaction1", op2);
        assertEquals(service.getOperationByAction("testaction1"), op1);
        //Test registration of new operation with allowOverride
        service.mapActionToOperation("testaction2", op2);
        assertEquals(service.getOperationByAction("testaction1"), op1);
        assertEquals(service.getOperationByAction("testaction2"), op2);
        //Test duplicate registration with different operation and no allowOverride
        service.mapActionToOperation("testaction1", op3);
        assertNull(service.getOperationByAction("testaction1"));
        assertEquals(service.getOperationByAction("testaction2"), op2);
    }

    public void testReleaseSchemaList() throws Exception {
        loadSampleSchemaFile(sampleSchemas);
        service.addSchema(sampleSchemas);
        assertTrue(service.getSchema().size() != 0);
        service.releaseSchemaList();
        assertTrue(service.getSchema().size() == 0);
    }

    public void testPrintSchema() throws Exception {
        loadSampleSchemaFile(sampleSchemas);
        service.addSchema(sampleSchemas);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        service.printSchema(stream);
        String a = stream.toString();
        stream.reset();
        for (XmlSchema schema : sampleSchemas) {
            schema.write(stream);
        }
        String b = stream.toString();
        stream.close();
        assertEquals(a, b);
    }

    public void testPrintXSD() throws Exception {
        InputStream is = new FileInputStream(SampleSchemasDirectory + "sampleSchema1.xsd");
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ArrayList<XmlSchema> xmlSchemas = new ArrayList<XmlSchema>();
        xmlSchemas.add(schema);
        service.addSchema(xmlSchemas);
        service.printXSD(stream, "");
        // service has a single schema and it is printed. The it is compared
        // with the saved file
        assertSimilarXML(readXMLfromSchemaFile(SampleSchemasDirectory
                + "printXSDReference.xsd"), stream.toString());
    }

    public void testPrintWSDL() throws Exception {
        service = Utils.createSimpleService(new QName("test"), "", new QName("test"));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        service.printWSDL(stream);

        String s = readWSDLFromFile("test-resources" + File.separator + "wsdl" + File.separator
                + "printWSDLreference.wsdl");
        assertSimilarXML(s, stream.toString());
    }

    public String convertXMLFileToString(String fileName) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            InputStream inputStream = new FileInputStream(new File(fileName));
            org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(
                    inputStream);
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void testGetSchema() throws Exception {
        loadSampleSchemaFile(sampleSchemas);
        service.addSchema(sampleSchemas);
        assertEquals(service.getSchema(0), sampleSchemas.get(0));
    }

    public void testGetSchemaElement() throws Exception {
        loadSampleSchemaFile(sampleSchemas);
        service.addSchema(sampleSchemas);
        XmlSchemaElement schemaElement1 = service.getSchemaElement(new QName(
                "http://www.w3schools.com", "note"));
        XmlSchemaElement schemaElement2 = sampleSchemas.get(0).getElementByName(
                new QName("http://www.w3schools.com", "note"));
        assertEquals(schemaElement1, schemaElement2);
    }

    /**
     * Sameple MessageContextListener which sets a property 
     * on the MessageContext when the SerivceContext is attached.
     */
    class MyMessageContextListener implements MessageContextListener {


        public void attachEnvelopeEvent(MessageContext mc) {
            
        }

        public void attachServiceContextEvent(ServiceContext sc, MessageContext mc) {
            String value = (String) sc.getProperty("SERVICE_PROPERTY");
            mc.setProperty("MESSAGE_PROPERTY", value);
        }
        
    }
}
