/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.doclitbare.sei.BareDocLitService;
import org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType;


public class BareTests extends TestCase {
	
	public void testTwoWaySync(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			
			BareDocLitService service = new BareDocLitService();
			DocLitBarePortType proxy = service.getBareDocLitPort();
			 BindingProvider p = (BindingProvider) proxy;
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_URI_PROPERTY, new Boolean(true));
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_URI_PROPERTY, "twoWaySimple");
			String response = proxy.twoWaySimple(10);
			System.out.println("Sync Response =" + response);
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
