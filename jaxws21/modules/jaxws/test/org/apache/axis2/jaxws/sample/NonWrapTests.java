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
import org.apache.axis2.jaxws.TestLogger;
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
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			TwoWay twoWay = new ObjectFactory().createTwoWay();
			twoWay.setTwowayStr("testing sync call for java bean non wrap endpoint");
			DocLitNonWrapService service = new DocLitNonWrapService();
			DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
			ReturnType returnValue = proxy.twoWay(twoWay);
            TestLogger.logger.debug(returnValue.getReturnStr());
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
    
    public void testTwoWaySyncNull() throws Exception{
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            TwoWay twoWay = null;  // This should cause an WebServiceException
            DocLitNonWrapService service = new DocLitNonWrapService();
            DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
            ReturnType returnValue = proxy.twoWay(twoWay);
            
            // TODO Revisit JAXB validation
            // JAXWS does not make the decision of whether a
            // null parameter can be marshalled.  This decision is
            // delegated to JAXB.  In this case, the schema indicates
            // that this is not a nillable element.  The assumption is
            // that JAXB will fail.  However the current version of 
            // JAXB considers this as 'validation checking' and is not
            // supported.  Thus JAXB will marshal the element without
            // an exception (and unmarshal without exception) even though
            // this is a violation of the schema.
            
            
            
            //fail("Expected WebServiceException");
            
            
        } catch(WebServiceException e){
            TestLogger.logger.debug(e.toString());
        }
    }

	public void testTwoWayASyncCallback(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			TwoWay twoWay = new ObjectFactory().createTwoWay();
			twoWay.setTwowayStr("testing Async call for java bean non wrap endpoint");
			DocLitNonWrapService service = new DocLitNonWrapService();
			DocLitNonWrapPortType proxy = service.getDocLitNonWrapPort();
			AsyncCallback callback = new AsyncCallback();
			Future<?> monitor = proxy.twoWayAsync(twoWay, callback);
			assertNotNull(monitor);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	public void testTwoWayHolder(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
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
            TestLogger.logger.debug("Holder string =" + twh.getTwoWayHolderStr());
            TestLogger.logger.debug("Holder int =" + twh.getTwoWayHolderInt());

            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testTwoWayHolderAsync(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
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

            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
}
