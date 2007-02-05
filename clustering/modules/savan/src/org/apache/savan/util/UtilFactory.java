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

package org.apache.savan.util;

import org.apache.savan.SavanMessageContext;
import org.apache.savan.filters.Filter;
import org.apache.savan.messagereceiver.MessageReceiverDeligater;
import org.apache.savan.subscribers.AbstractSubscriber;
import org.apache.savan.subscription.SubscriptionProcessor;

/**
 * Defines a Utility Factory in Savan. Each Protocol will provide its own set of 
 * utilities.
 * These utilities will be used in various levels in Savan.
 */
public interface UtilFactory {
	
	public abstract SavanMessageContext initializeMessage (SavanMessageContext messageContext);
	public abstract SubscriptionProcessor createSubscriptionProcessor ();
	public abstract MessageReceiverDeligater createMessageReceiverDeligater ();
//	public abstract AbstractSubscriber createSubscriber ();
	
}
