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
package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Formates the request message as application/xml
 */
public class ApplicationXMLFormatter implements MessageFormatter {

    public byte[] getBytes(MessageContext messageContext, OMOutputFormat format) throws AxisFault {

        OMElement omElement;

        if (messageContext.getFLOW() == MessageContext.OUT_FAULT_FLOW) {
            SOAPFault fault = messageContext.getEnvelope().getBody().getFault();
            SOAPFaultDetail soapFaultDetail = fault.getDetail();
            omElement = soapFaultDetail.getFirstElement();

            if (omElement == null) {
                omElement = fault.getReason();
            }

        } else {
            omElement = messageContext.getEnvelope().getBody().getFirstElement();
        }
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

        if (omElement != null) {

            try {
                omElement.serializeAndConsume(bytesOut, format);
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            }

            return bytesOut.toByteArray();
        }

        return new byte[0];
    }

    public void writeTo(MessageContext messageContext, OMOutputFormat format,
                        OutputStream outputStream, boolean preserve) throws AxisFault {

        try {
            byte[] b = getBytes(messageContext, format);

            if (b != null && b.length > 0) {
                outputStream.write(b);
            } else {
                outputStream.flush();
            }
        } catch (IOException e) {
            throw new AxisFault("An error occured while writing the request");
        }
    }

    public String getContentType(MessageContext messageContext, OMOutputFormat format,
                                 String soapAction) {

        String encoding = format.getCharSetEncoding();
        String contentType;
        contentType = (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);

        if (contentType == null) {
            contentType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        }

        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        // if soap action is there (can be there is soap response MEP is used) add it.
        if ((soapAction != null)
                && !"".equals(soapAction.trim())
                && !"\"\"".equals(soapAction.trim())) {
            contentType = contentType + ";action=\"" + soapAction + "\";";
        }

        return contentType;
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat format, URL targetURL)
            throws AxisFault {

        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);

        return targetURL;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat format,
                                   String soapAction) {
        return soapAction;
    }
}
