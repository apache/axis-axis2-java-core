package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.soap.SOAPFaultDetail;
import org.apache.axis.soap.SOAPFault;
import org.apache.axis.soap.impl.llom.SOAPFaultDetailImpl;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMOutput;
import org.apache.axis.om.impl.llom.OMSerializerUtil;
import org.apache.axis.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis.om.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public class SOAP11FaultDetailImpl extends SOAPFaultDetailImpl {
    public SOAP11FaultDetailImpl(SOAPFault parent) throws SOAPProcessingException {
        super(parent, false);
    }

    public SOAP11FaultDetailImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }

//    public void addDetailEntry(OMElement detailElement) {
//        throw new UnsupportedOperationException();
//    }
//
//    public Iterator getAllDetailEntries() {
//        throw new UnsupportedOperationException();
//    }

     public void serialize(OMOutput omOutput, boolean cache) throws XMLStreamException {

        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(omOutput));
        }
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (this.getNamespace() != null) {
           String prefix = this.getNamespace().getPrefix();
        String nameSpaceName = this.getNamespace().getName();
        writer.writeStartElement(prefix, SOAP11Constants.SOAP_FAULT_DETAIL_LOCAL_NAME,
                                nameSpaceName);
        }else{
            writer.writeStartElement(SOAP11Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
        }
        OMSerializerUtil.serializeAttributes(this, omOutput);
        OMSerializerUtil.serializeNamespaces(this, omOutput);


        String text = this.getText();
        writer.writeCharacters(text);


        if (firstChild != null) {
            firstChild.serialize(omOutput);
        }
        writer.writeEndElement();

        //serilize siblings
        if (this.nextSibling != null) {
            nextSibling.serialize(omOutput);
        }

    }

    
}
