/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersService;


public class AddNumbersTests extends TestCase {
	String axisEndpoint = "http://localhost:8080/axis2/services/AddNumbersService";
	public void testAddNumbers(){
		try{
			System.out.println("----------------------------------");
		    System.out.println("test: " + getName());
			AddNumbersService service = new AddNumbersService();
			AddNumbersPortType proxy = service.getAddNumbersPort();
			BindingProvider p =	(BindingProvider)proxy;
			p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);	
			int total = proxy.addNumbers(10,10);
			
			System.out.println("Total =" +total);
			System.out.println("----------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
