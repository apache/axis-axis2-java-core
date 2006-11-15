/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import java.util.concurrent.Future;

import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType;
import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapService;
import org.test.sample.nonwrap.ObjectFactory;
import org.test.sample.nonwrap.ReturnType;
import org.test.sample.nonwrap.TwoWay;

import junit.framework.TestCase;

public class NonWrapTests extends TestCase {

	
	public NonWrapTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public NonWrapTests(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void testTwoWaySync(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			TwoWay twoWay = new ObjectFactory().createTwoWay();
			twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
			DocLitNonWrapService service = new DocLitNonWrapService();
			DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
			ReturnType returnValue = proxy.twoWay(twoWay);
			System.out.println(returnValue.getReturnStr());
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void testTwoWayASyncCallback(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			TwoWay twoWay = new ObjectFactory().createTwoWay();
			twoWay.setTwowayStr("testing Async call for java bean non wrap endpoint");
			DocLitNonWrapService service = new DocLitNonWrapService();
			DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
			AsyncCallback callback = new AsyncCallback();
			Future<?> monitor = proxy.twoWayAsync(twoWay, callback);
			assertNotNull(monitor);
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
