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
package org.apache.axis2.cluster.context;

import org.apache.axis2.cluster.context.commands.*;
import org.apache.axis2.context.*;
import org.apache.axis2.deployment.DeploymentConstants;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public final class ContextClusteringCommandFactory {

    public static ContextClusteringCommand getUpdateMessage(AbstractContext context,
                                                            Map excludedPropertyPatterns) {

        ContextClusteringCommand cmd = null;
        if (context instanceof ConfigurationContext) {
            cmd = new UpdateConfigurationContextCommand();
            fillProperties((UpdateContextCommand) cmd,
                           context,
                           excludedPropertyPatterns);
        } else if (context instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) context;
            cmd = new UpdateServiceGroupContextCommand();
            UpdateServiceGroupContextCommand updateSgCmd = (UpdateServiceGroupContextCommand) cmd;

            updateSgCmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            updateSgCmd.setServiceGroupContextId(sgCtx.getId());
            fillProperties((UpdateContextCommand) cmd,
                           context,
                           excludedPropertyPatterns);
            //TODO: impl
        } else if (context instanceof ServiceContext) {
            ServiceContext serviceCtx = (ServiceContext) context;
            cmd = new UpdateServiceContextCommand();
            UpdateServiceContextCommand updateServiceCmd = (UpdateServiceContextCommand) cmd;

            // TODO impl
            updateServiceCmd.setServiceGroupName(serviceCtx.getGroupName());
            updateServiceCmd.setServiceName(serviceCtx.getAxisService().getName());
            fillProperties((UpdateContextCommand) cmd,
                           context,
                           excludedPropertyPatterns);
        }
        context.clearPropertyDifferences(); // Once we send the diffs, we should clear the diffs
        return cmd;
    }

    private static void fillProperties(UpdateContextCommand updateCmd,
                                       AbstractContext context,
                                       Map excludedPropertyPatterns) {
        Map diffs = context.getPropertyDifferences();
        for (Iterator iter = diffs.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Object prop = context.getProperty(key);
            if (prop instanceof Serializable) { // First check whether it is serializable

                // Next check whether it matches an excluded pattern
                if (!isExcluded(key, context.getClass().getName(), excludedPropertyPatterns)) {
                    System.err.println("..................... sending prop=" + key + "-" + prop);
                    PropertyDifference diff = (PropertyDifference) diffs.get(key);
                    diff.setValue(prop);
                    updateCmd.addProperty(diff);
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

    public static ContextClusteringCommand getCreateMessage(AbstractContext abstractContext) {
        if (abstractContext instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) abstractContext;
            ServiceGroupContextCommand cmd = new CreateServiceGroupContextCommand();
            //TODO impl
            cmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            cmd.setServiceGroupContextId(sgCtx.getId());
            return cmd;
        } else if (abstractContext instanceof ServiceContext) {
            ServiceContext serviceCtx = (ServiceContext) abstractContext;
            ServiceContextCommand cmd = new CreateServiceContextCommand();
            ServiceGroupContext parent = (ServiceGroupContext) serviceCtx.getParent();
            if (parent != null) {
                ((CreateServiceContextCommand) cmd).setServiceGroupContextId(parent.getId());
            }
            //TODO: check impl
            cmd.setServiceGroupName(serviceCtx.getGroupName());
            cmd.setServiceName(serviceCtx.getAxisService().getName());
            return cmd;
        }
        return null;
    }

    public static ContextClusteringCommand getRemoveMessage(AbstractContext abstractContext) {
        if (abstractContext instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) abstractContext;
            ServiceGroupContextCommand cmd = new DeleteServiceGroupContextCommand();
            // TODO: impl
            cmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            cmd.setServiceGroupContextId(sgCtx.getId());
            return cmd;
        } else if (abstractContext instanceof ServiceContext) {
            ServiceContext serviceCtx = (ServiceContext) abstractContext;
            ServiceContextCommand cmd = new DeleteServiceContextCommand();
            // TODO: impl
            cmd.setServiceGroupName(serviceCtx.getGroupName());
            cmd.setServiceName(serviceCtx.getAxisService().getName());
            return cmd;
        }
        return null;
    }
}
