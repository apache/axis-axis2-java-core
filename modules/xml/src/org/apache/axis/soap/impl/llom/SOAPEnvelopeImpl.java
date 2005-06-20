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

import org.apache.axis.om.*;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.soap.SOAPBody;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPHeader;

import javax.xml.namespace.QName;

/**
 * Class SOAPEnvelopeImpl
 */
public class SOAPEnvelopeImpl extends SOAPElement
        implements SOAPEnvelope, OMConstants {

    private SOAPBody soapBody;
    private SOAPHeader soapHeader;

    /**
     * @param builder
     */
    public SOAPEnvelopeImpl(OMXMLParserWrapper builder) {
        super(null, SOAPConstants.SOAPENVELOPE_LOCAL_NAME, builder);
    }

    /**
     * @param ns
     */
    public SOAPEnvelopeImpl(OMNamespace ns) {
        super(SOAPConstants.SOAPENVELOPE_LOCAL_NAME, ns);
    }

    /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE>
     * SOAPEnvelope</CODE> object. <P> This SOAPHeader will just be a container
     * for all the headers in the <CODE>OMMessage</CODE> </P>
     *
     * @return the <CODE>SOAPHeader</CODE> object or <CODE> null</CODE> if there
     *         is none
     * @throws org.apache.axis.om.OMException if there is a problem obtaining
     *                                        the <CODE>SOAPHeader</CODE>
     *                                        object
     * @throws OMException
     */
    public SOAPHeader getHeader() throws OMException {
        if (soapHeader == null) {
            soapHeader = (SOAPHeader) getFirstChildWithName(new QName(SOAPConstants.HEADER_LOCAL_NAME));
        }
        return soapHeader;
    }

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with this
     * <CODE>SOAPEnvelope</CODE> object. <P> This SOAPBody will just be a
     * container for all the BodyElements in the <CODE>OMMessage</CODE> </P>
     *
     * @return the <CODE>SOAPBody</CODE> object for this <CODE>
     *         SOAPEnvelope</CODE> object or <CODE>null</CODE> if there is none
     * @throws org.apache.axis.om.OMException if there is a problem obtaining
     *                                        the <CODE>SOAPBody</CODE> object
     * @throws OMException
     */
    public SOAPBody getBody() throws OMException {
        if (soapBody == null) {

            //check for the first element
            OMElement element = getFirstElement();
            if (SOAPConstants.BODY_LOCAL_NAME.equals(element.getLocalName())) {
                soapBody = (SOAPBody) element;
            } else {      // if not second element SHOULD be the body
                OMNode node = element.getNextSibling();
                while (node.getType() != OMNode.ELEMENT_NODE) {
                    node = node.getNextSibling();
                }
                element = (OMElement) node;

                if (SOAPConstants.BODY_LOCAL_NAME.equals(element.getLocalName())) {
                    soapBody = (SOAPBody) element;
                } else {
                    throw new OMException("SOAPEnvelope must contain a body element which is either first or second child element of the SOAPEnvelope.");
                }
            }
        }
        return soapBody;
    }

    /**
     * Method detach
     *
     * @throws OMException
     */
    public OMNode detach() throws OMException {
        throw new OMException("Root Element can not be detached");
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        // here do nothing as SOAPEnvelope doesn't have a parent !!!
    }
}
