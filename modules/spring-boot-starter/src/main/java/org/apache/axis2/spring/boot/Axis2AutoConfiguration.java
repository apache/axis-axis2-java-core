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

import org.apache.axis2.transport.http.AxisServlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot autoconfiguration for Apache Axis2.
 *
 * <p>Activates when {@code AxisServlet} is on the classpath and
 * {@code axis2.enabled} is not set to {@code false}.
 *
 * <p>Axis2 supports two protocol modes configured via {@code axis2.mode}:
 * <ul>
 *   <li><b>soap</b> — classic SOAP 1.1/1.2 with full dispatcher stack (since Axis2 1.0, 2006)</li>
 *   <li><b>json</b> — JSON-RPC with MCP/OpenAPI support (default for new projects)</li>
 * </ul>
 *
 * <p>These modes require different axis2.xml configurations and cannot be mixed
 * in a single deployment.
 */
@AutoConfiguration
@ConditionalOnClass(AxisServlet.class)
@ConditionalOnProperty(prefix = "axis2", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(Axis2Properties.class)
public class Axis2AutoConfiguration {
    // Placeholder — sub-configurations (servlet, repository, openapi) will be
    // added as @Import or as separate @AutoConfiguration classes in subsequent steps.
}
