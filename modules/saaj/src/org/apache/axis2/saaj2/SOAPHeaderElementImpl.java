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

import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.soap.SOAPHeaderBlock;

public class SOAPHeaderElementImpl extends SOAPElementImpl implements
		SOAPHeaderElement {

	SOAPHeaderBlock headerElem;
	/**
	 * @param element
	 */
	public SOAPHeaderElementImpl(SOAPHeaderBlock element) {
		super((ElementImpl)element);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeaderElement#setActor(java.lang.String)
	 */
	public void setActor(String actorURI) {
		this.headerElem.setRole(actorURI);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeaderElement#getActor()
	 */
	public String getActor() {
		return this.headerElem.getRole();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeaderElement#setMustUnderstand(boolean)
	 */
	public void setMustUnderstand(boolean mustUnderstand) {
		this.headerElem.setMustUnderstand(mustUnderstand);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeaderElement#getMustUnderstand()
	 */
	public boolean getMustUnderstand() {
		return this.headerElem.getMustUnderstand();
	}

}
