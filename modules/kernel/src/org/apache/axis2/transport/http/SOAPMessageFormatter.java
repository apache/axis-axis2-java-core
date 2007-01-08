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
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MIMEOutputUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.httpclient.methods.RequestEntity;

public class SOAPMessageFormatter implements MessageFormatter {
	private byte[] bytes;

	private OMElement element;

	private MessageContext msgCtxt;

	private String soapActionString;

	private OMOutputFormat format;
	
	private URL targetURL;

	public SOAPMessageFormatter(MessageContext msgCtxt, String soapActionString,
			OMOutputFormat format, URL targetURL) {
		this.msgCtxt = msgCtxt;
		this.soapActionString = soapActionString;
		this.format = format;
		this.element = msgCtxt.getEnvelope();
		this.targetURL = targetURL;
	}

	public void handleOMOutput(OutputStream out, boolean preserve)
			throws AxisFault {
		try {
			if (!(format.isOptimized()) & format.isDoingSWA()) {
				StringWriter bufferedSOAPBody = new StringWriter();
				if (preserve) {
					element.serialize(bufferedSOAPBody, format);
				} else {
					element.serializeAndConsume(bufferedSOAPBody, format);
				}
				MIMEOutputUtils.writeSOAPWithAttachmentsMessage(
						bufferedSOAPBody, out, msgCtxt.getAttachmentMap(),
						format);
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

	public byte[] getBytes() throws AxisFault {
		try {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			if (!format.isOptimized()) {
				// why are we creating a new OMOutputFormat
				OMOutputFormat format2 = new OMOutputFormat();
				format2.setCharSetEncoding(format.getCharSetEncoding());
				if (format.isDoingSWA()) {
					StringWriter bufferedSOAPBody = new StringWriter();
					element.serializeAndConsume(bufferedSOAPBody, format2);
					MIMEOutputUtils.writeSOAPWithAttachmentsMessage(
							bufferedSOAPBody, bytesOut, msgCtxt
									.getAttachmentMap(), format2);
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

	public long getContentLength() {
		try {
			if (bytes == null) {
				bytes = getBytes();
			}
			return bytes.length;

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
				&& !"".equals(soapActionString.trim())
				&& !"\"\"".equals(soapActionString.trim())) {
			contentType = contentType + ";action=\"" + soapActionString + "\";";
		}
		return contentType;
	}

	public String getSOAPAction() {
		//if SOAP 1.2 we attach the soap action to the content-type
		//No need to set it as a header.
        if (msgCtxt.isSOAP11()) {
            if ("".equals(soapActionString)){
             return  "\"\"";
            }else{
                if (soapActionString != null && !soapActionString.startsWith("\"")) {  // SOAPAction string must be a quoted string
                    soapActionString = "\"" + soapActionString + "\"";
                }
                return soapActionString;
            }
        } 
        return null;
	}

	public URL getTargetAddress() {
		// SOAP do not want to alter the target URL
		return targetURL;
	}
}
