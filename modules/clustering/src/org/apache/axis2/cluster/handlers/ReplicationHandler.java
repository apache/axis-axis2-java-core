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

package org.apache.axis2.cluster.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;

public class ReplicationHandler extends AbstractHandler {

	public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

		replicateState(msgContext);

		return InvocationResponse.CONTINUE;
	}

	public void flowComplete(MessageContext msgContext) {
		super.flowComplete(msgContext);

		replicateState(msgContext);
	}

	private void replicateState(MessageContext message) {

		ConfigurationContext configurationContext = message.getConfigurationContext();
		AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
		ClusterManager clusterManager = axisConfiguration.getClusterManager();

		if (clusterManager != null) {
			ServiceContext serviceContext = message.getServiceContext();
			ServiceGroupContext serviceGroupContext = message.getServiceGroupContext();

			clusterManager.updateState(configurationContext);

			if (serviceGroupContext != null) {
				clusterManager.updateState(serviceGroupContext);
			}

			if (serviceContext != null) {
				clusterManager.updateState(serviceContext);
			}

		}

	}

}
