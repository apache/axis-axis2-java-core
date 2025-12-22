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

package org.apache.axis2.openapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Swagger UI customization and behavior.
 *
 * <p>This class provides comprehensive configuration options for customizing
 * the Swagger UI interface, including appearance, behavior, and integration
 * settings.</p>
 *
 * <p>Common configuration options include:</p>
 * <ul>
 *   <li>OpenAPI specification URL</li>
 *   <li>UI theme and styling</li>
 *   <li>Default request/response formats</li>
 *   <li>Authentication and security settings</li>
 *   <li>Plugin and extension configuration</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SwaggerUiConfig config = new SwaggerUiConfig();
 * config.setUrl("/openapi.json");
 * config.setDocExpansion("none");
 * config.setDeepLinking(true);
 * config.setDisplayOperationId(false);
 * }</pre>
 *
 * @since Axis2 2.0.1
 */
public class SwaggerUiConfig {

    // ========== Core Configuration ==========

    /** URL to OpenAPI specification */
    private String url = "/openapi.json";

    /** URLs to multiple OpenAPI specifications */
    private String urls;

    /** Primary name for the API specification */
    private String primaryName;

    // ========== Display Configuration ==========

    /** Controls the default expansion setting for the operations and tags */
    private String docExpansion = "list"; // none, list, full

    /** Controls the display of operationId in operations list */
    private boolean displayOperationId = false;

    /** Controls the default expansion setting for the models */
    private Integer defaultModelsExpandDepth = 1;

    /** Controls how many levels to show when models are first rendered */
    private Integer defaultModelExpandDepth = 1;

    /** Controls the display of the request duration (in milliseconds) for Try it out requests */
    private boolean displayRequestDuration = false;

    // ========== Navigation Configuration ==========

    /** Enables deep linking for tags and operations */
    private boolean deepLinking = false;

    /** Enables filtering by tag */
    private boolean filter;

    /** Maximum number of tagged operations to show */
    private Integer maxDisplayedTags;

    // ========== Request/Response Configuration ==========

    /** List of HTTP methods that have the Try it out feature enabled */
    private String[] supportedSubmitMethods = {"get", "put", "post", "delete", "options", "head", "patch", "trace"};

    /** OAuth redirect URL */
    private String oauth2RedirectUrl;

    /** Show/hide the request headers section */
    private boolean showRequestHeaders = false;

    /** Show/hide the response headers section */
    private boolean showResponseHeaders = true;

    // ========== Validation Configuration ==========

    /** Enables validation of requests and responses */
    private boolean validatorUrl;

    // ========== Custom Configuration ==========

    /** Custom CSS URL for styling */
    private String customCss;

    /** Custom JavaScript for additional functionality */
    private String customJs;

    /** Additional custom configuration parameters */
    private Map<String, Object> configParameters = new HashMap<>();

    /** Query configuration enabled */
    private String queryConfigEnabled = "false";

    // ========== Constructors ==========

    /**
     * Default constructor with standard Swagger UI configuration.
     */
    public SwaggerUiConfig() {
        // Set default values
        setDefaults();
    }

    /**
     * Constructor with OpenAPI specification URL.
     *
     * @param url the URL to the OpenAPI specification
     */
    public SwaggerUiConfig(String url) {
        this();
        this.url = url;
    }

    // ========== Configuration Methods ==========

    private void setDefaults() {
        // Set sensible defaults for Axis2 integration
        docExpansion = "none";
        deepLinking = true;
        displayOperationId = false;
        filter = true;
        showRequestHeaders = true;
        validatorUrl = false;
    }

    // ========== Getters and Setters ==========

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUrls() { return urls; }
    public void setUrls(String urls) { this.urls = urls; }

    public String getPrimaryName() { return primaryName; }
    public void setPrimaryName(String primaryName) { this.primaryName = primaryName; }

    public String getDocExpansion() { return docExpansion; }
    public void setDocExpansion(String docExpansion) { this.docExpansion = docExpansion; }

    public boolean isDisplayOperationId() { return displayOperationId; }
    public void setDisplayOperationId(boolean displayOperationId) { this.displayOperationId = displayOperationId; }

    public Integer getDefaultModelsExpandDepth() { return defaultModelsExpandDepth; }
    public void setDefaultModelsExpandDepth(Integer defaultModelsExpandDepth) {
        this.defaultModelsExpandDepth = defaultModelsExpandDepth;
    }

    public Integer getDefaultModelExpandDepth() { return defaultModelExpandDepth; }
    public void setDefaultModelExpandDepth(Integer defaultModelExpandDepth) {
        this.defaultModelExpandDepth = defaultModelExpandDepth;
    }

    public boolean isDisplayRequestDuration() { return displayRequestDuration; }
    public void setDisplayRequestDuration(boolean displayRequestDuration) {
        this.displayRequestDuration = displayRequestDuration;
    }

    public boolean isDeepLinking() { return deepLinking; }
    public void setDeepLinking(boolean deepLinking) { this.deepLinking = deepLinking; }

    public boolean isFilter() { return filter; }
    public void setFilter(boolean filter) { this.filter = filter; }

    public Integer getMaxDisplayedTags() { return maxDisplayedTags; }
    public void setMaxDisplayedTags(Integer maxDisplayedTags) { this.maxDisplayedTags = maxDisplayedTags; }

    public String[] getSupportedSubmitMethods() { return supportedSubmitMethods; }
    public void setSupportedSubmitMethods(String[] supportedSubmitMethods) {
        this.supportedSubmitMethods = supportedSubmitMethods;
    }

    public String getOauth2RedirectUrl() { return oauth2RedirectUrl; }
    public void setOauth2RedirectUrl(String oauth2RedirectUrl) { this.oauth2RedirectUrl = oauth2RedirectUrl; }

    public boolean isShowRequestHeaders() { return showRequestHeaders; }
    public void setShowRequestHeaders(boolean showRequestHeaders) { this.showRequestHeaders = showRequestHeaders; }

    public boolean isShowResponseHeaders() { return showResponseHeaders; }
    public void setShowResponseHeaders(boolean showResponseHeaders) { this.showResponseHeaders = showResponseHeaders; }

    public boolean isValidatorUrl() { return validatorUrl; }
    public void setValidatorUrl(boolean validatorUrl) { this.validatorUrl = validatorUrl; }

    public String getCustomCss() { return customCss; }
    public void setCustomCss(String customCss) { this.customCss = customCss; }

    public String getCustomJs() { return customJs; }
    public void setCustomJs(String customJs) { this.customJs = customJs; }

    public Map<String, Object> getConfigParameters() { return configParameters; }
    public void setConfigParameters(Map<String, Object> configParameters) {
        this.configParameters = configParameters;
    }

    public String getQueryConfigEnabled() { return queryConfigEnabled; }
    public void setQueryConfigEnabled(String queryConfigEnabled) {
        this.queryConfigEnabled = queryConfigEnabled;
    }

    // ========== Convenience Methods ==========

    /**
     * Add a custom configuration parameter.
     */
    public void addConfigParameter(String key, Object value) {
        configParameters.put(key, value);
    }

    /**
     * Get all configuration as a map for JavaScript generation.
     */
    public Map<String, Object> toConfigMap() {
        Map<String, Object> config = new HashMap<>();

        if (url != null) config.put("url", url);
        if (urls != null) config.put("urls", urls);
        if (primaryName != null) config.put("primaryName", primaryName);

        config.put("docExpansion", docExpansion);
        config.put("displayOperationId", displayOperationId);
        config.put("defaultModelsExpandDepth", defaultModelsExpandDepth);
        config.put("defaultModelExpandDepth", defaultModelExpandDepth);
        config.put("displayRequestDuration", displayRequestDuration);
        config.put("deepLinking", deepLinking);
        config.put("filter", filter);

        if (maxDisplayedTags != null) config.put("maxDisplayedTags", maxDisplayedTags);

        config.put("supportedSubmitMethods", supportedSubmitMethods);

        if (oauth2RedirectUrl != null) config.put("oauth2RedirectUrl", oauth2RedirectUrl);

        config.put("showRequestHeaders", showRequestHeaders);
        config.put("showResponseHeaders", showResponseHeaders);
        config.put("validatorUrl", validatorUrl);

        if (customCss != null) config.put("customCss", customCss);
        if (customJs != null) config.put("customJs", customJs);

        // Add custom parameters
        config.putAll(configParameters);

        return config;
    }

    /**
     * Generate JavaScript configuration object.
     */
    public String toJavaScriptConfig() {
        StringBuilder js = new StringBuilder();
        js.append("{\n");

        Map<String, Object> config = toConfigMap();
        boolean first = true;

        for (Map.Entry<String, Object> entry : config.entrySet()) {
            if (!first) js.append(",\n");
            first = false;

            js.append("  ").append(entry.getKey()).append(": ");

            Object value = entry.getValue();
            if (value instanceof String) {
                js.append("\"").append(value).append("\"");
            } else if (value instanceof String[]) {
                js.append("[");
                String[] array = (String[]) value;
                for (int i = 0; i < array.length; i++) {
                    if (i > 0) js.append(", ");
                    js.append("\"").append(array[i]).append("\"");
                }
                js.append("]");
            } else {
                js.append(value);
            }
        }

        js.append("\n}");
        return js.toString();
    }

    /**
     * Create a copy of this configuration.
     */
    public SwaggerUiConfig copy() {
        SwaggerUiConfig copy = new SwaggerUiConfig();

        copy.url = this.url;
        copy.urls = this.urls;
        copy.primaryName = this.primaryName;
        copy.docExpansion = this.docExpansion;
        copy.displayOperationId = this.displayOperationId;
        copy.defaultModelsExpandDepth = this.defaultModelsExpandDepth;
        copy.defaultModelExpandDepth = this.defaultModelExpandDepth;
        copy.displayRequestDuration = this.displayRequestDuration;
        copy.deepLinking = this.deepLinking;
        copy.filter = this.filter;
        copy.maxDisplayedTags = this.maxDisplayedTags;
        copy.oauth2RedirectUrl = this.oauth2RedirectUrl;
        copy.showRequestHeaders = this.showRequestHeaders;
        copy.showResponseHeaders = this.showResponseHeaders;
        copy.validatorUrl = this.validatorUrl;
        copy.customCss = this.customCss;
        copy.customJs = this.customJs;
        copy.queryConfigEnabled = this.queryConfigEnabled;

        if (this.supportedSubmitMethods != null) {
            copy.supportedSubmitMethods = this.supportedSubmitMethods.clone();
        }

        copy.configParameters = new HashMap<>(this.configParameters);

        return copy;
    }

    @Override
    public String toString() {
        return "SwaggerUiConfig{" +
                "url='" + url + '\'' +
                ", docExpansion='" + docExpansion + '\'' +
                ", deepLinking=" + deepLinking +
                ", filter=" + filter +
                '}';
    }
}