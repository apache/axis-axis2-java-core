package org.apache.axis.om.impl.llom;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.SOAPFault;



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
 */
public class SOAPBodyImpl extends OMElementImpl implements SOAPBody, OMConstants {

    private boolean hasSOAPFault = false;


    /**
     * @param envelope
     */
    public SOAPBodyImpl(SOAPEnvelope envelope) {
        super(envelope);
        this.ns = envelope.getNamespace();
        this.localName = OMConstants.BODY_LOCAL_NAME;
    }

    public SOAPBodyImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        super(OMConstants.BODY_LOCAL_NAME, envelope.getNamespace(), envelope, builder);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to
     * this <code>SOAPBody</code> object.
     *
     * @return the new <code>SOAPFault</code> object
     * @throws org.apache.axis.om.OMException if there is a SOAP error
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
            Iterator soapFaultChildren = getChildrenWithName(new QName(SOAPFAULT_NAMESPACE_URI, SOAPFAULT_LOCAL_NAME));
            if (soapFaultChildren.hasNext()) {
                hasSOAPFault = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     * object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     *         object
     */
    public SOAPFault getFault() {
        Iterator soapFaultChildren = getChildrenWithName(new QName(SOAPFAULT_NAMESPACE_URI, SOAPFAULT_LOCAL_NAME));
        while (soapFaultChildren.hasNext()) {
            Object o = soapFaultChildren.next();
            if (o instanceof SOAPFault) {
                SOAPFault soapFault = (SOAPFault) o;
                return soapFault;
            }

        }
        return null;
    }


    /**
     * @param soapFault
     * @throws org.apache.axis.om.OMException
     */
    public void addFault(SOAPFault soapFault) throws OMException {
        if (hasSOAPFault) {
            throw new OMException("SOAP Body already has a SOAP Fault and there can not be more than one SOAP fault");
        }
        addChild(soapFault);
        hasSOAPFault = true;

    }


}
