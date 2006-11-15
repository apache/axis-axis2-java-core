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

package org.apache.savan.eventing.client;

import java.util.Date;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.databinding.types.Duration;

public class EventingClientBean {

	EndpointReference deliveryEPR;
	EndpointReference endToEPR;
	String filterDialect;
	String filter;
	Date expirationTime;
	Duration expirationDuration;
	
	public Duration getExpirationDuration() {
		return expirationDuration;
	}

	public void setExpirationDuration(Duration expirationDuration) {
		this.expirationDuration = expirationDuration;
	}

	public EndpointReference getDeliveryEPR() {
		return deliveryEPR;
	}
	
	public EndpointReference getEndToEPR() {
		return endToEPR;
	}
	
	public Date getExpirationTime() {
		return expirationTime;
	}
	
	public String getFilter() {
		return filter;
	}
	
	public String getFilterDialect() {
		return filterDialect;
	}
	
	public void setDeliveryEPR(EndpointReference deliveryEPR) {
		this.deliveryEPR = deliveryEPR;
	}
	
	public void setEndToEPR(EndpointReference endToEPR) {
		this.endToEPR = endToEPR;
	}
	
	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public void setFilterDialect(String filterDialect) {
		this.filterDialect = filterDialect;
	}
}
