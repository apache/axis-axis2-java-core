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

package org.apache.axis2.soap.impl.dom.soap11;

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.OMOutputImpl;
import org.apache.ws.commons.om.impl.llom.OMSerializerUtil;
import org.apache.ws.commons.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.ws.commons.soap.SOAPFaultText;
import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.axis2.soap.impl.dom.SOAPFaultReasonImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SOAP11FaultReasonImpl extends SOAPFaultReasonImpl {

	public SOAP11FaultReasonImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    /**
     * @param parent
     */
    public SOAP11FaultReasonImpl(SOAPFault parent) throws SOAPProcessingException {
        super(parent, false);
    }

    public void setSOAPText(SOAPFaultText soapFaultText) throws SOAPProcessingException {
        if (!(soapFaultText instanceof SOAP11FaultTextImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Text. But received some other implementation");
        }
        super.setSOAPText(soapFaultText);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }

    protected void serialize(OMOutputImpl omOutput, boolean cache) throws XMLStreamException {

        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(omOutput));
        }

        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (this.getNamespace() != null) {
            String prefix = this.getNamespace().getPrefix();
            String nameSpaceName = this.getNamespace().getName();
            writer.writeStartElement(prefix, SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME,
                    nameSpaceName);
        } else {
            writer.writeStartElement(
                    SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME);
        }
        OMSerializerUtil.serializeAttributes(this, omOutput);
        OMSerializerUtil.serializeNamespaces(this, omOutput);

        String text = this.getSOAPText().getText();
        writer.writeCharacters(text);
        writer.writeEndElement();
    }

}
