/*
 * Created on Mar 16, 2005
 *
 */
package org.apache.axis.saaj;

import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.apache.axis.om.OMNode;

/**
 * Class SOAPFaultImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 * 
 * SOAPFault specific classes not implemented in OM, so
 * throwing unsupported operation for the time being
 */
public class SOAPFaultImpl extends SOAPBodyElementImpl implements SOAPFault {
	
	/**
	 * Field fault   The omSOAPFault field
	 */
	org.apache.axis.soap.SOAPFault fault;
	
	/**
	 * Constructor SOAPFaultImpl
	 * @param fault
	 */
	public SOAPFaultImpl(org.apache.axis.soap.SOAPFault fault){
		this.fault = fault;
	}

	/**
	 * Method setFaultCode
	 * 
	 * @param faultCode
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPFault#setFaultCode(java.lang.String)
	 */
	public void setFaultCode(String faultCode) throws SOAPException {
		//QName qName = new QName(faultCode);
		//fault.setFaultCode(qName);
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * Method getFaultCode
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPFault#getFaultCode()
	 */
	public String getFaultCode() {
	
		//QName qName = fault.getFaultCode();
		//return qName.getLocalPart();
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method setFaultActor
	 * 
	 * @param faultActor
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPFault#setFaultActor(java.lang.String)
	 */
	public void setFaultActor(String faultActor) throws SOAPException {
		
		//fault.setFaultActor(faultActor);
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method getFaultActor
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPFault#getFaultActor()
	 */
	public String getFaultActor() {
	
		//return fault.getFaultActor();
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method setFaultString
	 * 
	 * @param faultString
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPFault#setFaultString(java.lang.String)
	 */
	public void setFaultString(String faultString) throws SOAPException {
	
		//fault.setFaultString(faultString);
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method getFaultString
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPFault#getFaultString()
	 */
	public String getFaultString() {
	
		//return fault.getFaultString();
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method getDetail
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPFault#getDetail()
	 */
	public Detail getDetail() {
		// May need to change after w have detail and detail entry implementation
		//in SoapEnvelope of OM
		/*QName detailName = new QName("detail");
		DetailImpl detail = new DetailImpl(detailName, fault);
		OMNode entry = fault.getDetailInformation();
		detail.addDetailEntry(entry);
		return detail;*/
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method addDetail
	 * 
	 * @return
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPFault#addDetail()
	 */
	public Detail addDetail() throws SOAPException {
		// Not sure what detail information to add
		//May need to change later
		/*QName detailName = new QName("detail");
		DetailImpl detail = new DetailImpl(detailName, fault);
		return detail;*/
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method setFaultCode
	 * 
	 * @param name
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPFault#setFaultCode(javax.xml.soap.Name)
	 */
	public void setFaultCode(Name name) throws SOAPException {
		
		/*QName qName = new QName(name.getURI(), name.getLocalName(), name.getPrefix());
		fault.setFaultCode(qName);*/
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method getFaultCodeAsName
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPFault#getFaultCodeAsName()
	 */
	public Name getFaultCodeAsName() {
	
		/*QName qName = fault.getFaultCode();
		Name name = new PrefixedQName(qName);
		return name;*/
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method seFaultString
	 * 
	 * @param faultString
	 * @param locale
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPFault#setFaultString(java.lang.String, java.util.Locale)
	 */
	public void setFaultString(String faultString, Locale locale)
			throws SOAPException {
	
		//fault.setFaultString(faultString);
		throw new UnsupportedOperationException("No supoprted for M2 release");
	}

	/**
	 * method getFaultStringLocale
	 * 
	 * @return
	 * @see javax.xml.soap.SOAPFault#getFaultStringLocale()
	 */
	public Locale getFaultStringLocale() {
		//No implementation in Axis 1.2 also, not sure what to do here
		return null;  //TODO
	}

}
