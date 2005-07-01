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
package org.apache.axis2.saaj;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class SOAPHeaderImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class SOAPHeaderImpl extends SOAPElementImpl implements SOAPHeader {

	/**
	 * Field omHeader	OM's SOAPHeader field
	 */
	private org.apache.axis2.soap.SOAPHeader omHeader;
	
	/**
	 * Constructor SOAPHeaderImpl
	 * @param header
	 */
	public SOAPHeaderImpl(org.apache.axis2.soap.SOAPHeader header){
		super(header);
		this.omHeader = header; 
	}
	
	/**
	 * Method addHeaderElement
	 * 
	 * @param name
	 * @return
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPHeader#addHeaderElement(javax.xml.soap.Name)
	 */
	public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException {
		// Create an OMHeaderBlock out of name and add it a SOAPHeaderElement
		//to SOAPHeader
		String localName = name.getLocalName();
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = omFactory.createOMNamespace(name.getURI(), name.getPrefix());
		org.apache.axis2.soap.SOAPHeaderBlock headerBlock = omHeader.addHeaderBlock(localName, ns);
		return (new SOAPHeaderElementImpl(headerBlock));
	}

	/**
	 * method examineHeaderElements
	 * 
	 * @param actor
	 * @return
	 * @see javax.xml.soap.SOAPHeader#examineHeaderElements(java.lang.String)
	 */
	public Iterator examineHeaderElements(String actor) {
		// Get all the om specific header elements in an iterator and wrap it
		// in a soap specific iterator and return
		Iterator headerElementsIter = omHeader.examineHeaderBlocks(actor);
		ArrayList aList = new ArrayList();
		while(headerElementsIter.hasNext()){
			Object o = headerElementsIter.next();
			if(o instanceof org.apache.axis2.soap.SOAPHeaderBlock){
				org.apache.axis2.soap.SOAPHeaderBlock headerBlock = (org.apache.axis2.soap.SOAPHeaderBlock)o;
				SOAPHeaderElement element = (new SOAPHeaderElementImpl(headerBlock)); 
				aList.add(element);
			}
		}
		return aList.iterator();
	}

	/**
	 * method extractHeaderElements
	 * 
	 * @param actor
	 * @return
	 * @see javax.xml.soap.SOAPHeader#extractHeaderElements(java.lang.String)
	 */
	public Iterator extractHeaderElements(String actor) {
		// Get all the om specific header elements in an iterator and wrap it
		// in a soap specific iterator and return
		Iterator headerElementsIter = omHeader.extractHeaderBlocks(actor);
		ArrayList aList = new ArrayList();
		while(headerElementsIter.hasNext()){
			Object o = headerElementsIter.next();
			if(o instanceof org.apache.axis2.soap.SOAPHeaderBlock){
				org.apache.axis2.soap.SOAPHeaderBlock headerBlock = (org.apache.axis2.soap.SOAPHeaderBlock)o;
				SOAPHeaderElement element = (new SOAPHeaderElementImpl(headerBlock)); 
				aList.add(element);
			}
		}
		return aList.iterator();
	}

	/**
	 * method examineMustUnderstandHeaderElements
	 * 
	 * @param actor
	 * @return
	 * @see javax.xml.soap.SOAPHeader#examineMustUnderstandHeaderElements(java.lang.String)
	 */
	public Iterator examineMustUnderstandHeaderElements(String actor) {
		// Get all the om specific header elements in an iterator and wrap it
		// in a soap specific iterator and return		
		Iterator headerElementsIter = omHeader.examineMustUnderstandHeaderBlocks(actor);
		ArrayList aList = new ArrayList();
		while(headerElementsIter.hasNext()){
			Object o = headerElementsIter.next();
			if(o instanceof org.apache.axis2.soap.SOAPHeaderBlock){
				org.apache.axis2.soap.SOAPHeaderBlock headerBlock = (org.apache.axis2.soap.SOAPHeaderBlock)o;
				SOAPHeaderElement element = (new SOAPHeaderElementImpl(headerBlock)); 
				aList.add(element);
			}
		}
		return aList.iterator();
	}

	/**
	 * method examineAllHeaderElements
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPHeader#examineAllHeaderElements()
	 */
	public Iterator examineAllHeaderElements() {
		// Get all the om specific header elements in an iterator and wrap it
		// in a soap specific iterator and return	
		Iterator headerElementsIter = omHeader.examineAllHeaderBlocks();
		ArrayList aList = new ArrayList();
		while(headerElementsIter.hasNext()){
			Object o = headerElementsIter.next();
			if(o instanceof org.apache.axis2.soap.SOAPHeaderBlock){
				org.apache.axis2.soap.SOAPHeaderBlock headerBlock = (org.apache.axis2.soap.SOAPHeaderBlock)o;
				SOAPHeaderElement element = (new SOAPHeaderElementImpl(headerBlock)); 
				aList.add(element);
			}
		}
		return aList.iterator();
	}

	/**
	 * method extractAllHeaderElements
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPHeader#extractAllHeaderElements()
	 */
	public Iterator extractAllHeaderElements() {
		// Get all the om specific header elements in an iterator and wrap it
		// in a soap specific iterator and return	
		Iterator headerElementsIter = omHeader.extractAllHeaderBlocks();
		ArrayList aList = new ArrayList();
		while(headerElementsIter.hasNext()){
			Object o = headerElementsIter.next();
			if(o instanceof org.apache.axis2.soap.SOAPHeaderBlock){
				org.apache.axis2.soap.SOAPHeaderBlock headerBlock = (org.apache.axis2.soap.SOAPHeaderBlock)o;
				SOAPHeaderElement element = (new SOAPHeaderElementImpl(headerBlock)); 
				aList.add(element);
			}
		}
		return aList.iterator();
	}
	
	/*public boolean equals(Object o){
		if(o instanceof SOAPHeaderImpl){
			if(this.omHeader.equals(((SOAPHeaderImpl)o).omHeader))
					return true;
		}
		return false;
	}*/

}
