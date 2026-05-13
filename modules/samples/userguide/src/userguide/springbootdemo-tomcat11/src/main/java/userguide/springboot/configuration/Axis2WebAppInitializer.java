
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
package userguide.springboot.configuration;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.axis2.deployment.WarBasedAxisConfigurator;
import org.apache.axis2.transport.http.AxisServlet;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.core.annotation.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.util.Set; 
 
@Configuration
@Order(4)
public class Axis2WebAppInitializer implements ServletContextInitializer { 
 
    private static final Logger logger = LogManager.getLogger(Axis2WebAppInitializer.class); 
    private static final String SERVICES_MAPPING = "/services/*"; 
 
    @Override 
    public void onStartup(ServletContext container) { 
        logger.warn("inside onStartup() ...");
        // Create the 'root' Spring application context 
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext(); 
 
        addAxis2Servlet(container, ctx);
        addOpenApiServlet(container);
        logger.warn("onStartup() completed ...");
    }

    private void addOpenApiServlet(ServletContext container) {
        ServletRegistration.Dynamic openApi = container.addServlet(
          "OpenApiServlet", new OpenApiServlet());
        openApi.setLoadOnStartup(2);
        openApi.addMapping("/openapi.json", "/openapi.yaml", "/swagger-ui", "/openapi-mcp.json");
        logger.warn("OpenApiServlet registered at /openapi.json, /openapi.yaml, /swagger-ui, /openapi-mcp.json");
    }

    private void addAxis2Servlet(ServletContext container, AnnotationConfigWebApplicationContext ctx) {

        ServletRegistration.Dynamic dispatcher = container.addServlet(
          "AxisServlet", new AxisServlet());
        dispatcher.setLoadOnStartup(1);

        // Set the Axis2 repository path so WarBasedAxisConfigurator finds
        // WEB-INF/conf/axis2.xml, services/*.aar, and modules/*.mar.
        //
        // Priority:
        // 1. System property "axis2.repo" — for embedded mode or custom layouts
        //    (e.g., -Daxis2.repo=/path/to/exploded-war/WEB-INF)
        // 2. ServletContext.getRealPath("/WEB-INF") — works on external Tomcat/WildFly
        //
        // Embedded Tomcat creates a temp docbase that lacks the Axis2 directory
        // structure. When running via "mvn spring-boot:run -Pembedded", pass
        // -Daxis2.repo=target/deploy/axis2-json-api/WEB-INF to point to the
        // exploded WAR from the build.
        String webInfPath = System.getProperty("axis2.repo");
        if (webInfPath == null) {
            webInfPath = container.getRealPath("/WEB-INF");
        }
        logger.info("addAxis2Servlet: axis2.repository.path = " + webInfPath);
        if (webInfPath != null) {
            java.io.File repoDir = new java.io.File(webInfPath);
            if (!repoDir.isDirectory() || !new java.io.File(repoDir, "conf").isDirectory()) {
                logger.warn("axis2.repository.path does not contain conf/ directory: " + webInfPath
                    + ". For embedded mode, set -Daxis2.repo=target/deploy/axis2-json-api/WEB-INF");
            }
            dispatcher.setInitParameter(WarBasedAxisConfigurator.PARAM_AXIS2_REPOSITORY_PATH, webInfPath);

            // Also set axis2.xml.path so the configurator loads the correct
            // axis2.xml (with JSON message builders, enableJSONOnly, etc.)
            // instead of falling back to the minimal classpath default.
            // This is critical for embedded Tomcat where
            // servletContext.getResourceAsStream("/WEB-INF/conf/axis2.xml")
            // returns null because the temp docbase is empty.
            java.io.File axis2xml = new java.io.File(repoDir, "conf/axis2.xml");
            if (axis2xml.isFile()) {
                try {
                    dispatcher.setInitParameter(WarBasedAxisConfigurator.PARAM_AXIS2_XML_PATH,
                        axis2xml.getCanonicalPath());
                } catch (java.io.IOException e) {
                    dispatcher.setInitParameter(WarBasedAxisConfigurator.PARAM_AXIS2_XML_PATH,
                        axis2xml.getAbsolutePath());
                }
                logger.info("addAxis2Servlet: axis2.xml.path = " + axis2xml.getAbsolutePath());
            }
        }

        Set<String> mappingConflicts = dispatcher.addMapping(SERVICES_MAPPING);
        if (!mappingConflicts.isEmpty()) {
            for (String s : mappingConflicts) {
                logger.error("Mapping conflict: " + s);
            }
            throw new IllegalStateException("'AxisServlet' could not be mapped to '" + SERVICES_MAPPING + "'");
        }
    }

}
