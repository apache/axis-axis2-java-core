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

package org.apache.savan.subscription;

import java.util.Date;

import org.apache.axis2.databinding.types.Duration;

/**
 * Defines a expiration. Could be based on a specific time in the future or a duration.
 */
public class ExpirationBean {

	Date dateValue;
	Duration durationValue;
	String subscriberID;
	boolean duration;
	
	public String getSubscriberID() {
		return subscriberID;
	}
	
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	
	public boolean isDuration() {
		return duration;
	}
	
	public void setDuration(boolean duration) {
		this.duration = duration;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public Duration getDurationValue() {
		return durationValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public void setDurationValue(Duration durationValue) {
		this.durationValue = durationValue;
	}
}
