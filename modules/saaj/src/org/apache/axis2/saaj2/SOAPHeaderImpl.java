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

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.om.impl.dom.NamespaceImpl;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11HeaderBlockImpl;

public class SOAPHeaderImpl extends SOAPElementImpl implements
		SOAPHeader {

    private org.apache.axis2.soap.SOAPHeader omHeader;
    
	/**
	 * @param element
	 */
	public SOAPHeaderImpl(org.apache.axis2.soap.SOAPHeader header) {
		super((ElementImpl)header);
		this.omHeader = header;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeader#addHeaderElement(javax.xml.soap.Name)
	 */
	public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException {
		OMNamespace ns = new NamespaceImpl(name.getURI(), name.getPrefix());
		SOAPHeaderBlock headerBlock = new SOAP11HeaderBlockImpl(name.getLocalName(), ns, this.omHeader);
		return new SOAPHeaderElementImpl(headerBlock);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeader#examineHeaderElements(java.lang.String)
	 */
	public Iterator examineHeaderElements(String actor) {
		Iterator headerElems = this.omHeader.examineHeaderBlocks(actor);
		ArrayList aList = new ArrayList();
		while (headerElems.hasNext()) {
			Object element =  headerElems.next();
			if(element instanceof SOAPHeaderBlock) {
				aList.add(new SOAPHeaderElementImpl((SOAPHeaderBlock)element));
			}
			
		}
		return aList.iterator();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeader#extractHeaderElements(java.lang.String)
	 */
	public Iterator extractHeaderElements(String actor) {
		Iterator headerElems = this.omHeader.extractHeaderBlocks(actor);
		ArrayList aList = new ArrayList();
		while (headerElems.hasNext()) {
			Object element =  headerElems.next();
			if(element instanceof SOAPHeaderBlock) {
				aList.add(new SOAPHeaderElementImpl((SOAPHeaderBlock)element));
			}
			
		}
		return aList.iterator();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeader#examineMustUnderstandHeaderElements(java.lang.String)
	 */
	public Iterator examineMustUnderstandHeaderElements(String actor) {
		Iterator headerElems = this.omHeader.examineMustUnderstandHeaderBlocks(actor);
		ArrayList aList = new ArrayList();
		while (headerElems.hasNext()) {
			Object element =  headerElems.next();
			if(element instanceof SOAPHeaderBlock) {
				aList.add(new SOAPHeaderElementImpl((SOAPHeaderBlock)element));
			}
			
		}
		return aList.iterator();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeader#examineAllHeaderElements()
	 */
	public Iterator examineAllHeaderElements() {
		Iterator headerElems = this.omHeader.examineAllHeaderBlocks();
		ArrayList aList = new ArrayList();
		while (headerElems.hasNext()) {
			Object element =  headerElems.next();
			if(element instanceof SOAPHeaderBlock) {
				aList.add(new SOAPHeaderElementImpl((SOAPHeaderBlock)element));
			}
			
		}
		return aList.iterator();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPHeader#extractAllHeaderElements()
	 */
	public Iterator extractAllHeaderElements() {
		Iterator headerElems = this.omHeader.extractAllHeaderBlocks();
		ArrayList aList = new ArrayList();
		while (headerElems.hasNext()) {
			Object element =  headerElems.next();
			if(element instanceof SOAPHeaderBlock) {
				aList.add(new SOAPHeaderElementImpl((SOAPHeaderBlock)element));
			}
			
		}
		return aList.iterator();
	}

	
}