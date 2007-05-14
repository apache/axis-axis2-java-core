/**
 * 
 */
package org.apache.axis2.jaxws.sample.doclitbare;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType;
import org.apache.axis2.jaxws.sample.doclitbare.sei.FaultBeanWithWrapper;
import org.apache.axis2.jaxws.sample.doclitbare.sei.SimpleFault;
import org.test.sample.doclitbare.Composite;

@WebService(endpointInterface="org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType")
public class DocLitBarePortTypeImpl implements DocLitBarePortType {

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType#oneWayEmpty()
	 */
	public void oneWayEmpty() {
		String retValue = "Running One way call";

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType#oneWay(java.lang.String)
	 */
	public void oneWay(String allByMyself) {
		// TODO Auto-generated method stub
		String retValue = "Running One way call with String input" + allByMyself;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType#twoWaySimple(int)
	 */
	public String twoWaySimple(int allByMyself) {
		// TODO Auto-generated method stub
		String retValue = "Acknowledgement: received input value as integer:"+ allByMyself;
		return retValue;
	}
	
	public void twoWayHolder(
	        @WebParam(name = "Composite", targetNamespace = "http://org.test.sample.doclitbare", mode = Mode.INOUT, partName = "allByMyself")
	        Holder<Composite> allByMyself)
	        throws FaultBeanWithWrapper, SimpleFault{
		
	}
	    
}
