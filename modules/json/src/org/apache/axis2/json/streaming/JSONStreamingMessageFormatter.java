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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.json.gson.GsonXMLStreamWriter;
import org.apache.axis2.json.gson.JsonHtmlEncoder;
import org.apache.axis2.kernel.MessageFormatter;
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

/**
 * Streaming JSON message formatter for Axis2.
 *
 * <p>Wraps the transport {@link OutputStream} with a
 * {@link FlushingOutputStream} that pushes data to the HTTP transport
 * layer every N bytes (default 64 KB). This converts a single buffered
 * HTTP response into a stream of small chunks — either HTTP/1.1 chunked
 * transfer encoding or HTTP/2 DATA frames — preventing reverse proxy
 * body-size rejections on large responses.</p>
 *
 * <h3>Usage</h3>
 *
 * <p>Drop-in replacement for {@code JsonFormatter}. Enable per-service
 * in {@code services.xml}:</p>
 *
 * <pre>{@code
 * <parameter name="messageFormatters">
 *   <messageFormatter contentType="application/json"
 *       class="org.apache.axis2.json.streaming.JSONStreamingMessageFormatter"/>
 * </parameter>
 * }</pre>
 *
 * <p>Or globally in {@code axis2.xml}:</p>
 *
 * <pre>{@code
 * <messageFormatter contentType="application/json"
 *     class="org.apache.axis2.json.streaming.JSONStreamingMessageFormatter"/>
 * }</pre>
 *
 * <h3>Configuration</h3>
 *
 * <p>The flush interval can be tuned per-service via a parameter:</p>
 *
 * <pre>{@code
 * <parameter name="streamingFlushIntervalBytes">131072</parameter>
 * }</pre>
 *
 * <p>Default is {@value FlushingOutputStream#DEFAULT_FLUSH_INTERVAL} bytes
 * (64 KB). Smaller values increase flush frequency (lower latency to first
 * byte, more HTTP frames); larger values reduce flush overhead at the cost
 * of larger transport buffers.</p>
 *
 * <h3>Design</h3>
 *
 * <p>This formatter does not require service code changes. It wraps the
 * OutputStream before serialization begins — GSON writes to the
 * {@link JsonWriter} → {@link OutputStreamWriter} → {@link FlushingOutputStream}
 * → transport. The service returns its response object as usual; the
 * periodic flushing happens transparently during GSON serialization.</p>
 *
 * <p>For the Axis2/C equivalent, the same pattern applies using
 * {@code ap_rflush()} on the Apache httpd response during
 * {@code json_object_to_json_string_ext()} output.</p>
 *
 * @see FlushingOutputStream
 * @since 2.0.1
 */
public class JSONStreamingMessageFormatter implements MessageFormatter {

    private static final Log log = LogFactory.getLog(JSONStreamingMessageFormatter.class);

    /** services.xml parameter name for flush interval override */
    private static final String PARAM_FLUSH_INTERVAL = "streamingFlushIntervalBytes";

    public void writeTo(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat,
                        OutputStream outputStream, boolean preserve) throws AxisFault {

        String charSetEncoding = (String) outMsgCtxt.getProperty(
            Constants.Configuration.CHARACTER_SET_ENCODING);
        if (charSetEncoding == null) {
            charSetEncoding = "UTF-8";
        }

        // Wrap the transport OutputStream with periodic flushing.
        // This is the only difference from the standard JsonFormatter —
        // everything else delegates to the same GSON serialization path.
        int flushInterval = getFlushInterval(outMsgCtxt);
        OutputStream flushingStream = new FlushingOutputStream(outputStream, flushInterval);

        if (log.isDebugEnabled()) {
            log.debug("JSONStreamingMessageFormatter: using FlushingOutputStream with "
                + flushInterval + " byte flush interval");
        }

        try (JsonWriter jsonWriter = new JsonWriter(
                new OutputStreamWriter(flushingStream, charSetEncoding))) {

            Object retObj = outMsgCtxt.getProperty(JsonConstant.RETURN_OBJECT);

            if (outMsgCtxt.isProcessingFault()) {
                writeFaultResponse(outMsgCtxt, jsonWriter);

            } else if (retObj == null) {
                writeElementResponse(outMsgCtxt, jsonWriter, preserve);

            } else {
                writeObjectResponse(outMsgCtxt, jsonWriter, retObj);
            }

            jsonWriter.flush();
            log.debug("JSONStreamingMessageFormatter.writeTo() completed");

        } catch (IOException e) {
            String msg = "Error during JSON streaming serialization";
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
            throw new AxisFault("Error writing fault response in JSONStreamingMessageFormatter", e);
        }
    }

    /**
     * Write an OM element response (no return object — schema-driven serialization).
     */
    private void writeElementResponse(MessageContext outMsgCtxt, JsonWriter jsonWriter,
                                      boolean preserve) throws AxisFault {
        OMElement element = outMsgCtxt.getEnvelope().getBody().getFirstElement();
        QName elementQname = outMsgCtxt.getAxisOperation().getMessage(
            WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getElementQName();

        ArrayList<XmlSchema> schemas = outMsgCtxt.getAxisService().getSchema();
        GsonXMLStreamWriter xmlsw = new GsonXMLStreamWriter(jsonWriter,
            elementQname, schemas, outMsgCtxt.getConfigurationContext());
        try {
            xmlsw.writeStartDocument();
            element.serialize(xmlsw, preserve);
            xmlsw.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new AxisFault("Error writing element response in JSONStreamingMessageFormatter", e);
        }
    }

    /**
     * Write a return-object response using GSON.
     *
     * <p>GSON serializes the object graph field-by-field into the JsonWriter.
     * Because the JsonWriter is backed by a {@link FlushingOutputStream},
     * the HTTP transport receives chunks as serialization progresses —
     * the full response is never buffered in a single String or byte[].</p>
     */
    private void writeObjectResponse(MessageContext outMsgCtxt, JsonWriter jsonWriter,
                                     Object retObj) throws AxisFault {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(String.class, new JsonHtmlEncoder());
            Gson gson = gsonBuilder.create();

            jsonWriter.beginObject();
            jsonWriter.name(JsonConstant.RESPONSE);
            Type returnType = (Type) outMsgCtxt.getProperty(JsonConstant.RETURN_TYPE);
            gson.toJson(retObj, returnType, jsonWriter);
            jsonWriter.endObject();

        } catch (IOException e) {
            String msg = "Error writing object response in JSONStreamingMessageFormatter";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Read the flush interval from the service's configuration.
     * Falls back to {@link FlushingOutputStream#DEFAULT_FLUSH_INTERVAL}.
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
