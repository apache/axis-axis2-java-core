
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

import javax.servlet.ServletContext; 
import javax.servlet.ServletRegistration; 

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
        logger.warn("onStartup() completed ...");
    }
 
    private void addAxis2Servlet(ServletContext container, AnnotationConfigWebApplicationContext ctx) { 

        ServletRegistration.Dynamic dispatcher = container.addServlet(
          "AxisServlet", new AxisServlet());
        dispatcher.setLoadOnStartup(1);
        Set<String> mappingConflicts = dispatcher.addMapping(SERVICES_MAPPING);
        if (!mappingConflicts.isEmpty()) { 
            for (String s : mappingConflicts) { 
                logger.error("Mapping conflict: " + s); 
            } 
            throw new IllegalStateException("'AxisServlet' could not be mapped to '" + SERVICES_MAPPING + "'"); 
        }
    }

}
