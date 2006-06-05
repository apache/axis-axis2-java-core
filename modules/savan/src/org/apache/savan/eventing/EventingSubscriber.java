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

package org.apache.savan.eventing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.subscribers.LeafSubscriber;



public class EventingSubscriber extends LeafSubscriber {

	private EndpointReference endToEPr;
	
	private Delivery delivery;
	
	private Filter filter;
	
	public Delivery getDelivery() {
		return delivery;
	}

	public EndpointReference getEndToEPr() {
		return endToEPr;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setEndToEPr(EndpointReference errorReportingEPR) {
		this.endToEPr = errorReportingEPR;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	public void doProtocolSpecificNotification(SavanMessageContext notificationMessage) throws SavanException {
		
		EndpointReference deliveryEPR  = delivery.getDeliveryEPR();
		
		try {
			ServiceClient sc = new ServiceClient (null,null);
			Options options = new Options ();
			options.setTo(deliveryEPR);
			sc.setOptions(options);
			
			//TODO correct this to send the complete envelope.
			sc.fireAndForget (notificationMessage.getEnvelope().getBody().getFirstElement());
			
		} catch (AxisFault e) {
			throw new SavanException (e);
		}
		
		
	}

}
