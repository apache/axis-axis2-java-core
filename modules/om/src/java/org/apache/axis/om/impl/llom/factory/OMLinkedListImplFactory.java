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
package org.apache.axis.om.impl.llom.factory;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMText;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.SOAPFault;
import org.apache.axis.om.SOAPHeader;
import org.apache.axis.om.SOAPHeaderBlock;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.OMTextImpl;
import org.apache.axis.om.impl.llom.SOAPBodyImpl;
import org.apache.axis.om.impl.llom.SOAPEnvelopeImpl;
import org.apache.axis.om.impl.llom.SOAPFaultImpl;
import org.apache.axis.om.impl.llom.SOAPHeaderBlockImpl;
import org.apache.axis.om.impl.llom.SOAPHeaderImpl;

import javax.xml.namespace.QName;

/**
 * Class OMLinkedListImplFactory
 */
public class OMLinkedListImplFactory extends OMFactory {
    /**
     * Field MAX_TO_POOL
     */
    public static final int MAX_TO_POOL = 100;

    /**
     * Method createOMElement
     *
     * @param localName
     * @param ns
     * @return
     */
    public OMElement createOMElement(String localName, OMNamespace ns) {
        OMElementImpl element = new OMElementImpl(localName, ns);
        return element;
    }

    /**
     * Method createOMElement
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public OMElement createOMElement(String localName, OMNamespace ns,
                                     OMElement parent,
                                     OMXMLParserWrapper builder) {
        OMElementImpl element = new OMElementImpl(localName, ns, parent,
                builder);
        return element;
    }

    /**
     * Method createOMElement
     *
     * @param localName
     * @param namespaceURI
     * @param namespacePrefix
     * @return
     */
    public OMElement createOMElement(String localName, String namespaceURI,
                                     String namespacePrefix) {
        return this.createOMElement(localName,
                this.createOMNamespace(namespaceURI,
                        namespacePrefix));
    }

    /**
     * Method createOMElement
     *
     * @param qname
     * @param parent
     * @return
     * @throws OMException
     */
    public OMElement createOMElement(QName qname, OMElement parent)
            throws OMException {
        return new OMElementImpl(qname, parent);
    }

    /**
     * Method createOMNamespace
     *
     * @param uri
     * @param prefix
     * @return
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        return new OMNamespaceImpl(uri, prefix);
    }

    /**
     * Method createText
     *
     * @param parent
     * @param text
     * @return
     */
    public OMText createText(OMElement parent, String text) {
        OMTextImpl textNode = new OMTextImpl(parent, text);
        return textNode;
    }

    /**
     * Method createText
     *
     * @param s
     * @return
     */
    public OMText createText(String s) {
        OMTextImpl textNode = new OMTextImpl(s);
    ;
        return textNode;
    }

    /**
     * Method createSOAPBody
     *
     * @param envelope
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope) {
        SOAPBody soapBody = new SOAPBodyImpl(envelope);
        return soapBody;
    }

    /**
     * Method createSOAPBody
     *
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                   OMXMLParserWrapper builder) {
        return new SOAPBodyImpl(envelope, builder);
    }

    /**
     * Method createSOAPEnvelope
     *
     * @param ns
     * @param builder
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns,
                                           OMXMLParserWrapper builder) {
        return new SOAPEnvelopeImpl(ns, builder);
    }

    /**
     * Method createSOAPEnvelope
     *
     * @param ns
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns) {
        return new SOAPEnvelopeImpl(ns);
    }

    /**
     * Method createSOAPHeader
     *
     * @param envelope
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope) {
        return new SOAPHeaderImpl(envelope);
    }

    /**
     * Method createSOAPHeader
     *
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                       OMXMLParserWrapper builder) {
        return new SOAPHeaderImpl(envelope, builder);
    }

    /**
     * Method createSOAPHeaderBlock
     *
     * @param localName
     * @param ns
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns) {
        return new SOAPHeaderBlockImpl(localName, ns);
    }

    /**
     * Method createSOAPHeaderBlock
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPHeaderBlockImpl(localName, ns, parent, builder);
    }

    /**
     * Method createSOAPFault
     *
     * @param parent
     * @param e
     * @return
     */
    public SOAPFault createSOAPFault(SOAPBody parent, Exception e) {
        return new SOAPFaultImpl(parent, e);
    }

    /**
     * Method createSOAPFault
     *
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFault createSOAPFault(OMNamespace ns, SOAPBody parent,
                                     OMXMLParserWrapper builder) {
        return new SOAPFaultImpl(ns, parent, builder);
    }

    /**
     * Method getDefaultEnvelope
     *
     * @return
     */
    public SOAPEnvelope getDefaultEnvelope() {
        // Create an envelope
        OMNamespace ns =
        new OMNamespaceImpl(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI,
                OMConstants.SOAPENVELOPE_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(ns);
        SOAPBodyImpl bodyImpl = new SOAPBodyImpl(env);
        env.addChild(bodyImpl);

        SOAPHeaderImpl headerImpl = new SOAPHeaderImpl(env);
        headerImpl.setComplete(true);
        env.addChild(headerImpl);
        return env;
    }
}
