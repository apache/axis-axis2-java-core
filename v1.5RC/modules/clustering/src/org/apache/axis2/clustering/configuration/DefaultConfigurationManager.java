/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.clustering.configuration;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.MessageSender;
import org.apache.axis2.clustering.configuration.commands.ApplyServicePolicyCommand;
import org.apache.axis2.clustering.configuration.commands.CommitCommand;
import org.apache.axis2.clustering.configuration.commands.ExceptionCommand;
import org.apache.axis2.clustering.configuration.commands.LoadServiceGroupsCommand;
import org.apache.axis2.clustering.configuration.commands.PrepareCommand;
import org.apache.axis2.clustering.configuration.commands.ReloadConfigurationCommand;
import org.apache.axis2.clustering.configuration.commands.RollbackCommand;
import org.apache.axis2.clustering.configuration.commands.UnloadServiceGroupsCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultConfigurationManager implements ConfigurationManager {
    private static final Log log = LogFactory.getLog(DefaultConfigurationManager.class);

    private ConfigurationManagerListener listener;
    private MessageSender sender;
    private ConfigurationContext configurationContext;
    private Map parameters = new HashMap();

    public DefaultConfigurationManager() {
    }

    public void setConfigurationManagerListener(ConfigurationManagerListener listener) {
        if (configurationContext != null) {
            listener.setConfigurationContext(configurationContext);
        }
        this.listener = listener;
    }

    public void applyPolicy(String serviceGroupName, String policy) throws ClusteringFault {
        log.debug("Enter: DefaultConfigurationManager::applyPolicy");

        ApplyServicePolicyCommand command = new ApplyServicePolicyCommand();
        command.setServiceName(serviceGroupName);
        command.setPolicy(policy);
        send(command);

        log.debug("Exit: DefaultConfigurationManager::applyPolicy");
    }

    public void commit() throws ClusteringFault {
        CommitCommand command = new CommitCommand();
        send(command);
    }

    public void exceptionOccurred(Throwable throwable) throws ClusteringFault {
        sender.sendToGroup(new ExceptionCommand(throwable));
    }

    public void loadServiceGroups(String[] serviceGroupNames) throws ClusteringFault {

        log.debug("Enter: DefaultConfigurationManager::loadServiceGroups");

        LoadServiceGroupsCommand command = new LoadServiceGroupsCommand();
        command.setServiceGroupNames(serviceGroupNames);
        send(command);

        log.debug("Exit: DefaultConfigurationManager::loadServiceGroups");
    }

    public void prepare() throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: DefaultConfigurationManager::prepare");
        }

        PrepareCommand command = new PrepareCommand();
        send(command);

        if (log.isDebugEnabled()) {
            log.debug("Exit: DefaultConfigurationManager::prepare");
        }
    }

    public void reloadConfiguration() throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: DefaultConfigurationManager::reloadConfiguration");
        }

        ReloadConfigurationCommand command = new ReloadConfigurationCommand();
        send(command);

        if (log.isDebugEnabled()) {
            log.debug("Exit: DefaultConfigurationManager::reloadConfiguration");
        }
    }

    public void rollback() throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: DefaultConfigurationManager::rollback");
        }

        RollbackCommand command = new RollbackCommand();
        send(command);

        if (log.isDebugEnabled()) {
            log.debug("Exit: DefaultConfigurationManager::rollback");
        }
    }

    public void unloadServiceGroups(String[] serviceGroupNames) throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: DefaultConfigurationManager::unloadServiceGroups");
        }

        UnloadServiceGroupsCommand command = new UnloadServiceGroupsCommand();
        command.setServiceGroupNames(serviceGroupNames);
        send(command);

        if (log.isDebugEnabled()) {
            log.debug("Exit: DefaultConfigurationManager::unloadServiceGroups");
        }
    }

    protected void send(Throwable throwable) throws ClusteringFault {
        sender.sendToGroup(new ExceptionCommand(throwable));
    }

    protected void send(ConfigurationClusteringCommand command) throws ClusteringFault {
        sender.sendToGroup(command);

        // Need to send the message to self too
        sender.sendToSelf(command);
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public void process(ConfigurationClusteringCommand command) throws ClusteringFault {

        switch (command.getCommandType()) {
            case ConfigurationClusteringCommand.RELOAD_CONFIGURATION:
                listener.configurationReloaded(command);
                break;
            case ConfigurationClusteringCommand.LOAD_SERVICE_GROUPS:
                listener.serviceGroupsLoaded(command);
                break;
            case ConfigurationClusteringCommand.UNLOAD_SERVICE_GROUPS:
                listener.serviceGroupsUnloaded(command);
                break;
            case ConfigurationClusteringCommand.APPLY_SERVICE_POLICY:
                listener.policyApplied(command);
                break;
            case ConfigurationClusteringCommand.PREPARE:
                listener.prepareCalled();
                break;
            case ConfigurationClusteringCommand.COMMIT:
                listener.commitCalled();
                break;
            case ConfigurationClusteringCommand.ROLLBACK:
                listener.rollbackCalled();
                break;
            case ConfigurationClusteringCommand.EXCEPTION:
                listener.handleException(((ExceptionCommand) command).getException());
                break;
            default:
                throw new ClusteringFault("Invalid ConfigurationClusteringCommand " +
                                          command.getClass().getName());
        }
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        if (listener != null) {
            listener.setConfigurationContext(configurationContext);
        }
    }

    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public Parameter getParameter(String name) {
        return (Parameter) parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList list = new ArrayList();
        for (Iterator iter = parameters.keySet().iterator(); iter.hasNext();) {
            list.add(parameters.get(iter.next()));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        return getParameter(parameterName).isLocked();
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }
}
