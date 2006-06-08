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

package org.apache.savan.publication;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.subscribers.Subscriber;


public abstract class PublicationProcessor {

	public PublicationErrorReport notifyListners (SavanMessageContext messageToBeNotified) {
		ConfigurationContext configurationContext = messageToBeNotified.getConfigurationContext();
		HashMap subscribers = (HashMap) configurationContext.getProperty(SavanConstants.SUBSCRIBER_TABLE);
		
		PublicationErrorReport report = new PublicationErrorReport ();
		
		updatePublication (messageToBeNotified);
		
		for (Iterator it=subscribers.keySet().iterator();it.hasNext();) {
			Subscriber subscriber = (Subscriber) subscribers.get(it.next());
			
			//TODO check weather the this subscriber is within the given filter
			try {
				subscriber.sendNotification(messageToBeNotified);
			} catch (SavanException e) {
				report.addReportEntry(subscriber.getId(),e);
			}
		}
		
		return report;
	}
	
	public abstract void updatePublication (SavanMessageContext messageToBeNotified);
}
