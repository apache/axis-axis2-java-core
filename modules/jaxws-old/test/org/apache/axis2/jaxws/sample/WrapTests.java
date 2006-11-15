/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrapService;

import junit.framework.TestCase;

/**
 * @author nvthaker
 *
 */
public class WrapTests extends TestCase {

	/**
	 * 
	 */
	public WrapTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public WrapTests(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void testTwoWaySync(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			String reqString = "Test twoWay Sync";
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			String response = proxy.twoWay(reqString);
			System.out.println("Sync Response =" + response);
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void testOneWayVoidWithNoInputParams(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			proxy.oneWayVoid();
			
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
