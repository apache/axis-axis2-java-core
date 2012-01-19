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

package org.apache.axis2.extensions.spring.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.InputStream;

/**
 * Util class of the spring module. It contains a method which is used to get
 * the spring application context for given spring web services.
 * 
 * @since 1.7.0
 */
public class ApplicationContextUtil {
    public static final String SPRING_APPLICATION_CONTEXT = "SpringApplicationContext";
    public static final String SPRING_APPLICATION_CONTEXT_LOCATION = "SpringContextLocation";

    /**
     * Method to get the spring application context for a spring service. This
     * method will first check the META-INF(or meta-inf) directory for the
     * '<service-name>-application-context.xml file. If the file is not found
     * then it will check whether file path is set as a parameter in
     * service.xml. If the context file is set as a parameter for a service
     * group, then the context will be add to the group or else it will be add
     * to the service.
     * 
     * @param axisService
     * @return GenericApplicationContext
     * @throws AxisFault
     */

    public static GenericApplicationContext getSpringApplicationContext(AxisService axisService)
            throws AxisFault {

        GenericApplicationContext appContext;
        Parameter appContextParameter = axisService.getParameter(SPRING_APPLICATION_CONTEXT);
        Parameter contextLocationParam = axisService
                .getParameter(SPRING_APPLICATION_CONTEXT_LOCATION);

        // return the application context
        if (appContextParameter != null) {
            appContext = (GenericApplicationContext) appContextParameter.getValue();
            // if the context is not found initialize a new one
        } else {
            appContext = new GenericApplicationContext();
            ClassLoader serviceCL = axisService.getClassLoader();
            appContext.setClassLoader(serviceCL);
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(serviceCL);
                XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);

                // load the bean context file from the parameter
                if (contextLocationParam != null) {
                    xbdr.loadBeanDefinitions(new ClassPathResource((String) contextLocationParam
                            .getValue()));
                    appContext.refresh();
                    AxisServiceGroup axisServiceGroup = axisService.getAxisServiceGroup();
                    Parameter springGroupCtxLocation = axisServiceGroup
                            .getParameter(SPRING_APPLICATION_CONTEXT_LOCATION);
                    // add the context to the service group or add it to the
                    // service
                    if (springGroupCtxLocation != null) {
                        axisServiceGroup.addParameter(new Parameter(SPRING_APPLICATION_CONTEXT,
                                appContext));
                    } else {
                        axisService.addParameter(new Parameter(SPRING_APPLICATION_CONTEXT,
                                appContext));
                    }
                    return appContext;
                }

                InputStream ctxFileInputStream = serviceCL
                        .getResourceAsStream(DeploymentConstants.META_INF + File.separator
                                + axisService.getName() + "-application-context.xml");
                // try for meta-inf
                if (ctxFileInputStream == null) {
                    ctxFileInputStream = serviceCL.getResourceAsStream(DeploymentConstants.META_INF
                            .toLowerCase()
                            + File.separator
                            + axisService.getName()
                            + "-application-context.xml");
                }
                // load the context file from meta-inf
                if (ctxFileInputStream != null) {
                    xbdr.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
                    xbdr.loadBeanDefinitions(new InputStreamResource(ctxFileInputStream));
                    appContext.refresh();
                    axisService.addParameter(new Parameter(SPRING_APPLICATION_CONTEXT, appContext));
                    return appContext;
                } else {
                    throw new AxisFault("Spring context file cannot be located for AxisService");
                }
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            } finally {
                // restore the class loader
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }
        return appContext;
    }
}
