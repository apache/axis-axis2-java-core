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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;

/**
 * 
 */
public class CreateServiceContextCommand extends ServiceContextCommand {
    private String serviceGroupContextId;

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        /*AxisService axisService =
                configurationContext.getAxisConfiguration().
                        getService(message.getContextID());
        ServiceGroupContext sgCtx =
                configurationContext.getServiceGroupContext(message.getParentContextID());
        // This will create service context if one is not available
        sgCtx.getServiceContext(axisService);*/

        // TODO: Implementation
        ServiceGroupContext sgCtx =
                configurationContext.getServiceGroupContext(serviceGroupContextId);
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
                        addServiceGroupContextintoApplicatoionScopeTable(sgCtx);
            } else if (scope.equals(Constants.SCOPE_SOAP_SESSION)) {
                configurationContext.
                        registerServiceGroupContextintoSoapSessionTable(sgCtx);
            }
            //TODO: Handle transport session properties
//            configurationContext.s
        }
        try {
            sgCtx.getServiceContext(axisService, false);
        } catch (AxisFault axisFault) {
            throw new ClusteringFault(axisFault);
        }
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }

    public int getCommandType() {
        return CREATE_SERVICE_CONTEXT;
    }
}
