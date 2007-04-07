/*
 * Copyright 2006,2007 The Apache Software Foundation.
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

package org.apache.axis2.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

public class SOAPBuilder implements Builder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        XMLStreamReader streamReader;
        try {
            String charSetEncoding = (String) messageContext
                    .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            streamReader = StAXUtils.createXMLStreamReader(BuilderUtil.getReader(inputStream,
                                                                                 charSetEncoding));
            StAXBuilder builder = new StAXSOAPModelBuilder(streamReader);
            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();
            BuilderUtil
                    .validateSOAPVersion(BuilderUtil.getEnvelopeNamespace(contentType), envelope);
            BuilderUtil.validateCharSetEncoding(charSetEncoding, builder.getDocument()
                    .getCharsetEncoding(), envelope.getNamespace().getNamespaceURI());
            return envelope;
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
