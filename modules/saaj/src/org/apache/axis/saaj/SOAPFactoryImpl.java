/*
 * Created on Apr 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMAbstractFactory;

/**
 * @author shaas02
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SOAPFactoryImpl extends javax.xml.soap.SOAPFactory {

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFactory#createElement(javax.xml.soap.Name)
	 */
	public SOAPElement createElement(Name name) throws SOAPException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFactory#createElement(java.lang.String)
	 */
	public SOAPElement createElement(String localName) throws SOAPException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFactory#createElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public SOAPElement createElement(String localName, String prefix, String uri)
			throws SOAPException {
		OMElement newOMElement = OMAbstractFactory.getOMFactory().createOMElement(localName, uri, prefix);
		return new SOAPElementImpl(newOMElement);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFactory#createDetail()
	 */
	public Detail createDetail() throws SOAPException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFactory#createName(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Name createName(String localName, String prefix, String uri)
			throws SOAPException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFactory#createName(java.lang.String)
	 */
	public Name createName(String localName) throws SOAPException {
		// TODO Auto-generated method stub
		return null;
	}

}
