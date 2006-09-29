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

import javax.jws.WebService;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

import junit.framework.TestCase;

public class AnnotationProviderImplDescriptionTests extends TestCase {
    
    public void testBasicProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(BasicProviderTestImpl.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        assertNotNull(testEndpointDesc);
        assertEquals(Service.Mode.MESSAGE, testEndpointDesc.getServiceModeValue());
        assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/", testEndpointDesc.getBindingTypeValue());
        // The WebServiceProvider annotation specified no values on it.
        // TODO: When the Description package changes to provide default values when no annotation present, this may need to change.
        assertEquals("", testEndpointDesc.getWSDLLocation());
        assertEquals("", testEndpointDesc.getServiceName());
        assertEquals("", testEndpointDesc.getPortName());
        assertEquals("", testEndpointDesc.getTargetNamespace());
    }

    public void testWebServiceProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(WebServiceProviderTestImpl.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        assertNotNull(testEndpointDesc);
        assertEquals(Service.Mode.PAYLOAD, testEndpointDesc.getServiceModeValue());
        assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/", testEndpointDesc.getBindingTypeValue());

        assertEquals("http://wsdl.test", testEndpointDesc.getWSDLLocation());
        assertEquals("ProviderService", testEndpointDesc.getServiceName());
        assertEquals("ProviderServicePort", testEndpointDesc.getPortName());
        assertEquals("http://namespace.test", testEndpointDesc.getTargetNamespace());
    }
    
    public void testDefaultServiceModeProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(DefaultServiceModeProviderTestImpl.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        // Default ServiceMode is PAYLOAD per JAXWS p. 80
        assertEquals(Service.Mode.PAYLOAD, testEndpointDesc.getServiceModeValue());
        assertEquals("", testEndpointDesc.getBindingTypeValue());
    }
    
    public void testNoServiceModeProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(NoServiceModeProviderTestImpl.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        assertEquals(javax.xml.ws.Service.Mode.PAYLOAD, testEndpointDesc.getServiceModeValue());
        assertEquals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, testEndpointDesc.getBindingTypeValue());
    }
    
    public void testNoWebServiceProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        try {
            ServiceDescription serviceDesc = 
                DescriptionFactory.createServiceDescriptionFromServiceImpl(NoWebServiceProviderTestImpl.class, null);
            fail("Expected WebServiceException not caught");
        }
        catch (WebServiceException e) {
            // This is the expected successful test path
        }
        catch (Exception e) {
            fail ("Wrong exception caught.  Expected WebServiceException but caught " + e);
        }
    }

    public void testBothWebServiceAnnotations() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        try {
            ServiceDescription serviceDesc = 
                DescriptionFactory.createServiceDescriptionFromServiceImpl(BothWebServiceAnnotationTestImpl.class, null);
            fail("Expected WebServiceException not caught");
        }
        catch (WebServiceException e) {
            // This is the expected successful test path
        }
        catch (Exception e) {
            fail ("Wrong exception caught.  Expected WebServiceException but caught " + e);
        }
    }
    
    public void testServiceModeOnNonProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(WebServiceSEITestImpl.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        assertNull(testEndpointDesc.getServiceModeValue());
        assertEquals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, testEndpointDesc.getBindingTypeValue());
    }
}

// ===============================================
// Basic Provider service implementation class
// ===============================================
@ServiceMode(value=Service.Mode.MESSAGE)
@WebServiceProvider()
@BindingType(value="http://www.w3.org/2003/05/soap/bindings/HTTP/")
class BasicProviderTestImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }
}

// ===============================================
// WebServiceProvider service implementation class
// ===============================================
@ServiceMode(value=Service.Mode.PAYLOAD)
@WebServiceProvider(serviceName="ProviderService", portName="ProviderServicePort", 
        targetNamespace="http://namespace.test", wsdlLocation="http://wsdl.test")
@BindingType(value="http://www.w3.org/2003/05/soap/bindings/HTTP/")
class WebServiceProviderTestImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }
}

// ===============================================
// Default ServiceMode and BindingType Provider service implementation class
// Default is PAYLOAD per JAXWS p. 80
// ===============================================
@ServiceMode()
@WebServiceProvider()
@BindingType()
class DefaultServiceModeProviderTestImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }
}

// ===============================================
// No ServiceMode and no BindingType Provider service implementation class
// ===============================================
@WebServiceProvider()
class NoServiceModeProviderTestImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }
}

// ===============================================
// NO WebServiceProvider Provider service implementation class
// This is an INVALID service implementation
// ===============================================
@ServiceMode(value=Service.Mode.MESSAGE)
@BindingType(value="http://www.w3.org/2003/05/soap/bindings/HTTP/")
class NoWebServiceProviderTestImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }  
}

// ===============================================
// BOTH WebService and WebServiceProvider Provider service implementation class
// This is an INVALID service implementation
//===============================================
@ServiceMode(value=Service.Mode.MESSAGE)
@WebService()
@WebServiceProvider()
@BindingType(value="http://www.w3.org/2003/05/soap/bindings/HTTP/")
class BothWebServiceAnnotationTestImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }  
}

// ===============================================
// WebService service implementation class; not 
// Provider-based
// ===============================================

@WebService()
class WebServiceSEITestImpl {
    public String echo (String s) {
        return "From WebServiceSEITestImpl " + "s";
    }
}

