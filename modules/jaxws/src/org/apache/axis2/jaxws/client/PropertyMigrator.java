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

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MEPContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigrator;

import javax.xml.ws.handler.MessageContext.Scope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The PropertyMigrator implements the ApplicationContextMigrator in order to perform the necessary
 * manipulations of properties during a request or response flow.
 */
public class PropertyMigrator implements ApplicationContextMigrator {

    public void migratePropertiesFromMessageContext(Map<String, Object> userContext,
                                                    MessageContext messageContext) {

        // TODO: we don't want to copy all of the properties to the userContext, just the APPLICATION scoped ones
        MessageContext requestMC = messageContext.getMEPContext().getRequestMessageContext();
        MessageContext responseMC = messageContext.getMEPContext().getResponseMessageContext();
        // sanity check
        if ((requestMC != messageContext) && (responseMC != messageContext))  // object check, not .equals()
            // an exception that should never happen nor be exposed to a user, but it certainly helps us debug
            throw ExceptionFactory.makeWebServiceException("The MessageContext from which we are copying properties does not match the MessageContext in the MEP");
        else if (requestMC == null)
            // TODO also not an exception to expose to a user
            throw ExceptionFactory.makeWebServiceException("The MessageContext from which we are copying properties is null.");

        HashMap<String, Object> mergedProperties = new HashMap<String, Object>();
        // responseMC properties take priority since they are used later in the MEP
        mergedProperties.putAll(requestMC.getProperties());
        if (responseMC != null)
            mergedProperties.putAll(responseMC.getProperties());
        
        for(Iterator it = mergedProperties.entrySet().iterator();it.hasNext();) {
            Entry<String, Object> entry = (Entry<String, Object>)it.next();
            /*
             * Call getScope on the MEPContext so it will check both MCs on the MEPMC.
             * Client apps are permitted to see APPLICATION scoped properties that were
             * set on or changed to APPLICATION scope on the outbound and inbound flows.
             */
            Scope scope = (Scope)messageContext.getMEPContext().getScope(entry.getKey());
            if ((scope != null) && (scope.equals(Scope.APPLICATION))) {
                userContext.put(entry.getKey(), entry.getValue());
            }
        }

    }

    public void migratePropertiesToMessageContext(Map<String, Object> userContext,
                                                  MessageContext messageContext) {

        /*
         * TODO JAXWS 4.2.1 Request section:  should I be setting all props to HANDLER scope?
         * The CTS has a test where a prop is set on the request ctx by a client app, then
         * later that prop is retrieved.  This indicates to me that props set by a client app
         * would be APPLICATION scoped, and the CTS seems to confirm this, in spite of the spec.
         * I'm choosing to conform to the CTS rather than the spec on this one.
         */
        messageContext.getProperties().putAll(userContext);
        if (messageContext.getMEPContext() == null) {
            messageContext.setMEPContext(new MEPContext(messageContext));
        }
        for(Iterator it = userContext.keySet().iterator();it.hasNext();) {
            messageContext.getMEPContext().setScope((String)it.next(), Scope.APPLICATION);
        }

    }

}
