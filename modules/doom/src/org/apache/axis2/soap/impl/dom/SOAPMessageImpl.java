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

import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.OMNodeEx;
import org.apache.ws.commons.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPMessage;
import org.apache.ws.commons.soap.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;

public class SOAPMessageImpl extends DocumentImpl implements SOAPMessage {

    public SOAPMessageImpl() {
    }

    public SOAPMessageImpl(SOAPEnvelope envelope, OMXMLParserWrapper parserWrapper) {
    	this(parserWrapper);
    	this.setSOAPEnvelope(envelope);
    }

    public SOAPMessageImpl(OMXMLParserWrapper parserWrapper) {
        super();
        this.builder = parserWrapper;
    }


    public SOAPEnvelope getSOAPEnvelope() throws SOAPProcessingException {
        return (SOAPEnvelope) getOMDocumentElement();
    }

    public void setSOAPEnvelope(SOAPEnvelope envelope) throws SOAPProcessingException {
    	this.addChild(envelope);
    }

    protected void serialize(OMOutputImpl omOutput, boolean cache, boolean includeXMLDeclaration) throws XMLStreamException {
        if (cache) {
            ((OMNodeEx)this.ownerNode.getDocumentElement()).serialize(omOutput);
        } else {
        	((OMNodeEx)this.ownerNode.getDocumentElement()).serializeAndConsume(omOutput);
        }
    }
}
