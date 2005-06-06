/*
 * Created on Mar 16, 2005
 *
 */
package org.apache.axis.saaj;

import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.soap.SOAPHeaderBlock;

/**
 * Class SOAPHeaderImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class SOAPHeaderElementImpl extends SOAPElementImpl implements
		SOAPHeaderElement {
	
	/**
	 * Field omHeaderElement
	 */
	SOAPHeaderBlock omHeaderElement;
	
	/**
	 * Constructor SOAPHeaderElementImpl
	 * @param headerElement
	 */
	public SOAPHeaderElementImpl(org.apache.axis.soap.SOAPHeaderBlock headerElement){
		super(headerElement);
		this.omHeaderElement = headerElement;
	}

	/**
	 * method setActor
	 * 
	 * @param actorURI
	 * @see javax.xml.soap.SOAPHeaderElement#setActor(java.lang.String)
	 */
	public void setActor(String actorURI) {
	
		omHeaderElement.setRole(actorURI);
	}

	/**
	 * method getActor
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPHeaderElement#getActor()
	 */
	public String getActor() {

		return omHeaderElement.getRole();
	}

	/**
	 * method setMustUnderstand
	 * 
	 * @param mustUnderstand
	 * @see javax.xml.soap.SOAPHeaderElement#setMustUnderstand(boolean)
	 */
	public void setMustUnderstand(boolean mustUnderstand) {
		
		omHeaderElement.setMustUnderstand(mustUnderstand);
	}

	/**
	 * method getMustUnderstand
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPHeaderElement#getMustUnderstand()
	 */
	public boolean getMustUnderstand() {
		
		return omHeaderElement.getMustUnderstand();
	}

}
