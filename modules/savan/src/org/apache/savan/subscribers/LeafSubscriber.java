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

package org.apache.savan.subscribers;

import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;


public abstract class LeafSubscriber extends Subscriber {
	
	private long subscriptionEndingTime = -1;
	
	public void renewSubscription (long renewAmount) {
		if (subscriptionEndingTime<0)
			subscriptionEndingTime = 0;
		
		subscriptionEndingTime = subscriptionEndingTime + renewAmount;
	}
	
	public long getSubscriptionEndingTime () {
		return subscriptionEndingTime;
	}
	
	public void sendNotification(SavanMessageContext notificationMessage) throws SavanException {
		long timeNow = System.currentTimeMillis();
		
		if (subscriptionEndingTime>0 && subscriptionEndingTime<=timeNow) {
			String message = "Cant notify the listner since the subscription ending time has been passed";
			throw new SavanException (message);
		}
		
		doProtocolSpecificNotification (notificationMessage);
	}
	
	public abstract void doProtocolSpecificNotification (SavanMessageContext notificationMessage) throws SavanException;
}
