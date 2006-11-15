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

import org.apache.axis2.addressing.EndpointReference;

public class Delivery {

	EndpointReference deliveryEPR;
	String deliveryMode;

	public EndpointReference getDeliveryEPR() {
		return deliveryEPR;
	}

	public String getDeliveryMode() {
		return deliveryMode;
	}

	public void setDeliveryEPR(EndpointReference deliveryEPR) {
		this.deliveryEPR = deliveryEPR;
	}

	public void setDeliveryMode(String deliveryMode) {
		this.deliveryMode = deliveryMode;
	}
	
	
	
}
