package org.apache.axis2.jaxws.description;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import junit.framework.TestCase;

public class ServiceAnnotationTests extends TestCase {

    public void testWebServiceDefaults() {
        String className = "WebServiceDefaultTest";
        EndpointDescription testEndpointDesc = getEndpointDesc(WebServiceDefaultTest.class);
        assertNotNull(testEndpointDesc.getWebServiceAnnotation());
        assertNull(testEndpointDesc.getWebServiceProviderAnnotation());
        assertEquals(className, testEndpointDesc.getWebServiceName());
        assertEquals("", testEndpointDesc.getWebServiceEndpointInterface());
        assertEquals("http://description.jaxws.axis2.apache.org/", testEndpointDesc.getWebServiceTargetNamespace());
        assertEquals(className + "Service", testEndpointDesc.getWebServiceServiceName());
        assertEquals(className + "Port", testEndpointDesc.getWebServicePortName());
        assertEquals("", testEndpointDesc.getWebServiceWSDLLocation());
    }
    
    public void testWebServiceProviderDefaults() {
        String className = "WebServiceProviderDefaultTest";
        EndpointDescription testEndpointDesc = getEndpointDesc(WebServiceProviderDefaultTest.class);
        assertNull(testEndpointDesc.getWebServiceAnnotation());
        assertNotNull(testEndpointDesc.getWebServiceProviderAnnotation());
        // name element not allowed on WebServiceProvider
        assertEquals("", testEndpointDesc.getWebServiceName());
        // EndpointInterface element not allowed on WebServiceProvider
        assertEquals("", testEndpointDesc.getWebServiceEndpointInterface());
        assertEquals("http://description.jaxws.axis2.apache.org/", testEndpointDesc.getWebServiceTargetNamespace());
        assertEquals(className + "Service", testEndpointDesc.getWebServiceServiceName());
        assertEquals(className + "Port", testEndpointDesc.getWebServicePortName());
        assertEquals("", testEndpointDesc.getWebServiceWSDLLocation());
    }
    
    public void testWebServiceName() {
        String className = "WebServiceName"; 
        EndpointDescription testEndpointDesc = getEndpointDesc(WebServiceName.class);
        assertNotNull(testEndpointDesc.getWebServiceAnnotation());
        assertNull(testEndpointDesc.getWebServiceProviderAnnotation());
        assertEquals("WebServiceNameElement", testEndpointDesc.getWebServiceName());
        assertEquals("", testEndpointDesc.getWebServiceEndpointInterface());
        assertEquals("http://description.jaxws.axis2.apache.org/", testEndpointDesc.getWebServiceTargetNamespace());
        // Note that per JSR-181 MR Sec 4.1 pg 16, the portName uses WebService.name, but serviceName does not!
        assertEquals(className + "Service", testEndpointDesc.getWebServiceServiceName());
        assertEquals("WebServiceNameElementPort", testEndpointDesc.getWebServicePortName());
        assertEquals("", testEndpointDesc.getWebServiceWSDLLocation());
    }
    
    public void testWebServiceNameAndPort() {
        String className = "WebServiceNameAndPort"; 
        EndpointDescription testEndpointDesc = getEndpointDesc(WebServiceNameAndPort.class);
        assertNotNull(testEndpointDesc.getWebServiceAnnotation());
        assertNull(testEndpointDesc.getWebServiceProviderAnnotation());
        assertEquals("WebServiceNameAndPortElement", testEndpointDesc.getWebServiceName());
        assertEquals("", testEndpointDesc.getWebServiceEndpointInterface());
        assertEquals("http://description.jaxws.axis2.apache.org/", testEndpointDesc.getWebServiceTargetNamespace());
        // Note that per JSR-181 MR Sec 4.1 pg 16, the portName uses WebService.name, but serviceName does not!
        assertEquals(className + "Service", testEndpointDesc.getWebServiceServiceName());
        assertEquals("WebServicePortName", testEndpointDesc.getWebServicePortName());
        assertEquals("", testEndpointDesc.getWebServiceWSDLLocation());
    }
    
    public void testWebServiceAll() {
        String className = "WebServiceAll"; 
        EndpointDescription testEndpointDesc = getEndpointDesc(WebServiceAll.class);
        assertNotNull(testEndpointDesc.getWebServiceAnnotation());
        assertNull(testEndpointDesc.getWebServiceProviderAnnotation());
        assertEquals("WebServiceAllNameElement", testEndpointDesc.getWebServiceName());
        assertEquals("org.apache.axis2.jaxws.description.MyEndpointInterface", testEndpointDesc.getWebServiceEndpointInterface());
        assertEquals("http://namespace.target.jaxws.axis2.apache.org/", testEndpointDesc.getWebServiceTargetNamespace());
        assertEquals("WebServiceAllServiceElement", testEndpointDesc.getWebServiceServiceName());
        assertEquals("WebServiceAllPortElement", testEndpointDesc.getWebServicePortName());
        assertEquals("http://my.wsdl.location/foo.wsdl", testEndpointDesc.getWebServiceWSDLLocation());
    }
    
    public void testWebServiceProviderAll() {
        String className = "WebServiceProviderAll"; 
        EndpointDescription testEndpointDesc = getEndpointDesc(WebServiceProviderAll.class);
        assertNull(testEndpointDesc.getWebServiceAnnotation());
        assertNotNull(testEndpointDesc.getWebServiceProviderAnnotation());
        assertEquals("", testEndpointDesc.getWebServiceName());
        assertEquals("", testEndpointDesc.getWebServiceEndpointInterface());
        assertEquals("http://namespace.target.jaxws.axis2.apache.org/", testEndpointDesc.getWebServiceTargetNamespace());
        assertEquals("WebServiceProviderAllServiceElement", testEndpointDesc.getWebServiceServiceName());
        assertEquals("WebServiceProviderAllPortElement", testEndpointDesc.getWebServicePortName());
        assertEquals("http://my.wsdl.other.location/foo.wsdl", testEndpointDesc.getWebServiceWSDLLocation());
        
    }
    
    /*
     * Method to return the endpoint interface description for a given implementation class.
     */
    private EndpointDescription getEndpointDesc(Class implementationClass) {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(implementationClass, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        return testEndpointDesc;
    }
}

// ===============================================
// WebService Defaults test impl
// ===============================================
@WebService()
class WebServiceDefaultTest {
    
}

@WebServiceProvider()
class WebServiceProviderDefaultTest {
    
}

// ===============================================
// WebService Name test impl
// ===============================================
// Note that name is only allowed on @WebService; not @WebServiceProvider
@WebService(name="WebServiceNameElement")
class WebServiceName {
    
}

@WebService(name="WebServiceNameAndPortElement", portName="WebServicePortName")
class WebServiceNameAndPort {
    
}

// ===============================================
// WebService All test impl
// ===============================================
@WebService(
        name="WebServiceAllNameElement", 
        endpointInterface="org.apache.axis2.jaxws.description.MyEndpointInterface",
        targetNamespace="http://namespace.target.jaxws.axis2.apache.org/",
        serviceName="WebServiceAllServiceElement",
        portName="WebServiceAllPortElement",
        wsdlLocation="http://my.wsdl.location/foo.wsdl")
class WebServiceAll {
    
}

@WebService()
interface MyEndpointInterface {
    
}

@WebServiceProvider(
        targetNamespace="http://namespace.target.jaxws.axis2.apache.org/",
        serviceName="WebServiceProviderAllServiceElement",
        portName="WebServiceProviderAllPortElement",
        wsdlLocation="http://my.wsdl.other.location/foo.wsdl")
class WebServiceProviderAll {
    
}
