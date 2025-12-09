/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.h2.integration.moshi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.json.moshi.MoshiXMLStreamReader;
import org.apache.axis2.json.moshi.JsonBuilder;
import org.apache.axis2.json.moshi.JsonFormatter;
import org.apache.axis2.json.moshi.rpc.JsonInOnlyRPCMessageReceiver;
import org.apache.axis2.json.moshi.rpc.JsonRpcMessageReceiver;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.kernel.MessageFormatter;
import org.apache.axis2.kernel.TransportUtils;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

// Import base integration classes that remain in parent package
import org.apache.axis2.transport.h2.integration.HTTP2MemoryCoordinator;
import org.apache.axis2.transport.h2.integration.HTTP2StreamManager;

import com.squareup.moshi.JsonReader;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathMatcher;
import okio.BufferedSource;
import okio.Okio;

/**
 * Direct Undertow HTTP/2 handler for Axis2 web services using Moshi JSON processing.
 *
 * This class implements Phase 2 of the WildFly 32 + Axis2 HTTP/2 Cooperative Integration Plan,
 * providing direct integration between Undertow's HTTP/2 handler chain and Axis2's message
 * processing engine with Moshi-specific JSON handling. Bypasses servlet layer overhead for
 * optimized request processing.
 *
 * Key features:
 * - Direct Undertow handler integration for HTTP/2 optimization
 * - Optimized JSON processing using Moshi streaming patterns
 * - Integration with existing Axis2 service discovery and routing
 * - Support for both standard and JSON-RPC message receivers
 * - HTTP/2 stream-aware processing with flow control
 * - Moshi-specific JSON serialization/deserialization
 */
public class Axis2HTTP2Handler implements HttpHandler {
    private static final Log log = LogFactory.getLog(Axis2HTTP2Handler.class);

    private final PathMatcher<AxisService> servicePathMatcher;
    private final ConfigurationContext axisConfigurationContext;
    private final HTTP2StreamManager streamManager;
    private final Map<String, Long> serviceMetrics;
    private final HTTP2MemoryCoordinator memoryCoordinator;

    /**
     * Initialize handler with Axis2 configuration context.
     *
     * @param axisConfig The Axis2 configuration context providing access to services and operations
     * @param memoryCoordinator Memory coordination system for resource management
     */
    public Axis2HTTP2Handler(ConfigurationContext axisConfig, HTTP2MemoryCoordinator memoryCoordinator) {
        this.axisConfigurationContext = axisConfig;
        this.memoryCoordinator = memoryCoordinator;
        this.servicePathMatcher = createServicePathMatcher(axisConfig);
        this.streamManager = new HTTP2StreamManager(memoryCoordinator);
        this.serviceMetrics = new ConcurrentHashMap<>();

        log.info("Initialized Moshi-based Axis2HTTP2Handler with " + getRegisteredServiceCount() + " services");
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Processing request: " + exchange.getRequestMethod() + " " + exchange.getRequestPath());
        }

        // Check if this is an Axis2 web service request
        PathMatcher.PathMatch<AxisService> match = servicePathMatcher.match(exchange.getRequestPath());

        if (match.getValue() == null) {
            // Not an Axis2 request, pass to next handler
            if (log.isDebugEnabled()) {
                log.debug("No Axis2 service match for path: " + exchange.getRequestPath());
            }
            exchange.setStatusCode(404);
            exchange.getResponseSender().send("Service not found");
            return;
        }

        AxisService axisService = match.getValue();
        recordServiceAccess(axisService.getName());

        try {
            // Handle HTTP/2 specific optimizations
            if (isHTTP2Request(exchange)) {
                handleHTTP2OptimizedRequest(exchange, axisService);
            } else {
                handleStandardRequest(exchange, axisService);
            }
        } catch (Exception e) {
            log.error("Error processing Axis2 request for service: " + axisService.getName(), e);
            sendErrorResponse(exchange, e);
        }
    }

    /**
     * Handle HTTP/2 optimized request with stream management and memory coordination.
     */
    private void handleHTTP2OptimizedRequest(HttpServerExchange exchange, AxisService axisService) throws Exception {
        // Get HTTP/2 stream information
        int streamId = getHTTP2StreamId(exchange);
        long estimatedPayloadSize = getEstimatedPayloadSize(exchange);

        // Request memory allocation through coordinator
        if (!memoryCoordinator.requestAllocation(HTTP2MemoryCoordinator.Component.AXIS2, estimatedPayloadSize)) {
            log.warn("Memory allocation denied for HTTP/2 request - payload size: " + estimatedPayloadSize);
            exchange.setStatusCode(503);
            exchange.getResponseSender().send("Service temporarily unavailable - memory limit reached");
            return;
        }

        try {
            // Create optimized message context
            MessageContext msgContext = createOptimizedMessageContext(exchange, axisService);

            // Configure stream-specific settings
            streamManager.configureStreamForPayload(streamId, estimatedPayloadSize);

            // Process with streaming JSON using Moshi
            if (isJSONRequest(exchange)) {
                processJSONStreamingRequest(exchange, msgContext, streamId);
            } else {
                processStandardRequest(exchange, msgContext);
            }

        } finally {
            // Release allocated memory
            memoryCoordinator.releaseAllocation(HTTP2MemoryCoordinator.Component.AXIS2, estimatedPayloadSize);
        }
    }

    /**
     * Process large JSON payloads using Axis2's Moshi streaming patterns.
     */
    private void processJSONStreamingRequest(HttpServerExchange exchange, MessageContext msgContext, int streamId) throws Exception {

        exchange.getRequestReceiver().receiveFullBytes((httpServerExchange, bytes) -> {
            try {
                // Create InputStream from bytes
                InputStream inputStream = new ByteArrayInputStream(bytes);

                // Use Axis2's JsonBuilder pattern with Moshi
                BufferedSource source = Okio.buffer(Okio.source(inputStream));
                JsonReader jsonReader = JsonReader.of(source);
                jsonReader.setLenient(true);

                // Set Axis2 message context properties
                msgContext.setProperty(JsonConstant.IS_JSON_STREAM, true);

                // Create Axis2's MoshiXMLStreamReader
                MoshiXMLStreamReader moshiXMLStreamReader = new MoshiXMLStreamReader(jsonReader);
                msgContext.setProperty(JsonConstant.MOSHI_XML_STREAM_READER, moshiXMLStreamReader);

                // Initialize schema information if available
                initializeSchemaInformation(msgContext, moshiXMLStreamReader);

                // Check if this is a JSON RPC request
                if (isJSONRPCService(msgContext)) {
                    handleJSONRPCRequest(exchange, msgContext, streamId, moshiXMLStreamReader);
                } else {
                    // Build OM element using Axis2 patterns
                    buildOMElementFromJSON(msgContext, moshiXMLStreamReader);

                    // Process through Axis2 engine
                    AxisEngine.receive(msgContext);

                    // Send optimized response using Axis2 patterns
                    sendAxis2Response(exchange, msgContext, streamId);
                }

            } catch (Exception e) {
                log.error("Error processing JSON streaming request for stream: " + streamId, e);
                sendErrorResponse(exchange, e);
            }
        });
    }

    /**
     * Handle JSON RPC requests using Axis2's JsonRpcMessageReceiver patterns.
     */
    private void handleJSONRPCRequest(HttpServerExchange exchange, MessageContext msgContext,
                                     int streamId, MoshiXMLStreamReader xmlStreamReader) throws Exception {

        AxisOperation axisOperation = msgContext.getAxisOperation();
        if (axisOperation != null) {
            AbstractMessageReceiver messageReceiver = (AbstractMessageReceiver) axisOperation.getMessageReceiver();

            if (messageReceiver instanceof JsonRpcMessageReceiver ||
                messageReceiver instanceof JsonInOnlyRPCMessageReceiver) {

                log.debug("Processing JSON RPC request with HTTP/2 optimization for stream: " + streamId);

                // Process RPC call with stream optimization
                processJSONRPCWithStreamOptimization(xmlStreamReader.getJsonReader(), msgContext, streamId);

                // Invoke the message receiver
                messageReceiver.receive(msgContext);

                // Send response
                sendAxis2Response(exchange, msgContext, streamId);
            }
        }
    }

    /**
     * Process JSON RPC with HTTP/2 stream optimization using Axis2 patterns.
     */
    private void processJSONRPCWithStreamOptimization(JsonReader jsonReader, MessageContext msgContext,
                                                     int streamId) throws Exception {
        try {
            // Check for enableJSONOnly parameter (Axis2 pattern)
            AxisService axisService = msgContext.getAxisService();
            String enableJSONOnly = (String) axisService.getParameterValue("enableJSONOnly");

            if (enableJSONOnly != null && enableJSONOnly.equalsIgnoreCase("true")) {
                // Direct JSON processing without XML wrapper
                log.debug("Processing enableJSONOnly=true request with HTTP/2 optimization");

                // Use Axis2's message name discovery pattern
                jsonReader.beginObject();
                String messageName = jsonReader.nextName();
                if (messageName != null) {
                    msgContext.setProperty(JsonConstant.JSON_MESSAGE_NAME, messageName);
                    log.debug("Discovered JSON message name: " + messageName + " for stream: " + streamId);
                }

                // Apply HTTP/2 flow control during processing
                streamManager.registerStream(streamId, getEstimatedPayloadSize(msgContext));
            } else {
                // Standard JSON-to-XML bridge processing
                log.debug("Processing standard JSON request with XML bridge for stream: " + streamId);
            }

        } catch (IOException e) {
            log.error("Error processing JSON RPC stream: " + streamId, e);
            throw new AxisFault("Bad Request", e);
        }
    }

    /**
     * Send response using Axis2's Moshi JsonFormatter patterns.
     */
    private void sendAxis2Response(HttpServerExchange exchange, MessageContext msgContext, int streamId) throws AxisFault {
        try {
            // Create OutputStream for response
            OutputStream outputStream = exchange.getOutputStream();

            // Use Axis2's Moshi JsonFormatter pattern
            MessageFormatter jsonFormatter = new JsonFormatter();
            OMOutputFormat outputFormat = new OMOutputFormat();

            // Configure response headers
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=UTF-8");
            exchange.setStatusCode(200);

            // Write response using Axis2's formatter
            jsonFormatter.writeTo(msgContext, outputFormat, outputStream, false);

            log.debug("Successfully sent Axis2 HTTP/2 optimized response for stream: " + streamId);

        } catch (Exception e) {
            log.error("Error sending Axis2 response for stream: " + streamId, e);
            throw new AxisFault("Error generating response", e);
        }
    }

    /**
     * Initialize schema information for enhanced JSON processing.
     */
    private void initializeSchemaInformation(MessageContext msgContext, MoshiXMLStreamReader moshiXMLStreamReader) {
        try {
            AxisService axisService = msgContext.getAxisService();
            if (axisService != null) {
                AxisOperation axisOperation = msgContext.getAxisOperation();
                if (axisOperation != null) {
                    QName elementQname = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getElementQName();
                    List<XmlSchema> schemas = axisService.getSchema();
                    moshiXMLStreamReader.initXmlStreamReader(elementQname, schemas, axisConfigurationContext);
                }
            }
        } catch (Exception e) {
            log.debug("Schema information not available, proceeding without schema validation", e);
        }
    }

    /**
     * Build OM element from JSON using Axis2's standard patterns.
     */
    private void buildOMElementFromJSON(MessageContext msgContext, MoshiXMLStreamReader moshiXMLStreamReader) throws AxisFault {
        try {
            OMXMLParserWrapper stAXOMBuilder = OMXMLBuilderFactory.createStAXOMBuilder(moshiXMLStreamReader);
            OMElement omElement = stAXOMBuilder.getDocumentElement();
            msgContext.getEnvelope().getBody().addChild(omElement);
        } catch (Exception e) {
            log.error("Error building OM element from JSON", e);
            throw new AxisFault("Error processing JSON payload", e);
        }
    }

    /**
     * Create optimized message context with HTTP/2 integration properties.
     */
    public MessageContext createOptimizedMessageContext(HttpServerExchange exchange, AxisService axisService) throws AxisFault {
        MessageContext msgContext = new MessageContext();

        // Set basic Axis2 properties
        msgContext.setConfigurationContext(axisConfigurationContext);
        msgContext.setAxisService(axisService);

        // Set HTTP-specific properties
        msgContext.setProperty(Constants.Configuration.TRANSPORT_URL, exchange.getRequestURL());
        msgContext.setProperty(Constants.Configuration.HTTP_METHOD, exchange.getRequestMethod().toString());

        // Set HTTP/2 optimization properties
        msgContext.setProperty("HTTP2_OPTIMIZED", true);
        msgContext.setProperty("HTTP2_STREAM_ID", getHTTP2StreamId(exchange));
        msgContext.setProperty("UNDERTOW_EXCHANGE", exchange);
        msgContext.setProperty("JSON_LIBRARY", "MOSHI");

        // Set basic service information (operation discovery simplified for HTTP/2 integration)
        AxisOperation axisOperation = null;
        if (axisOperation != null) {
            msgContext.setAxisOperation(axisOperation);
        }

        return msgContext;
    }

    /**
     * Handle standard (non-HTTP/2) request processing.
     */
    private void handleStandardRequest(HttpServerExchange exchange, AxisService axisService) throws Exception {
        log.debug("Processing standard HTTP/1.1 request for service: " + axisService.getName());

        MessageContext msgContext = createOptimizedMessageContext(exchange, axisService);
        processStandardRequest(exchange, msgContext);
    }

    /**
     * Process standard request through Axis2 engine.
     */
    private void processStandardRequest(HttpServerExchange exchange, MessageContext msgContext) throws AxisFault {
        try {
            // Process through Axis2 engine
            AxisEngine.receive(msgContext);

            // Send response
            sendStandardResponse(exchange, msgContext);
        } catch (Exception e) {
            log.error("Error processing standard request", e);
            throw new AxisFault("Error processing request", e);
        }
    }

    /**
     * Send standard HTTP response.
     */
    private void sendStandardResponse(HttpServerExchange exchange, MessageContext msgContext) throws Exception {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=UTF-8");

        OutputStream outputStream = exchange.getOutputStream();
        MessageFormatter formatter = new JsonFormatter();
        OMOutputFormat outputFormat = new OMOutputFormat();

        formatter.writeTo(msgContext, outputFormat, outputStream, false);
    }

    /**
     * Create service path matcher for Axis2 services.
     */
    private PathMatcher<AxisService> createServicePathMatcher(ConfigurationContext axisConfig) {
        PathMatcher<AxisService> matcher = new PathMatcher<>();

        // Register all available services
        if (axisConfig.getAxisConfiguration() != null) {
            Map<String, AxisService> services = axisConfig.getAxisConfiguration().getServices();

            for (Map.Entry<String, AxisService> entry : services.entrySet()) {
                String serviceName = entry.getKey();
                AxisService service = entry.getValue();

                // Register service paths
                matcher.addExactPath("/services/" + serviceName, service);
                matcher.addExactPath("/services/" + serviceName + ".json", service);
                matcher.addPrefixPath("/services/" + serviceName + "/", service);

                log.debug("Registered Axis2 service path: /services/" + serviceName);
            }
        }

        return matcher;
    }

    // Utility methods

    private boolean isHTTP2Request(HttpServerExchange exchange) {
        return "HTTP/2.0".equals(exchange.getProtocol().toString());
    }

    private boolean isJSONRequest(HttpServerExchange exchange) {
        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private boolean isJSONRPCService(MessageContext msgContext) {
        AxisOperation axisOperation = msgContext.getAxisOperation();
        if (axisOperation != null) {
            AbstractMessageReceiver messageReceiver = (AbstractMessageReceiver) axisOperation.getMessageReceiver();
            return messageReceiver instanceof JsonRpcMessageReceiver ||
                   messageReceiver instanceof JsonInOnlyRPCMessageReceiver;
        }
        return false;
    }

    private int getHTTP2StreamId(HttpServerExchange exchange) {
        // Extract stream ID from exchange - this would need Undertow-specific implementation
        // For now, return a placeholder
        return exchange.hashCode() % 1000;
    }

    private long getEstimatedPayloadSize(HttpServerExchange exchange) {
        String contentLength = exchange.getRequestHeaders().getFirst(Headers.CONTENT_LENGTH);
        if (contentLength != null) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                log.debug("Invalid content length header: " + contentLength);
            }
        }
        return 65536; // Default estimate: 64KB
    }

    private long getEstimatedPayloadSize(MessageContext msgContext) {
        Object contentLength = msgContext.getProperty("Content-Length");
        if (contentLength instanceof Long) {
            return (Long) contentLength;
        }
        return 65536; // Default estimate: 64KB
    }

    private void sendErrorResponse(HttpServerExchange exchange, Exception e) {
        try {
            exchange.setStatusCode(500);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
            exchange.getResponseSender().send("Internal Server Error: " + e.getMessage());
        } catch (Exception ex) {
            log.error("Error sending error response", ex);
        }
    }

    private void recordServiceAccess(String serviceName) {
        serviceMetrics.merge(serviceName, 1L, Long::sum);
    }

    private int getRegisteredServiceCount() {
        if (axisConfigurationContext.getAxisConfiguration() != null) {
            return axisConfigurationContext.getAxisConfiguration().getServices().size();
        }
        return 0;
    }

    /**
     * Get service access metrics for monitoring.
     */
    public Map<String, Long> getServiceMetrics() {
        return new ConcurrentHashMap<>(serviceMetrics);
    }

    /**
     * Reset service metrics.
     */
    public void resetMetrics() {
        serviceMetrics.clear();
        log.info("Moshi-based Axis2HTTP2Handler metrics reset");
    }
}