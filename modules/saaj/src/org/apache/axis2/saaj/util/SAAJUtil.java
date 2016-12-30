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

package org.apache.axis2.saaj.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttachmentAccessor;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.transform.stax.StAXSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Utility class for the Axis2-WSS4J Module */
public class SAAJUtil {

    /**
     * Create a DOM Document using the org.apache.axiom.soap.SOAPEnvelope
     *
     * @param env An org.apache.axiom.soap.SOAPEnvelope instance
     * @return the DOM Document of the given SOAP Envelope
     */
    public static Document getDocumentFromSOAPEnvelope(org.apache.axiom.soap.SOAPEnvelope env) {
        return (Document)OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getMetaFactory(OMAbstractFactory.FEATURE_DOM),
                env.getXMLStreamReader()).getSOAPMessage();
    }

    /**
     * Create a DOM Document using the org.apache.axiom.soap.SOAPEnvelope
     *
     * @param env An org.apache.axiom.soap.SOAPEnvelope instance
     * @return the org.apache.axis2.soap.impl.dom.SOAPEnvelopeImpl of the given SOAP Envelope
     */
    public static Element toDOOMSOAPEnvelope(org.apache.axiom.soap.SOAPEnvelope env) {
        return (Element)OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getMetaFactory(OMAbstractFactory.FEATURE_DOM),
                env.getXMLStreamReader()).getSOAPEnvelope();
    }

    public static org.apache.axiom.soap.SOAPEnvelope
            getSOAPEnvelopeFromDOOMDocument(org.w3c.dom.Document doc) {
        return OMXMLBuilderFactory.createStAXSOAPModelBuilder(((OMElement)doc.getDocumentElement()).getXMLStreamReader()).getSOAPEnvelope();
    }


    public static org.apache.axiom.soap.SOAPEnvelope
            toOMSOAPEnvelope(org.w3c.dom.Element elem) {
        return OMXMLBuilderFactory.createStAXSOAPModelBuilder(((OMElement)elem).getXMLStreamReader()).getSOAPEnvelope();
    }
    
    /**
     * Convert a SAAJ message to an Axiom SOAP envelope object and process xop:Include
     * elements.
     * 
     * @param message the SAAJ message
     * @return the OM SOAP envelope
     * @throws SOAPException
     */
    public static org.apache.axiom.soap.SOAPEnvelope toOMSOAPEnvelope(
            javax.xml.soap.SOAPMessage message) throws SOAPException {
        final Map<String,DataHandler> attachments = new HashMap<String,DataHandler>();
        for (Iterator it = message.getAttachments(); it.hasNext(); ) {
            AttachmentPart attachment = (AttachmentPart)it.next();
            String contentId = attachment.getContentId();
            if (contentId != null) {
                DataHandler dh = attachment.getDataHandler();
                if (dh == null) {
                    throw new SOAPException("Attachment with NULL DataHandler");
                }
                if (contentId.startsWith("<") && contentId.endsWith(">")) {
                    contentId = contentId.substring(1, contentId.length()-1);
                }
                attachments.put(contentId, dh);
            }
        }
        OMElement docElem = (OMElement)message.getSOAPPart().getDocumentElement();
        OMAttachmentAccessor attachmentAccessor = new OMAttachmentAccessor() {
            @Override
            public DataHandler getDataHandler(String contentID) {
                return attachments.get(contentID);
            }
        };
        return OMXMLBuilderFactory.createSOAPModelBuilder(OMAbstractFactory.getMetaFactory(),
                new StAXSource(docElem.getXMLStreamReader()), attachmentAccessor).getSOAPEnvelope();
    }

    /**
     * Convert a given OMElement to a DOM Element
     *
     * @param element
     * @return DOM Element
     */
    public static org.w3c.dom.Element toDOM(OMElement element) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        element.serialize(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(bais).getDocumentElement();
    }

    /**
     * Create a copy of an existing {@link MimeHeaders} object.
     * 
     * @param headers the object to copy
     * @return a copy of the {@link MimeHeaders} object
     */
    public static MimeHeaders copyMimeHeaders(MimeHeaders headers) {
        MimeHeaders result = new MimeHeaders();
        Iterator iterator = headers.getAllHeaders();
        while (iterator.hasNext()) {
            MimeHeader hdr = (MimeHeader)iterator.next();
            result.addHeader(hdr.getName(), hdr.getValue());
        }
        return result;
    }

    /**
     * Normalize a content type specification. This removes all parameters
     * from the content type and converts it to lower case.
     * 
     * @param contentType the content type to normalize
     * @return the normalized content type
     */
    public static String normalizeContentType(String contentType) {
        int idx = contentType.indexOf(";");
        return (idx == -1 ? contentType : contentType.substring(0, idx)).trim().toLowerCase();
    }
    
    public static boolean compareContentTypes(String contentType1, String contentType2) {
        String ct1 = (contentType1 == null) ? "" : contentType1.trim().toLowerCase();
        String ct2 = (contentType2 == null) ? "" : contentType2.trim().toLowerCase();
        return ct1.equals(ct2);        
    }
}
