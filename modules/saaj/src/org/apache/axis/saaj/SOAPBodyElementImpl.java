/*
 * Created on Mar 16, 2005
 *
 */
package org.apache.axis.saaj;

import javax.xml.soap.SOAPBodyElement;

import org.apache.axis.om.OMElement;

/**
 * Class SOAPBodeElementImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class SOAPBodyElementImpl extends SOAPElementImpl implements
		SOAPBodyElement {
	
	/**
	 * Constructor SOAPBodeElementImpl
	 *
	 */
	public SOAPBodyElementImpl(){
		super();
	}
	
	/**
	 * Constructor SOAPBodeElementImpl
	 * @param bodyElement
	 */
	public SOAPBodyElementImpl(OMElement bodyElement){
		super(bodyElement);
	}

}
