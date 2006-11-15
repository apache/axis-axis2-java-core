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

/**
 * Encapsulates a data for a subscrpition renewal.
 */
public class RenewBean {

	long renewMount;
	String subscriberID;
	
	public long getRenewMount() {
		return renewMount;
	}
	public String getSubscriberID() {
		return subscriberID;
	}
	public void setRenewMount(long renewMount) {
		this.renewMount = renewMount;
	}
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	
	
}
