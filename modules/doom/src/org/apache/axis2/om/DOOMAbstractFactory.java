/**
 * 
 */
package org.apache.axis2.om;

import org.apache.axis2.om.impl.dom.factory.OMDOMFactory;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axis2.soap.impl.dom.soap12.SOAP12Factory;

public class DOOMAbstractFactory {

	public static OMFactory getOMFactory() {
		return new OMDOMFactory();
	}
	
	public static SOAPFactory getSOAP11Factory() {
		return new SOAP11Factory();
	}

	public static SOAPFactory getSOAP12Factory() {
		return new SOAP12Factory();
	}
}
