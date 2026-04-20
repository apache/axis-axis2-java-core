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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.kernel.MessageFormatter;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Decorator that wraps any {@link MessageFormatter} and filters
 * response JSON to include only caller-requested fields.
 *
 * <p>When a {@code fields} query parameter is present on the inbound
 * request (e.g., {@code ?fields=status,portfolioVariance,calcTimeUs}),
 * or when a {@code Set<String>} is stored as the
 * {@link JsonConstant#FIELD_FILTER} property on the MessageContext,
 * this formatter serializes the response through the delegate and then
 * re-streams only the selected fields to the transport output.</p>
 *
 * <p>When no field filter is requested, the delegate is called directly
 * with zero overhead — no capture, no re-stream.</p>
 *
 * <p>Field filtering operates on the top-level fields inside the
 * {@code "response"} wrapper object. Nested sub-fields and arrays are
 * included or excluded as whole values. The response wrapper itself
 * ({@code {"response": {...}}}) is always preserved.</p>
 *
 * <h3>Usage in axis2.xml</h3>
 * <pre>{@code
 * <messageFormatter contentType="application/json"
 *     class="org.apache.axis2.json.streaming.FieldFilteringMessageFormatter">
 *     <parameter name="delegateFormatter">
 *         org.apache.axis2.json.streaming.MoshiStreamingMessageFormatter
 *     </parameter>
 * </messageFormatter>
 * }</pre>
 *
 * <h3>Client usage</h3>
 * <pre>{@code
 * GET /services/FinancialBenchmarkService?fields=status,portfolioVariance,calcTimeUs
 * }</pre>
 *
 * @see JsonConstant#FIELD_FILTER
 * @since 2.0.1
 */
public class FieldFilteringMessageFormatter implements MessageFormatter {

    private static final Log log = LogFactory.getLog(FieldFilteringMessageFormatter.class);

    /** services.xml parameter name for the delegate formatter class. */
    static final String PARAM_DELEGATE_CLASS = "delegateFormatter";

    /** Default delegate if none is configured. */
    private static final String DEFAULT_DELEGATE =
        "org.apache.axis2.json.streaming.MoshiStreamingMessageFormatter";

    private volatile MessageFormatter delegate;

    /**
     * Construct with no delegate — resolved lazily from the service
     * parameter {@value #PARAM_DELEGATE_CLASS}.
     */
    public FieldFilteringMessageFormatter() {
    }

    /**
     * Construct with an explicit delegate (used by unit tests).
     */
    public FieldFilteringMessageFormatter(MessageFormatter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void writeTo(MessageContext outMsgCtxt, OMOutputFormat format,
                        OutputStream outputStream, boolean preserve) throws AxisFault {

        resolveDelegate(outMsgCtxt);

        Set<String> fieldFilter = getFieldFilter(outMsgCtxt);

        boolean filterWasSet = false;
        try {
            if (fieldFilter != null && !fieldFilter.isEmpty()
                    && !outMsgCtxt.isProcessingFault()) {
                // Store the filter set on the MessageContext so the delegate
                // formatter (MoshiStreamingMessageFormatter) can pick it up
                // in writeObjectResponse() and use reflection-based selective
                // serialization — no capture buffer, true streaming preserved.
                outMsgCtxt.setProperty(JsonConstant.FIELD_FILTER, fieldFilter);
                filterWasSet = true;

                if (log.isDebugEnabled()) {
                    log.debug("FieldFilteringMessageFormatter: set field filter "
                        + fieldFilter + " on MessageContext for streaming delegate");
                }
            }

            // Always delegate — the streaming formatter handles the filtering
            // natively when FIELD_FILTER is present on the MessageContext.
            // When no filter is set, the delegate runs at full speed with no
            // overhead. Faults are never filtered (different JSON structure).
            delegate.writeTo(outMsgCtxt, format, outputStream, preserve);

        } finally {
            // Clean up the property to prevent leaks to other components
            // that might operate on the same MessageContext later.
            if (filterWasSet) {
                outMsgCtxt.removeProperty(JsonConstant.FIELD_FILTER);
            }
        }
    }

    /**
     * Extract the field filter from the MessageContext.
     *
     * <p>Checks (in order):
     * <ol>
     *   <li>Outbound MessageContext property {@link JsonConstant#FIELD_FILTER}
     *       (a {@code Set<String>} set by a handler or message receiver)</li>
     *   <li>Inbound MessageContext's query string {@code ?fields=a,b,c}</li>
     * </ol>
     *
     * @return field name set, or null if no filter requested
     */
    @SuppressWarnings("unchecked")
    Set<String> getFieldFilter(MessageContext outMsgCtxt) {
        // Check explicit property first.
        Object prop = outMsgCtxt.getProperty(JsonConstant.FIELD_FILTER);
        if (prop instanceof Set) {
            return (Set<String>) prop;
        }

        // Fall back to parsing the inbound request's query string.
        // Try multiple sources — the property location varies by transport.
        try {
            OperationContext opCtx = outMsgCtxt.getOperationContext();
            if (opCtx != null) {
                MessageContext inMsgCtxt = opCtx.getMessageContext(
                    WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inMsgCtxt != null) {
                    // Source 1: HttpServletRequest (WildFly, Tomcat, Spring Boot)
                    Object servletReq = inMsgCtxt.getProperty(
                        HTTPConstants.MC_HTTP_SERVLETREQUEST);
                    if (servletReq instanceof jakarta.servlet.http.HttpServletRequest) {
                        String qs = ((jakarta.servlet.http.HttpServletRequest)
                            servletReq).getQueryString();
                        if (qs != null) {
                            Set<String> fields = parseFieldsFromQueryString(qs);
                            if (fields != null) return fields;
                        }
                    }

                    // Source 2: TransportInURL property
                    Object urlObj = inMsgCtxt.getProperty(
                        Constants.Configuration.TRANSPORT_IN_URL);
                    if (urlObj == null) {
                        urlObj = inMsgCtxt.getTo() != null
                            ? inMsgCtxt.getTo().getAddress() : null;
                    }
                    if (urlObj instanceof String) {
                        return parseFieldsFromUrl((String) urlObj);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract fields from inbound URL", e);
        }

        return null;
    }

    /**
     * Parse {@code fields=a,b,c} from a raw query string (no leading '?').
     *
     * @return field set, or null if no {@code fields} parameter
     */
    static Set<String> parseFieldsFromQueryString(String queryString) {
        if (queryString == null) return null;
        for (String param : queryString.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "fields".equals(kv[0].trim())) {
                try {
                    String decoded = URLDecoder.decode(kv[1],
                        StandardCharsets.UTF_8.name());
                    return parseFieldsCsv(decoded);
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError("UTF-8 not supported", e);
                }
            }
        }
        return null;
    }

    /**
     * Parse {@code ?fields=a,b,c} from a full URL string.
     *
     * @return field set, or null if no {@code fields} parameter
     */
    static Set<String> parseFieldsFromUrl(String url) {
        if (url == null) return null;
        int queryStart = url.indexOf('?');
        if (queryStart < 0) return null;
        String query = url.substring(queryStart + 1);
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "fields".equals(kv[0].trim())) {
                try {
                    String decoded = URLDecoder.decode(kv[1],
                        StandardCharsets.UTF_8.name());
                    return parseFieldsCsv(decoded);
                } catch (UnsupportedEncodingException e) {
                    // Should never happen with UTF-8
                    return parseFieldsCsv(kv[1]);
                }
            }
        }
        return null;
    }

    /**
     * Parse a comma-separated field list into a set.
     *
     * @return non-null set (may be empty)
     */
    static Set<String> parseFieldsCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> fields = new LinkedHashSet<>();
        for (String f : csv.split(",")) {
            String trimmed = f.trim();
            if (!trimmed.isEmpty()) {
                fields.add(trimmed);
            }
        }
        return fields;
    }

    /**
     * Lazily resolve the delegate formatter from the service parameter.
     */
    private void resolveDelegate(MessageContext msgCtxt) throws AxisFault {
        if (delegate != null) return;

        synchronized (this) {
            if (delegate != null) return;

            String className = DEFAULT_DELEGATE;
            AxisService service = msgCtxt.getAxisService();
            if (service != null) {
                Parameter param = service.getParameter(PARAM_DELEGATE_CLASS);
                if (param != null && param.getValue() != null) {
                    className = param.getValue().toString().trim();
                }
            }

            try {
                delegate = (MessageFormatter) Class.forName(className)
                    .getDeclaredConstructor().newInstance();
                log.info("FieldFilteringMessageFormatter: delegate resolved to " + className);
            } catch (Exception e) {
                throw new AxisFault(
                    "Cannot instantiate delegate formatter: " + className, e);
            }
        }
    }

    @Override
    public String getContentType(MessageContext outMsgCtxt, OMOutputFormat format,
                                 String soapAction) {
        return (String) outMsgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
    }

    @Override
    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format,
                                URL url) throws AxisFault {
        return null;
    }

    @Override
    public String formatSOAPAction(MessageContext msgCtxt, OMOutputFormat format,
                                   String soapAction) {
        return null;
    }
}
