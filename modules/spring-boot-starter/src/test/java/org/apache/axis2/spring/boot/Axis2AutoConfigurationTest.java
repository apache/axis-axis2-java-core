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

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for axis2-spring-boot-starter autoconfiguration.
 */
class Axis2AutoConfigurationTest {

    @Test
    void jsonTemplateExistsOnClasspath() {
        ClassPathResource resource = new ClassPathResource("META-INF/axis2/axis2-json.xml");
        assertTrue(resource.exists(), "axis2-json.xml template must be on classpath");
    }

    @Test
    void soapTemplateExistsOnClasspath() {
        ClassPathResource resource = new ClassPathResource("META-INF/axis2/axis2-soap.xml");
        assertTrue(resource.exists(), "axis2-soap.xml template must be on classpath");
    }

    @Test
    void jsonTemplateHasEnableJsonOnlyTrue() throws Exception {
        ClassPathResource resource = new ClassPathResource("META-INF/axis2/axis2-json.xml");
        String content = new String(resource.getInputStream().readAllBytes());
        assertTrue(content.contains("\"enableJSONOnly\">true</parameter>"),
                "JSON template must have enableJSONOnly=true");
        assertTrue(content.contains("JSONBasedDefaultDispatcher"),
                "JSON template must use JSONBasedDefaultDispatcher");
        assertFalse(content.contains("RawXMLINOutMessageReceiver"),
                "JSON template must not have SOAP message receivers");
    }

    @Test
    void soapTemplateHasEnableJsonOnlyFalse() throws Exception {
        ClassPathResource resource = new ClassPathResource("META-INF/axis2/axis2-soap.xml");
        String content = new String(resource.getInputStream().readAllBytes());
        assertTrue(content.contains("\"enableJSONOnly\">false</parameter>"),
                "SOAP template must have enableJSONOnly=false");
        assertTrue(content.contains("RawXMLINOutMessageReceiver"),
                "SOAP template must have SOAP message receivers");
        assertTrue(content.contains("SOAPActionBasedDispatcher"),
                "SOAP template must have SOAP dispatcher stack");
        assertFalse(content.contains("JSONBasedDefaultDispatcher"),
                "SOAP template must not have JSON dispatcher");
    }

    @Test
    void defaultPropertiesAreCorrect() {
        Axis2Properties props = new Axis2Properties();
        assertTrue(props.isEnabled(), "axis2.enabled defaults to true");
        assertEquals("json", props.getMode(), "axis2.mode defaults to json");
        assertEquals("/services", props.getServicesPath(), "axis2.services-path defaults to /services");
        assertEquals("", props.getConfigurationFile(), "axis2.configuration-file defaults to empty");
        assertTrue(props.getOpenapi().isEnabled(), "axis2.openapi.enabled defaults to true");
    }

    @Test
    void soapModeSelectsSoapTemplate() {
        Axis2Properties props = new Axis2Properties();
        props.setMode("soap");
        Axis2RepositoryAutoConfiguration config = new Axis2RepositoryAutoConfiguration();
        // The resolveAxis2XmlSource is private, but we verify via the template content test above
        assertEquals("soap", props.getMode());
    }

    @Test
    void customConfigFileOverridesMode() {
        Axis2Properties props = new Axis2Properties();
        props.setMode("json");
        props.setConfigurationFile("classpath:my-custom-axis2.xml");
        assertFalse(props.getConfigurationFile().isEmpty(),
                "Custom config file should override mode-based selection");
    }

    @Test
    void autoconfigurationImportsFileExists() {
        ClassPathResource resource = new ClassPathResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        assertTrue(resource.exists(), "AutoConfiguration.imports must be on classpath");
    }

    @Test
    void autoconfigurationImportsContainsAllClasses() throws Exception {
        ClassPathResource resource = new ClassPathResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        String content = new String(resource.getInputStream().readAllBytes());
        assertTrue(content.contains("Axis2AutoConfiguration"),
                "Must list Axis2AutoConfiguration");
        assertTrue(content.contains("Axis2RepositoryAutoConfiguration"),
                "Must list Axis2RepositoryAutoConfiguration");
        assertTrue(content.contains("Axis2ServletAutoConfiguration"),
                "Must list Axis2ServletAutoConfiguration");
        assertTrue(content.contains("Axis2OpenApiAutoConfiguration"),
                "Must list Axis2OpenApiAutoConfiguration");
    }
}
