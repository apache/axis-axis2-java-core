package org.apache.axis2.saaj;

import org.apache.axis2.om.DOOMAbstractFactory;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.om.impl.dom.NodeImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultDetailImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultReasonImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultRoleImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultTextImpl;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPFaultCode;
import org.apache.ws.commons.soap.SOAPFaultReason;
import org.apache.ws.commons.soap.SOAPFaultRole;
import org.apache.ws.commons.soap.SOAPFaultText;
import org.apache.ws.commons.soap.SOAPFaultValue;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFaultElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

public class SOAPFaultImpl extends SOAPBodyElementImpl implements SOAPFault {

    protected org.apache.ws.commons.soap.SOAPFault fault;
    private boolean isDetailAdded;
    private Name faultCodeName;
    private Locale faultReasonLocale;

    /**
     * @param fault
     */
    public SOAPFaultImpl(org.apache.ws.commons.soap.SOAPFault fault) {
        super((ElementImpl) fault);
        this.fault = fault;
    }

    /**
     * Sets this <CODE>SOAPFault</CODE> object with the given
     * fault code.
     * <p/>
     * <P>Fault codes, which given information about the fault,
     * are defined in the SOAP 1.1 specification.</P>
     *
     * @param faultCode a <CODE>String</CODE> giving
     *                  the fault code to be set; must be one of the fault codes
     *                  defined in the SOAP 1.1 specification
     * @throws SOAPException if there was an error in
     *                       adding the <CODE>faultCode</CODE> to the underlying XML
     *                       tree.
     * @see #getFaultCode() getFaultCode()
     */
    public void setFaultCode(String faultCode) throws SOAPException {
        org.apache.ws.commons.soap.SOAPFactory soapFactory = DOOMAbstractFactory.getSOAP11Factory();
        SOAPFaultCode fCode = soapFactory.createSOAPFaultCode(fault);
        SOAPFaultValue fValue = soapFactory.createSOAPFaultValue(fCode);
        fCode.setValue(fValue);
        fValue.setText(faultCode);

        this.fault.setCode(fCode);
    }

    /**
     * Gets the fault code for this <CODE>SOAPFault</CODE>
     * object.
     *
     * @return a <CODE>String</CODE> with the fault code
     * @see #setFaultCode(java.lang.String) setFaultCode(java.lang.String)
     */
    public String getFaultCode() {
        if (fault != null && fault.getCode() != null && fault.getCode().getValue() != null) {
            return fault.getCode().getValue().getText();
        }
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#setFaultActor(java.lang.String)
      */
    public void setFaultActor(String faultActor) throws SOAPException {
        if (this.fault.getRole() == null) {
            SOAP11FaultRoleImpl faultRoleImpl = new SOAP11FaultRoleImpl(
                    this.fault, (SOAPFactory) this.element.getOMFactory());
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
        if (this.fault.getRole() != null) {
            return this.fault.getRole().getRoleValue();
        }
        return null;
    }

    /**
     * Sets the fault string for this <CODE>SOAPFault</CODE>
     * object to the given string.
     *
     * @param faultString a <CODE>String</CODE>
     *                    giving an explanation of the fault
     * @throws SOAPException if there was an error in
     *                       adding the <CODE>faultString</CODE> to the underlying XML
     *                       tree.
     * @see #getFaultString() getFaultString()
     */
    public void setFaultString(String faultString) throws SOAPException {
        if (this.fault.getReason() != null) {
            SOAPFaultReason reason = this.fault.getReason();
            if (reason.getSOAPText() != null) {
                reason.getSOAPText().getFirstOMChild().detach();
                reason.getSOAPText().setText(faultString);
            } else {
                SOAPFaultText text = new SOAP11FaultTextImpl(reason,
                        (SOAPFactory) this.element.getOMFactory());
                text.setText(faultString);
                reason.setSOAPText(text);
            }
        } else {
            org.apache.ws.commons.soap.SOAPFactory soapFactory =
                    DOOMAbstractFactory.getSOAP11Factory();
            SOAPFaultReason fReason = soapFactory.createSOAPFaultReason(fault);
            SOAPFaultText fText = soapFactory.createSOAPFaultText(fReason);
            fText.setText(faultString);
            fReason.setSOAPText(fText);
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultString()
      */
    public String getFaultString() {
        if (this.fault.getReason() != null && this.fault.getReason().getSOAPText() != null) {
            return this.fault.getReason().getSOAPText().getText();
        }
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getDetail()
      */
    public Detail getDetail() {
        return (Detail) toSAAJNode((org.w3c.dom.Node) fault.getDetail());
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#setFaultCode(javax.xml.soap.Name)
      */
    public void setFaultCode(Name name) throws SOAPException {
        this.faultCodeName = name;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#addDetail()
      */
    public Detail addDetail() throws SOAPException {
        if (isDetailAdded) {
            throw new SOAPException("This SOAPFault already contains a Detail element. " +
                                    "Please remove the existing Detail element before " +
                                    "calling addDetail()");
        }
        SOAP11FaultDetailImpl omDetail = new SOAP11FaultDetailImpl(this.fault,
                (SOAPFactory) this.element.getOMFactory());
        Detail saajDetail = new DetailImpl(omDetail);
        ((NodeImpl) fault.getDetail()).setUserData(SAAJ_NODE, saajDetail, null);
        isDetailAdded = true;
        return saajDetail;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultCodeAsName()
      */
    public Name getFaultCodeAsName() {
        return this.faultCodeName;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#setFaultString(java.lang.String, java.util.Locale)
      */
    public void setFaultString(String faultString, Locale locale) throws SOAPException {
        if (this.fault.getReason() != null) {
            SOAPFaultReason reason = this.fault.getReason();
            if (reason.getSOAPText() != null) {
                reason.getSOAPText().setText(faultString);
                reason.getSOAPText().setLang(locale.getLanguage());
            } else {
                SOAPFaultText text = new SOAP11FaultTextImpl(reason,
                        (SOAPFactory) this.element.getOMFactory());
                text.setText(faultString);
                text.setLang(locale.getLanguage());
                reason.setSOAPText(text);
            }
        } else {
            SOAPFaultReason reason = new SOAP11FaultReasonImpl(this.fault,
                    (SOAPFactory) this.element.getOMFactory());
            SOAPFaultText text = new SOAP11FaultTextImpl(reason,
                    (SOAPFactory) this.element.getOMFactory());
            text.setText(faultString);
            text.setLang(locale.getLanguage());
            reason.setSOAPText(text);
            this.fault.setReason(reason);
        }
        this.faultReasonLocale = locale;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultStringLocale()
      */
    public Locale getFaultStringLocale() {
        return this.faultReasonLocale;
    }

    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        return getChildren(element.getChildrenWithName(qName));
    }

    public Iterator getChildElements() {
        return getChildren(element.getChildren());
    }

    private Iterator getChildren(Iterator childIter) {
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            org.w3c.dom.Node domNode = (org.w3c.dom.Node) childIter.next();
            Node saajNode = toSAAJNode(domNode);
            if (!(saajNode instanceof SOAPFaultElement)) {
                // silently replace node, as per saaj 1.2 spec
                SOAPFaultElement bodyEle = new SOAPFaultElementImpl((ElementImpl) domNode);
                ((NodeImpl) domNode).setUserData(SAAJ_NODE, bodyEle, null);
                childElements.add(bodyEle);
            } else {
                childElements.add(saajNode);
            }
        }
        return childElements.iterator();
    }
}
