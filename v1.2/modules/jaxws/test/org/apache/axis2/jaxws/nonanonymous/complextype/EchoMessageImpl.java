/**
 * 
 */
package org.apache.axis2.jaxws.nonanonymous.complextype;

import javax.jws.WebService;

import org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType;
import org.apache.axis2.jaxws.TestLogger;

@WebService(targetNamespace="http://testApp.jaxws",
            endpointInterface="org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType")

public class EchoMessageImpl implements EchoMessagePortType {

	/**
	 * 
	 */
	public EchoMessageImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.nonanonymous.complextype.sei.EchoMessagePortType#echoMessage(java.lang.String)
	 */
	public String echoMessage(String request) {
		String response = null;
        TestLogger.logger.debug("echoMessage received: " + request);
        response = request.replaceAll("Server", "Client");
        return response;

	}

}
