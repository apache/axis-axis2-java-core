/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.saaj;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

/**
 *
 */
public class SOAPEnvelopeImpl extends SOAPElementImpl<SOAPEnvelope> implements javax.xml.soap.SOAPEnvelope {

    private SOAPPartImpl soapPart;

    public SOAPEnvelopeImpl(SOAPEnvelope envelope) {
        super(envelope);
    }

    /**
     * Creates a new <CODE>Name</CODE> object initialized with the given local name, namespace
     * prefix, and namespace URI.
     * <p/>
     * <P>This factory method creates <CODE>Name</CODE> objects for use in the SOAP/XML document.
     *
     * @param localName a <CODE>String</CODE> giving the local name
     * @param prefix    a <CODE>String</CODE> giving the prefix of the namespace
     * @param uri       a <CODE>String</CODE> giving the URI of the namespace
     * @return a <CODE>Name</CODE> object initialized with the given local name, namespace prefix,
     *         and namespace URI
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Name createName(String localName, String prefix, String uri) throws SOAPException {
        try {
            return new PrefixedQName(uri, localName, prefix);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Creates a new <CODE>Name</CODE> object initialized with the given local name.
     * <p/>
     * <P>This factory method creates <CODE>Name</CODE> objects for use in the SOAP/XML document.
     *
     * @param localName a <CODE>String</CODE> giving the local name
     * @return a <CODE>Name</CODE> object initialized with the given local name
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Name createName(String localName) throws SOAPException {
        try {
            return new PrefixedQName(null, localName, null);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * <P>A new <CODE>SOAPMessage</CODE> object is by default created with a
     * <CODE>SOAPEnvelope</CODE> object that contains an empty <CODE>SOAPHeader</CODE> object. As a
     * result, the method <CODE>getHeader</CODE> will always return a <CODE>SOAPHeader</CODE> object
     * unless the header has been removed and a new one has not been added.
     *
     * @return the <CODE>SOAPHeader</CODE> object or <CODE> null</CODE> if there is none
     * @throws javax.xml.soap.SOAPException if there is a problem obtaining the <CODE>SOAPHeader</CODE>
     *                                      object
     */
    public SOAPHeader getHeader() throws SOAPException {
        return (SOAPHeader)toSAAJNode((org.w3c.dom.Node)omTarget.getHeader());
    }

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with this <CODE>SOAPEnvelope</CODE>
     * object.
     * <p/>
     * <P>A new <CODE>SOAPMessage</CODE> object is by default created with a
     * <CODE>SOAPEnvelope</CODE> object that contains an empty <CODE>SOAPBody</CODE> object. As a
     * result, the method <CODE>getBody</CODE> will always return a <CODE>SOAPBody</CODE> object
     * unless the body has been removed and a new one has not been added.
     *
     * @return the <CODE>SOAPBody</CODE> object for this <CODE> SOAPEnvelope</CODE> object or
     *         <CODE>null</CODE> if there is none
     * @throws javax.xml.soap.SOAPException if there is a problem obtaining the <CODE>SOAPBody</CODE>
     *                                      object
     */
    public SOAPBody getBody() throws SOAPException {
        return (SOAPBody)toSAAJNode((org.w3c.dom.Node)omTarget.getBody());
    }

    /**
     * Creates a <CODE>SOAPHeader</CODE> object and sets it as the <CODE>SOAPHeader</CODE> object
     * for this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * <P>It is illegal to add a header when the envelope already contains a header. Therefore, this
     * method should be called only after the existing header has been removed.
     *
     * @return the new <CODE>SOAPHeader</CODE> object
     * @throws javax.xml.soap.SOAPException if this <CODE> SOAPEnvelope</CODE> object already
     *                                      contains a valid <CODE>SOAPHeader</CODE> object
     */
    public SOAPHeader addHeader() throws SOAPException {
        org.apache.axiom.soap.SOAPHeader header = omTarget.getHeader();
        if (header == null) {
            SOAPHeaderImpl saajSOAPHeader;
            header = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPHeader(omTarget);
            saajSOAPHeader = new SOAPHeaderImpl(header);
            saajSOAPHeader.setParentElement(this);
            ((Element)omTarget.getHeader()).setUserData(SAAJ_NODE, saajSOAPHeader, null);
            return saajSOAPHeader;
        } else {
            throw new SOAPException("Header already present, can't set header again without " +
                    "deleting the existing header. " +
                    "Use getHeader() method and detach the header instead.");
        }
    }

    /**
     * Creates a <CODE>SOAPBody</CODE> object and sets it as the <CODE>SOAPBody</CODE> object for
     * this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * <P>It is illegal to add a body when the envelope already contains a body. Therefore, this
     * method should be called only after the existing body has been removed.
     *
     * @return the new <CODE>SOAPBody</CODE> object
     * @throws javax.xml.soap.SOAPException if this <CODE> SOAPEnvelope</CODE> object already
     *                                      contains a valid <CODE>SOAPBody</CODE> object
     */
    public SOAPBody addBody() throws SOAPException {
        org.apache.axiom.soap.SOAPBody body = omTarget.getBody();
        if (body == null) {
            body = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPBody(omTarget);
            SOAPBodyImpl saajSOAPBody = new SOAPBodyImpl(body);
            saajSOAPBody.setParentElement(this);
            ((Element)omTarget.getBody()).setUserData(SAAJ_NODE, saajSOAPBody, null);
            return saajSOAPBody;
        } else {
            throw new SOAPException("Body already present, can't set body again without " +
                    "deleting the existing body. Use getBody() method instead.");
        }
    }

    public SOAPElement addTextNode(String text) throws SOAPException {
        Node firstChild = target.getFirstChild();
        if (firstChild instanceof org.w3c.dom.Text) {
            ((org.w3c.dom.Text)firstChild).setData(text);
        } else {
            // Else this is a header
            ((OMNode)firstChild).insertSiblingBefore(this.omTarget.getOMFactory().createOMText(text));
        }
        return this;
    }

    /**
     * Override SOAPElement.addAttribute SOAP1.2 should not allow encodingStyle attribute to be set
     * on Envelop
     */
    public SOAPElement addAttribute(Name name, String value) throws SOAPException {
        if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP12Version.getSingleton()) {
            if ("encodingStyle".equals(name.getLocalName())) {
                throw new SOAPException(
                        "SOAP1.2 does not allow encodingStyle attribute to be set " +
                                "on Envelope");
            }
        }
        return super.addAttribute(name, value);
    }

    /**
     * Override SOAPElement.addChildElement SOAP 1.2 should not allow element to be added after body
     * element
     */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP12Version.getSingleton()) {
            throw new SOAPException("Cannot add elements after body element");
        } else if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP11Version.getSingleton()) {
            //Let elements to be added any where.
            return super.addChildElement(name);
        }
        return null;
    }
    
    /**
     * Set SOAPPart parent
     * @param sp
     */
    void setSOAPPartParent(SOAPPartImpl sp) {
        this.soapPart = sp;
    }
    
    /**
     * @return SOAPPart
     */
    SOAPPartImpl getSOAPPartParent() {
        return this.soapPart;
    }
}
