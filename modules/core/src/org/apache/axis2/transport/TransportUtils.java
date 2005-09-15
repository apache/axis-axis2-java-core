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
package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

public class TransportUtils {
   public static SOAPEnvelope createSOAPMessage(MessageContext msgContext, String soapNamespaceURI) throws AxisFault {

        InputStream inStream = (InputStream) msgContext.getProperty(
                MessageContext.TRANSPORT_IN);
        msgContext.setProperty(MessageContext.TRANSPORT_IN, null);
        //this inputstram is set by the TransportSender represents a two way transport or
        //by a Transport Recevier
        if (inStream == null) {
            throw new AxisFault(Messages.getMessage("inputstreamNull"));
        }
        return createSOAPMessage(msgContext, inStream, soapNamespaceURI);
    }

    private static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
                                                 InputStream inStream, String soapNamespaceURI)
            throws AxisFault {
        try {
            Object contentType = null;
            OperationContext opContext = msgContext.getOperationContext();
            if (opContext != null) {
                contentType =
                        opContext.getProperty(
                                HTTPConstants.MTOM_RECIVED_CONTENT_TYPE);
            } else {
                throw new AxisFault(Messages.getMessage("cannotBeNullOperationContext"));
            }

            StAXBuilder builder = null;
            SOAPEnvelope envelope = null;

            String charSetEnc = (String)msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);
            if(charSetEnc == null) {
            	charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            
			if (contentType != null) {
                msgContext.setDoingMTOM(true);
                builder =
                        HTTPTransportUtils.selectBuilderForMIME(msgContext,
                                inStream,
                                (String) contentType);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            } else if (msgContext.isDoingREST()) {
                XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(
                                inStream,charSetEnc);
                SOAPFactory soapFactory = new SOAP11Factory();
                builder = new StAXOMBuilder(xmlreader);
                builder.setOmbuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
            } else {
                XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(
                        		inStream,charSetEnc);
                builder = new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }
            return envelope;
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

}
