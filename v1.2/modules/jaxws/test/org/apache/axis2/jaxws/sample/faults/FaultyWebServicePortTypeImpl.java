/**
 * 
 */
package org.apache.axis2.jaxws.sample.faults;

import javax.jws.WebService;

import org.test.faults.FaultyWebServiceFault;

@WebService(endpointInterface="org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType")
public class FaultyWebServicePortTypeImpl {

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType#faultyWebService(int)
	 */
	public int faultyWebService(int arg0) throws FaultyWebServiceFault_Exception {
		
		FaultyWebServiceFault bean = new FaultyWebServiceFault();
		bean.setFaultInfo("bean custom fault info");
		bean.setMessage("bean custom message");
		
		throw new FaultyWebServiceFault_Exception("custom exception", bean);
	}


}
