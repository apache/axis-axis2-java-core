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

package org.apache.savan.subscribers;

import java.util.Date;

import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.filters.Filter;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.subscription.ExpirationBean;

/**
 * Defines methods common to all subscribers.
 */
public interface Subscriber {
	
	/**
	 * To get the Filter object
	 * @return
	 */
	Filter getFilter ();
	
	/**
	 * To set the Filter object
	 * @param filter
	 */
	void setFilter (Filter filter);
	
	/**
	 * To get the subscriber Id.
	 * @return
	 */
	String getId ();
	
	/**
	 * To set the subscriber Id
	 * @param id
	 */
	void setId (String id);
	
	/**
	 * To add a property to the subscriber.
	 * 
	 * @param key
	 * @param value
	 */
	void addProperty (String key, Object value);
	
	/**
	 * To get a property from the Subscriber.
	 * 
	 * @param key
	 * @return
	 */
	public Object getProperty (String key);
	
	/**
	 * To check weather a certain message complies with the filter.
	 * 
	 * @param smc
	 * @return
	 * @throws SavanException
	 */
	boolean doesMessageBelongToTheFilter (SavanMessageContext smc) throws SavanException;
	
	
	void processPublication (SavanMessageContext publication,PublicationReport report) throws SavanException;
	
	/**
	 * To set the Subscription expiration time.
	 * 
	 * @param subscriptionEndingTime
	 */
	void setSubscriptionEndingTime (Date subscriptionEndingTime);
	
	/**
	 * To renew a subscription.
	 * 
	 * @param bean
	 */
	void renewSubscription (ExpirationBean bean);

	
	
}
