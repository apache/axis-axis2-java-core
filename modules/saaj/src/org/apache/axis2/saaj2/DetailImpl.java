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

import java.util.Iterator;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.soap.SOAPFaultDetail;

public class DetailImpl extends SOAPFaultElementImpl implements Detail {

	SOAPFaultDetail faultDetail;
	
	/**
	 * @param element
	 */
	public DetailImpl(SOAPFaultDetail element) {
		super((ElementImpl)element);
		this.faultDetail = element;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.Detail#addDetailEntry(javax.xml.soap.Name)
	 */
	public DetailEntry addDetailEntry(Name name) throws SOAPException {
		SOAPElementImpl childElement = (SOAPElementImpl)this.addChildElement(name);
		DetailEntryImpl detailEntryImpl = new DetailEntryImpl(childElement.element);
		this.faultDetail.addDetailEntry((OMElement)detailEntryImpl);
		return detailEntryImpl;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.Detail#getDetailEntries()
	 */
	public Iterator getDetailEntries() {
		return this.faultDetail.getAllDetailEntries();
	}

}
