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
