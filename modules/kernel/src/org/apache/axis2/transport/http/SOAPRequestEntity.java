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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MIMEOutputUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.httpclient.methods.RequestEntity;

public class SOAPRequestEntity implements RequestEntity {
    private byte[] bytes;
    private boolean chunked;
    private OMElement element;
    private MessageContext msgCtxt;
    private String soapActionString;
    private OMOutputFormat format;
    private boolean isAllowedRetry;

    public SOAPRequestEntity(OMElement element, boolean chunked, MessageContext msgCtxt,
                             String soapActionString,
                             OMOutputFormat format,
                             boolean isAllowedRetry) {
        this.element = element;
        this.chunked = chunked;
        this.msgCtxt = msgCtxt;
        this.soapActionString = soapActionString;
        this.format = format;
        this.isAllowedRetry = isAllowedRetry;
    }

    private void handleOMOutput(OutputStream out)
            throws XMLStreamException {
        if (!(format.isOptimized()) & format.isDoingSWA()) {
            StringWriter bufferedSOAPBody = new StringWriter();
            if (isAllowedRetry) {
                element.serialize(bufferedSOAPBody, format);
            } else {
                element.serializeAndConsume(bufferedSOAPBody, format);
            }
            MIMEOutputUtils.writeSOAPWithAttachmentsMessage(bufferedSOAPBody, out, msgCtxt.getAttachmentMap(), format);
        } else {
            if (isAllowedRetry) {
                element.serialize(out, format);
            } else {
                element.serializeAndConsume(out, format);
            }
        }
    }

    public byte[] writeBytes() throws AxisFault {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            if (!format.isOptimized()) {
                // why are we creating a new OMOutputFormat
                OMOutputFormat format2 = new OMOutputFormat();
                format2.setCharSetEncoding(format.getCharSetEncoding());
                if (format.isDoingSWA()) {
                    StringWriter bufferedSOAPBody = new StringWriter();
                    element.serializeAndConsume(bufferedSOAPBody, format2);
                    MIMEOutputUtils.writeSOAPWithAttachmentsMessage(bufferedSOAPBody, bytesOut, msgCtxt.getAttachmentMap(), format2);
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

    public void writeRequest(OutputStream out) throws IOException {
        Object gzip = msgCtxt.getOptions().getProperty(HTTPConstants.MC_GZIP_REQUEST);
        if (gzip != null && JavaUtils.isTrueExplicitly(gzip) && chunked) {
            out = new GZIPOutputStream(out);
        }
        try {
            if (chunked) {
                this.handleOMOutput(out);
            } else {
                if (bytes == null) {
                    bytes = writeBytes();
                }
                out.write(bytes);
            }
            if (out instanceof GZIPOutputStream) {
                ((GZIPOutputStream) out).finish();
            }
            out.flush();
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public long getContentLength() {
        try {
            if (chunked) {
                return -1;
            } else {
                if (bytes == null) {
                    bytes = writeBytes();
                }
                return bytes.length;
            }
        } catch (AxisFault e) {
            return -1;
        }
    }

    public String getContentType() {
        String encoding = format.getCharSetEncoding();
        String contentType = format.getContentType();
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        // action header is not mandated in SOAP 1.2. So putting it, if available
        if (!msgCtxt.isSOAP11() && (soapActionString != null)
                && !"".equals(soapActionString.trim()) && !"\"\"".equals(soapActionString.trim())) {
            contentType = contentType + ";action=\"" + soapActionString + "\";";
        }
        return contentType;
    }

    public boolean isRepeatable() {
        return true;
    }
}
