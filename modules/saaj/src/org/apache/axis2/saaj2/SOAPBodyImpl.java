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
package org.apache.axis2.saaj2;

import java.util.Locale;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultImpl;
import org.w3c.dom.Document;

public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody {

    private org.apache.axis2.soap.SOAPBody omSOAPBody;
    
	/**
	 * @param element
	 */
	public SOAPBodyImpl(org.apache.axis2.soap.SOAPBody omSOAPBody) {
		super((ElementImpl)omSOAPBody);
		this.omSOAPBody = omSOAPBody;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#addFault()
	 */
	public SOAPFault addFault() throws SOAPException {
		SOAP11FaultImpl fault = new SOAP11FaultImpl(omSOAPBody);
		this.omSOAPBody.addFault(fault);
		return new SOAPFaultImpl(fault);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#hasFault()
	 */
	public boolean hasFault() {
		return this.omSOAPBody.hasFault();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#getFault()
	 */
	public SOAPFault getFault() {
		if(this.omSOAPBody.hasFault()) {
			return new SOAPFaultImpl(this.omSOAPBody.getFault());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#addBodyElement(javax.xml.soap.Name)
	 */
	public SOAPBodyElement addBodyElement(Name name) throws SOAPException {
		SOAPElementImpl elem = (SOAPElementImpl)this.addChildElement(name);
		return new SOAPBodyElementImpl(elem.element);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#addFault(javax.xml.soap.Name, java.lang.String, java.util.Locale)
	 */
	public SOAPFault addFault(Name faultCode, String faultString, Locale locale) throws SOAPException {
		SOAP11FaultImpl fault = new SOAP11FaultImpl(this.omSOAPBody, new Exception(faultString));
		SOAPFaultImpl faultImpl = new SOAPFaultImpl(fault);
		faultImpl.setFaultCode(faultCode);
		if(locale != null) {
			faultImpl.setFaultString(faultString, locale);
		} else {
			faultImpl.setFaultString(faultString);
		}
		
		return faultImpl;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#addFault(javax.xml.soap.Name, java.lang.String)
	 */
	public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException {
		return this.addFault(faultCode, faultString, null);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPBody#addDocument(org.w3c.dom.Document)
	 */
	public SOAPBodyElement addDocument(Document document) throws SOAPException {
		
		SOAPElementImpl elem = new SOAPElementImpl((ElementImpl)document.getDocumentElement());
		return new SOAPBodyElementImpl(elem.element);
		
	}


}
