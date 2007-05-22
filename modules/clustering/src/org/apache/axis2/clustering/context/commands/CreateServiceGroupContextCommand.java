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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisServiceGroup;

/**
 * 
 */
public class CreateServiceGroupContextCommand extends ServiceGroupContextCommand {

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        AxisServiceGroup axisServiceGroup =
                configContext.getAxisConfiguration()
                        .getServiceGroup(serviceGroupName);
        ServiceGroupContext ctx = new ServiceGroupContext(configContext, axisServiceGroup);
        ctx.setId(serviceGroupContextId);
        configContext.registerServiceGroupContextintoSoapSessionTable(ctx);
    }

    public int getCommandType() {
        return CREATE_SERVICE_GROUP_CONTEXT;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        System.err.println("$$$$$$$$$$$$$$$ set sg ctx ID=" + serviceGroupContextId);
        this.serviceGroupContextId = serviceGroupContextId;
    }
}
