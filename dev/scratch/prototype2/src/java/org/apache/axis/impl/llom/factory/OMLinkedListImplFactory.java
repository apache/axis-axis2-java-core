package org.apache.axis.impl.llom.factory;

import org.apache.axis.impl.llom.OMAttributeImpl;
import org.apache.axis.impl.llom.OMElementImpl;
import org.apache.axis.impl.llom.OMNamedNodeImpl;
import org.apache.axis.impl.llom.OMNamespaceImpl;
import org.apache.axis.impl.llom.OMNodeImpl;
import org.apache.axis.impl.llom.OMTextImpl;
import org.apache.axis.impl.llom.SOAPBodyImpl;
import org.apache.axis.impl.llom.SOAPEnvelopeImpl;
import org.apache.axis.impl.llom.SOAPHeaderBlockImpl;
import org.apache.axis.impl.llom.SOAPHeaderImpl;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamedNode;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.SOAPHeader;
import org.apache.axis.om.SOAPHeaderBlock;

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

    public SOAPBody createOMBody(SOAPEnvelope envelope) {
        return new SOAPBodyImpl(envelope);
    }

    public SOAPBody createOMBody(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPBodyImpl(localName, ns, parent, builder);
    }

    public SOAPEnvelope createOMEnvelope(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPEnvelopeImpl(localName, ns, parent, builder);
    }

    public SOAPEnvelope createOMEnvelope(String localName, OMNamespace ns) {
        return new SOAPEnvelopeImpl(localName, ns);
    }

    public SOAPEnvelope createOMEnvelope(OMXMLParserWrapper parserWrapper) {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    //TODO there should be a method to create an SOAPEnvelope giving OMXMLParserWrapper, as OMMessage is no longer there

    public SOAPHeader createHeader(SOAPEnvelope envelope) {
        return new SOAPHeaderImpl(envelope);
    }

    public SOAPHeader createHeader(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPHeaderImpl(localName, ns, parent, builder);
    }

    public SOAPHeaderBlock createOMHeaderBlock(String localName, OMNamespace ns) {
        return new SOAPHeaderBlockImpl(localName, ns);
    }

    public SOAPHeaderBlock createOMHeaderBlock(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPHeaderBlockImpl(localName, ns, parent, builder);
    }

    public SOAPEnvelope getDefaultEnvelope() {
        //Create an envelop
        OMNamespace ns = new OMNamespaceImpl(OMConstants.SOAP_ENVELOPE_NAMESPACE_URI,OMConstants.SOAPENVELOPE_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(OMConstants.SOAPENVELOPE_LOCAL_NAME,ns);
        
        SOAPBodyImpl bodyImpl = new SOAPBodyImpl(env);
        bodyImpl.setComplete(true);
        env.addChild(bodyImpl);
        
        SOAPHeaderImpl headerImpl = new SOAPHeaderImpl(env);
        headerImpl.setComplete(true);
        env.addChild(headerImpl);
        
        return env;
        


    }

}
