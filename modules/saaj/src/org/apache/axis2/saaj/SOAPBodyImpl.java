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
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.soap.SOAPFactory;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.util.Locale;

/**
 * Class SOAPBodeImpl
 *
 * @author Jayachandra
 *         jayachandra@gmail.com
 */
public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody {
    /**
     * Field omSOAPBody
     * omSOAPBody is the OM's SOAPBody object that is used for delegation purpose
     */
    private org.apache.axis2.soap.SOAPBody omSOAPBody;

    /**
     * Constructor SOAPBodeImpl
     * The constructor to facilitate conversion of SAAJ SOAPBody out of OM SOAPBody
     *
     * @param omSoapBody
     */
    public SOAPBodyImpl(org.apache.axis2.soap.SOAPBody omSoapBody) {
        super(omSoapBody);
        this.omSOAPBody = omSoapBody;
    }

    /**
     * Method addFault
     *
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPBody#addFault()
     */
    public SOAPFault addFault() throws SOAPException {
        try {
            //OM SOAPFaultImpl has SOAPFaultImpl(OMElement parent, Exception e) constructor, will use that
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
            org.apache.axis2.soap.SOAPFault omSoapFault = soapFactory.createSOAPFault(
                    omSOAPBody);
            omSOAPBody.addFault(omSoapFault);
            return (new SOAPFaultImpl(omSoapFault));
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Method hasFault
     *
     * @return
     * @see javax.xml.soap.SOAPBody#hasFault()
     */
    public boolean hasFault() {
        return omSOAPBody.hasFault();
    }

    /**
     * Method getFault
     *
     * @return
     * @see javax.xml.soap.SOAPBody#getFault()
     */
    public SOAPFault getFault() {
        return (new SOAPFaultImpl(omSOAPBody.getFault()));
    }

    /**
     * Method addBodyElement
     *
     * @param name
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPBody#addBodyElement(javax.xml.soap.Name)
     */
    public SOAPBodyElement addBodyElement(Name name) throws SOAPException {

        try {
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            QName qname = new QName(name.getURI(),
                    name.getLocalName(),
                    name.getPrefix());
            OMElement bodyElement = omFactory.createOMElement(qname,
                    omSOAPBody);
            omSOAPBody.addChild(bodyElement);
            return (new SOAPBodyElementImpl(bodyElement));
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Method addFault
     *
     * @param faultCode
     * @param faultString
     * @param
     * @throws SOAPException
     * @see javax.xml.soap.SOAPBody#addFault(javax.xml.soap.Name, java.lang.String, java.util.Locale)
     */
    public SOAPFault addFault(Name faultCode,
                              String faultString,
                              Locale locale)
            throws SOAPException {
        try {
            //OM SOAPFaultImpl has SOAPFaultImpl(OMElement parent, Exception e) constructor, will use that
            //actually soap fault is created with the OM's default SOAPFAULT_LOCALNAME and PREFIX, b'coz I've droppe the name param
            //a work around can be possible but would be confusing as there is no straight forward soapfault constructor in om.
            //So am deferring it.
            //even locale param is dropped, don't know how to handle it at the moment. so dropped it.
            SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
            org.apache.axis2.soap.SOAPFault omSoapFault = soapFactory.createSOAPFault(
                    omSOAPBody, new Exception(faultString));
            omSOAPBody.addFault(omSoapFault);
            return (new SOAPFaultImpl(omSoapFault));
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Method addFault
     *
     * @param faultCode
     * @param faultString
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPBody#addFault(javax.xml.soap.Name, java.lang.String)
     */
    public SOAPFault addFault(Name faultCode, String faultString)
            throws SOAPException {
        try {
            //OM SOAPFaultImpl has SOAPFaultImpl(OMElement parent, Exception e) constructor, will use that
            //actually soap fault is created with the OM's default SOAPFAULT_LOCALNAME and PREFIX, b'coz I've droppe the name param
            //a work around can be possible but would be confusing as there is no straight forward soapfault constructor in om.
            //So am deferring it.
            SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
            org.apache.axis2.soap.SOAPFault omSoapFault = soapFactory.createSOAPFault(
                    omSOAPBody, new Exception(faultString));
            omSOAPBody.addFault(omSoapFault);
            return (new SOAPFaultImpl(omSoapFault));
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Method addDocument
     *
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
