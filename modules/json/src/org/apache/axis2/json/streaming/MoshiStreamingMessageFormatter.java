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

package org.apache.axis2.json.streaming;

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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.json.moshi.JsonHtmlEncoder;
import org.apache.axis2.json.moshi.MoshiXMLStreamWriter;
import org.apache.axis2.kernel.MessageFormatter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Streaming Moshi JSON message formatter for Axis2.
 *
 * <p>Drop-in replacement for {@link org.apache.axis2.json.moshi.JsonFormatter}
 * that wraps the transport {@link OutputStream} with a
 * {@link FlushingOutputStream}. This pushes data to the HTTP transport
 * layer every N bytes (default 64 KB), converting a single buffered
 * response into a stream of HTTP/2 DATA frames or HTTP/1.1 chunks.</p>
 *
 * <h3>Usage</h3>
 *
 * <p>In {@code axis2.xml} (global) or {@code services.xml} (per-service):</p>
 *
 * <pre>{@code
 * <messageFormatter contentType="application/json"
 *     class="org.apache.axis2.json.streaming.MoshiStreamingMessageFormatter"/>
 * }</pre>
 *
 * <p>Optional flush interval tuning:</p>
 *
 * <pre>{@code
 * <parameter name="streamingFlushIntervalBytes">131072</parameter>
 * }</pre>
 *
 * @see FlushingOutputStream
 * @see org.apache.axis2.json.streaming.JSONStreamingMessageFormatter
 * @since 2.0.1
 */
public class MoshiStreamingMessageFormatter implements MessageFormatter {

    private static final Log log = LogFactory.getLog(MoshiStreamingMessageFormatter.class);

    /** services.xml parameter name for flush interval override */
    private static final String PARAM_FLUSH_INTERVAL = "streamingFlushIntervalBytes";

    public void writeTo(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat,
                        OutputStream outputStream, boolean preserve) throws AxisFault {

        // Wrap the transport OutputStream with periodic flushing.
        int flushInterval = getFlushInterval(outMsgCtxt);
        OutputStream flushingStream = new FlushingOutputStream(outputStream, flushInterval);

        if (log.isDebugEnabled()) {
            log.debug("MoshiStreamingMessageFormatter: using FlushingOutputStream with "
                + flushInterval + " byte flush interval");
        }

        Moshi moshi = new Moshi.Builder()
            .add(String.class, new JsonHtmlEncoder())
            .add(Date.class, new Rfc3339DateJsonAdapter())
            .build();
        JsonAdapter<Object> adapter = moshi.adapter(Object.class);

        try (BufferedSink sink = Okio.buffer(Okio.sink(flushingStream));
             JsonWriter jsonWriter = JsonWriter.of(sink)) {

            Object retObj = outMsgCtxt.getProperty(JsonConstant.RETURN_OBJECT);

            if (outMsgCtxt.isProcessingFault()) {
                writeFaultResponse(outMsgCtxt, jsonWriter);

            } else if (retObj == null) {
                writeElementResponse(outMsgCtxt, jsonWriter, preserve);

            } else {
                writeObjectResponse(jsonWriter, adapter, retObj, outMsgCtxt);
            }

            jsonWriter.flush();
            log.debug("MoshiStreamingMessageFormatter.writeTo() completed");

        } catch (IOException e) {
            String msg = "Error in MoshiStreamingMessageFormatter";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Write a SOAP fault as JSON.
     */
    private void writeFaultResponse(MessageContext outMsgCtxt, JsonWriter jsonWriter)
            throws AxisFault {
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
        } catch (IOException e) {
            throw new AxisFault("Error writing fault response in MoshiStreamingMessageFormatter", e);
        }
    }

    /**
     * Write an OM element response (schema-driven serialization).
     */
    private void writeElementResponse(MessageContext outMsgCtxt, JsonWriter jsonWriter,
                                      boolean preserve) throws AxisFault {
        OMElement element = outMsgCtxt.getEnvelope().getBody().getFirstElement();
        QName elementQname = outMsgCtxt.getAxisOperation().getMessage(
            WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getElementQName();

        ArrayList<XmlSchema> schemas = outMsgCtxt.getAxisService().getSchema();
        MoshiXMLStreamWriter xmlsw = new MoshiXMLStreamWriter(jsonWriter,
            elementQname, schemas, outMsgCtxt.getConfigurationContext());
        try {
            xmlsw.writeStartDocument();
            element.serialize(xmlsw, preserve);
            xmlsw.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new AxisFault("Error writing element response in MoshiStreamingMessageFormatter", e);
        }
    }

    /**
     * Write a return-object response using Moshi.
     *
     * <p>Moshi serializes the object graph field-by-field into the JsonWriter.
     * The JsonWriter is backed by an Okio sink wrapping a
     * {@link FlushingOutputStream}, so the HTTP transport receives chunks
     * as serialization progresses.</p>
     */
    private void writeObjectResponse(JsonWriter jsonWriter, JsonAdapter<Object> adapter,
                                     Object retObj, MessageContext outMsgCtxt) throws AxisFault {
        try {
            jsonWriter.beginObject();
            jsonWriter.name(JsonConstant.RESPONSE);
            adapter.toJson(jsonWriter, retObj);
            jsonWriter.endObject();

        } catch (IOException e) {
            String msg = "Error writing object response in MoshiStreamingMessageFormatter";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Read the flush interval from the service's configuration.
     */
    private int getFlushInterval(MessageContext msgCtxt) {
        AxisService service = msgCtxt.getAxisService();
        if (service != null) {
            Parameter param = service.getParameter(PARAM_FLUSH_INTERVAL);
            if (param != null) {
                try {
                    int interval = Integer.parseInt(param.getValue().toString().trim());
                    if (interval > 0) {
                        return interval;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid " + PARAM_FLUSH_INTERVAL + " value: "
                        + param.getValue() + "; using default");
                }
            }
        }
        return FlushingOutputStream.DEFAULT_FLUSH_INTERVAL;
    }

    public String getContentType(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat,
                                 String soapAction) {
        return (String) outMsgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat omOutputFormat,
                                URL url) throws AxisFault {
        return null;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat,
                                   String soapAction) {
        return null;
    }
}
