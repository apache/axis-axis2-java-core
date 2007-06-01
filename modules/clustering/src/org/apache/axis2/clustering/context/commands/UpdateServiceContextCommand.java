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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.context.PropertyUpdater;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.PropertyDifference;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * 
 */
public class UpdateServiceContextCommand extends UpdateContextCommand {

    private static final Log log = LogFactory.getLog(UpdateServiceContextCommand.class);

    private PropertyUpdater propertyUpdater = new PropertyUpdater();
    protected String serviceGroupName;
    protected String serviceGroupContextId;
    protected String serviceName;

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        log.debug("Updating service context properties...");
        ServiceGroupContext sgCtx =
                configurationContext.getServiceGroupContext(serviceGroupContextId);
        if (sgCtx != null) {
            try {
                AxisService axisService =
                        configurationContext.getAxisConfiguration().getService(serviceName);
                ServiceContext serviceContext = sgCtx.getServiceContext(axisService);
                propertyUpdater.updateProperties(serviceContext);
            } catch (AxisFault e) {
                throw new ClusteringFault(e);
            }
        } else {
            sgCtx = configurationContext.getServiceGroupContext(serviceGroupContextId);
            AxisService axisService;
            try {
                axisService = configurationContext.getAxisConfiguration().getService(serviceName);
            } catch (AxisFault axisFault) {
                throw new ClusteringFault(axisFault);
            }
            String scope = axisService.getScope();
            if (sgCtx == null) {
                sgCtx = new ServiceGroupContext(configurationContext,
                                                configurationContext.getAxisConfiguration().
                                                        getServiceGroup(serviceGroupName));
                sgCtx.setId(serviceGroupContextId);
                if (scope.equals(Constants.SCOPE_APPLICATION)) {
                    configurationContext.
                            addServiceGroupContextIntoApplicationScopeTable(sgCtx);
                } else if (scope.equals(Constants.SCOPE_SOAP_SESSION)) {
                    configurationContext.
                            addServiceGroupContextIntoSoapSessionTable(sgCtx);
                }
            }
            try {
                ServiceContext serviceContext = sgCtx.getServiceContext(axisService);
                propertyUpdater.updateProperties(serviceContext);
            } catch (AxisFault axisFault) {
                throw new ClusteringFault(axisFault);
            }
        }
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
