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
package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.clustering.control.GetStateResponseCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;

/**
 * 
 */
public class TribesControlCommandProcessor {
    private ConfigurationContext configurationContext;

    private ChannelSender channelSender;

    public void setChannelSender(ChannelSender channelSender) {
        this.channelSender = channelSender;
    }

    public TribesControlCommandProcessor(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void process(ControlCommand command, Member sender) throws ClusteringFault {

        if (command instanceof GetStateCommand) {

            // If a GetStateRequest is received by a node which has not yet initialized
            // (i.e. by getting the GetStateResponseCommand), this node cannot send a response
            // to the state requester. So we simply return.
            if (configurationContext.
                    getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
                return;
            }
            command.execute(configurationContext);
            GetStateResponseCommand getStateRespCmd = new GetStateResponseCommand();
            getStateRespCmd.setCommands(((GetStateCommand) command).getCommands());
            channelSender.sendToMember(getStateRespCmd, sender);
        } else {
            command.execute(configurationContext);
        }
    }
}
