/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.apache.axis2.clustering.context;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.clustering.context.commands.ContextClusteringCommandCollection;
import org.apache.axis2.clustering.context.commands.UpdateConfigurationContextCommand;
import org.apache.axis2.clustering.context.commands.UpdateContextCommand;
import org.apache.axis2.clustering.context.commands.UpdateServiceContextCommand;
import org.apache.axis2.clustering.context.commands.UpdateServiceGroupContextCommand;
import org.apache.axis2.clustering.tribes.AckManager;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.PropertyDifference;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public final class ContextClusteringCommandFactory {

    private static final Log log = LogFactory.getLog(ContextClusteringCommandFactory.class);

    public static ContextClusteringCommandCollection
            getCommandCollection(AbstractContext[] contexts,
                                 Map excludedReplicationPatterns) {

        ArrayList commands = new ArrayList(contexts.length);
        ContextClusteringCommandCollection collection =
                new ContextClusteringCommandCollection(commands);
        for (int i = 0; i < contexts.length; i++) {
            ContextClusteringCommand cmd = getUpdateCommand(contexts[i],
                                                            excludedReplicationPatterns,
                                                            false);
            if (cmd != null) {
                commands.add(cmd);
            }
        }
        collection.setUniqueId(UUIDGenerator.getUUID());
        AckManager.addInitialAcknowledgement(collection);
        return collection;
    }

    /**
     * @param context
     * @param excludedPropertyPatterns
     * @param includeAllProperties     True - Include all properties,
     *                                 False - Include only property differences
     * @return ContextClusteringCommand
     */
    public static ContextClusteringCommand getUpdateCommand(AbstractContext context,
                                                            Map excludedPropertyPatterns,
                                                            boolean includeAllProperties) {

        UpdateContextCommand cmd = null;
        if (context instanceof ConfigurationContext) {
            cmd = new UpdateConfigurationContextCommand();
        } else if (context instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) context;
            cmd = new UpdateServiceGroupContextCommand();
            UpdateServiceGroupContextCommand updateSgCmd = (UpdateServiceGroupContextCommand) cmd;
            updateSgCmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            updateSgCmd.setServiceGroupContextId(sgCtx.getId());
        } else if (context instanceof ServiceContext) {
            ServiceContext serviceCtx = (ServiceContext) context;
            cmd = new UpdateServiceContextCommand();
            UpdateServiceContextCommand updateServiceCmd = (UpdateServiceContextCommand) cmd;
            String sgName =
                    serviceCtx.getServiceGroupContext().getDescription().getServiceGroupName();
            updateServiceCmd.setServiceGroupName(sgName);
            updateServiceCmd.setServiceGroupContextId(serviceCtx.getServiceGroupContext().getId());
            updateServiceCmd.setServiceName(serviceCtx.getAxisService().getName());
        }
        if (cmd != null) {
            cmd.setUniqueId(UUIDGenerator.getUUID());
            fillProperties(cmd,
                           context,
                           excludedPropertyPatterns,
                           includeAllProperties);
            if (cmd.isPropertiesEmpty()) {
                cmd = null;
            } else {
                AckManager.addInitialAcknowledgement(cmd);
            }
        }

        synchronized (context) {
            context.clearPropertyDifferences(); // Once we send the diffs, we should clear the diffs
        }
        return cmd;
    }

    /**
     * @param updateCmd
     * @param context
     * @param excludedPropertyPatterns
     * @param includeAllProperties     True - Include all properties,
     *                                 False - Include only property differences
     */
    private static void fillProperties(UpdateContextCommand updateCmd,
                                       AbstractContext context,
                                       Map excludedPropertyPatterns,
                                       boolean includeAllProperties) {
        if (!includeAllProperties) {
            synchronized (context) {
                Map diffs = context.getPropertyDifferences();
                for (Iterator iter = diffs.keySet().iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    Object prop = context.getPropertyNonReplicable(key);

                    // First check whether it is serializable
                    if (prop instanceof Serializable) {

                        // Next check whether it matches an excluded pattern
                        if (!isExcluded(key,
                                        context.getClass().getName(),
                                        excludedPropertyPatterns)) {
                            log.debug("sending property =" + key + "-" + prop);
                            PropertyDifference diff = (PropertyDifference) diffs.get(key);
                            diff.setValue(prop);
                            updateCmd.addProperty(diff);
                        }
                    }
                }
            }
        } else {
            synchronized (context) {
                for (Iterator iter = context.getPropertyNames(); iter.hasNext();) {
                    String key = (String) iter.next();
                    Object prop = context.getPropertyNonReplicable(key);
                    if (prop instanceof Serializable) { // First check whether it is serializable

                        // Next check whether it matches an excluded pattern
                        if (!isExcluded(key, context.getClass().getName(), excludedPropertyPatterns)) {
                            log.debug("sending property =" + key + "-" + prop);
                            PropertyDifference diff = new PropertyDifference(key, prop, false);
                            updateCmd.addProperty(diff);
                        }
                    }
                }
            }
        }
    }

    private static boolean isExcluded(String propertyName,
                                      String ctxClassName,
                                      Map excludedPropertyPatterns) {

        // First check in the default excludes
        List defaultExcludes =
                (List) excludedPropertyPatterns.get(DeploymentConstants.TAG_DEFAULTS);
        if (defaultExcludes == null) {
            return false;
        }
        if (isExcluded(defaultExcludes, propertyName)) {
            return true;
        } else {
            // If not, check in the excludes list specific to the context
            List specificExcludes =
                    (List) excludedPropertyPatterns.get(ctxClassName);
            return isExcluded(specificExcludes, propertyName);
        }
    }

    private static boolean isExcluded(List list, String propertyName) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String pattern = (String) iter.next();
            if (pattern.startsWith("*")) {
                pattern = pattern.replaceAll("\\*", "");
                if (propertyName.endsWith(pattern)) {
                    return true;
                }
            } else if (pattern.endsWith("*")) {
                pattern = pattern.replaceAll("\\*", "");
                if (propertyName.startsWith(pattern)) {
                    return true;
                }
            } else if (pattern.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }
}
