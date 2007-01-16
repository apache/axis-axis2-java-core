/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.json;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.JScriptConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;


public class JSONMessageFormatter implements MessageFormatter {

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
            String contentType;
            String encoding = format.getCharSetEncoding();
            if (msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
                contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
            } else {
                contentType = JScriptConstants.MEDIA_TYPE_APPLICATION_JSON;
            }

            if (encoding != null) {
                contentType += "; charset=" + encoding;
            }
            return contentType;
    }

    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format) throws AxisFault {
    	OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            BadgerFishXMLStreamWriter jsonWriter = new BadgerFishXMLStreamWriter(new OutputStreamWriter(bytesOut));
            element.serializeAndConsume(jsonWriter);
            jsonWriter.writeEndDocument();

            return bytesOut.toByteArray();

        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
    }

    public String formatSOAPAction(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
        return null;
    }

    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
			OutputStream out, boolean preserve) throws AxisFault {
    	OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();
    	BadgerFishXMLStreamWriter jsonWriter = new BadgerFishXMLStreamWriter(new OutputStreamWriter(out));
        try {
            element.serializeAndConsume(jsonWriter);
            jsonWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }
    }

    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format, URL targetURL) {
        return targetURL;
    }

}
