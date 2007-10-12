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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Utility class for the Axis2-WSS4J Module */
public class SAAJUtil {

    /**
     * Create a DOM Document using the org.apache.axiom.soap.SOAPEnvelope
     *
     * @param env An org.apache.axiom.soap.SOAPEnvelope instance
     * @return the DOM Document of the given SOAP Envelope
     */
    public static Document getDocumentFromSOAPEnvelope(org.apache.axiom.soap.SOAPEnvelope env) {
        env.build();

        //Check the namespace and find SOAP version and factory
        String nsURI;
        SOAPFactory factory;
        if (env.getNamespace().getNamespaceURI()
                .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            nsURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            factory = DOOMAbstractFactory.getSOAP11Factory();
        } else {
            nsURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            factory = DOOMAbstractFactory.getSOAP11Factory();
        }

        StAXSOAPModelBuilder stAXSOAPModelBuilder =
                new StAXSOAPModelBuilder(env.getXMLStreamReader(), factory, nsURI);
        SOAPEnvelope envelope = (stAXSOAPModelBuilder).getSOAPEnvelope();
        envelope.build();

        Element envElem = (Element)envelope;
        return envElem.getOwnerDocument();
    }

    /**
     * Create a DOM Document using the org.apache.axiom.soap.SOAPEnvelope
     *
     * @param env An org.apache.axiom.soap.SOAPEnvelope instance
     * @return the org.apache.axis2.soap.impl.dom.SOAPEnvelopeImpl of the given SOAP Envelope
     */
    public static org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl
            toDOOMSOAPEnvelope(org.apache.axiom.soap.SOAPEnvelope env) {
        env.build();

        //Check the namespace and find SOAP version and factory
        String nsURI;
        SOAPFactory factory;
        if (env.getNamespace().getNamespaceURI()
                .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            nsURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            factory = DOOMAbstractFactory.getSOAP11Factory();
        } else {
            nsURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            factory = DOOMAbstractFactory.getSOAP11Factory();
        }

        StAXSOAPModelBuilder stAXSOAPModelBuilder =
                new StAXSOAPModelBuilder(env.getXMLStreamReader(), factory, nsURI);
        SOAPEnvelope envelope = (stAXSOAPModelBuilder).getSOAPEnvelope();
        envelope.build();

        return (org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl)envelope;
    }

    public static org.apache.axiom.soap.SOAPEnvelope
            getSOAPEnvelopeFromDOOMDocument(org.w3c.dom.Document doc) {

        OMElement docElem = (OMElement)doc.getDocumentElement();
        StAXSOAPModelBuilder stAXSOAPModelBuilder =
                new StAXSOAPModelBuilder(docElem.getXMLStreamReader(), null);
        return stAXSOAPModelBuilder.getSOAPEnvelope();
    }


    public static org.apache.axiom.soap.SOAPEnvelope
            toOMSOAPEnvelope(org.w3c.dom.Element elem) {

        OMElement docElem = (OMElement)elem;
        StAXSOAPModelBuilder stAXSOAPModelBuilder =
                new StAXSOAPModelBuilder(docElem.getXMLStreamReader(), null);
        return stAXSOAPModelBuilder.getSOAPEnvelope();
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
}
