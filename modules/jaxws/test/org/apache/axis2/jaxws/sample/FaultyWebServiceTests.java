/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import java.net.UnknownHostException;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.faults.FaultyWebServiceFault_Exception;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServiceService;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrapService;


public class FaultyWebServiceTests extends TestCase {
	String axisEndpoint = "http://localhost:8080/axis2/services/FaultyWebServiceService";
	public void testFaultyWebService(){
		FaultyWebServiceFault_Exception exception = null;
		try{
			System.out.println("----------------------------------");
		    System.out.println("test: " + getName());
		    FaultyWebServiceService service = new FaultyWebServiceService();
		    FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
			BindingProvider p =	(BindingProvider)proxy;
			p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);

			// the invoke will throw an exception, if the test is performed right
			int total = proxy.faultyWebService(10);
			
		}catch(FaultyWebServiceFault_Exception e){
			exception = e;
		}
		
		System.out.println("----------------------------------");
		
		assertNotNull(exception);
		assertEquals("custom exception", exception.getMessage());
		assertNotNull(exception.getFaultInfo());
		assertEquals("bean custom fault info", exception.getFaultInfo().getFaultInfo());
		assertEquals("bean custom message", exception.getFaultInfo().getMessage());
		
	}
    
    public void testFaultyWebService_badEndpoint(){
        
        String host = "this.is.a.bad.endpoint.terrible.in.fact";
        String badEndpoint = "http://" + host;
        
        WebServiceException exception = null;

        try{
            System.out.println("----------------------------------");
            System.out.println("test: " + getName());
            FaultyWebServiceService service = new FaultyWebServiceService();
            FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,badEndpoint);

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.faultyWebService(10);

        }catch(FaultyWebServiceFault_Exception e) {
            // shouldn't get this exception
            fail(e.toString());
        }catch(WebServiceException e) {
            exception = e;
        }catch(Exception e) {
            fail("This testcase should only produce a WebServiceException.  We got: " + e.toString());
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        assertTrue(exception.getCause() instanceof UnknownHostException);
        assertEquals(exception.getMessage(), host);

    }

    // TODO should also have an invoke oneway bad endpoint test to make sure
    // we get an exception as indicated in JAXWS 6.4.2.

    
    public void testFaultyWebService_badEndpoint_oneWay() {
        
        String host = "this.is.a.bad.endpoint.terrible.in.fact";
        String badEndpoint = "http://" + host;
        
        WebServiceException exception = null;
        
        System.out.println("------------------------------");
        System.out.println("Test : "+getName());
        try{
            
            DocLitWrapService service = new DocLitWrapService();
            DocLitWrap proxy = service.getDocLitWrapPort();
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,badEndpoint);
            proxy.oneWayVoid();
            
        }catch(WebServiceException e) {
            exception = e;
        }catch(Exception e) {
            fail("This testcase should only produce a WebServiceException.  We got: " + e.toString());
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        assertTrue(exception.getCause() instanceof UnknownHostException);
        assertEquals(exception.getMessage(), host);
        
    }
    
}
