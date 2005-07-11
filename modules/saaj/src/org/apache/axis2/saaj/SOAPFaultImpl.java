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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.*;

import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import java.util.Locale;

/**
 * Class SOAPFaultImpl
 *
 * @author Ashutosh Shahi
 *         ashutosh.shahi@gmail.com
 *         <p/>
 *         SOAPFault specific classes not implemented in OM, so
 *         throwing unsupported operation for the time being
 */
public class SOAPFaultImpl extends SOAPBodyElementImpl implements SOAPFault {

    /**
     * Field fault   The omSOAPFault field
     */
    protected org.apache.axis2.soap.SOAPFault fault;

    /**
     * Constructor SOAPFaultImpl
     *
     * @param fault
     */
    public SOAPFaultImpl(org.apache.axis2.soap.SOAPFault fault) {
        this.fault = fault;
    }

    public org.apache.axis2.soap.SOAPFault getOMFault() {
        return fault;
    }

    /**
     * Method setFaultCode
     *
     * @param faultCode
     * @throws SOAPException
     * @see javax.xml.soap.SOAPFault#setFaultCode(java.lang.String)
     */
    public void setFaultCode(String faultCode) throws SOAPException {
        // No direct mapping of SOAP 1.1 faultCode to SOAP 1.2, Mapping it to
        // (Fault Value of FaultCode) in OM impl
        SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
        SOAPFaultCode fCode = soapFactory.createSOAPFaultCode(fault);
        SOAPFaultValue value = soapFactory.createSOAPFaultValue(fCode);
        fCode.setValue(value);
        value.setText(faultCode);
    }

    /**
     * Method getFaultCode
     *
     * @return
     * @see javax.xml.soap.SOAPFault#getFaultCode()
     */
    public String getFaultCode() {

        //FaultCode mapped to Fault.FaultCode.FaultValue in OM
        return fault.getCode().getValue().getText();
    }

    /**
     * method setFaultActor
     *
     * @param faultActor
     * @throws SOAPException
     * @see javax.xml.soap.SOAPFault#setFaultActor(java.lang.String)
     */
    public void setFaultActor(String faultActor) throws SOAPException {

        //faultActor mapped to SOAPFaultNode in OM
        SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
        SOAPFaultNode fNode = soapFactory.createSOAPFaultNode(fault);
        fNode.setNodeValue(faultActor);
    }

    /**
     * method getFaultActor
     *
     * @return
     * @see javax.xml.soap.SOAPFault#getFaultActor()
     */
    public String getFaultActor() {

        // return the text value in SOAPFaultNode of OM
        return fault.getNode().getNodeValue();
    }

    /**
     * method setFaultString
     *
     * @param faultString
     * @throws SOAPException
     * @see javax.xml.soap.SOAPFault#setFaultString(java.lang.String)
     */
    public void setFaultString(String faultString) throws SOAPException {

        //FaultString mapped to text elemtnt of SOAPFaultReason->SOAPFaultText in OM
        SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
        SOAPFaultReason fReason = soapFactory.createSOAPFaultReason(fault);
        SOAPFaultText text = soapFactory.createSOAPFaultText(fReason);
        text.setText(faultString);
        fReason.setSOAPText(text);
    }

    /**
     * method getFaultString
     *
     * @return
     * @see javax.xml.soap.SOAPFault#getFaultString()
     */
    public String getFaultString() {

        //return text elemtnt of SOAPFaultReason->SOAPFaultText in OM
        return fault.getReason().getSOAPText().getText();
    }

    /**
     * method getDetail
     *
     * @return
     * @see javax.xml.soap.SOAPFault#getDetail()
     */
    public Detail getDetail() {

        SOAPFaultDetail detail = fault.getDetail();
        return new DetailImpl(detail);
    }

    /**
     * method addDetail
     *
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPFault#addDetail()
     */
    public Detail addDetail() throws SOAPException {

        SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
        SOAPFaultDetail detail = soapFactory.createSOAPFaultDetail(fault);
        return new DetailImpl(detail);
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
        //FaultString mapped to text elemtnt of SOAPFaultReason->SOAPFaultText in OM
        // Not using Locale information
        SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
        SOAPFaultReason fReason = soapFactory.createSOAPFaultReason(fault);
        SOAPFaultText text = soapFactory.createSOAPFaultText(fReason);
        text.setText(faultString);
        fReason.setSOAPText(text);
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
