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

import java.util.Date;
import java.util.HashMap;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.filters.Filter;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.subscription.ExpirationBean;

/**
 * Defines a subscriber which is the entity that define a specific subscription 
 * in savan. Independent of the protocol type.
 *
 */
public abstract class AbstractSubscriber implements Subscriber {

	String id;
	Filter filter = null;
	HashMap properties = null;
	
	public AbstractSubscriber () {
		properties = new HashMap ();
	}
	
	public void addProperty (String key, Object value) {
		properties.put(key,value);
	}
	
	public Object getProperty (String key) {
		return properties.get(key);
	}
	
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean doesMessageBelongToTheFilter(SavanMessageContext smc) throws SavanException {
		if (filter!=null) {
			SOAPEnvelope envelope = smc.getEnvelope();
			return filter.checkEnvelopeCompliance(envelope);
		} else 
			return true;
	}
	
	/**
	 * This method first checks weather the passed message complies with the current filter.
	 * If so message is sent, and the subscriberID is added to the PublicationReport.
	 * Else message is ignored.
	 * 
	 * @param smc
	 * @param report
	 * @throws SavanException
	 */
	public void processPublication (SavanMessageContext publication,PublicationReport report) throws SavanException {
		if (doesMessageBelongToTheFilter(publication)) {
			sendPublication(publication,report);
			if (getId()!=null)
				report.addNotifiedSubscriber(getId());
		}
	}
	
	public abstract void setSubscriptionEndingTime (Date subscriptionEndingTime);
	public abstract void renewSubscription (ExpirationBean bean);
	
	/**
	 * This should be used by based classes to sendThe publication in its own manner
	 * 
	 * @param publication
	 * @param report
	 * @throws SavanException
	 */
	protected abstract void sendPublication (SavanMessageContext publication,PublicationReport report) throws SavanException;
}
