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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.subscription.ExpirationBean;

/**
 * Defines a set of subscribers that are acting as a group or a Topic.
 *
 */
public class CompositeSubscriber extends AbstractSubscriber {

	ArrayList subscribers = null;
	
	public CompositeSubscriber (){
		subscribers = new ArrayList ();
	}
	
	public void addSubscriber (AbstractSubscriber subscriber) {
		subscribers.add(subscriber);
	}
	
	public void sendPublication(SavanMessageContext publication, PublicationReport report)  throws SavanException {
		for (Iterator it = subscribers.iterator();it.hasNext();) {
			AbstractSubscriber subscriber = (AbstractSubscriber) it.next();
			subscriber.processPublication(publication,report);
		}
	}

	public void renewSubscription(ExpirationBean bean) {
		for (Iterator it = subscribers.iterator();it.hasNext();) {
			AbstractSubscriber subscriber = (AbstractSubscriber) it.next();
			subscriber.renewSubscription(bean);
		}
	}

	public void setSubscriptionEndingTime(Date subscriptionEndingTime) {
		for (Iterator it = subscribers.iterator();it.hasNext();) {
			AbstractSubscriber subscriber = (AbstractSubscriber) it.next();
			subscriber.setSubscriptionEndingTime(subscriptionEndingTime);
		}
	}

}
