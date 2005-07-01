/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.saaj;

import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;

import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

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
