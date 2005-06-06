/*
 * Created on Mar 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import java.util.Locale;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.w3c.dom.Document;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.om.OMAbstractFactory;

import javax.xml.namespace.QName;

/**
 * Class SOAPBodeImpl
 * 
 * @author Jayachandra
 * jayachandra@gmail.com
 */
public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody {
	/**
	 * Field omSOAPBody
	 * omSOAPBody is the OM's SOAPBody object that is used for delegation purpose
	 */
	private org.apache.axis.soap.SOAPBody omSOAPBody;

	/**
	 * Constructor SOAPBodeImpl
	 * The constructor to facilitate conversion of SAAJ SOAPBody out of OM SOAPBody
	 * @param omSoapBody
	 */
	public SOAPBodyImpl(org.apache.axis.soap.SOAPBody omSoapBody)
	{
		super(omSoapBody);
		this.omSOAPBody = omSoapBody;
	}

	/**
	 * Method addFault
	 * @see javax.xml.soap.SOAPBody#addFault()
	 * @return
	 * @throws SOAPException
	 */
	public SOAPFault addFault() throws SOAPException {
		try{
			//OM SOAPFaultImpl has SOAPFaultImpl(OMElement parent, Exception e) constructor, will use that
			SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
			org.apache.axis.soap.SOAPFault omSoapFault = soapFactory.createSOAPFault(omSOAPBody, new Exception("No explicit faultstring available"));
			omSOAPBody.addFault(omSoapFault);
			return (new SOAPFaultImpl(omSoapFault));
		}catch(Exception e)
		{
			throw new SOAPException(e);
		}
	}

	/**
	 * Method hasFault
	 * @see javax.xml.soap.SOAPBody#hasFault()
	 * @return
	 */
	public boolean hasFault() {
		return omSOAPBody.hasFault();
	}

	/**
	 * Method getFault
	 * @see javax.xml.soap.SOAPBody#getFault()
	 * @return
	 */
	public SOAPFault getFault() {
		return (new SOAPFaultImpl(omSOAPBody.getFault()));
	}

	/**
	 * Method addBodyElement
	 * @param name
	 * @return
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPBody#addBodyElement(javax.xml.soap.Name)
	 */
	public SOAPBodyElement addBodyElement(Name name) throws SOAPException {

		try {
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		QName qname = new QName(name.getURI(), name.getLocalName(), name.getPrefix());
		OMElement bodyElement = omFactory.createOMElement(qname, omSOAPBody);
		omSOAPBody.addChild(bodyElement);
		return (new SOAPBodyElementImpl(bodyElement));
		} catch (Exception e)
		{
			throw new SOAPException(e);
		}
	}

	/**
	 * Method addFault
	 * @param faultCode
	 * @param faultString
	 * @param 
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPBody#addFault(javax.xml.soap.Name, java.lang.String, java.util.Locale)
	 */
	public SOAPFault addFault(Name faultCode, String faultString, Locale locale)
			throws SOAPException {
		try{
			//OM SOAPFaultImpl has SOAPFaultImpl(OMElement parent, Exception e) constructor, will use that
			//actually soap fault is created with the OM's default SOAPFAULT_LOCALNAME and PREFIX, b'coz I've droppe the name param
			//a work around can be possible but would be confusing as there is no straight forward soapfault constructor in om.
			//So am deferring it.
			//even locale param is dropped, don't know how to handle it at the moment. so dropped it.
			SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
			org.apache.axis.soap.SOAPFault omSoapFault = soapFactory.createSOAPFault(omSOAPBody, new Exception(faultString));
			omSOAPBody.addFault(omSoapFault);
			return (new SOAPFaultImpl(omSoapFault));
		}catch(Exception e)
		{
			throw new SOAPException(e);
		}
	}

	/**
	 * Method addFault
	 * @param faultCode
	 * @param faultString
	 * @return
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPBody#addFault(javax.xml.soap.Name, java.lang.String)
	 */
	public SOAPFault addFault(Name faultCode, String faultString)
			throws SOAPException {
		try{
			//OM SOAPFaultImpl has SOAPFaultImpl(OMElement parent, Exception e) constructor, will use that
			//actually soap fault is created with the OM's default SOAPFAULT_LOCALNAME and PREFIX, b'coz I've droppe the name param
			//a work around can be possible but would be confusing as there is no straight forward soapfault constructor in om.
			//So am deferring it.
			SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
			org.apache.axis.soap.SOAPFault omSoapFault = soapFactory.createSOAPFault(omSOAPBody, new Exception(faultString));
			omSOAPBody.addFault(omSoapFault);
			return (new SOAPFaultImpl(omSoapFault));
		}catch(Exception e)
		{
			throw new SOAPException(e);
		}
	}

	/**
	 * Method addDocument
	 * @param document
	 * @return
	 * @throws SOAPException
	 * @see javax.xml.soap.SOAPBody#addDocument(org.w3c.dom.Document)
	 */
	public SOAPBodyElement addDocument(Document document) throws SOAPException {
		/*
		 * Don't know how to resolve this as yet. So deferring it.
		 */
		return null;
	}

}
