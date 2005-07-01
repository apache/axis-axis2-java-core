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
package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.OMAttributeImpl;
import org.apache.axis2.om.impl.llom.OMElementImpl;
import org.apache.axis2.om.impl.llom.OMNamespaceImpl;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;

import javax.xml.namespace.QName;

/**
 * Class SOAPHeaderBlockImpl
 */
public abstract class SOAPHeaderBlockImpl extends OMElementImpl
        implements SOAPHeaderBlock {

    private boolean processed = false;
    /**
     * @param localName
     * @param ns
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns, SOAPHeader parent) throws SOAPProcessingException {
        super(localName, ns, parent);
        this.setNamespace(ns);
    }

    /**
     * Constructor SOAPHeaderBlockImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAPHeaderBlockImpl(String localName, OMNamespace ns,
                               OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
        this.setNamespace(ns);
    }

    /**
     * @param attributeName
     * @param attrValue
     */
    protected void setAttribute(String attributeName, String attrValue, String soapEnvelopeNamespaceURI) {
        OMAttribute omAttribute = this.getFirstAttribute(
                new QName(soapEnvelopeNamespaceURI, attributeName));
        if (omAttribute != null) {
            omAttribute.setValue(attrValue);
        } else {
            OMAttribute attribute = new OMAttributeImpl(
                    attributeName,
                    new OMNamespaceImpl(
                            soapEnvelopeNamespaceURI,
                            SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX), attrValue);
            this.addAttribute(attribute);
        }
    }

    /**
     * Method getAttribute
     *
     * @param attrName
     * @return
     */
    protected String getAttribute(String attrName, String soapEnvelopeNamespaceURI) {
        OMAttribute omAttribute = this.getFirstAttribute(
                new QName(soapEnvelopeNamespaceURI, attrName));
        return (omAttribute != null)
                ? omAttribute.getValue()
                : null;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        processed = true;
    }
}
