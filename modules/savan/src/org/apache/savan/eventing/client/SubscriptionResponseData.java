package org.apache.savan.eventing.client;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.savan.subscription.ExpirationBean;

public class SubscriptionResponseData {

	EndpointReference subscriptionManager = null;
	ExpirationBean  expiration = null;
	
	public SubscriptionResponseData () {
		expiration = new ExpirationBean ();
	}

	public EndpointReference getSubscriptionManager() {
		return subscriptionManager;
	}

	public ExpirationBean getExpiration() {
		return expiration;
	}

	public void setExpiration(ExpirationBean expiration) {
		this.expiration = expiration;
	}

	public void setSubscriptionManager(EndpointReference subscriptionManager) {
		this.subscriptionManager = subscriptionManager;
	}
}
