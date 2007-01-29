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
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;


public class JSONMessageFormatter implements MessageFormatter {

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
        String contentType;
        String encoding = format.getCharSetEncoding();
        if (msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
            contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
        } else {
            contentType = (String) msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE);

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
            XMLStreamWriter jsonWriter = getJSONWriter(bytesOut);
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

    protected XMLStreamWriter getJSONWriter(OutputStream outStream) {
        MappedNamespaceConvention mnc = new MappedNamespaceConvention();
        return new MappedXMLStreamWriter(mnc, new OutputStreamWriter(outStream));
    }

    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
                        OutputStream out, boolean preserve) throws AxisFault {
        OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();
        XMLStreamWriter jsonWriter = getJSONWriter(out);

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
