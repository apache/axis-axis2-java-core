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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;

public class ContextPropertyMigratorUtil {
    
    /**
     * Register a new ContextPropertyMigrator.
     * 
     * @param configurationContext
     * @param contextMigratorListID The name of the property in the
     *                              ConfigurationContext that contains
     *                              the list of migrators.
     * @param migrator
     */
    public static void addContextPropertyMigrator(ConfigurationContext configurationContext, 
                                                  String contextMigratorListID, 
                                                  ContextPropertyMigrator migrator) {
        List<ContextPropertyMigrator> migratorList = (List<ContextPropertyMigrator>) configurationContext.getProperty(contextMigratorListID);

        if (migratorList == null) {
            migratorList = new LinkedList<ContextPropertyMigrator>();
            configurationContext.setProperty(contextMigratorListID, migratorList);
        }
      
        migratorList.add(migrator);
    }

    /**
     * 
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
        
        ConfigurationContext configCtx = messageContext.getServiceDescription().getAxisConfigContext();
        List<ContextPropertyMigrator> migratorList = (List<ContextPropertyMigrator>) configCtx.getProperty(contextMigratorListID);
        
        if (migratorList != null) {
            ListIterator<ContextPropertyMigrator> itr = migratorList.listIterator();
            while (itr.hasNext()) {
                ContextPropertyMigrator cpm = itr.next();
                cpm.migratePropertiesToMessageContext(requestContext, messageContext);
            }
        }        
    }
    
    /**
     * 
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
        
        ConfigurationContext configCtx = messageContext.getServiceDescription().getAxisConfigContext();
        List<ContextPropertyMigrator> migratorList = (List<ContextPropertyMigrator>) configCtx.getProperty(contextMigratorListID);
        
        if (migratorList != null) {
            ListIterator<ContextPropertyMigrator> itr = migratorList.listIterator();
            while (itr.hasNext()) {
                ContextPropertyMigrator cpm = itr.next();
                cpm.migratePropertiesFromMessageContext(responseContext, messageContext);
            }
        }        
    }
}
