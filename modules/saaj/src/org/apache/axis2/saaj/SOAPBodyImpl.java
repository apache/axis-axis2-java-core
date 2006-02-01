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

import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11FaultImpl;
import org.w3c.dom.Document;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import java.util.Locale;

public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody {

    private org.apache.ws.commons.soap.SOAPBody omSOAPBody;

    /**
     * @param omSOAPBody
     */
    public SOAPBodyImpl(org.apache.ws.commons.soap.SOAPBody omSOAPBody) {
        super((ElementImpl) omSOAPBody);
        this.omSOAPBody = omSOAPBody;
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to
     * this <code>SOAPBody</code> object.
     *
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault() throws SOAPException {
        SOAP11FaultImpl fault = new SOAP11FaultImpl(omSOAPBody);
        return new SOAPFaultImpl(fault);
    }

    /**
     * Indicates whether a <code>SOAPFault</code> object exists in
     * this <code>SOAPBody</code> object.
     *
     * @return <code>true</code> if a <code>SOAPFault</code> object exists in
     *         this <code>SOAPBody</code> object; <code>false</code>
     *         otherwise
     */
    public boolean hasFault() {
        return omSOAPBody.hasFault();
    }

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     * object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     *         object
     */
    public SOAPFault getFault() {
        if (omSOAPBody.hasFault()) {
            return new SOAPFaultImpl(omSOAPBody.getFault());
        }
        return null;
    }

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the
     * specified name and adds it to this <code>SOAPBody</code> object.
     *
     * @param name a <code>Name</code> object with the name for the new
     *             <code>SOAPBodyElement</code> object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPBodyElement addBodyElement(Name name) throws SOAPException {
        SOAPElementImpl elem = (SOAPElementImpl) addChildElement(name);
        return new SOAPBodyElementImpl(elem.element);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this
     * <code>SOAPBody</code> object. The new <code>SOAPFault</code> will have a
     * <code>faultcode</code> element that is set to the <code>faultCode</code>
     * parameter and a <code>faultstring</code> set to <code>faultstring</code>
     * and localized to <code>locale</code>.
     *
     * @param faultCode   a <code>Name</code> object giving the fault code to be
     *                    set; must be one of the fault codes defined in the SOAP 1.1
     *                    specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the
     *                    fault
     * @param locale      a <code>Locale</code> object indicating the native language
     *                    of the <ocde>faultString</code>
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(Name faultCode, String faultString, Locale locale) throws SOAPException {
        SOAP11FaultImpl fault = new SOAP11FaultImpl(omSOAPBody, new Exception(faultString));
        SOAPFaultImpl faultImpl = new SOAPFaultImpl(fault);
        faultImpl.setFaultCode(faultCode);
        if (locale != null) {
            faultImpl.setFaultString(faultString, locale);
        } else {
            faultImpl.setFaultString(faultString);
        }

        return faultImpl;
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this
     * <code>SOAPBody</code> object. The new <code>SOAPFault</code> will have a
     * <code>faultcode</code> element that is set to the <code>faultCode</code>
     * parameter and a <code>faultstring</code> set to <code>faultstring</code>.
     *
     * @param faultCode   a <code>Name</code> object giving the fault code to be
     *                    set; must be one of the fault codes defined in the SOAP 1.1
     *                    specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the
     *                    fault
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException {
        return addFault(faultCode, faultString, null);
    }

    /**
     * Adds the root node of the DOM <code>Document</code> to this
     * <code>SOAPBody</code> object.
     * <p/>
     * Calling this method invalidates the <code>document</code> parameter. The
     * client application should discard all references to this
     * <code>Document</code> and its contents upon calling
     * <code>addDocument</code>. The behavior of an application that continues
     * to use such references is undefined.
     *
     * @param document the <code>Document</code> object whose root node will be
     *                 added to this <code>SOAPBody</code>
     * @return the <code>SOAPBodyElement</code> that represents the root node
     *         that was added
     * @throws SOAPException if the <code>Document</code> cannot be added
     */
    public SOAPBodyElement addDocument(Document document) throws SOAPException {

        SOAPElementImpl elem = new SOAPElementImpl((ElementImpl) document.getDocumentElement());
        return new SOAPBodyElementImpl(elem.element);

    }
}
