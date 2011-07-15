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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * This builder is used when the serialization of the message is application/xml.
 */
public class ApplicationXMLBuilder implements Builder {

    /**
     * @return Returns the document element.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        if (inputStream != null) {
            try {
                // Apply a detachable inputstream.  This can be used later
                // to (a) get the length of the incoming message or (b)
                // free transport resources.
                DetachableInputStream is = new DetachableInputStream(inputStream);
                messageContext.setProperty(Constants.DETACHABLE_INPUT_STREAM, is);
                
                PushbackInputStream pushbackInputStream = new PushbackInputStream(is);
                int b;
                if ((b = pushbackInputStream.read()) > 0) {
                    pushbackInputStream.unread(b);
                    OMXMLParserWrapper builder =
                            BuilderUtil.createPOXBuilder(pushbackInputStream,
                                    (String) messageContext.getProperty(
                                            Constants.Configuration.CHARACTER_SET_ENCODING));
                    OMElement documentElement = builder.getDocumentElement(true);
                    SOAPBody body = soapEnvelope.getBody();
                    body.addChild(documentElement);
                }

            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        }
        return soapEnvelope;
    }

}
