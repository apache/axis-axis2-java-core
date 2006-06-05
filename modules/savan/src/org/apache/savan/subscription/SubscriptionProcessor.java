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

package org.apache.savan.subscription;

import java.util.HashMap;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.subscribers.Subscriber;


public abstract class SubscriptionProcessor {
	
	public abstract void init (SavanMessageContext smc) throws SavanException;
	
	public void unsubscribe(SavanMessageContext endSubscriptionMessage)  throws SavanException {
		ConfigurationContext configurationContext = endSubscriptionMessage.getConfigurationContext();
		HashMap subscribers = (HashMap) configurationContext.getProperty(SavanConstants.SUBSCRIBER_TABLE);
		if (subscribers==null) {
			subscribers = new HashMap ();
			configurationContext.setProperty(SavanConstants.SUBSCRIBER_TABLE,subscribers);
		}
		
		String subscriberID = getSubscriberID (endSubscriptionMessage);
		if (subscriberID==null) {
			String message = "Cannot find the subscriber ID";
			throw new SavanException (message);
		}
		
		subscribers.remove(subscriberID);
	}

	public void renewSubscription(SavanMessageContext renewMessage)  throws SavanException {

		ConfigurationContext configurationContext = renewMessage.getConfigurationContext();
		HashMap subscribers = (HashMap) configurationContext.getProperty(SavanConstants.SUBSCRIBER_TABLE);
		if (subscribers==null) {
			throw new SavanException ("Given subscriber is not present");
		}
			
		RenewBean renewBean = getRenewBean(renewMessage);
		Subscriber subscriber = (Subscriber) subscribers.get(renewBean.getSubscriberID());
		if (subscriber==null) {
			throw new SavanException ("Given subscriber is not present");
		}
		
		subscriber.renewSubscription(renewBean.getRenewMount());
	}

	public void subscribe(SavanMessageContext subscriptionMessage) throws SavanException {
		ConfigurationContext configurationContext = subscriptionMessage.getConfigurationContext();
		HashMap subscribers = (HashMap) configurationContext.getProperty(SavanConstants.SUBSCRIBER_TABLE);
		if (subscribers==null) {
			subscribers = new HashMap ();
			configurationContext.setProperty(SavanConstants.SUBSCRIBER_TABLE,subscribers);
		}
		
		Subscriber subscriber = getSubscriberFromMessage (subscriptionMessage);
		subscribers.put(subscriber.getId(),subscriber);
	}
	
	public abstract void pauseSubscription (SavanMessageContext pauseSubscriptionMessage) throws SavanException;
	
	public abstract void resumeSubscription (SavanMessageContext resumeSubscriptionMessage) throws SavanException;
	
	public abstract Subscriber getSubscriberFromMessage (SavanMessageContext smc) throws SavanException;
	
	public abstract RenewBean getRenewBean (SavanMessageContext renewMessage) throws SavanException;
	
	public abstract String getSubscriberID (SavanMessageContext smc) throws SavanException;

}
