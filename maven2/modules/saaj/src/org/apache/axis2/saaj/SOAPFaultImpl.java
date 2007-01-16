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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFaultElement;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultReasonImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultRoleImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultTextImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultTextImpl;

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
//    	OMNamespace omNamespace = this.element.getNamespace();
//        if(omNamespace.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
//        		omNamespace.getPrefix().equals(SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX))
//        {
//        	soapFactory = DOOMAbstractFactory.getSOAP11Factory();
//        }
//        else if(omNamespace.getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
//        		omNamespace.getPrefix().equals(SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX))
//        {
//        	soapFactory = DOOMAbstractFactory.getSOAP12Factory();
//        }
    	org.apache.axiom.soap.SOAPFactory soapFactory = null;    	
    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP11Factory();
    	}
    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
    	}
    	
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
            } 
            else 
            {
            	SOAPFaultText text = null;
            	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
                    text = new SOAP11FaultTextImpl(reason,
                            (SOAPFactory) this.element.getOMFactory());
            	}else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
                    text = new SOAP12FaultTextImpl(reason,
                            (SOAPFactory) this.element.getOMFactory());
            	}
                text.setText(faultString);
                reason.addSOAPText(text);
            }
        } 
        else 
        {
        	org.apache.axiom.soap.SOAPFactory soapFactory = null;
        	
        	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
        		soapFactory = DOOMAbstractFactory.getSOAP11Factory();
        	}
        	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
        		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
        	}
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
//        Detail saajDetail = null;
//        
//    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
//    		SOAP11FaultDetailImpl omDetail = new SOAP11FaultDetailImpl(this.fault,
//            		(SOAPFactory) this.element.getOMFactory());
//    		saajDetail = new DetailImpl(omDetail);
//    	}
//    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
//    		SOAP12FaultDetailImpl omDetail = new SOAP12FaultDetailImpl(this.fault,
//            		(SOAPFactory) this.element.getOMFactory());
//    		saajDetail = new DetailImpl(omDetail);
//    	}
        
        SOAPFaultDetail omDetail;
        SOAPFactory factory = (SOAPFactory) this.element.getOMFactory();
        if (factory instanceof SOAP11Factory) {
            omDetail = new SOAP11FaultDetailImpl(this.fault,
                    factory);
        } else {
            omDetail = new SOAP12FaultDetailImpl(this.fault,
                    factory);
        }
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

    public void addFaultReasonText(String text, Locale locale) throws SOAPException {
        //TODO - check
    	org.apache.axiom.soap.SOAPFactory soapFactory = null;
    	
    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
    		 throw new UnsupportedOperationException();   		
    	}
    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
    	}
    		
    	if(this.fault.getReason() == null){
    		SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason(this.fault);
    		this.fault.setReason(soapFaultReason);
    	}
    	SOAPFaultText soapFaultText = soapFactory.createSOAPFaultText(this.fault.getReason());
    	soapFaultText.setText(text);
    	soapFaultText.setLang(locale.getLanguage());
    }

    
    /**
     * Adds a <CODE>Subcode</CODE> to the end of the sequence of Subcodes contained by this 
     * SOAPFault.
     * <p/>
     * @param subcode a <CODE>String</CODE> giving the sub code to be set
     * @throws SOAPException if there is an error in
     *                       adding the <CODE>subcode</CODE> to the underlying XML
     *                       tree.
     */
    
    public void appendFaultSubcode(QName subcode) throws SOAPException {
        //TODO - check
    	org.apache.axiom.soap.SOAPFactory soapFactory = null;
    	
    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
    		throw new UnsupportedOperationException();    		
    	}
    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
    	}
		if(this.fault.getCode() == null){
			soapFactory.createSOAPFaultCode(this.fault);			
		}
		SOAPFaultSubCode soapFaultSubCode = soapFactory.createSOAPFaultSubCode(this.fault.getCode());
		if(soapFaultSubCode != null){
			soapFaultSubCode.setText(subcode);
		}
    }
    
    /**
     * Gets the fault code for this SOAPFault object as a <CODE>QName</CODE> object.
     * <p/>
     */
    public QName getFaultCodeAsQName() {
        //TODO - check
    	SOAPFaultCode soapFaultCode = this.fault.getCode();
    	if(soapFaultCode != null){
    		return soapFaultCode.getQName();
    	}
    	return null;
    }

    /**
     * Returns the optional Node element value for this SOAPFault object
     * <p/>
     */
    public String getFaultNode() {
        //TODO - check
    	if (fault != null && fault.getNode() != null && fault.getNode().getText() != null) {
            return fault.getNode().getText();
        }
        return null;
    	
    }

    /**
     * Returns an Iterator over a distinct sequence of Locales for which there are 
     * associated Reason Text items.
     * 
     * @throws SOAPException if there is an error in retrieving locales
     */
    public Iterator getFaultReasonLocales() throws SOAPException {
        //TODO - implement
    	ArrayList faultReasonLocales = new ArrayList();
    	return faultReasonLocales.iterator();
    }

    /**
     * Returns the Reason Text associated with the given Locale.
     * @throws SOAPException if there is an error in retrieving text for give locale
     */
    public String getFaultReasonText(Locale locale) throws SOAPException {
        //TODO - check
    	Iterator soapTextsItr = this.fault.getReason().getAllSoapTexts().iterator();
    	while (soapTextsItr.hasNext()) {
    		SOAPFaultText soapFaultText = (SOAPFaultText) soapTextsItr.next();
    		if(soapFaultText.getLang().equals(locale.getLanguage())){
    			return soapFaultText.getText();
    		}
		}
    	return null;
    }
    
    /**
     * Returns an Iterator over a sequence of String objects containing all of the
     * Reason Text items for this SOAPFault.
     * 
     * @throws SOAPException if there is an error in retrieving texts for Reason objects 
     */

    public Iterator getFaultReasonTexts() throws SOAPException {
        //TODO - check
    	Iterator soapTextsItr = this.fault.getReason().getAllSoapTexts().iterator();
    	ArrayList reasonTexts = new ArrayList();
    	while (soapTextsItr.hasNext()) {
    		SOAPFaultText soapFaultText = (SOAPFaultText) soapTextsItr.next();
    		reasonTexts.add(soapFaultText.getText());
		}
    	return reasonTexts.iterator();
    }

    /**
     * Returns the optional Role element value for this SOAPFault object.
     */
    public String getFaultRole() {
        //TODO - check
    	if (this.fault.getRole() != null){
    		return this.fault.getRole().getText();
    	}else{
    		return null;
    	}
    }

    /**
     * Gets the Subcodes for this SOAPFault as an iterator over QNames
     */
    public Iterator getFaultSubcodes() {
        //TODO - check
    	//The structure of the env:Subcode element has been chosen to be hierarchical - each child env:Subcode element has 
    	//a mandatory env:Value and an optional env:Subcode sub-element - to allow application-specific codes to be carried. 
    	//This hierarchical structure of the env:Code element allows for an uniform mechanism for conveying multiple 
    	//level of fault codes. 
    	//How to implement this?
    	ArrayList faultSubcodes = new ArrayList();
    	faultSubcodes.add(this.fault.getCode().getSubCode());
    	return faultSubcodes.iterator();
    }

    /**
     * Returns true if this SOAPFault has a Detail subelement and false otherwise.
     */
    public boolean hasDetail() {
        //TODO - check
    	if(this.fault.getDetail() != null){
    		return true;
    	}else{
    		return false;
    	}

    }

    /**
     * Removes any Subcodes that may be contained by this SOAPFault
     */
    public void removeAllFaultSubcodes() {
        //TODO - Not yet implemented
    }

    
    public void setFaultCode(QName qname) throws SOAPException {
        //TODO - check
    	org.apache.axiom.soap.SOAPFactory soapFactory = null;
    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP11Factory();    		
    	}
    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
    	}
        SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode(this.fault);
        soapFaultCode.setLocalName(qname.getLocalPart());
        OMNamespace omNamespace = new OMNamespaceImpl(qname.getNamespaceURI(),qname.getPrefix());
        soapFaultCode.setNamespace(omNamespace);
        
        //This method is unsupported
        //soapFaultCode.setText(qname);
        this.fault.setCode(soapFaultCode);    	
    }

    public void setFaultNode(String s) throws SOAPException {
        //TODO - check
    	org.apache.axiom.soap.SOAPFactory soapFactory = null;
    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP11Factory();    		
    	}
    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
    	}
        SOAPFaultNode soapFaultNode = soapFactory.createSOAPFaultNode(this.fault);
        soapFaultNode.setText(s);
        this.fault.setNode(soapFaultNode);    	
    }

    /**
     * Creates or replaces any existing Role element value for this SOAPFault object.
     * throw
     */
    public void setFaultRole(String s) throws SOAPException {
        //TODO - check
    	org.apache.axiom.soap.SOAPFactory soapFactory = null;
    	if(SOAPConstants.SOAP_1_1_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP11Factory();    		
    	}
    	else if(SOAPConstants.SOAP_1_2_PROTOCOL.equals(getSOAPVersion(this.element))){
    		soapFactory = DOOMAbstractFactory.getSOAP12Factory();
    	}
        SOAPFaultRole soapFaultRole = soapFactory.createSOAPFaultRole(this.fault);
        this.fault.setRole(soapFaultRole);
    	
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
    
    /**
     * @param rootElement
     * @return SOAP version of the element using SOAPConstants.SOAP_1_1_PROTOCOL
     * 		   or SOAPConstants.SOAP_1_2_PROTOCOL
     */
    //TODO : check
//    private String getSOAPVersion(ElementImpl rootElement){
//    	OMNamespace omNamespace = rootElement.getNamespace();
//        if(omNamespace.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
//        		omNamespace.getPrefix().equals(SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX))
//        {
//        	return SOAPConstants.SOAP_1_1_PROTOCOL;
//        }
//        else if(omNamespace.getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
//        		omNamespace.getPrefix().equals(SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX))
//        {
//        	return SOAPConstants.SOAP_1_2_PROTOCOL;
//        }
//    	return null;
//    }
}
