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
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * 
 */
public class UpdateServiceGroupContextCommand extends UpdateContextCommand {

    private static Log log = LogFactory.getLog(UpdateServiceGroupContextCommand.class);
    private PropertyUpdater propertyUpdater = new PropertyUpdater();

    protected String serviceGroupName;
    protected String serviceGroupContextId;

    public String getServiceGroupName() {
        return serviceGroupName;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public String getServiceGroupContextId() {
        return serviceGroupContextId;
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        ServiceGroupContext sgCtx =
                configContext.getServiceGroupContext(serviceGroupContextId);

        // If the ServiceGroupContext is not found, create it
        if (sgCtx == null) {
            AxisServiceGroup axisServiceGroup =
                    configContext.getAxisConfiguration()
                            .getServiceGroup(serviceGroupName);
            sgCtx = new ServiceGroupContext(configContext, axisServiceGroup);
            sgCtx.setId(serviceGroupContextId);
            configContext.addServiceGroupContextIntoSoapSessionTable(sgCtx);  // TODO: Check this
        }
        log.debug("###### Gonna update SG prop in " + serviceGroupContextId + "===" + sgCtx);
        propertyUpdater.updateProperties(sgCtx);
    }

    public boolean isPropertiesEmpty() {
        if (propertyUpdater.getProperties() == null) {
            propertyUpdater.setProperties(new HashMap());
            return true;
        }
        return propertyUpdater.getProperties().isEmpty();
    }

    public void addProperty(PropertyDifference diff) {
        if (propertyUpdater.getProperties() == null) {
            propertyUpdater.setProperties(new HashMap());
        }
        propertyUpdater.addContextProperty(diff);
    }
}
