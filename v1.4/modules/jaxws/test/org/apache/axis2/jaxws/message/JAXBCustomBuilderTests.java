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
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.datasource.jaxb.JAXBCustomBuilder;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.datasource.jaxb.JAXBDataSource;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import test.EchoString;
import test.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.TreeSet;

/**
 * Unit Testing of JAXBCustomBuilder.
 * 
 * A JAXBCustomBuilder is an implementation of the Axiom CustomBuilder.
 * When registered on a StAXOMBuilder, the builder will delegate to 
 * the JAXBCustomBuilder when the payload element is encountered.
 * 
 * The JAXBCustomBuilder will build an OMSourcedElement backed by a JAXB
 * object and place it in the OM tree.  This is much faster and more
 * efficient than building an entire OM tree.
 * 
 * @see org.apache.axis2.jaxws.sample.WrapTests for an actual use case
 *
 */
public class JAXBCustomBuilderTests extends TestCase {

    private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    public void test() throws Exception {

        // Setup: Create a jaxb object
        ObjectFactory factory = new ObjectFactory();
        EchoString jaxb = factory.createEchoString(); 
        jaxb.setInput("Hello World");
        
        // Now create a Context for this object
        TreeSet<String> contextPackages = new TreeSet<String>();
        contextPackages.add(EchoString.class.getPackage().getName());
        JAXBContext context = JAXBUtils.getJAXBContext(contextPackages);
           
        // Write out the xml
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(sw);
        Marshaller marshaller = JAXBUtils.getJAXBMarshaller(context);
        marshaller.marshal(jaxb, writer);
        JAXBUtils.releaseJAXBMarshaller(context, marshaller);
        writer.flush();
        sw.flush();
        StringReader sr = new StringReader(sw.toString());
        
        // Read the sample text using OM backed by StAX.
        XMLStreamReader inputReader = inputFactory.createXMLStreamReader(sr);
        StAXOMBuilder builder = new StAXOMBuilder(inputReader); 
        
        // Create the JAXBCustomBuilder
        JAXBDSContext jdsContext = new JAXBDSContext(contextPackages);
        JAXBCustomBuilder jcb = new JAXBCustomBuilder(jdsContext);
        
        // Register the JAXBCustomBuilder...this will intercept the payload
        // and build a jaxb element
        builder.registerCustomBuilderForPayload(jcb);
        
        // Get the OM element
        OMElement om = builder.getDocumentElement();  
        
        // Verify that the OM Element is backed by an unmarshalled jaxb object
        assertTrue(om instanceof OMSourcedElement);
        OMDataSource ds = ((OMSourcedElement) om).getDataSource();
        assertTrue(ds instanceof JAXBDataSource);
        JAXBDataSource jaxbDS = (JAXBDataSource) ds;
        Object jaxbObject = jaxbDS.getObject();
        assertTrue(jaxbObject instanceof EchoString);
        EchoString result = (EchoString) jaxbObject;
        assertTrue(result.getInput().equals("Hello World"));
    }
    
}
