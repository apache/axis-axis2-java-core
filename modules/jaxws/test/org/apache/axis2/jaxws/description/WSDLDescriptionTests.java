/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.axis2.jaxws.description;

import java.lang.reflect.Field;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.ws.axis2.tests.EchoPort;

import junit.framework.TestCase;

/**
 * Directly test the Description classes built with a WSDL file.
 */
public class WSDLDescriptionTests extends TestCase {
    
    private Service service;
    private ServiceDelegate serviceDelegate;
    private ServiceDescription serviceDescription;

    private static final String VALID_PORT = "EchoPort";
    private static final String VALID_NAMESPACE = "http://ws.apache.org/axis2/tests";
    private QName validPortQName = new QName(VALID_NAMESPACE, VALID_PORT);

    
    protected void setUp() {
        // Create a new service for each test to test various valid and invalid
        // flows
        String namespaceURI = VALID_NAMESPACE;
        String localPart = "EchoService";
        URL wsdlURL = DescriptionTestUtils.getWSDLURL();
        assertNotNull(wsdlURL);
        service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
        serviceDelegate = DescriptionTestUtils.getServiceDelegate(service);
        serviceDescription = serviceDelegate.getServiceDescription();
    }
    
    /*
     * ========================================================================
     * ServiceDescription Tests
     * ========================================================================
     */
    public void testInvalidLocalpartServiceGetEndpoint() {
        QName invalidPortQname = new QName(VALID_NAMESPACE, "InvalidEchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(invalidPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }

    public void testInvalidNamespaceServiceGetEndpoint() {
        QName invalidPortQname = new QName("http://ws.apache.org/axis2/tests/INVALID", VALID_PORT);
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(invalidPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }

    // ========================================================================
    // EndpointDescription Tests
    // ========================================================================
    
    public void testValidGetPortWithClass() {
        // TODO: This is currently not supported (in Beta) but needs to be supported; this test tests the unsupported behavior
        //       The try/catch should be removed and the pass/fail criteria changed.
        try {
            EchoPort echoPort = service.getPort(EchoPort.class);
            fail("BETA ONLY: didn't catch expected exception");
        }
        catch (UnsupportedOperationException e) {
            // FOR BETA ONLY: expect this exception
        }
    }
    
    public void testValidGetPortWithClassAndQName() {
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);

        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        EndpointDescription endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class)[0];
        assertNotNull(endpointDescViaSEI);
        assertEquals(endpointDesc, endpointDescViaSEI);
        
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        Class sei = endpointInterfaceDesc.getSEIClass();
        assertEquals(EchoPort.class, sei);
    }
    
    public void testValidAddPort() {
        QName dispatchPortQN = new QName(VALID_NAMESPACE, "dispatchPort");
        service.addPort(dispatchPortQN, null, null);
        
        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(dispatchPortQN);
        assertNotNull(endpointDesc);
       
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNull(endpointInterfaceDesc);
    }
    
    public void testInvalidAddPort() {
        try {
            service.addPort(validPortQName, null, null);
            fail("Shouldn't be able to add a port that exists in the WSDL");
        }
        catch (WebServiceException e) {
            // Expected path
        }
    }
    
    public void testValidAddAndGetPort() {
        QName dispatchPortQN = new QName(VALID_NAMESPACE, "dispatchPort");
        service.addPort(dispatchPortQN, null, null);
        
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);

        
        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        EndpointDescription endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class)[0];
        assertNotNull(endpointDescViaSEI);
        assertEquals(endpointDesc, endpointDescViaSEI);
        
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        Class sei = endpointInterfaceDesc.getSEIClass();
        assertEquals(EchoPort.class, sei);

        EndpointDescription endpointDescDispatch = serviceDescription.getEndpointDescription(dispatchPortQN);
        assertNotNull(endpointDescDispatch);
       
        EndpointInterfaceDescription endpointInterfaceDescDispatch = endpointDescDispatch.getEndpointInterfaceDescription();
        assertNull(endpointInterfaceDescDispatch);
    }
    
    public void testInvalidAddAndGetPort() {
        // Should not be able to do a getPort on one that was added with addPort
        QName dispatchPortQN = new QName(VALID_NAMESPACE, "dispatchPort");
        service.addPort(dispatchPortQN, null, null);
        try {
            EchoPort echoPort = service.getPort(dispatchPortQN, EchoPort.class);
            fail("Should have thrown a WebServiceException");
        }
        catch (WebServiceException e) {
            // Expected path
        }

        
    }
}
