/**
 * 
 */
package org.apache.axis2.jaxws.sample.wrap;

import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.test.sample.nonwrap.ObjectFactory;
import org.test.sample.nonwrap.ReturnType;
import org.test.sample.wrap.FinancialOperation;

@WebService(endpointInterface="org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap")
public class DocLitWrapImpl implements DocLitWrap {

	public FinancialOperation finOp(FinancialOperation op) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public DocLitWrapImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#oneWayVoid()
	 */
	public void oneWayVoid() {
		System.out.println("OneWayVoid with no parameters called");

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#oneWay(java.lang.String)
	 */
	public void oneWay(String onewayStr) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#twoWayHolder(javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	public void twoWayHolder(Holder<String> twoWayHolderStr,
			Holder<Integer> twoWayHolderInt) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#twoWay(java.lang.String)
	 */
	public String twoWay(String twowayStr) {
		String retStr = new String("Acknowledgement : Request String received = "+ twowayStr);
		return retStr;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap#invoke(java.lang.String)
	 */
	public String invoke(String invokeStr) {
		// TODO Auto-generated method stub
		return null;
	}

}
