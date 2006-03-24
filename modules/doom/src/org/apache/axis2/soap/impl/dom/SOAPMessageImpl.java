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

package org.apache.axis2.soap.impl.dom;

import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.OMOutputImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.om.impl.dom.DocumentImpl;

import javax.xml.stream.XMLStreamException;

public class SOAPMessageImpl extends DocumentImpl implements SOAPMessage {

    public SOAPMessageImpl(SOAPFactory factory) {
        super(factory);
    }

    public SOAPMessageImpl(SOAPEnvelope envelope,
            OMXMLParserWrapper parserWrapper, SOAPFactory factory) {
    	this(parserWrapper, factory);
    	this.setSOAPEnvelope(envelope);
    }

    public SOAPMessageImpl(OMXMLParserWrapper parserWrapper, SOAPFactory factory) {
        super(factory);
        this.builder = parserWrapper;
    }


    public SOAPEnvelope getSOAPEnvelope() throws SOAPProcessingException {
        return (SOAPEnvelope) getOMDocumentElement();
    }

    public void setSOAPEnvelope(SOAPEnvelope envelope)
            throws SOAPProcessingException {
    	this.addChild(envelope);
    }

    protected void serialize(OMOutputImpl omOutput, boolean cache,
            boolean includeXMLDeclaration) throws XMLStreamException {
        if (cache) {
            ((OMNodeEx)this.ownerNode.getDocumentElement()).serialize(omOutput);
        } else {
        	((OMNodeEx)this.ownerNode.getDocumentElement()).serializeAndConsume(omOutput);
        }
    }
}
