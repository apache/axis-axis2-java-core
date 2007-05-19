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
package org.apache.axis2.cluster.tribes.context;

import org.apache.axis2.cluster.context.ContextCommandMessage;
import org.apache.axis2.cluster.tribes.context.messages.*;
import org.apache.axis2.context.*;

import java.util.Map;
import java.util.Iterator;
import java.io.Serializable;

/**
 * 
 */
public final class ContextCommandMessageFactory {
    public static final int CREATE = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;

    public static ContextCommandMessage getMessage(AbstractContext abstractContext,
                                                   int operationType) {
        if (abstractContext instanceof ConfigurationContext && operationType == UPDATE) {
            UpdateConfigurationContextCommand cmd = new UpdateConfigurationContextCommand();
            Map diffs = abstractContext.getPropertyDifferences();
            for (Iterator iter = diffs.keySet().iterator(); iter.hasNext();) {
                String key =  (String) iter.next();
                Object prop = abstractContext.getProperty(key);
//                if (prop instanceof Serializable) { //TODO: Handling only Strings now
                if (prop instanceof String || prop instanceof Integer) { //TODO: Handling only Strings now
                    System.err.println("..................... sending prop=" + key + "-" + prop);
                    PropertyDifference diff = (PropertyDifference) diffs.get(key);
                    diff.setValue(prop);

                    // TODO: Before adding it here, exclude all the properties with names matching the exclude patterns
                    cmd.addConfigurationContextProperty(diff);
                }
            }
            abstractContext.clearPropertyDifferences(); // Once we send the diffs, we should clear the diffs
            return cmd;
        } else if (abstractContext instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) abstractContext;
            ServiceGroupContextCommand cmd;
            switch (operationType) {
                case CREATE:
                    cmd = new CreateServiceGroupContextCommand();
                    break;
                case UPDATE:
                    cmd = new UpdateServiceGroupContextCommand();

                    // TODO: Need to get a diff between old & new properties
                    // TODO call UpdateServiceGroupContextCommand#addServiceGroupContextProperty
                    break;
                case DELETE:
                    cmd = new DeleteServiceGroupContextCommand();
                    break;
                default:
                    return null;
            }
            cmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            cmd.setServiceGroupContextId(sgCtx.getId());
            return cmd;
        } else if (abstractContext instanceof ServiceContext) {
            ServiceContext serviceCtx = (ServiceContext) abstractContext;
            ServiceContextCommand cmd;
            switch (operationType) {
                case CREATE:
                    cmd = new CreateServiceContextCommand();
                    ServiceGroupContext parent = (ServiceGroupContext)serviceCtx.getParent();
                    if (parent != null) {
                        ((CreateServiceContextCommand) cmd).setServiceGroupContextId(parent.getId());
                    }
                    break;
                case UPDATE:
                    return new UpdateServiceContextCommand();
                case DELETE:
                    return new DeleteServiceContextCommand();
                default:
                    return null;
            }
            cmd.setServiceGroupName(serviceCtx.getGroupName());
            cmd.setServiceName(serviceCtx.getAxisService().getName());
            return cmd;
        }
        return null;
    }
}
