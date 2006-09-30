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

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultReasonImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultRoleImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultTextImpl;

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

    protected org.apache.axiom.soap.SOAPFault fault;
    private boolean isDetailAdded;
    private Name faultCodeName;
    private Locale faultReasonLocale;

    /**
     * @param fault
     */
    public SOAPFaultImpl(org.apache.axiom.soap.SOAPFault fault) {
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
        org.apache.axiom.soap.SOAPFactory soapFactory = DOOMAbstractFactory.getSOAP11Factory();
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
            if (reason.getFirstSOAPText() != null) {
                reason.getFirstSOAPText().getFirstOMChild().detach();
                reason.getFirstSOAPText().setText(faultString);
            } else {
                SOAPFaultText text = new SOAP11FaultTextImpl(reason,
                        (SOAPFactory) this.element.getOMFactory());
                text.setText(faultString);
                reason.addSOAPText(text);
            }
        } else {
            org.apache.axiom.soap.SOAPFactory soapFactory =
                    DOOMAbstractFactory.getSOAP11Factory();
            SOAPFaultReason fReason = soapFactory.createSOAPFaultReason(fault);
            SOAPFaultText fText = soapFactory.createSOAPFaultText(fReason);
            fText.setText(faultString);
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultString()
      */
    public String getFaultString() {
        if (this.fault.getReason() != null && this.fault.getReason().getFirstSOAPText() != null) {
            return this.fault.getReason().getFirstSOAPText().getText();
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
            if (reason.getFirstSOAPText() != null) {
                reason.getFirstSOAPText().setText(faultString);
                reason.getFirstSOAPText().setLang(locale.getLanguage());
            } else {
                SOAPFaultText text = new SOAP11FaultTextImpl(reason,
                        (SOAPFactory) this.element.getOMFactory());
                text.setText(faultString);
                text.setLang(locale.getLanguage());
                reason.addSOAPText(text);
            }
        } else {
            SOAPFaultReason reason = new SOAP11FaultReasonImpl(this.fault,
                    (SOAPFactory) this.element.getOMFactory());
            SOAPFaultText text = new SOAP11FaultTextImpl(reason,
                    (SOAPFactory) this.element.getOMFactory());
            text.setText(faultString);
            text.setLang(locale.getLanguage());
            reason.addSOAPText(text);
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
