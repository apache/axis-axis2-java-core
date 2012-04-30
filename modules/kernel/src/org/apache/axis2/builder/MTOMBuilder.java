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

package org.apache.axis2.builder;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

import java.io.InputStream;

public class MTOMBuilder implements MIMEAwareBuilder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        throw new AxisFault("A message with content type application/xop+xml can only appear in a MIME multipart message");
    }
    
    public OMElement processMIMEMessage(Attachments attachments, String contentType,
            MessageContext messageContext) throws AxisFault {
        try {
            // TODO: this will be changed later (see AXIS2-5308)
            messageContext.setAttachmentMap(attachments);
            
            SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(attachments);
            OMDocument document = builder.getDocument();
            String charsetEncoding = document.getCharsetEncoding();
            if (charsetEncoding == null) {
                charsetEncoding = MessageContext.UTF_8;
            }
            messageContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                                       charsetEncoding);
            
            SOAPEnvelope envelope = (SOAPEnvelope)document.getOMDocumentElement();
            
            // TODO: Axiom should take care of this, but we need to validate that
//            BuilderUtil
//                    .validateSOAPVersion(BuilderUtil.getEnvelopeNamespace(contentType), envelope);
//            BuilderUtil.validateCharSetEncoding(charSetEncoding, builder.getDocument()
//                    .getCharsetEncoding(), envelope.getNamespace().getNamespaceURI());
            
            messageContext.setDoingMTOM(true);
            return envelope;
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
