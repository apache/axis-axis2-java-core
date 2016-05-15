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

package org.apache.axis2.jaxws.message;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.jaxws.unitTest.TestLogger;
import test.Data;
import test.ObjectFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.io.ByteArrayOutputStream;
import java.util.TreeSet;

/**
 * JAXBDSContextTests
 * Tests to create and validate JAXBDSContext
 * These are not client/server tests.
 */
public class JAXBDSContextTests extends TestCase {
    /**
     * Create a Block representing an JAXB and simulate a 
     * normal Dispatch<JAXB> flow
     * @throws Exception
     */
    public void testMarshal() throws Exception {
        
        // Create a JAXBDSContext for the package containing Data
        TreeSet<String> packages = new TreeSet<String>();
        packages.add(Data.class.getPackage().getName());
        final JAXBDSContext context = new JAXBDSContext(packages);
        
        TestLogger.logger.debug(context.getJAXBContext().toString());
        
        // Force marshal by type
        context.setProcessType(Data.class);
        
        // Create an Data value
        ObjectFactory factory = new ObjectFactory();
        Data value = factory.createData(); 
        value.setInput("Hello World");
        
        // Create a JAXBElement
        QName qName = new QName("urn://sample", "data");
        final JAXBElement<Data> jaxbElement = new JAXBElement<Data>(qName, Data.class, value);

        // Create a writer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        
        // Marshal the value
        OMAbstractFactory.getOMFactory().createOMElement(new AbstractPushOMDataSource() {
            @Override
            public void serialize(XMLStreamWriter writer) throws XMLStreamException {
                writer.writeStartElement("", "root", "");
                try {
                    context.marshal(jaxbElement, writer);
                } catch (JAXBException ex) {
                    throw new OMException(ex);
                }
                writer.writeEndElement();
            }
            
            @Override
            public boolean isDestructiveWrite() {
                return false;
            }
        }).serialize(baos, format);
        
        assertTrue(baos.toString().indexOf("Hello World") > 0);
        assertTrue(baos.toString().indexOf("</root>") > 0);
    }
    
    /**
     * Create a Block representing an JAXB and simulate a 
     * normal Dispatch<JAXB> flow
     * @throws Exception
     */
    public void testMarshalArray() throws Exception {
        
        // Create a JAXBDSContext for the package containing Data
        TreeSet<String> packages = new TreeSet<String>();
        packages.add(Data.class.getPackage().getName());
        final JAXBDSContext context = new JAXBDSContext(packages);
        
        TestLogger.logger.debug(context.getJAXBContext().toString());
        
        // Force marshal by type
        context.setProcessType(Data[].class);
        
        // Create an Data value
        ObjectFactory factory = new ObjectFactory();
        Data value[] = new Data[3];
        value[0] = factory.createData(); 
        value[0].setInput("Hello");
        value[1] = factory.createData(); 
        value[1].setInput("Beautiful");
        value[2] = factory.createData(); 
        value[2].setInput("World");
        
        // Create a JAXBElement.
        // To indicate "occurrence elements", the value is wrapped in
        // an OccurrenceArray
        QName qName = new QName("urn://sample", "data");
        OccurrenceArray occurrenceArray = new OccurrenceArray(value);
        final JAXBElement jaxbElement = new JAXBElement(qName, Data[].class, occurrenceArray);

        // Create a writer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        
        // Marshal the value
        OMAbstractFactory.getOMFactory().createOMElement(new AbstractPushOMDataSource() {
            @Override
            public void serialize(XMLStreamWriter writer) throws XMLStreamException {
                writer.writeStartElement("", "root", "");
                try {
                    context.marshal(jaxbElement, writer);
                } catch (JAXBException ex) {
                    throw new OMException(ex);
                }
                writer.writeEndElement();
            }
            
            @Override
            public boolean isDestructiveWrite() {
                return false;
            }
        }).serialize(baos, format);
        
        String outputText = baos.toString();
        String subText = outputText;
        int count = 0;
        while (subText.indexOf("data") > 0) {
            count++;
            subText = subText.substring(subText.indexOf("data") + 1);
        }
        // 3 data refs for start tag name
        // 3 data refs for end tag name
        // 3 xsi type refs
        assertTrue("Expected 9 data tags but found "+count+"  Text is:"+outputText, count == 9);
    }
   
}
