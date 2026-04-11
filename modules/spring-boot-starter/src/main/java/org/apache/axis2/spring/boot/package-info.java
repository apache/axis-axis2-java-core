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

/**
 * Spring Boot autoconfiguration for Apache Axis2.
 *
 * <p>This package provides autoconfiguration that registers
 * {@link org.apache.axis2.transport.http.AxisServlet} and the OpenAPI/MCP
 * servlet with sensible defaults for both SOAP and JSON-RPC modes.
 *
 * <p>For the full user guide — including how AxisServlet works, SOAP vs JSON
 * mode selection, configuration properties, and migration steps — see
 * {@code src/site/xdoc/docs/spring-boot-starter.xml} in the source tree.
 *
 * @see org.apache.axis2.spring.boot.Axis2Properties
 * @see org.apache.axis2.spring.boot.Axis2AutoConfiguration
 */
package org.apache.axis2.spring.boot;
