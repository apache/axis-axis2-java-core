/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.Holder;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrapService;
import org.apache.axis2.jaxws.TestLogger;
import org.test.sample.wrap.Header;
import org.test.sample.wrap.HeaderPart0;
import org.test.sample.wrap.HeaderPart1;
import org.test.sample.wrap.HeaderResponse;

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
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			String reqString = "Test twoWay Sync";
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			String response = proxy.twoWay(reqString);
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testOneWayVoidWithNoInputParams(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			proxy.oneWayVoid();

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
			String holderString = new String("Test twoWay Sync");
			Integer holderInteger = new Integer(0);
			Holder<String> strHolder = new Holder<String>(holderString);
			Holder<Integer> intHolder = new Holder<Integer>(holderInteger);
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			proxy.twoWayHolder(strHolder, intHolder);
            TestLogger.logger.debug("Holder Response String =" + strHolder.value);;
            TestLogger.logger.debug("Holder Response Integer =" + intHolder.value);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testTwoWayWithHeadersAndHolders(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			Header header = new Header();
			header.setOut(0);
			HeaderPart0 hp0= new HeaderPart0();
			hp0.setHeaderType("Client setup Header Type for HeaderPart0");
			HeaderPart1 hp1 = new HeaderPart1();
			hp1.setHeaderType("Client setup Header Type for HeaderPart0");
			Holder<HeaderPart0> holder = new Holder<HeaderPart0>(hp0);
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			HeaderResponse hr = proxy.header(header, holder, hp1);
			hp0=holder.value;
            TestLogger.logger.debug("Holder Response String =" + hp0.getHeaderType());
            TestLogger.logger.debug("Header Response Long =" + hr.getOut());
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
			String holderString = new String("Test twoWay Sync");
			Integer holderInteger = new Integer(0);
			Holder<String> strHolder = new Holder<String>(holderString);
			Holder<Integer> intHolder = new Holder<Integer>(holderInteger);
			DocLitWrapService service = new DocLitWrapService();
			DocLitWrap proxy = service.getDocLitWrapPort();
			proxy.twoWayHolder(strHolder, intHolder);
            TestLogger.logger.debug("Holder Response String =" + strHolder.value);;
            TestLogger.logger.debug("Holder Response Integer =" + intHolder.value);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
    
    /**
     * This is a test of a doc/lit method that passes the 
     * request in a header.  This can only be reproduced via
     * annotations and WSGEN.  WSImport will not allow this.
     */
    public void testEchoStringWSGEN1() {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            String request = "hello world";
            
            DocLitWrapService service = new DocLitWrapService();
            DocLitWrap proxy = service.getDocLitWrapPort();
            String response = proxy.echoStringWSGEN1(request);
            assertTrue(response.equals(request));
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * This is a test of a doc/lit method that passes the 
     * response in a header.  This can only be reproduced via
     * annotations and WSGEN.  WSImport will not allow this.
     */
    
    public void testEchoStringWSGEN2() {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            String request = "hello world 2";
            
            DocLitWrapService service = new DocLitWrapService();
            DocLitWrap proxy = service.getDocLitWrapPort();
            String response = proxy.echoStringWSGEN2(request);
            assertTrue(response.equals(request));
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
    
}
