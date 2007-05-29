/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.feature.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class WebServiceFeatureConfigUtil {

    private static final Log log = LogFactory.getLog(WebServiceFeatureConfigUtil.class);

    /**
     * 
     * @param configurationContext
     * @param contextConfiguratorListID
     * @param configurator
     */
    public static void addWebServiceFeatureConfigurator(ConfigurationContext configurationContext,
                                                     String contextConfiguratorListID,
                                                     WebServiceFeatureConfigurator configurator) {
        List<WebServiceFeatureConfigurator> migratorList =
                (List<WebServiceFeatureConfigurator>)configurationContext
                        .getProperty(contextConfiguratorListID);

        if (migratorList == null) {
            migratorList = new LinkedList<WebServiceFeatureConfigurator>();
            configurationContext.setProperty(contextConfiguratorListID, migratorList);
        }

        if (log.isDebugEnabled()) {
            log.debug("Adding WebServiceFeatureConfigurator: " + configurator.getClass().getName());
        }
        migratorList.add(configurator);
    }

    /**
     * 
     * @param contextConfiguratorListID
     * @param messageContext
     * @param provider
     */
    public static void performConfiguration(String contextConfiguratorListID,
                                            MessageContext messageContext,
                                            BindingProvider provider) {
        if (messageContext == null) {
            throw ExceptionFactory.makeWebServiceException("Null MessageContext");
        }

        ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
        if (sd != null) {
            ConfigurationContext configCtx = sd.getAxisConfigContext();
            List<WebServiceFeatureConfigurator> migratorList =
                    (List<WebServiceFeatureConfigurator>)configCtx.getProperty(contextConfiguratorListID);

            if (migratorList != null) {
                ListIterator<WebServiceFeatureConfigurator> itr = migratorList.listIterator();
                while (itr.hasNext()) {
                    WebServiceFeatureConfigurator con = itr.next();
                    if (log.isDebugEnabled()) {
                        log.debug("configurator: " + con.getClass().getName() +
                                ".performConfiguration");
                    }
                    con.performConfiguration(messageContext, provider);
                }
            }
        }
    }

    /**
     * 
     * @param contextConfiguratorListID
     * @param messageContext
     * @param provider
     */
    public static void performConfiguration(String contextConfiguratorListID,
                                            ServiceDescription serviceDescription) {
        if (serviceDescription == null) {
            throw ExceptionFactory.makeWebServiceException("Null Service Description");
        }

        ConfigurationContext configCtx = serviceDescription.getAxisConfigContext();
        List<WebServiceFeatureConfigurator> migratorList =
                (List<WebServiceFeatureConfigurator>)configCtx.getProperty(contextConfiguratorListID);

        if (migratorList != null) {
            ListIterator<WebServiceFeatureConfigurator> itr = migratorList.listIterator();
            while (itr.hasNext()) {
                WebServiceFeatureConfigurator con = itr.next();
                if (log.isDebugEnabled()) {
                    log.debug("configurator: " + con.getClass().getName() +
                            ".performConfiguration");
                }
                con.performConfiguration(serviceDescription);
            }
        }
    }
}
