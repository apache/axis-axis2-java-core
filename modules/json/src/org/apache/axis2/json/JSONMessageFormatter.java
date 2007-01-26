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
import org.apache.axis2.JScriptConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;


public class JSONMessageFormatter implements MessageFormatter {

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
        String contentType;
        String encoding = format.getCharSetEncoding();
        if (msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
            contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
        } else {
            if (msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE).equals(JScriptConstants.MEDIA_TYPE_APPLICATION_JSON_BADGERFISH)) {
                contentType = JScriptConstants.MEDIA_TYPE_APPLICATION_JSON_BADGERFISH;
            } else {
                contentType = JScriptConstants.MEDIA_TYPE_APPLICATION_JSON;
            }

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
            XMLStreamWriter jsonWriter;

//            if (msgCtxt.getProperty(JScriptConstants.JSON_CONVENTION) != null && msgCtxt.getProperty(JScriptConstants.JSON_CONVENTION).equals(JScriptConstants.BADGERFISH)) {
            if (msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE).equals(JScriptConstants.MEDIA_TYPE_APPLICATION_JSON_BADGERFISH)) {
                jsonWriter = new BadgerFishXMLStreamWriter(new OutputStreamWriter(bytesOut));
            } else {
                MappedNamespaceConvention mnc = new MappedNamespaceConvention();
                jsonWriter = new MappedXMLStreamWriter(mnc, new OutputStreamWriter(bytesOut));
            }

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

        XMLStreamWriter jsonWriter;

//        if (msgCtxt.getProperty(JScriptConstants.JSON_CONVENTION) != null && msgCtxt.getProperty(JScriptConstants.JSON_CONVENTION).equals(JScriptConstants.BADGERFISH)) {
        if (msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE).equals(JScriptConstants.MEDIA_TYPE_APPLICATION_JSON_BADGERFISH)) {
            jsonWriter = new BadgerFishXMLStreamWriter(new OutputStreamWriter(out));
        } else {
            MappedNamespaceConvention mnc = new MappedNamespaceConvention();
            jsonWriter = new MappedXMLStreamWriter(mnc, new OutputStreamWriter(out));
        }

        try {
            element.serializeAndConsume(jsonWriter);
            jsonWriter.writeEndDocument();

//            jsonWriter.close();

        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }
    }

    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format, URL targetURL) {
        return targetURL;
    }

}
