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

package org.apache.axis2.json.moshi;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
import okio.BufferedSink;
import okio.Okio;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;


public class JsonFormatter implements MessageFormatter {
    private static final Log log = LogFactory.getLog(JsonFormatter.class);

    public void writeTo(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat, OutputStream outputStream, boolean preserve) throws AxisFault {
        String charSetEncoding = (String) outMsgCtxt.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
        JsonWriter jsonWriter;
        String msg;

        try {
            Moshi moshi = new Moshi.Builder().add(String.class, new JsonHtmlEncoder()).add(Date.class, new Rfc3339DateJsonAdapter()).build();
            JsonAdapter<Object> adapter = moshi.adapter(Object.class);
            BufferedSink sink = Okio.buffer(Okio.sink(outputStream));
            jsonWriter = JsonWriter.of(sink);

            Object retObj = outMsgCtxt.getProperty(JsonConstant.RETURN_OBJECT);

            if (outMsgCtxt.isProcessingFault()) {
                OMElement element = outMsgCtxt.getEnvelope().getBody().getFirstElement();
                try {
                    jsonWriter.beginObject();
                    jsonWriter.name(element.getLocalName());
                    jsonWriter.beginObject();
                    Iterator childrenIterator = element.getChildElements();
                    while (childrenIterator.hasNext()) {
                        Object next = childrenIterator.next();
                        OMElement omElement = (OMElement) next;
                        jsonWriter.name(omElement.getLocalName());
                        jsonWriter.value(omElement.getText());
                    }
                    jsonWriter.endObject();
                    jsonWriter.endObject();
                    jsonWriter.flush();
                    jsonWriter.close();
                } catch (IOException e) {
                    throw new AxisFault("Error while processing fault code in JsonWriter");
                }

            } else if (retObj == null) {
                OMElement element = outMsgCtxt.getEnvelope().getBody().getFirstElement();
                QName elementQname = outMsgCtxt.getAxisOperation().getMessage
                        (WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getElementQName();

                ArrayList<XmlSchema> schemas = outMsgCtxt.getAxisService().getSchema();
                MoshiXMLStreamWriter xmlsw = new MoshiXMLStreamWriter(jsonWriter,
                                                                    elementQname,
                                                                    schemas,
                                                                    outMsgCtxt.getConfigurationContext());
                try {
                    xmlsw.writeStartDocument();
                    element.serialize(xmlsw, preserve);
                    xmlsw.writeEndDocument();
                } catch (XMLStreamException e) {
                    throw new AxisFault("Error while writing to the output stream using JsonWriter", e);
                }

            } else {
                try {
                    jsonWriter.beginObject();
                    jsonWriter.name(JsonConstant.RESPONSE);
                    Type returnType = (Type) outMsgCtxt.getProperty(JsonConstant.RETURN_TYPE);
                    adapter.toJson(jsonWriter, retObj);
                    jsonWriter.endObject();
                    jsonWriter.flush();

                } catch (IOException e) {
                    msg = "Exception occurred while writting to JsonWriter at the JsonFormatter ";
                    log.error(msg, e);
                    throw AxisFault.makeFault(e);
                }
            }
            log.debug("JsonFormatter.writeTo() has completed");
        } catch (Exception e) {
            msg = "Exception occurred when try to encode output stream using  " +
                    Constants.Configuration.CHARACTER_SET_ENCODING + " charset";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    public String getContentType(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat, String s) {
        return (String)outMsgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat omOutputFormat, URL url) throws AxisFault {
        return null;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        return null;
    }
}
