package org.apache.axis.om.impl;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.soap.SOAPBody;
import org.apache.axis.om.soap.SOAPBodyElement;
import org.apache.axis.om.soap.SOAPEnvelope;
import org.apache.axis.om.soap.SOAPFault;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 2, 2004
 * Time: 4:29:00 PM
 */
public class SOAPBodyImpl extends OMElementImpl implements SOAPBody {



    /**
     * @param envelope
     */
    public SOAPBodyImpl(SOAPEnvelope envelope) {
        super(envelope);
    }

    public SOAPBodyImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to
     * this <code>SOAPBody</code> object.
     *
     * @return the new <code>SOAPFault</code> object
     * @throws org.apache.axis.om.OMException if there is a SOAP error
     */
    public SOAPFault addFault() throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
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
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     * object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     *         object
     */
    public SOAPFault getFault() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the
     * specified name and adds it to this <code>SOAPBody</code> object.
     *
     * @param element a <code>OMNamedNode</code> object with the name for the new
     *                <code>SOAPBodyElement</code> object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws org.apache.axis.om.OMException if a SOAP error occurs
     */
    public SOAPBodyElement addBodyElement(String localName, OMNamespace ns) throws OMException {
        SOAPBodyElement soapBodyElement = new SOAPBodyElementImpl(localName, ns);
        this.addChild(soapBodyElement);
        soapBodyElement.setComplete(true);
        return soapBodyElement;
    }

    /**
     * @param soapFault
     * @throws org.apache.axis.om.OMException
     */
    public void addFault(SOAPFault soapFault) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
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
     * @param element the <code>Document</code> object whose root node will be
     *                added to this <code>SOAPBody</code>
     * @return the <code>SOAPBodyElement</code> that represents the root node
     *         that was added
     * @throws org.apache.axis.om.OMException if the <code>Document</code> cannot be added
     */
    public SOAPBodyElement addDocument(OMElement element) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }
}
