/*
 * Created on Mar 17, 2005
 *
 */
package org.apache.axis.saaj;

import javax.xml.soap.DetailEntry;

/**
 * Class DetailEntryImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class DetailEntryImpl extends SOAPElementImpl implements DetailEntry {
	
	/**
	 * Field detailEntry
	 */
	private org.apache.axis.om.OMElement detailEntry;
	
	/**
	 * Constructor DetailEntryImpl
	 *
	 */
	public DetailEntryImpl(){
		
	}
	
	/**
	 * Constructor DetailEntryImpl
	 * 
	 * @param detailEntry
	 */
	public DetailEntryImpl(org.apache.axis.om.OMElement detailEntry){
		this.detailEntry = detailEntry;
	}

}
