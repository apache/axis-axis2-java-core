package org.apache.axis.om.impl.factory;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.OMAttributeImpl;
import org.apache.axis.om.impl.OMElementImpl;
import org.apache.axis.om.impl.OMNamedNodeImpl;
import org.apache.axis.om.impl.OMNamespaceImpl;
import org.apache.axis.om.impl.OMNodeImpl;
import org.apache.axis.om.impl.OMTextImpl;
import org.apache.axis.om.impl.SOAPBodyElementImpl;
import org.apache.axis.om.impl.SOAPBodyImpl;
import org.apache.axis.om.impl.SOAPEnvelopeImpl;
import org.apache.axis.om.impl.SOAPHeaderElementImpl;
import org.apache.axis.om.impl.SOAPHeaderImpl;
import org.apache.axis.om.impl.SOAPMessageImpl;
import org.apache.axis.om.soap.*;

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
 * Date: Nov 11, 2004
 * Time: 2:21:13 PM
 */
public class OMLinkedListImplFactory extends OMFactory {
    public OMAttribute createOMAttribute(String localName, OMNamespace ns, String value, OMElement parent) {
        return new OMAttributeImpl(localName, ns, value, parent);
    }

    public OMAttribute createOMAttribute(String localName, OMNamespace ns, String value) {
        return new OMAttributeImpl(localName, ns, value);
    }

    public OMElement createOMElement(OMElement parent) {
        return new OMElementImpl(parent);
    }

    public OMElement createOMElement(String localName, OMNamespace ns) {
        return new OMElementImpl(localName, ns);
    }

    public OMElement createOMElement(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new OMElementImpl(localName, ns, parent, builder);
    }

    public OMNamedNode createOMNamedNode(String localName, OMNamespace ns, OMElement parent) {
        return new OMNamedNodeImpl(localName, ns, parent);
    }

    public OMNamedNode createOMNamedNode(OMElement parent) {
        return new OMNamedNodeImpl(parent);
    }

    public OMNamespace createOMNamespace(String uri, String prefix) {
        return new OMNamespaceImpl(uri, prefix);
    }

    public OMNode createOMNode(OMElement parent) {
        return new OMNodeImpl(parent);
    }

    public OMText createText(OMElement parent, String text) {
        return new OMTextImpl(parent, text);
    }

    public OMText createText(String s) {
        return new OMTextImpl(s);
    }

    public SOAPBodyElement createSOAPBodyElement(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPBodyElementImpl(localName, ns, parent, builder);
    }

    public SOAPBodyElement createSOAPBodyElement(String localName, OMNamespace ns) {
        return new SOAPBodyElementImpl(localName, ns);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope) {
        return new SOAPBodyImpl(envelope);
    }

    public SOAPBody createSOAPBody(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPBodyImpl(localName, ns, parent, builder);
    }

    public SOAPEnvelope createSOAPEnvelope(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPEnvelopeImpl(localName, ns, parent, builder);
    }

    public SOAPEnvelope createSOAPEnvelope(String localName, OMNamespace ns) {
        return new SOAPEnvelopeImpl(localName, ns);
    }

    public SOAPHeader createHeader(SOAPEnvelope envelope) {
        return new SOAPHeaderImpl(envelope);
    }

    public SOAPHeader createHeader(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPHeaderImpl(localName, ns, parent, builder);
    }

    public SOAPHeaderElement createSOAPHeaderElement(String localName, OMNamespace ns) {
        return new SOAPHeaderElementImpl(localName, ns);
    }

    public SOAPHeaderElement createSOAPHeaderElement(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPHeaderElementImpl(localName, ns, parent, builder);
    }

    public SOAPMessage createSOAPMessage(OMXMLParserWrapper parserWrapper) {
        return new SOAPMessageImpl(parserWrapper);
    }
}
