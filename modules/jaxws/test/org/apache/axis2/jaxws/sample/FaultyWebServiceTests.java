/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.faults.FaultyWebServiceFault_Exception;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServiceService;


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
}
