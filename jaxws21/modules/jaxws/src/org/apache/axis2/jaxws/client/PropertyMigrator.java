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
package org.apache.axis2.jaxws.client;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigrator;

import java.util.Map;

/**
 * The PropertyMigrator implements the ApplicationContextMigrator in order to perform the necessary
 * manipulations of properties during a request or response flow.
 */
public class PropertyMigrator implements ApplicationContextMigrator {

    public void migratePropertiesFromMessageContext(Map<String, Object> userContext,
                                                    MessageContext messageContext) {

    }

    public void migratePropertiesToMessageContext(Map<String, Object> userContext,
                                                  MessageContext messageContext) {
        // Copy all of the properties from the request context into the MessageContext
        messageContext.getProperties().putAll(userContext);
    }

}
