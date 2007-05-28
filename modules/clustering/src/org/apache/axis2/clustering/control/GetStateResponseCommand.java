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
package org.apache.axis2.clustering.control;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.context.ContextClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;

/**
 * 
 */
public class GetStateResponseCommand extends ControlCommand {

    private ContextClusteringCommand[] commands;

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (configurationContext.
                getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
            configurationContext.
                    setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED, "true");
            if (commands != null) {
                for (int i = 0; i < commands.length; i++) {
                    commands[i].execute(configurationContext);
                }
            }
        }
    }

    public void setCommands(ContextClusteringCommand[] commands) {
        this.commands = commands;
    }
}
