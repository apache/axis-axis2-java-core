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

package org.apache.axis2.cluster.tribes;

import java.io.Serializable;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.context.ContextEvent;
import org.apache.axis2.cluster.context.ContextManager;
import org.apache.axis2.cluster.tribes.configuration.TribesConfigurationManager;
import org.apache.axis2.cluster.tribes.context.ContextListenerEventType;
import org.apache.axis2.cluster.tribes.context.ContextType;
import org.apache.axis2.cluster.tribes.context.ContextUpdater;
import org.apache.axis2.cluster.tribes.context.ContextCommandMessage;
import org.apache.axis2.cluster.tribes.context.TribesContextManager;
import org.apache.axis2.cluster.tribes.context.ContextUpdateEntryCommandMessage;
import org.apache.axis2.cluster.tribes.info.TransientTribesChannelInfo;
import org.apache.axis2.cluster.tribes.info.TransientTribesMemberInfo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TribesClusterManager implements ClusterManager, ChannelListener {


	TribesConfigurationManager descriptionManager = null;
	TribesContextManager contextManager = null;
	ConfigurationContext configContext = null;
	private Channel channel;
	private ContextUpdater updater;
	private static long timeout = 1000L; // this should be configured in the axis2.xml
	
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);
    
    public TribesClusterManager () {
		contextManager = new TribesContextManager ();
		descriptionManager = new TribesConfigurationManager ();
    }
	
	public ContextManager getContextManager() {
		return contextManager;
	}

	public ConfigurationManager getConfigurationManager() {
		return descriptionManager;
	}

	public void init(ConfigurationContext context) throws ClusteringFault {
		log.debug("initializing tibes");

		try {
			this.configContext = context;

			TransientTribesChannelInfo channelInfo = new TransientTribesChannelInfo();
			TransientTribesMemberInfo memberInfo = new TransientTribesMemberInfo();

			configContext.setProperty("MEMBER_INFO", memberInfo);
			configContext.setProperty("CHANNEL_INFO", channelInfo);

			channel = new GroupChannel();
			channel.addChannelListener(this);
			channel.addChannelListener(channelInfo);
			channel.addMembershipListener(memberInfo);
			channel.start(Channel.DEFAULT);
			updater = new ContextUpdater (channel, TribesClusterManager.timeout, context.getAxisConfiguration()
					.getSystemClassLoader());
			
			contextManager.setChannel(channel);
			contextManager.setUpdater(updater);
			contextManager.setConfigContext(context);
			
			registerTribesInfoService(configContext);
		} catch (ChannelException e) {
			String message = "Error starting Tribes channel";
			throw new ClusteringFault (message, e);
		}
	}

	private void registerTribesInfoService(ConfigurationContext configContext2) throws ClusteringFault {
		try {
			AxisService service = AxisService.createService(
					"org.apache.axis2.cluster.tribes.info.TribesInfoWebService", configContext
							.getAxisConfiguration(), RPCMessageReceiver.class);

			configContext.getAxisConfiguration().addService(service);
		} catch (AxisFault e) {
			String message = "Unable to create Tribes info web service";
			throw new ClusteringFault (message, e);
		}
	}

	public void messageReceived(Serializable msg, Member sender) {

		if (!(msg instanceof ContextCommandMessage)) {
			return;
		}

		ContextCommandMessage comMsg = (ContextCommandMessage) msg;

		// TODO make sure to remove from the duplicate lists when remove is
		// requested for both service group and service contexts
		// TODO fix this to support scopes other than SOAP Session.

		if (comMsg.getCommandName().equals(CommandType.CREATE_SERVICE_GROUP_CONTEXT)) {

			// add to the duplicate list to prevent cyclic replication
			contextManager.addToDuplicateServiceGroupContexts(comMsg.getContextId());
			updater.addServiceGroupContext(comMsg.getContextId());

			ContextEvent event = new ContextEvent();
			event.setContextType(ContextType.SERVICE_GROUP_CONTEXT);
			event.setContextID(comMsg.getContextId());
			event.setParentContextID(comMsg.getParentId());
			event.setDescriptionID(comMsg.getAxisDescriptionName());

			contextManager.notifyListeners(event, ContextListenerEventType.ADD_CONTEXT);

		} else if (comMsg.getCommandName().equals(CommandType.CREATE_SERVICE_CONTEXT)) {

			// add to the duplicate list to prevent cyclic replication
			contextManager.addToDuplicateServiceContexts(comMsg.getParentId()
					+ comMsg.getContextId());
			updater.addServiceContext(comMsg.getParentId(), comMsg.getContextId());

			ContextEvent event = new ContextEvent();
			event.setContextType(ContextType.SERVICE_CONTEXT);
			event.setContextID(comMsg.getContextId());
			event.setParentContextID(comMsg.getParentId());
			event.setDescriptionID(comMsg.getAxisDescriptionName());

			contextManager.notifyListeners(event, ContextListenerEventType.ADD_CONTEXT);

		} else if (comMsg.getCommandName().equals(CommandType.UPDATE_STATE)) {

			if (comMsg.getContextType() == ContextType.SERVICE_GROUP_CONTEXT) {

				ContextEvent event = new ContextEvent();
				event.setContextType(ContextType.SERVICE_GROUP_CONTEXT);
				event.setContextID(comMsg.getContextId());
				event.setParentContextID(comMsg.getParentId());
				event.setDescriptionID(comMsg.getAxisDescriptionName());

				contextManager.notifyListeners(event, ContextListenerEventType.UPDATE_CONTEXT);

			} else if (comMsg.getContextType() == ContextType.SERVICE_CONTEXT) {

				ContextEvent event = new ContextEvent();
				event.setContextType(ContextType.SERVICE_CONTEXT);
				event.setContextID(comMsg.getContextId());
				event.setParentContextID(comMsg.getParentId());
				event.setDescriptionID(comMsg.getAxisDescriptionName());

				contextManager.notifyListeners(event, ContextListenerEventType.UPDATE_CONTEXT);

			}

		} else if (comMsg.getCommandName().equals(CommandType.UPDATE_STATE_MAP_ENTRY)) {

			ContextUpdateEntryCommandMessage mapEntryMsg = (ContextUpdateEntryCommandMessage) comMsg;
			if (mapEntryMsg.getCtxType() == ContextUpdateEntryCommandMessage.SERVICE_GROUP_CONTEXT) {
				Map props = updater.getServiceGroupProps(comMsg.getContextId());
				if (mapEntryMsg.getOperation() == ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY) {
					props.put(mapEntryMsg.getKey(), mapEntryMsg.getValue());
				} else {
					props.remove(mapEntryMsg.getKey());
				}
			} else if (mapEntryMsg.getCtxType() == ContextUpdateEntryCommandMessage.SERVICE_CONTEXT) {
				Map props = updater.getServiceProps(comMsg.getParentId(), comMsg.getContextId());
				if (mapEntryMsg.getOperation() == ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY) {
					props.put(mapEntryMsg.getKey(), mapEntryMsg.getValue());
				} else {
					props.remove(mapEntryMsg.getKey());
				}
			}
		}
	}

	public boolean accept(Serializable msg, Member sender) {
		// return msg instanceof TribesCommandMessage;
		return true;
	}

}
