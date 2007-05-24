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

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.clustering.control.GetStateResponseCommand;
import org.apache.axis2.clustering.configuration.ConfigurationClusteringCommand;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.context.ContextClusteringCommand;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;


public class ChannelListener implements org.apache.catalina.tribes.ChannelListener {
    private static final Log log = LogFactory.getLog(ChannelListener.class);
    
    private DefaultContextManager contextManager;
    private DefaultConfigurationManager configurationManager;
    private TribesControlCommandProcessor controlCommandProcessor;

    public ChannelListener(DefaultConfigurationManager configurationManager,
                           DefaultContextManager contextManager,
                           TribesControlCommandProcessor controlCommandProcessor) {
        this.configurationManager = configurationManager;
        this.contextManager = contextManager;
        this.controlCommandProcessor = controlCommandProcessor;
    }

    public void setContextManager(DefaultContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public void setConfigurationManager(DefaultConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public boolean accept(Serializable msg, Member sender) {
        return true;
    }

    public void messageReceived(Serializable msg, Member sender) {
        log.debug("Message received : " + msg);
        if (msg instanceof ContextClusteringCommand) {
            try {
                contextManager.notifyListener((ContextClusteringCommand) msg);
            } catch (ClusteringFault e) {
                log.error("Could not process ContextCommand", e);
            }
        } else if (msg instanceof ConfigurationClusteringCommand) {
            try {
                configurationManager.notifyListener((ConfigurationClusteringCommand) msg);
            } catch (ClusteringFault e) {
                log.error("Could not process ConfigurationCommand", e);
            }
        } else if(msg instanceof ControlCommand){
            try {
                controlCommandProcessor.process((ControlCommand) msg, sender);
            } catch (ClusteringFault e) {
                log.error("Could not process ControlCommand", e);
            }
        }
    }
}
