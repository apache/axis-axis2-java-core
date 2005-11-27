package org.apache.axis2.saaj2;

import java.util.Locale;

import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.SOAPFaultRole;
import org.apache.axis2.soap.SOAPFaultText;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultCodeImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultDetailImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultReasonImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultRoleImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultTextImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultValueImpl;

public class SOAPFaultImpl extends SOAPBodyElementImpl implements
		SOAPFault {
	
	protected org.apache.axis2.soap.SOAPFault fault;
	
	/**
	 * @param element
	 */
	public SOAPFaultImpl(org.apache.axis2.soap.SOAPFault element) {
		super((ElementImpl)element);
		fault = element;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#setFaultCode(java.lang.String)
	 */
	public void setFaultCode(String faultCode) throws SOAPException {
		SOAPFaultCode code = new SOAP11FaultCodeImpl(fault);
		SOAP11FaultValueImpl faultValueImpl = new SOAP11FaultValueImpl(code);
		faultValueImpl.setText(faultCode);
		code.setValue(faultValueImpl);
		this.fault.setCode(code);
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#getFaultCode()
	 */
	public String getFaultCode() {
		return this.fault.getCode().getValue().getText();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#setFaultActor(java.lang.String)
	 */
	public void setFaultActor(String faultActor) throws SOAPException {
		if(this.fault.getRole() == null) {
			SOAP11FaultRoleImpl faultRoleImpl = new SOAP11FaultRoleImpl(this.fault);
			faultRoleImpl.setRoleValue(faultActor);
			this.fault.setRole(faultRoleImpl);
		} else {
			SOAPFaultRole role = this.fault.getRole();
			role.setRoleValue(faultActor);
		}
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#getFaultActor()
	 */
	public String getFaultActor() {
		if(this.fault.getRole() != null) {
			return this.fault.getRole().getRoleValue();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#setFaultString(java.lang.String)
	 */
	public void setFaultString(String faultString) throws SOAPException {
		if(this.fault.getReason() != null) {
			SOAPFaultReason reason = this.fault.getReason();
			if(reason.getSOAPText() != null) {
				reason.getSOAPText().setText(faultString);
			} else {
				SOAPFaultText text = new SOAP11FaultTextImpl(reason);
				text.setText(faultString);
				reason.setSOAPText(text);
			}
		} else {
			SOAPFaultReason reason = new SOAP11FaultReasonImpl(this.fault);
			SOAPFaultText text = new SOAP11FaultTextImpl(reason);
			text.setText(faultString);
			reason.setSOAPText(text);
			this.fault.setReason(reason);
		}
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#getFaultString()
	 */
	public String getFaultString() {
		if(this.fault.getReason() != null && this.fault.getReason().getSOAPText() != null) {
			return this.fault.getReason().getSOAPText().getText();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#getDetail()
	 */
	public Detail getDetail() {
		if(this.fault.getDetail() != null) {
			return new DetailImpl(this.fault.getDetail());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#addDetail()
	 */
	public Detail addDetail() throws SOAPException {
		SOAP11FaultDetailImpl omDetail = new SOAP11FaultDetailImpl(this.fault);
		this.fault.setDetail(omDetail);
		Detail detail = new DetailImpl(omDetail);
		return detail;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#setFaultCode(javax.xml.soap.Name)
	 */
	public void setFaultCode(Name name) throws SOAPException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#getFaultCodeAsName()
	 */
	public Name getFaultCodeAsName() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#setFaultString(java.lang.String, java.util.Locale)
	 */
	public void setFaultString(String faultString, Locale locale) throws SOAPException {
		if(this.fault.getReason() != null) {
			SOAPFaultReason reason = this.fault.getReason();
			if(reason.getSOAPText() != null) {
				reason.getSOAPText().setText(faultString);
				reason.getSOAPText().setLang(locale.getLanguage());
			} else {
				SOAPFaultText text = new SOAP11FaultTextImpl(reason);
				text.setText(faultString);
				text.setLang(locale.getLanguage());
				reason.setSOAPText(text);
			}
		} else {
			SOAPFaultReason reason = new SOAP11FaultReasonImpl(this.fault);
			SOAPFaultText text = new SOAP11FaultTextImpl(reason);
			text.setText(faultString);
			text.setLang(locale.getLanguage());
			reason.setSOAPText(text);
			this.fault.setReason(reason);
		}
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPFault#getFaultStringLocale()
	 */
	public Locale getFaultStringLocale() {
		//We only save the language in OM,
		//Can we construct a Locale with it :-?
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

}
