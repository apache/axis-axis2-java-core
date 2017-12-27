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

package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.addressbook.data.AddEntry;
import org.apache.axis2.jaxws.sample.addressbook.data.AddEntryResponse;
import org.apache.axis2.jaxws.sample.addressbook.AddressBook;
import org.apache.axis2.jaxws.sample.addressbook.data.AddressBookEntry;
import org.apache.axis2.jaxws.sample.addressbook.data.ObjectFactory;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

/**
 * This tests the AddressBook same service that exists under
 * org.apache.axis2.jaxws.sample.addressbook.*
 */
public class AddressBookTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private static final String NAMESPACE = "http://org/apache/axis2/jaxws/sample/addressbook";
    private static final QName QNAME_SERVICE = new QName(
            NAMESPACE, "AddressBookService");
    private static final QName QNAME_PORT = new QName(
            NAMESPACE, "AddressBook");

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("AddressBookService.AddressBookImplPort");
    }

    /**
     * Test the endpoint by invoking it with a JAX-WS Dispatch.  
     */
    @Test
    public void testAddressBookWithDispatch() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        JAXBContext jbc = JAXBContext.newInstance("org.apache.axis2.jaxws.sample.addressbook.data");
        
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING, getEndpoint());
        Dispatch<Object> dispatch = service.createDispatch(
                QNAME_PORT, jbc, Mode.PAYLOAD);
                
        // Create the JAX-B object that will hold the data
        ObjectFactory factory = new ObjectFactory();
        AddEntry request = factory.createAddEntry();
        AddressBookEntry content = factory.createAddressBookEntry();
        
        content.setFirstName("Ron");
        content.setLastName("Testerson");
        content.setPhone("512-459-2222");
        
        // Since this is a doc/lit wrapped WSDL, we need to set the 
        // data inside of a request wrapper element.
        request.setEntry(content);
        
        AddEntryResponse response = (AddEntryResponse) dispatch.invoke(request);

        // Validate the results
        assertNotNull(response);
        assertTrue(response.isStatus());
        TestLogger.logger.debug("[pass]     - valid response received");
        TestLogger.logger.debug("[response] - " + response.isStatus());

        
        // Try the dispatch again
        response = (AddEntryResponse) dispatch.invoke(request);

        // Validate the results
        assertNotNull(response);
        assertTrue(response.isStatus());
        TestLogger.logger.debug("[pass]     - valid response received");
        TestLogger.logger.debug("[response] - " + response.isStatus());
    }
    
    
    /**
     * Test the "addEntry" operation.  This sends a complex type and returns
     * a simple type.
     */
    @Test
    public void testAddEntry() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        AddressBook ab = service.getPort(QNAME_PORT, AddressBook.class);
        BindingProvider p1 = (BindingProvider) ab;
        p1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
        
        ObjectFactory factory = new ObjectFactory();
        AddressBookEntry content = factory.createAddressBookEntry();
        content.setFirstName("Foo");
        content.setLastName("Bar");
        content.setPhone("512-459-2222");
        
        boolean added = ab.addEntry(content);
        
        // Validate the results
        assertNotNull(added);
        assertTrue(added);
        
        
        // Try the test again
        added = ab.addEntry(content);
        
        // Validate the results
        assertNotNull(added);
        assertTrue(added);
    }
    
    /**
     * Test the "findEntryByName" operation.  This sends a simple type and 
     * returns a complex type.
     */
    @Test
    public void testFindEntryByName() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        AddressBook ab = service.getPort(QNAME_PORT, AddressBook.class);
        BindingProvider p1 = (BindingProvider) ab;
        p1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
        
        String fname = "Joe";
        String lname = "Test";
        AddressBookEntry result = ab.findEntryByName(fname, lname);
        
        // Validate the results
        assertNotNull(result);
        assertNotNull(result.getFirstName());
        assertNotNull(result.getLastName());
        assertTrue(result.getFirstName().equals(fname));
        assertTrue(result.getLastName().equals(lname));
        
        // Try the invoke again to verify
        result = ab.findEntryByName(fname, lname);
        
        // Validate the results
        assertNotNull(result);
        assertNotNull(result.getFirstName());
        assertNotNull(result.getLastName());
        assertTrue(result.getFirstName().equals(fname));
        assertTrue(result.getLastName().equals(lname));
    }
    
}
