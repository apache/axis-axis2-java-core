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

import java.util.ArrayList;

import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.configuration.ConfigurationManagerListener;
import org.apache.axis2.cluster.tribes.ChannelSender;
import org.apache.neethi.Policy;

public class TribesConfigurationManager implements ConfigurationManager {

	private ArrayList listeners = null;
	private ChannelSender sender = null;
	
	
	public TribesConfigurationManager () {
		listeners = new ArrayList ();
	}
	
	public void addConfigurationManagerListener(ConfigurationManagerListener listener) {
		listeners.add(listener);
	}

	public void applyPolicy(String serviceGroupName, Policy policy) {
		throw new UnsupportedOperationException ();
	}

	public void commit() {
		ConfigurationCommand command = new ConfigurationCommand ();
		command.setCommandType(ConfigurationCommand.COMMAND_COMMIT);
		
		send (command);
	}

	public void loadServiceGroup(String serviceGroupName) {
		ConfigurationCommand command = new ConfigurationCommand ();
		command.setCommandType(ConfigurationCommand.COMMAND_LOAD_SERVICE_GROUP);
		command.setSgcName(serviceGroupName);
		
		send (command);
	}

	public void prepare() {
		ConfigurationCommand command = new ConfigurationCommand ();
		command.setCommandType(ConfigurationCommand.COMMAND_PREPARE);
		
		send (command);
	}

	public void reloadConfiguration() {
		ConfigurationCommand command = new ConfigurationCommand ();
		command.setCommandType(ConfigurationCommand.COMMAND_RELOAD_CONFIGURATION);
		
		send (command);		
	}

	public void rollback() {
		ConfigurationCommand command = new ConfigurationCommand ();
		command.setCommandType(ConfigurationCommand.COMMAND_ROLLBACK);
		
		send (command);			
	}

	public void unloadServiceGroup(String serviceGroupName) {
		ConfigurationCommand command = new ConfigurationCommand ();
		command.setCommandType(ConfigurationCommand.COMMAND_LOAD_SERVICE_GROUP);
		command.setSgcName(serviceGroupName);
		
		send (command);
	}

	private void send (ConfigurationCommand command) {
		throw new UnsupportedOperationException ();
	}

	public void setSender(ChannelSender sender) {
		this.sender = sender;
	}
}
