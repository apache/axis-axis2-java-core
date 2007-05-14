/**
 * 
 */
package org.apache.axis2.jaxws.nonanonymous.complextype;

import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType;
import org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessageService;
import org.apache.axis2.jaxws.TestLogger;

public class NonAnonymousComplexTypeTests extends TestCase {

	/**
	 * 
	 */
	public NonAnonymousComplexTypeTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public NonAnonymousComplexTypeTests(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	public void testSimpleProxy() {
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try {
			String msg = "Hello Server";
		    EchoMessagePortType myPort = (new EchoMessageService()).getEchoMessagePort();
		    String response = myPort.echoMessage(msg);
            TestLogger.logger.debug(response);
            TestLogger.logger.debug("------------------------------");
		} catch (WebServiceException webEx) {
			webEx.printStackTrace();
			fail();
		}
	}

		    


}
