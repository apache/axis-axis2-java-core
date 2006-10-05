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
