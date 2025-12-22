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

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Interface for customizing OpenAPI specifications after generation.
 *
 * <p>Implementations of this interface can be used to post-process the generated
 * OpenAPI specification, allowing for advanced customizations that are not
 * possible through standard configuration options.</p>
 *
 * <p>Common use cases include:</p>
 * <ul>
 *   <li>Adding custom extensions</li>
 *   <li>Modifying security schemes based on runtime context</li>
 *   <li>Adding custom tags and external documentation</li>
 *   <li>Filtering operations based on user permissions</li>
 *   <li>Enhancing schemas with additional metadata</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * OpenApiCustomizer customizer = new OpenApiCustomizer() {
 *     @Override
 *     public void customize(OpenAPI openAPI) {
 *         // Add custom server
 *         openAPI.addServersItem(new Server().url("https://api.example.com"));
 *
 *         // Add custom extension
 *         openAPI.addExtension("x-custom-property", "custom-value");
 *     }
 * };
 *
 * OpenApiConfiguration config = new OpenApiConfiguration();
 * config.setCustomizer(customizer);
 * }</pre>
 *
 * @since Axis2 2.0.1
 */
public interface OpenApiCustomizer {

    /**
     * Customize the OpenAPI specification after generation.
     *
     * <p>This method is called after the basic OpenAPI specification has been
     * generated from service metadata but before it is serialized to JSON/YAML.
     * Implementations can modify any aspect of the OpenAPI object.</p>
     *
     * @param openAPI the generated OpenAPI specification to customize
     */
    void customize(OpenAPI openAPI);

    /**
     * Set whether to use dynamic base path resolution.
     *
     * <p>When enabled, the base path will be determined dynamically based on
     * the incoming request rather than using a fixed configured value.</p>
     *
     * @param dynamicBasePath true to enable dynamic base path resolution
     */
    default void setDynamicBasePath(boolean dynamicBasePath) {
        // Default implementation does nothing
    }

    /**
     * Check if dynamic base path resolution is enabled.
     *
     * @return true if dynamic base path resolution is enabled
     */
    default boolean isDynamicBasePath() {
        return false;
    }

    /**
     * Get the priority of this customizer.
     *
     * <p>When multiple customizers are configured, they will be applied in
     * order of priority (lower numbers first). Customizers with the same
     * priority will be applied in an undefined order.</p>
     *
     * @return the priority value (default is 0)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Check if this customizer should be applied to the given OpenAPI specification.
     *
     * <p>This method allows customizers to conditionally apply their modifications
     * based on the content of the OpenAPI specification.</p>
     *
     * @param openAPI the OpenAPI specification to check
     * @return true if this customizer should be applied
     */
    default boolean shouldApply(OpenAPI openAPI) {
        return true;
    }
}