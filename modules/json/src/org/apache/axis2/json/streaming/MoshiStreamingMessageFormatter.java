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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
     *
     * <p>When {@link JsonConstant#FIELD_FILTER} is set on the MessageContext,
     * only the requested top-level fields of the return object are serialized.
     * This is done via reflection-based selective serialization — each field
     * is checked against the filter set before being written to the JsonWriter.
     * The streaming pipeline is never broken: non-selected fields are simply
     * never written, so no capture buffer is needed.</p>
     */
    @SuppressWarnings("unchecked")
    private void writeObjectResponse(JsonWriter jsonWriter, JsonAdapter<Object> adapter,
                                     Object retObj, MessageContext outMsgCtxt) throws AxisFault {
        try {
            jsonWriter.beginObject();
            jsonWriter.name(JsonConstant.RESPONSE);

            Object filterProp = outMsgCtxt.getProperty(JsonConstant.FIELD_FILTER);
            if (filterProp instanceof Set && !((Set<?>) filterProp).isEmpty()) {
                writeFilteredObject(jsonWriter, retObj, (Set<String>) filterProp);
            } else {
                adapter.toJson(jsonWriter, retObj);
            }

            jsonWriter.endObject();

        } catch (IOException e) {
            String msg = "Error writing object response in MoshiStreamingMessageFormatter";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Serialize only the fields in {@code allowedFields} from the return
     * object, directly to the JsonWriter. No intermediate buffer.
     *
     * <p>Uses Java reflection to iterate over the object's declared fields.
     * For each field whose name is in the allowed set, the field value is
     * serialized via the Moshi adapter for that field's type. Fields not in
     * the set are silently skipped — they are never serialized, never
     * buffered, never written to the wire.</p>
     *
     * <p>This keeps the streaming pipeline intact: JsonWriter → Okio sink →
     * FlushingOutputStream → transport. Each allowed field's value flows
     * through to the client as soon as serialization produces enough bytes
     * to trigger a flush.</p>
     */
    /** Shared Moshi instance for field-level serialization (thread-safe, reusable). */
    private static final Moshi FIELD_FILTER_MOSHI = new Moshi.Builder()
        .add(String.class, new JsonHtmlEncoder())
        .add(Date.class, new Rfc3339DateJsonAdapter())
        .build();

    /**
     * Serialize only the requested fields from the return object.
     *
     * <p>Supports two field specification formats:</p>
     * <ul>
     *   <li><b>Flat:</b> {@code "status"} — include this top-level field as-is</li>
     *   <li><b>Dot-notation:</b> {@code "items.id"} — include the "items"
     *       container but filter its contents to only "id"</li>
     * </ul>
     *
     * <p>The dot-notation form is essential for services that return large
     * nested data structures, such as a list of wide objects:</p>
     * <pre>{@code
     * {"response": {
     *     "status": "SUCCESS",
     *     "items": [
     *       {"id":"item-1", "name":"Widget A", ... 125 more fields ...},
     *       {"id":"item-2", "name":"Widget B", ... 125 more fields ...}
     *     ]
     * }}
     * }</pre>
     *
     * <p>With {@code ?fields=status,items.id}, the response becomes:</p>
     * <pre>{@code
     * {"response": {
     *     "status": "SUCCESS",
     *     "items": [
     *       {"id":"item-1"},
     *       {"id":"item-2"}
     *     ]
     * }}
     * }</pre>
     *
     * <p>Compatible with nested field filtering logic in other Axis2 language
     * bindings. The streaming pipeline (Moshi → Okio → FlushingOutputStream)
     * is preserved — no capture buffer is used.</p>
     *
     * <p><b>Nesting depth:</b> Multi-level dot-notation is supported.
     * {@code ?fields=data.records.id} walks three levels deep:
     * keep "data" at top level, keep "records" inside data, keep
     * "id" inside each records element. This enables filtering
     * inside deeply nested Map/Collection structures without requiring
     * service-side changes. The Axis2/C implementation supports single-
     * level dot-notation only.</p>
     *
     * <p><b>Limitation:</b> Field names that contain a literal dot character
     * cannot be selected, as the dot is always interpreted as a nesting
     * delimiter.</p>
     */
    private void writeFilteredObject(JsonWriter jsonWriter, Object retObj,
                                     Set<String> allowedFields)
            throws IOException {

        if (retObj == null) {
            jsonWriter.nullValue();
            return;
        }

        /*
         * Step 1: Parse the allowedFields set into two structures.
         *
         * Input: {"status", "items.id", "items.name"}
         *
         * Output:
         *   topLevel    = {"status", "items"}     — fields to keep at top level
         *   nestedSpecs = {"items" -> {"id", "name"}}  — sub-fields per container
         *
         * "items" appears in BOTH topLevel (so it survives the top-level pass)
         * AND nestedSpecs (so its contents get filtered in the nested pass).
         * This mirrors the two-phase approach in the Axis2/C implementation.
         */
        Set<String> topLevel = new LinkedHashSet<>();
        java.util.Map<String, Set<String>> nestedSpecs = new java.util.LinkedHashMap<>();

        for (String fieldSpec : allowedFields) {
            int dot = fieldSpec.indexOf('.');
            if (dot > 0 && dot < fieldSpec.length() - 1) {
                // Dot-notation: "container.subField"
                String container = fieldSpec.substring(0, dot);
                String subField = fieldSpec.substring(dot + 1);
                topLevel.add(container);
                nestedSpecs.computeIfAbsent(container, k -> new LinkedHashSet<>())
                    .add(subField);
            } else {
                // Simple top-level field: "status", "calcTimeUs"
                topLevel.add(fieldSpec);
            }
        }

        /*
         * Step 2: Iterate the POJO's fields via reflection.
         *
         * For each field:
         *   - Skip if not in topLevel set
         *   - If it has nestedSpecs, serialize with inner filtering
         *   - Otherwise serialize the whole value (flat field or
         *     container with no dot-notation sub-fields)
         */
        /*
         * Request-scoped cache for reflected field lists. Avoids calling
         * getAllFields() repeatedly on the same class when filtering a
         * collection of same-typed objects (e.g., 500 elements of the
         * same Record class). Not static — scoped to this single request
         * to avoid classloader complexity.
         */
        java.util.Map<Class<?>, List<Field>> fieldCache = new java.util.HashMap<>();

        List<Field> allFields = fieldCache.computeIfAbsent(
            retObj.getClass(), MoshiStreamingMessageFormatter::getAllFields);
        boolean prevSerializeNulls = jsonWriter.getSerializeNulls();
        jsonWriter.setSerializeNulls(true);
        try {
            jsonWriter.beginObject();

            for (Field field : allFields) {
                // Top-level prune: skip fields not in the keep set
                if (!topLevel.contains(field.getName())) {
                    continue;
                }

                Object value;
                try {
                    field.setAccessible(true);
                    value = field.get(retObj);
                } catch (IllegalAccessException | SecurityException e) {
                    log.warn("Cannot access field "
                        + field.getName().replaceAll("[\r\n]", "_")
                        + " for field filtering; skipping", e);
                    continue;
                }

                jsonWriter.name(field.getName());

                // Check if this field has nested sub-field specs
                Set<String> subFields = nestedSpecs.get(field.getName());
                if (subFields != null && value != null) {
                    // Nested filtering: serialize container but prune its contents
                    writeFilteredNested(jsonWriter, value, subFields,
                        field.getGenericType(), fieldCache);
                } else if (value == null) {
                    jsonWriter.nullValue();
                } else {
                    // No nested specs: serialize the entire field value as-is
                    @SuppressWarnings("unchecked")
                    JsonAdapter<Object> fieldAdapter =
                        (JsonAdapter<Object>) FIELD_FILTER_MOSHI.adapter(
                            field.getGenericType());
                    fieldAdapter.toJson(jsonWriter, value);
                }
            }

            jsonWriter.endObject();
        } finally {
            jsonWriter.setSerializeNulls(prevSerializeNulls);
        }

        if (log.isDebugEnabled()) {
            log.debug("writeFilteredObject: serialized fields from "
                + allowedFields + " (streaming, no buffer)");
        }
    }

    /**
     * Serialize a nested field (object or collection) with only the
     * specified sub-fields included.
     *
     * <p>Handles three cases:</p>
     * <ol>
     *   <li><b>Collection (List, Set):</b> The key use case for services returning
     *       arrays of wide objects. A {@code List<Record>} where each record has
     *       100+ fields — filter each element independently, keeping only the
     *       requested sub-fields.
     *       Output: {@code [{"id":"item-1"},{"id":"item-2"}]}</li>
     *   <li><b>Map:</b> Filter by key name. Keeps only entries whose key matches
     *       one of the sub-fields.</li>
     *   <li><b>Single POJO:</b> Filter its declared fields, same as a single
     *       array element.</li>
     * </ol>
     *
     * <p>If the value is a scalar (String, Number, etc.), it is serialized as-is
     * since there are no sub-fields to filter inside a primitive.</p>
     *
     * <p>Designed to handle both object and array containers, compatible
     * with nested field filtering logic in other Axis2 language bindings.</p>
     */
    /**
     * Serialize a nested field with recursive dot-notation support.
     *
     * <p>Sub-fields may themselves contain dots for multi-level filtering.
     * For example, with the JSON-RPC service response pattern:</p>
     * <pre>{@code
     * {"response": {
     *     "status": "SUCCESS",
     *     "data": {                          // Map<String, Object>
     *       "records": [            // List<Map<String, Object>>
     *         {"id":"item-1", "name":"Widget A", ... 125 more ...},
     *         {"id":"item-2", "name":"Widget B", ... 125 more ...}
     *       ],
     *       "notes": [...],
     *       "diagnostics": {...}
     *     }
     * }}
     * }</pre>
     *
     * <p>The query {@code ?fields=status,data.records.id}
     * produces:</p>
     * <ol>
     *   <li>Top level: keep "status" and "data"</li>
     *   <li>Inside "data": keep only "records"</li>
     *   <li>Inside each "records" element: keep only "id"</li>
     * </ol>
     *
     * <p>Result: {@code {"response":{"status":"SUCCESS","data":
     * {"records":[{"id":"item-1"},{"id":"item-2"}]}}}}</p>
     */
    @SuppressWarnings("unchecked")
    private void writeFilteredNested(JsonWriter jsonWriter, Object value,
                                     Set<String> subFields, Type declaredType,
                                     java.util.Map<Class<?>, List<Field>> fieldCache)
            throws IOException {

        /*
         * Before processing, check if any sub-fields contain dots —
         * meaning we need to recurse deeper. Parse into immediate-level
         * keeps and deeper nested specs, same pattern as writeFilteredObject.
         *
         * Example: subFields = {"records.id", "records.name"}
         *   immediateKeep = {"records"}
         *   deeperSpecs   = {"records" -> {"id", "name"}}
         */
        Set<String> immediateKeep = new LinkedHashSet<>();
        java.util.Map<String, Set<String>> deeperSpecs = new java.util.LinkedHashMap<>();

        for (String spec : subFields) {
            int dot = spec.indexOf('.');
            if (dot > 0 && dot < spec.length() - 1) {
                String container = spec.substring(0, dot);
                String remainder = spec.substring(dot + 1);
                immediateKeep.add(container);
                deeperSpecs.computeIfAbsent(container, k -> new LinkedHashSet<>())
                    .add(remainder);
            } else {
                immediateKeep.add(spec);
            }
        }

        if (value instanceof java.util.Collection) {
            /*
             * Array of objects — filter each element independently.
             * If there are deeper specs, each element is filtered recursively.
             */
            jsonWriter.beginArray();
            for (Object element : (java.util.Collection<?>) value) {
                if (element == null) {
                    jsonWriter.nullValue();
                } else if (element instanceof java.util.Map) {
                    writeFilteredMap(jsonWriter, (java.util.Map<?, ?>) element,
                        immediateKeep, deeperSpecs, fieldCache);
                } else if (element instanceof java.util.Collection) {
                    // Nested collection — recurse with the same sub-fields
                    writeFilteredNested(jsonWriter, element, subFields,
                        Object.class, fieldCache);
                } else {
                    writeFilteredPojo(jsonWriter, element,
                        immediateKeep, deeperSpecs, fieldCache);
                }
            }
            jsonWriter.endArray();

        } else if (value instanceof java.util.Map) {
            /*
             * Map — the JSON-RPC service pattern. The "data" field is a Map<String, Object>
             * where keys are "records", "metadata", "diagnostics", etc.
             * Filter keys by immediateKeep, then recurse into deeperSpecs.
             */
            writeFilteredMap(jsonWriter, (java.util.Map<?, ?>) value,
                immediateKeep, deeperSpecs, fieldCache);

        } else if (value.getClass().getName().startsWith("java.lang.")) {
            /* Scalar — nothing to filter inside. */
            JsonAdapter<Object> adapter =
                (JsonAdapter<Object>) FIELD_FILTER_MOSHI.adapter(declaredType);
            adapter.toJson(jsonWriter, value);

        } else {
            /* Single POJO — filter its declared fields recursively. */
            writeFilteredPojo(jsonWriter, value, immediateKeep, deeperSpecs, fieldCache);
        }
    }

    /**
     * Serialize a Map with field filtering and recursive dot-notation.
     *
     * <p>This is the core of JSON-RPC service response filtering. A Map like
     * {@code {"records":[...], "metadata":[...], "diagnostics":{...}}}
     * is filtered to keep only the keys in {@code immediateKeep}. For keys
     * that have {@code deeperSpecs}, the value is recursively filtered.</p>
     */
    @SuppressWarnings("unchecked")
    private void writeFilteredMap(JsonWriter jsonWriter, java.util.Map<?, ?> map,
                                  Set<String> immediateKeep,
                                  java.util.Map<String, Set<String>> deeperSpecs,
                                  java.util.Map<Class<?>, List<Field>> fieldCache)
            throws IOException {

        jsonWriter.beginObject();
        for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());

            if (!immediateKeep.contains(key)) {
                continue;  // Key not requested — skip
            }

            jsonWriter.name(key);
            Object entryValue = entry.getValue();

            Set<String> deeper = deeperSpecs.get(key);
            if (deeper != null && entryValue != null) {
                // This key has deeper sub-field specs — recurse
                writeFilteredNested(jsonWriter, entryValue, deeper,
                    Object.class, fieldCache);
            } else if (entryValue == null) {
                jsonWriter.nullValue();
            } else {
                // No deeper filtering — serialize the full value
                JsonAdapter<Object> valAdapter =
                    (JsonAdapter<Object>) FIELD_FILTER_MOSHI.adapter(Object.class);
                valAdapter.toJson(jsonWriter, entryValue);
            }
        }
        jsonWriter.endObject();
    }

    /**
     * Serialize a POJO with recursive field filtering.
     *
     * <p>Mirrors {@link #writeFilteredMap} but operates on POJO fields via
     * reflection. For each field in {@code immediateKeep}, checks if there
     * are {@code deeperSpecs} and recurses into nested structures.</p>
     *
     * <p>Used for both standalone nested POJOs and individual elements
     * within a filtered collection. Uses the request-scoped field cache
     * to avoid repeated reflection — critical when filtering a 500-element
     * collection where every element is the same type.</p>
     */
    @SuppressWarnings("unchecked")
    private void writeFilteredPojo(JsonWriter jsonWriter, Object pojo,
                                   Set<String> immediateKeep,
                                   java.util.Map<String, Set<String>> deeperSpecs,
                                   java.util.Map<Class<?>, List<Field>> fieldCache)
            throws IOException {

        List<Field> fields = fieldCache.computeIfAbsent(
            pojo.getClass(), MoshiStreamingMessageFormatter::getAllFields);
        jsonWriter.beginObject();
        for (Field field : fields) {
            if (!immediateKeep.contains(field.getName())) {
                continue;
            }
            Object value;
            try {
                field.setAccessible(true);
                value = field.get(pojo);
            } catch (IllegalAccessException | SecurityException e) {
                log.warn("Cannot access field "
                    + field.getDeclaringClass().getName().replaceAll("[\r\n]", "_")
                    + "." + field.getName().replaceAll("[\r\n]", "_")
                    + " for nested field filtering; skipping", e);
                continue;
            }

            jsonWriter.name(field.getName());
            Set<String> deeper = deeperSpecs != null ? deeperSpecs.get(field.getName()) : null;
            if (deeper != null && value != null) {
                // Recurse into this field's value
                writeFilteredNested(jsonWriter, value, deeper,
                    field.getGenericType(), fieldCache);
            } else if (value == null) {
                jsonWriter.nullValue();
            } else {
                JsonAdapter<Object> adapter =
                    (JsonAdapter<Object>) FIELD_FILTER_MOSHI.adapter(
                        field.getGenericType());
                adapter.toJson(jsonWriter, value);
            }
        }
        jsonWriter.endObject();
    }

    /**
     * Collect all non-static, non-transient fields from the class hierarchy.
     * Walks from the concrete class up through all superclasses to (but not
     * including) Object. This ensures inherited fields are included when
     * a response object extends a base class.
     *
     * <p>Note: this method reflects over the class on each call. For extreme
     * performance needs, the result could be cached in a
     * {@code ConcurrentHashMap<Class<?>, List<Field>>}. The current approach
     * avoids cache-related complexity with dynamic classloaders.</p>
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int mods = field.getModifiers();
                if (!Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
                    result.add(field);
                }
            }
        }
        return result;
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
