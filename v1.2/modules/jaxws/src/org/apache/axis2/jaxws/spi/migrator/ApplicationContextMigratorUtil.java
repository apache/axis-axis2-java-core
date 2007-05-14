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
package org.apache.axis2.jaxws.spi.migrator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ApplicationContextMigratorUtil {

    private static final Log log = LogFactory.getLog(ApplicationContextMigrator.class);

    /**
     * Register a new ContextPropertyMigrator.
     *
     * @param configurationContext
     * @param contextMigratorListID The name of the property in the ConfigurationContext that
     *                              contains the list of migrators.
     * @param migrator
     */
    public static void addApplicationContextMigrator(ConfigurationContext configurationContext,
                                                     String contextMigratorListID,
                                                     ApplicationContextMigrator migrator) {
        List<ApplicationContextMigrator> migratorList =
                (List<ApplicationContextMigrator>)configurationContext
                        .getProperty(contextMigratorListID);

        if (migratorList == null) {
            migratorList = new LinkedList<ApplicationContextMigrator>();
            configurationContext.setProperty(contextMigratorListID, migratorList);
        }

        if (log.isDebugEnabled()) {
            log.debug("Adding ApplicationContextMigrator: " + migrator.getClass().getName());
        }
        migratorList.add(migrator);
    }

    /**
     * @param contextMigratorListID
     * @param requestContext
     * @param messageContext
     */
    public static void performMigrationToMessageContext(String contextMigratorListID,
                                                        Map<String, Object> requestContext,
                                                        MessageContext messageContext) {
        if (messageContext == null) {
            throw ExceptionFactory.makeWebServiceException("Null MessageContext");
        }

        ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
        if (sd != null) {
            ConfigurationContext configCtx = sd.getAxisConfigContext();
            List<ApplicationContextMigrator> migratorList =
                    (List<ApplicationContextMigrator>)configCtx.getProperty(contextMigratorListID);

            if (migratorList != null) {
                ListIterator<ApplicationContextMigrator> itr = migratorList.listIterator();
                while (itr.hasNext()) {
                    ApplicationContextMigrator cpm = itr.next();
                    if (log.isDebugEnabled()) {
                        log.debug("migrator: " + cpm.getClass().getName() +
                                ".migratePropertiesToMessageContext");
                    }
                    cpm.migratePropertiesToMessageContext(requestContext, messageContext);
                }
            }
        }
    }

    /**
     * @param contextMigratorListID
     * @param responseContext
     * @param messageContext
     */
    public static void performMigrationFromMessageContext(String contextMigratorListID,
                                                          Map<String, Object> responseContext,
                                                          MessageContext messageContext) {
        if (messageContext == null) {
            throw ExceptionFactory.makeWebServiceException("Null MessageContext");
        }

        ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
        if (sd != null) {
            ConfigurationContext configCtx = sd.getAxisConfigContext();
            List<ApplicationContextMigrator> migratorList =
                    (List<ApplicationContextMigrator>)configCtx.getProperty(contextMigratorListID);

            if (migratorList != null) {
                ListIterator<ApplicationContextMigrator> itr = migratorList.listIterator();
                while (itr.hasNext()) {
                    ApplicationContextMigrator cpm = itr.next();
                    if (log.isDebugEnabled()) {
                        log.debug("migrator: " + cpm.getClass().getName() +
                                ".migratePropertiesFromMessageContext");
                    }
                    cpm.migratePropertiesFromMessageContext(responseContext, messageContext);
                }
            }
        }
    }
}
