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
package org.apache.axis.soap.impl.llom;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.soap.SOAPBody;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFault;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Constants;

/**
 * Class SOAPBodyImpl
 */
public class SOAPBodyImpl extends OMElementImpl
        implements SOAPBody, OMConstants {
    /**
     * Field hasSOAPFault
     */
    private boolean hasSOAPFault = false;
    
    /**
     * @param envelope
     */
    public SOAPBodyImpl(SOAPEnvelope envelope) {
        super(envelope);
        this.ns = envelope.getNamespace();
        this.localName = SOAPConstants.BODY_LOCAL_NAME;
    }

    /**
     * Constructor SOAPBodyImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAPBodyImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        super(SOAPConstants.BODY_LOCAL_NAME, envelope.getNamespace(), envelope,
                builder);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to
     * this <code>SOAPBody</code> object.
     *
     * @param e
     * @return the new <code>SOAPFault</code> object
     * @throws org.apache.axis.om.OMException if there is a SOAP error
     * @throws OMException
     */
    public SOAPFault addFault(Exception e) throws OMException {
        SOAPFault soapFault = new SOAPFaultImpl(this, e);
        addFault(soapFault);
        return soapFault;
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
        if (hasSOAPFault) {
            return true;
        } else {
            OMElement element = getFirstElement();
            if(element != null 
                && SOAPConstants.SOAPFAULT_LOCAL_NAME.equals(element.getLocalName())
                && SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(element.getNamespace().getName())){
                hasSOAPFault = true;
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     * object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     *         object
     */
    public SOAPFault getFault() {
        if(hasSOAPFault){
            OMElement element = getFirstElement();
            if(element != null 
                && SOAPConstants.SOAPFAULT_LOCAL_NAME.equals(element.getLocalName())
                && SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(element.getNamespace().getName())){
                hasSOAPFault = true;
                return (SOAPFault)element;
            }else{
                return null;
            }
        }
        return null;
    }

    /**
     * @param soapFault
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public void addFault(SOAPFault soapFault) throws OMException {
        if (hasSOAPFault) {
            throw new OMException(
                    "SOAP Body already has a SOAP Fault and there can not be more than one SOAP fault");
        }
        addChild(soapFault);
        hasSOAPFault = true;
    }
}
