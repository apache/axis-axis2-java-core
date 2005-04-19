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
package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.SOAPHeader;

/**
 * Class SOAPEnvelopeImpl
 */
public class SOAPEnvelopeImpl extends OMElementImpl
        implements SOAPEnvelope, OMConstants {
    /**
     * @param builder
     */
    public SOAPEnvelopeImpl(OMXMLParserWrapper builder) {
        super(SOAPConstants.SOAPENVELOPE_LOCAL_NAME, null, null, builder);
    }

    /**
     * Constructor SOAPEnvelopeImpl
     *
     * @param ns
     * @param builder
     */
    public SOAPEnvelopeImpl(OMNamespace ns, OMXMLParserWrapper builder) {
        super(SOAPConstants.SOAPENVELOPE_LOCAL_NAME, ns, null, builder);
    }

    /**
     * @param ns
     */
    public SOAPEnvelopeImpl(OMNamespace ns) {
        super(SOAPConstants.SOAPENVELOPE_LOCAL_NAME, ns);
    }

    /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE>
     * SOAPEnvelope</CODE> object.
     * <P> This SOAPHeader will just be a container for all the headers in the
     * <CODE>OMMessage</CODE>
     * </P>
     *
     * @return the <CODE>SOAPHeader</CODE> object or <CODE>
     *         null</CODE> if there is none
     * @throws org.apache.axis.om.OMException if there is a problem
     *                                        obtaining the <CODE>SOAPHeader</CODE> object
     * @throws OMException
     */
    public SOAPHeader getHeader() throws OMException {
        OMElement element = getFirstElement();
        if (SOAPConstants.HEADER_LOCAL_NAME.equals(element.getLocalName())) {
            return (SOAPHeader) element;
        }
        return null;
    }

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with
     * this <CODE>SOAPEnvelope</CODE> object.
     * <P> This SOAPBody will just be a container for all the BodyElements in the
     * <CODE>OMMessage</CODE>
     * </P>
     *
     * @return the <CODE>SOAPBody</CODE> object for this <CODE>
     *         SOAPEnvelope</CODE> object or <CODE>null</CODE> if there
     *         is none
     * @throws org.apache.axis.om.OMException if there is a problem
     *                                        obtaining the <CODE>SOAPBody</CODE> object
     * @throws OMException
     */
    public SOAPBody getBody() throws OMException {
        OMElement element = getFirstElement();
        if (SOAPConstants.BODY_LOCAL_NAME.equals(element.getLocalName())) {
            return (SOAPBody) element;
        }else{
            element = element.getNextSiblingElement();
            if (SOAPConstants.BODY_LOCAL_NAME.equals(element.getLocalName())) {
                return (SOAPBody) element;
            }
        }
        return null;
    }

    /**
     * Method detach
     *
     * @throws OMException
     */
    public void detach() throws OMException {
        throw new OMException("Root Element can not be detached");
    }
}
