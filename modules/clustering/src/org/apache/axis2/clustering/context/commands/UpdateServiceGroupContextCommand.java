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
package org.apache.axis2.clustering.context.commands;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.context.PropertyUpdater;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.PropertyDifference;
import org.apache.axis2.context.ServiceGroupContext;

import java.util.HashMap;

/**
 * 
 */
public class UpdateServiceGroupContextCommand
        extends ServiceGroupContextCommand implements UpdateContextCommand {

    private PropertyUpdater propertyUpdater = new PropertyUpdater();

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        ServiceGroupContext sgCtx =
                configurationContext.getServiceGroupContext(serviceGroupContextId);
        propertyUpdater.updateProperties(sgCtx);
    }

    public int getCommandType() {
        return UPDATE_SERVICE_GROUP_CONTEXT;
    }

    public void addProperty(PropertyDifference diff) {
        if (propertyUpdater.getProperties() == null) {
            propertyUpdater.setProperties(new HashMap());
        }
        propertyUpdater.addContextProperty(diff);
    }
}
