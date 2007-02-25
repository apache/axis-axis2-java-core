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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.MalformedURLException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MIMEOutputUtils;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.axis2.util.JavaUtils;

public class SOAPMessageFormatter implements MessageFormatter{

    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
            OutputStream out, boolean preserve) throws AxisFault {
        OMElement element = msgCtxt.getEnvelope();
        try {
            if (!(format.isOptimized()) & format.isDoingSWA()) {
                StringWriter bufferedSOAPBody = new StringWriter();
                if (preserve) {
                    element.serialize(bufferedSOAPBody, format);
                } else {
                    element.serializeAndConsume(bufferedSOAPBody, format);
                }
                writeSwAMessage(msgCtxt,bufferedSOAPBody,out,format);
            } else {
                if (preserve) {
                    element.serialize(out, format);
                } else {
                    element.serializeAndConsume(out, format);
                }
            }
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }
    }

    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format)
            throws AxisFault {
        OMElement element = msgCtxt.getEnvelope();
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            if (!format.isOptimized()) {
                // why are we creating a new OMOutputFormat
                OMOutputFormat format2 = new OMOutputFormat();
                format2.setCharSetEncoding(format.getCharSetEncoding());
                if (format.isDoingSWA()) {
                    StringWriter bufferedSOAPBody = new StringWriter();
                    element.serializeAndConsume(bufferedSOAPBody, format2);
                    writeSwAMessage(msgCtxt,bufferedSOAPBody,bytesOut,format);
                } else {
                    element.serializeAndConsume(bytesOut, format2);
                }
                return bytesOut.toByteArray();
            } else {
                element.serializeAndConsume(bytesOut, format);
                return bytesOut.toByteArray();
            }
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
    }

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format,
            String soapActionString) {
        String encoding = format.getCharSetEncoding();
        String contentType = format.getContentType();
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        // action header is not mandated in SOAP 1.2. So putting it, if
        // available
        if (!msgCtxt.isSOAP11() && (soapActionString != null)
                && !"".equals(soapActionString.trim())
                && !"\"\"".equals(soapActionString.trim())) {
            contentType = contentType + ";action=\"" + soapActionString + "\";";
        }
        return contentType;
    }

    public String formatSOAPAction(MessageContext msgCtxt, OMOutputFormat format,
            String soapActionString) {
        // if SOAP 1.2 we attach the soap action to the content-type
        // No need to set it as a header.
        if (msgCtxt.isSOAP11()) {
            if ("".equals(soapActionString)) {
                return "\"\"";
            } else {
                if (soapActionString != null
                        && !soapActionString.startsWith("\"")) { 
                    // SOAPAction string must be a quoted string
                    soapActionString = "\"" + soapActionString + "\"";
                }
                return soapActionString;
            }
        }
        return null;
    }

    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format,
            URL targetURL) throws AxisFault{

        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL,msgCtxt,false);
        return targetURL;
    }
    
    private void writeSwAMessage(MessageContext msgCtxt,
            StringWriter bufferedSOAPBody, OutputStream outputStream,
            OMOutputFormat format) {
        Object property = msgCtxt
                .getProperty(Constants.Configuration.MM7_COMPATIBLE);
        boolean MM7CompatMode = false;
        if (property != null) {
            MM7CompatMode = JavaUtils.isTrueExplicitly(property);
        }
        if (!MM7CompatMode) {
            MIMEOutputUtils.writeSOAPWithAttachmentsMessage(bufferedSOAPBody,
                    outputStream, msgCtxt.getAttachmentMap(), format);
        } else {
            String innerBoundary;
            String partCID;
            Object innerBoundaryProperty = msgCtxt
                    .getProperty(Constants.Configuration.MM7_INNER_BOUNDARY);
            if (innerBoundaryProperty != null) {
                innerBoundary = (String) innerBoundaryProperty;
            } else {
                innerBoundary = "innerBoundary"
                        + UUIDGenerator.getUUID().replace(':', '_');
            }
            Object partCIDProperty = msgCtxt
                    .getProperty(Constants.Configuration.MM7_PART_CID);
            if (partCIDProperty != null) {
                partCID = (String) partCIDProperty;
            } else {
                partCID = "innerCID"
                        + UUIDGenerator.getUUID().replace(':', '_');
            }
            MIMEOutputUtils.writeMM7Message(bufferedSOAPBody, outputStream,
                    msgCtxt.getAttachmentMap(), format, partCID, innerBoundary);
        }
    }
    
}
