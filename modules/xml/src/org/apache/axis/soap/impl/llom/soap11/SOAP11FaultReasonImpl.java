package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis.om.impl.llom.OMSerializerUtil;
import org.apache.axis.soap.impl.llom.SOAPFaultReasonImpl;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.soap.SOAPFaultText;
import org.apache.axis.soap.SOAPFault;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class SOAP11FaultReasonImpl extends SOAPFaultReasonImpl {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

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
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Text. But received some other implementation");
        }
        super.setSOAPText(soapFaultText);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }

    protected void serialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException {

        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(writer));
        }


        if (this.getNamespace() != null) {
           String prefix = this.getNamespace().getPrefix();
        String nameSpaceName = this.getNamespace().getName();
        writer.writeStartElement(prefix, SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME,
                                nameSpaceName);
        }else{
            writer.writeStartElement(SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME);
        }
        OMSerializerUtil.serializeAttributes(this, writer);
        OMSerializerUtil.serializeNamespaces(this, writer);

        String text = this.getSOAPText().getText();
        writer.writeCharacters(text);
        writer.writeEndElement();

        //serilize siblings
        if (this.nextSibling != null) {
            nextSibling.serialize(writer);
        } else if (this.parent != null) {
            if (!this.parent.isComplete()) {
                builder.setCache(cache);
                builder.next();
            }
        }

    }

    public String getLocalName() {
        return SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME;
    }
}
