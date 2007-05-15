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
import org.apache.axis2.cluster.listeners.DefaultContextManagerListener;
import org.apache.axis2.cluster.tribes.ChannelSender;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.AxisFault;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;

import java.util.*;

public class TribesConfigurationManager implements ConfigurationManager {

    private List listeners = null;
    private ChannelSender sender = null;
    private ConfigurationContext configurationContext = null;
    private Map parameters = new HashMap();
	private static final Log log = LogFactory.getLog(TribesConfigurationManager.class);
	
    public TribesConfigurationManager() {
        listeners = new ArrayList();
    }

    public void addConfigurationManagerListener(ConfigurationManagerListener listener) {
        if (configurationContext != null) {
            listener.setConfigurationContext(configurationContext);
        }

        listeners.add(listener);
    }

    public void applyPolicy(String serviceGroupName, String policy) throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::applyPolicy");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.APPLY_POLICY);
        command.setServiceName(serviceGroupName);
        command.setPolicy(policy);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::applyPolicy");
    }

    public void commit() throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::commit");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.COMMIT);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::commit");
    }

    public void exceptionOccurred(Throwable throwable) throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::exceptionOccurred");
    	
        send(throwable);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::exceptionOccurred");
    }

    public void loadServiceGroups(String[] serviceGroupNames) throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::loadServiceGroups");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.LOAD_SERVICE_GROUPS);
        command.setServiceGroupNames(serviceGroupNames);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::loadServiceGroups");
    }

    public void prepare() throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::prepare");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.PREPARE);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::prepare");
    }

    public void reloadConfiguration() throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::reloadConfiguration");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.RELOAD_CONFIGURATION);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::reloadConfiguration");
    }

    public void rollback() throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::rollback");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.ROLLBACK);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::rollback");
    }

    public void unloadServiceGroups(String[] serviceGroupNames) throws ClusteringFault {
    	
    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::unloadServiceGroups");
    	
        ConfigurationCommand command = new ConfigurationCommand(CommandType.UNLOAD_SERVICE_GROUPS);
        command.setServiceGroupNames(serviceGroupNames);
        send(command);
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::unloadServiceGroups");
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

    	if (log.isDebugEnabled())
			log.debug("Enter: TribesConfigurationManager::notifyListeners");
    	
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ConfigurationManagerListener listener = (ConfigurationManagerListener) it.next();

            if (CommandType.LOAD_SERVICE_GROUPS == command) {
                listener.serviceGroupsLoaded(event);
            } else if (CommandType.UNLOAD_SERVICE_GROUPS == command) {
                listener.serviceGroupsUnloaded(event);
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
        
    	if (log.isDebugEnabled())
			log.debug("Exit: TribesConfigurationManager::notifyListeners");
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ConfigurationManagerListener listener = (ConfigurationManagerListener) it.next();
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
