package org.apache.savan.eventing.subscribers;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.savan.eventing.Delivery;
import org.apache.savan.subscribers.Subscriber;

/**
 * Defines methods common to all eventing subscribers.
 */
public interface EventingSubscriber extends Subscriber {
	
	/**
	 * To get the EndTo EPR
	 * @return
	 */
	EndpointReference getEndToEPr();
	
	/**
	 * To get the Delivery object
	 * @return
	 */
	Delivery getDelivery();
	
	/**
	 * To se the Delivery object
	 * @param delivery
	 */
	void setDelivery(Delivery delivery);
	
	/**
	 * To set the EndTo EPR
	 * @param errorReportingEPR
	 */
	void setEndToEPr(EndpointReference errorReportingEPR);
	
}
