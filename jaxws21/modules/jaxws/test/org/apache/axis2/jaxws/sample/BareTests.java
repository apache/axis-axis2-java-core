/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.sample.doclitbare.sei.BareDocLitService;
import org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class BareTests extends TestCase {
	
	public void testTwoWaySync(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		
		try{
			
			BareDocLitService service = new BareDocLitService();
			DocLitBarePortType proxy = service.getBareDocLitPort();
			 BindingProvider p = (BindingProvider) proxy;
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_URI_PROPERTY, "twoWaySimple");
			String response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
    public void testTwoWaySyncWithBodyRouting(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        
        try{
            
            BareDocLitService service = new BareDocLitService();
            DocLitBarePortType proxy = service.getBareDocLitPort();
            String response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }

    public void testOneWayEmpty(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		
		try{
			
			BareDocLitService service = new BareDocLitService();
			DocLitBarePortType proxy = service.getBareDocLitPort();
			 BindingProvider p = (BindingProvider) proxy;
			
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_URI_PROPERTY, "oneWayEmpty");
			proxy.oneWayEmpty();

            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
}
