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

package org.apache.axis2.cluster.tribes.configuration;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.CommandType;
import org.apache.axis2.cluster.configuration.ConfigurationEvent;
import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.configuration.ConfigurationManagerListener;
import org.apache.axis2.cluster.tribes.ChannelSender;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.neethi.Policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TribesConfigurationManager implements ConfigurationManager {

    private List listeners = null;
    private ChannelSender sender = null;
    private AxisConfiguration axisConfiguration = null;

    public TribesConfigurationManager() {
        listeners = new ArrayList();
    }

    public void addConfigurationManagerListener(ConfigurationManagerListener listener) {
        if (axisConfiguration != null) {
            listener.setAxisConfiguration(axisConfiguration);
        }

        listeners.add(listener);
    }

    public void applyPolicy(String serviceGroupName, Policy policy) throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.APPLY_POLICY);
        command.setSgcName(serviceGroupName);
        command.setPolicyId(policy.getId());
        send(command);
    }

    public void commit() throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.COMMIT);
        send(command);
    }

    public void exceptionOccurred(Throwable throwable) throws ClusteringFault {
        send(throwable);
    }

    public void loadServiceGroup(String serviceGroupName) throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.LOAD_SERVICE_GROUP);
        command.setSgcName(serviceGroupName);
        send(command);
    }

    public void prepare() throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.PREPARE);
        send(command);
    }

    public void reloadConfiguration() throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.RELOAD_CONFIGURATION);
        send(command);
    }

    public void rollback() throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.ROLLBACK);
        send(command);
    }

    public void unloadServiceGroup(String serviceGroupName) throws ClusteringFault {
        ConfigurationCommand command = new ConfigurationCommand(CommandType.UNLOAD_SERVICE_GROUP);
        command.setSgcName(serviceGroupName);
        send(command);
    }

    private void send(Throwable throwable) throws ClusteringFault {
        sender.send(throwable);
    }

    private void send(ConfigurationCommand command) throws ClusteringFault {
        sender.send(command);

        // Need to send the message to self too
        Channel channel = sender.getChannel();
        try {
            channel.send(new Member[]{channel.getLocalMember(true)},
                         command,
                         Channel.SEND_OPTIONS_USE_ACK);
        } catch (ChannelException e) {
            throw new ClusteringFault(e);
        }
    }

    public void setSender(ChannelSender sender) {
        this.sender = sender;
    }

    public void notifyListeners(int command, ConfigurationEvent event) {

        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ConfigurationManagerListener listener = (ConfigurationManagerListener) it.next();

            if (CommandType.LOAD_SERVICE_GROUP == command) {
                listener.serviceGroupLoaded(event);
            } else if (CommandType.UNLOAD_SERVICE_GROUP == command) {
                listener.serviceGroupUnloaded(event);
            } else if (CommandType.APPLY_POLICY == command) {
                listener.policyApplied(event);
            } else if (CommandType.RELOAD_CONFIGURATION == command) {
                listener.configurationReloaded(event);
            } else if (CommandType.PREPARE == command) {
                listener.prepareCalled(event);
            } else if (CommandType.COMMIT == command) {
                listener.commitCalled(event);
            } else if (CommandType.ROLLBACK == command) {
                listener.rollbackCalled(event);
            }
        }
    }

    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }

}
