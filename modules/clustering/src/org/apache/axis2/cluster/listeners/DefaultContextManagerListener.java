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

package org.apache.axis2.cluster.listeners;

import java.util.Iterator;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.context.ContextEvent;
import org.apache.axis2.cluster.context.ContextManagerListener;
import org.apache.axis2.cluster.tribes.context.ContextType;
import org.apache.axis2.cluster.tribes.context.ContextUpdater;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultContextManagerListener implements ContextManagerListener {

	ContextUpdater updater = null;
	ConfigurationContext configurationContext = null;
	private static final Log log = LogFactory.getLog(DefaultContextManagerListener.class);
	
	public DefaultContextManagerListener (ConfigurationContext configurationContext) {
		this.configurationContext = configurationContext;
	}
	
	public ContextUpdater getUpdater() {
		return updater;
	}

	public void setUpdater(ContextUpdater updater) {
		this.updater = updater;
	}

	public void contextAdded(ContextEvent event) {
		
		try {
			
			if (event.getContextType()==ContextType.SERVICE_GROUP_CONTEXT) {
				AxisServiceGroup axisServiceGroup = configurationContext.getAxisConfiguration()
								.getServiceGroup(event.getDescriptionID());
				ServiceGroupContext ctx = new ServiceGroupContext(configurationContext, axisServiceGroup);
				ctx.setId(event.getContextID());
				configurationContext.registerServiceGroupContextintoSoapSessionTable(ctx);
			} else if (event.getContextType()==ContextType.SERVICE_CONTEXT) {
				AxisService axisService = configurationContext.getAxisConfiguration().
								getService(event.getContextID ());
				ServiceGroupContext srvGrpCtx = configurationContext.getServiceGroupContext(event
								.getParentContextID ());
				// This will create service context if one is not available
				srvGrpCtx.getServiceContext(axisService);
			}
			
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		
	}

	public void contextRemoved(ContextEvent event) {

	}

	public void contextUpdated(ContextEvent event) {

		if (event.getContextType()==ContextType.SERVICE_GROUP_CONTEXT) {
			
			ServiceGroupContext srvGrpCtx = configurationContext.getServiceGroupContext(
							event.getContextID());
			Map props = updater.getServiceGroupProps(event.getContextID());
			if (props != null)
				srvGrpCtx.setProperties(props);
				
		} else if (event.getContextType() == ContextType.SERVICE_CONTEXT) {

			ServiceGroupContext srvGrpCtx = configurationContext.getServiceGroupContext
							(event.getParentContextID ());
			Iterator iter = srvGrpCtx.getServiceContexts();
			String serviceCtxName = event.getDescriptionID();
			ServiceContext serviceContext = null;
			while (iter.hasNext()) {
				ServiceContext serviceContext2 = (ServiceContext) iter.next();
				if (serviceContext2.getName() != null
						&& serviceContext2.getName().equals(serviceCtxName))
					serviceContext = serviceContext2;
			}

			if (serviceContext != null) {

				Map srvProps = updater.getServiceProps(event.getParentContextID(), event.getContextID());

				if (srvProps != null) {
					serviceContext.setProperties(srvProps);
				}

			} else {
				String message = "Cannot find the ServiceContext with the ID:" + serviceCtxName;
				log.error(message);
			}

		}
		
	}

}
