/*
 * Created on Mar 17, 2005
 *
 */
package org.apache.axis.saaj;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMNamespace;

/**
 * Class DetailImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class DetailImpl extends SOAPFaultElementImpl implements Detail {
	
	/**
	 * Field detail
	 */
	OMElement detail;
	
	/**
	 * Constructor DetailImpl
	 * 
	 * @param detailName
	 * @param parent
	 */
	public DetailImpl(javax.xml.namespace.QName detailName, OMElement parent){
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		detail = omFactory.createOMElement(detailName, parent);
	}
	
	/*public DetailImpl(OMElement detail){
		this.detail = detail;
	}*/

	/**
	 * Method addDetailEntry
	 * 
	 * @param name
	 * @return
	 * @throws SOAPException
	 * @see javax.xml.soap.Detail#addDetailEntry(javax.xml.soap.Name)
	 */
	public DetailEntry addDetailEntry(Name name) throws SOAPException {
		
		// Create a OMElement and add it as a child of Detail
		// May need change after OM allows adding multiple detailEntries
		// as then we can delegate the task there rather than dealing with OMElement here 
		String localName = name.getLocalName();
		OMFactory omFactory = OMAbstractFactory.getOMFactory(); 
		OMNamespace ns = omFactory.createOMNamespace(name.getURI(), name.getPrefix());
		OMElement detailEntry = omFactory.createOMElement(localName, ns);
		detail.addChild(detailEntry);
		return (new DetailEntryImpl(detailEntry));
	}
	
	/**
	 * Method addDetailEntry
	 * 
	 * @param detailEntry
	 * @return
	 */
	protected DetailEntry addDetailEntry(org.apache.axis.om.OMNode detailEntry){
		detail.addChild(detailEntry);
		return (new DetailEntryImpl((OMElement)detailEntry));
	}

	/**
	 * Method getDetailEntries
	 * 
	 * @return
	 * @see javax.xml.soap.Detail#getDetailEntries()
	 */
	public Iterator getDetailEntries() {
		// Get the detailEntried which will be omElements
		// convert them to soap DetailEntry and return the iterator
		Iterator detailEntryIter = detail.getChildren();
		ArrayList aList = new ArrayList();
		while(detailEntryIter.hasNext()){
			Object o = detailEntryIter.next();
			if(o instanceof org.apache.axis.om.OMElement){
				OMElement omDetailEntry = (OMElement)o;
				DetailEntry detailEntry = new DetailEntryImpl(omDetailEntry);
				aList.add(detailEntry);
			}
		}
		return aList.iterator();
	}

}
