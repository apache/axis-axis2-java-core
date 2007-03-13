/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import java.util.concurrent.Future;

import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapPortType;
import org.apache.axis2.jaxws.sample.nonwrap.sei.DocLitNonWrapService;
import org.test.sample.nonwrap.ObjectFactory;
import org.test.sample.nonwrap.ReturnType;
import org.test.sample.nonwrap.TwoWay;
import org.test.sample.nonwrap.TwoWayHolder;

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
			fail();
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
			fail();
		}
	}
	public void testTwoWayHolder(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			TwoWayHolder twh = new TwoWayHolder();
			twh.setTwoWayHolderInt(new Integer(0));
			twh.setTwoWayHolderStr(new String("Request Holder String"));
			Holder<TwoWayHolder> holder = new Holder<TwoWayHolder>(twh);
			TwoWay twoWay = new ObjectFactory().createTwoWay();
			twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
			DocLitNonWrapService service = new DocLitNonWrapService();
			DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
			proxy.twoWayHolder(holder);
			twh = holder.value;
			System.out.println("Holder string ="+twh.getTwoWayHolderStr());
			System.out.println("Holder int ="+twh.getTwoWayHolderInt());
			
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testTwoWayHolderAsync(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			TwoWayHolder twh = new TwoWayHolder();
			twh.setTwoWayHolderInt(new Integer(0));
			twh.setTwoWayHolderStr(new String("Request Holder String"));
			Holder<TwoWayHolder> holder = new Holder<TwoWayHolder>(twh);
			TwoWay twoWay = new ObjectFactory().createTwoWay();
			twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
			DocLitNonWrapService service = new DocLitNonWrapService();
			DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
			AsyncCallback callback = new AsyncCallback();
			Future<?> monitor =proxy.twoWayHolderAsync(twh, callback);
			while(!monitor.isDone()){
				Thread.sleep(1000);
			}
			
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
}
