/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.savan.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.ConfigurationManager;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscription.SubscriptionProcessor;
import org.apache.savan.util.UtilFactory;
import org.apache.savan.util.ProtocolManager;

/**
 * The handler of Savan in the InFlow.
 * Will handle the control messages like subscription, renew, unsubscription.
 * 
 */
public class SavanInHandler extends AbstractHandler  {

	public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
		
		SavanMessageContext smc = new SavanMessageContext (msgContext);
		
		//setting the Protocol
		Protocol protocol = ProtocolManager.getMessageProtocol(smc);
		smc.setProtocol(protocol);
		
		AxisService axisService = msgContext.getAxisService();
		if (axisService==null)
			throw new SavanException ("Service context is null");
		
		//setting the AbstractSubscriber Store
		Parameter parameter = axisService.getParameter(SavanConstants.SUBSCRIBER_STORE);
		if (parameter==null){
			setSubscriberStore (smc);
			parameter = axisService.getParameter(SavanConstants.SUBSCRIBER_STORE);
		}
		
		UtilFactory utilFactory = smc.getProtocol().getUtilFactory();
		utilFactory.initializeMessage (smc);
		
		int messageType = smc.getMessageType ();

		SubscriptionProcessor processor = utilFactory.createSubscriptionProcessor ();
		processor.init (smc);
		if (messageType==SavanConstants.MessageTypes.SUBSCRIPTION_MESSAGE) {
		   processor.subscribe(smc);
		} else if (messageType==SavanConstants.MessageTypes.UNSUBSCRIPTION_MESSAGE) {
			processor.unsubscribe(smc);
		} else if (messageType==SavanConstants.MessageTypes.RENEW_MESSAGE) {
			processor.renewSubscription(smc);
		}
        return InvocationResponse.CONTINUE;        
	}
	
	private void setSubscriberStore (SavanMessageContext smc) throws SavanException {
		MessageContext msgContext = smc.getMessageContext();
		AxisService axisService = msgContext.getAxisService();
		
		Parameter parameter = axisService.getParameter(SavanConstants.SUBSCRIBER_STORE_KEY);
		String subscriberStoreKey = SavanConstants.DEFAULT_SUBSCRIBER_STORE_KEY;
		if (parameter!=null)
			subscriberStoreKey = (String) parameter.getValue();
		
		ConfigurationManager configurationManager = (ConfigurationManager) smc.getConfigurationContext().getProperty(SavanConstants.CONFIGURATION_MANAGER);
		SubscriberStore store = configurationManager.getSubscriberStoreInstance(subscriberStoreKey);

		parameter = new Parameter ();
		parameter.setName(SavanConstants.SUBSCRIBER_STORE);
		parameter.setValue(store);
		
		try {
			axisService.addParameter(parameter);
		} catch (AxisFault e) {
			throw new SavanException (e);
		}
		
	}
	
}
