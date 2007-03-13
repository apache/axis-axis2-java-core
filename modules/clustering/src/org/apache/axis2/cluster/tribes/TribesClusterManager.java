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

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.context.ContextManager;
import org.apache.axis2.cluster.tribes.configuration.TribesConfigurationManager;
import org.apache.axis2.cluster.tribes.context.ContextUpdater;
import org.apache.axis2.cluster.tribes.context.TribesContextManager;
import org.apache.axis2.cluster.tribes.info.TransientTribesChannelInfo;
import org.apache.axis2.cluster.tribes.info.TransientTribesMemberInfo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TribesClusterManager implements ClusterManager {


	TribesConfigurationManager configurationManager = null;
	TribesContextManager contextManager = null;
	ConfigurationContext configContext = null;
	private ContextUpdater updater;
	private static long timeout = 1000L; // this should be configured in the axis2.xml
	
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);
    
    public TribesClusterManager () {
		contextManager = new TribesContextManager ();
		configurationManager = new TribesConfigurationManager ();
    }
	
	public ContextManager getContextManager() {
		return contextManager;
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void init(ConfigurationContext context) throws ClusteringFault {
		log.debug("initializing tibes");

		this.configContext = context;
		contextManager.setConfigContext(context);
		
		ChannelSender sender = new ChannelSender ();
		ChannelListener listener = new ChannelListener ();

		TransientTribesChannelInfo channelInfo = new TransientTribesChannelInfo();
		TransientTribesMemberInfo memberInfo = new TransientTribesMemberInfo();

		configContext.setProperty("MEMBER_INFO", memberInfo);
		configContext.setProperty("CHANNEL_INFO", channelInfo);

		contextManager.setSender(sender);
		configurationManager.setSender(sender);
		
		try {
			Channel channel = new GroupChannel();
			channel.addChannelListener (listener);
			channel.addChannelListener(channelInfo);
			channel.addMembershipListener(memberInfo);
			channel.start(Channel.DEFAULT);
			sender.setChannel(channel);
			contextManager.setSender(sender);
			configurationManager.setSender(sender);
			
			updater = new ContextUpdater ();
			contextManager.setUpdater(updater);
			
			listener.setUpdater(updater);
			listener.setContextManager(contextManager);
			
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

}
