/**
 * 
 */
package org.apache.axis2.jaxws.sample.addnumbers;

import javax.jws.WebService;


@WebService(endpointInterface="org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType")
public class AddNumbersPortTypeImpl implements AddNumbersPortType {

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType#addNumbers(int, int)
	 */
	public int addNumbers(int arg0, int arg1) throws AddNumbersFault_Exception {
		// TODO Auto-generated method stub
		return arg0+arg1;
		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType#oneWayInt(int)
	 */
	public void oneWayInt(int arg0) {
		// TODO Auto-generated method stub

	}

}
