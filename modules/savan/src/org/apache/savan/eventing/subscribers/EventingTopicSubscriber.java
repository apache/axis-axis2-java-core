package org.apache.savan.eventing.subscribers;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.savan.eventing.Delivery;
import org.apache.savan.subscribers.CompositeSubscriber;

public class EventingTopicSubscriber extends CompositeSubscriber implements EventingSubscriber {
	
	private EndpointReference endToEPr;
	
	private Delivery delivery;
	
	public Delivery getDelivery() {
		return delivery;
	}

	public EndpointReference getEndToEPr() {
		return endToEPr;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setEndToEPr(EndpointReference errorReportingEPR) {
		this.endToEPr = errorReportingEPR;
	}
	
	
}
