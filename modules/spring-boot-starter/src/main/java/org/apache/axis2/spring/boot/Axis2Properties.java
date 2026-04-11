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
package org.apache.axis2.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Apache Axis2 Spring Boot integration.
 *
 * <p>SOAP and JSON-RPC require fundamentally different axis2.xml configurations —
 * different message receivers, different dispatchers, and different content-type
 * handling. The {@code axis2.mode} property selects which built-in template to use.
 * Alternatively, provide your own axis2.xml via {@code axis2.configuration-file}.
 *
 * <p>Example {@code application.properties}:
 * <pre>
 * axis2.enabled=true
 * axis2.mode=json
 * axis2.services-path=/services
 * axis2.openapi.enabled=true
 * </pre>
 */
@ConfigurationProperties(prefix = "axis2")
public class Axis2Properties {

    /**
     * Whether Axis2 autoconfiguration is active.
     * Set to false to disable Axis2 servlet registration entirely
     * (useful for server roles that don't serve web services).
     */
    private boolean enabled = true;

    /**
     * Protocol mode: "soap" or "json".
     *
     * <p>This selects the built-in axis2.xml template:
     * <ul>
     *   <li><b>soap</b> — SOAP 1.1/1.2 message receivers (RawXMLINOutMessageReceiver),
     *       full SOAP dispatcher stack (SOAPAction, RequestURI, SOAPMessageBody, etc.),
     *       enableJSONOnly=false. This is the classic Axis2 configuration used since 1.0
     *       (2006). Choose this for WSDL-first services, WS-Security, or interop with
     *       .NET/PHP SOAP clients.</li>
     *   <li><b>json</b> — JSON-RPC message receivers (JsonRpcMessageReceiver),
     *       JSONBasedDefaultDispatcher, enableJSONOnly=true. Choose this for new services,
     *       MCP/AI integration, and REST/OpenAPI consumers.</li>
     * </ul>
     *
     * <p>SOAP and JSON modes cannot be mixed in a single deployment — the message
     * receivers and dispatchers are incompatible. This matches Axis2's runtime behavior.
     *
     * <p>Ignored when {@code axis2.configuration-file} points to a custom axis2.xml.
     */
    private String mode = "json";

    /**
     * Servlet mapping path for AxisServlet (default: /services).
     * The servlet is registered at {services-path}/* .
     */
    private String servicesPath = "/services";

    /**
     * Path to a custom axis2.xml.
     * When set, overrides the built-in template selected by {@code axis2.mode}.
     * Supports classpath: and file: prefixes.
     * Default: empty (use built-in template based on mode).
     */
    private String configurationFile = "";

    /**
     * OpenAPI and MCP sub-configuration.
     */
    private OpenApi openapi = new OpenApi();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getServicesPath() {
        return servicesPath;
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    public OpenApi getOpenapi() {
        return openapi;
    }

    public void setOpenapi(OpenApi openapi) {
        this.openapi = openapi;
    }

    /**
     * OpenAPI/MCP endpoint configuration.
     * Only active when axis2-openapi is on the classpath.
     */
    public static class OpenApi {

        /**
         * Whether to register the OpenAPI servlet at the standard paths.
         * Requires axis2-openapi on the classpath.
         */
        private boolean enabled = true;

        /**
         * Servlet paths for OpenAPI and MCP endpoints.
         */
        private String[] paths = {
            "/openapi.json", "/openapi.yaml", "/swagger-ui", "/openapi-mcp.json"
        };

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String[] getPaths() {
            return paths;
        }

        public void setPaths(String[] paths) {
            this.paths = paths;
        }
    }
}
