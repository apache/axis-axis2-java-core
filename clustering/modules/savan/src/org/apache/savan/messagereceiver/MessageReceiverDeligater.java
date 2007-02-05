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

package org.apache.savan.messagereceiver;

import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;

/**
 * Provide abstract functions that may be done by protocols at the MessageReceiver level.
 *
 */
public interface MessageReceiverDeligater {
	
	/**
	 * Handles a subscription request at the MessageReceiver level.
	 * (may possibly send a subscription response).
	 * 
	 * @param subscriptionMessage
	 * @param outMessage
	 * @throws SavanException
	 */
	void handleSubscriptionRequest (SavanMessageContext subscriptionMessage, MessageContext outMessage) throws SavanException ;
	
	/**
	 * Handles a renew request at the MessageReceiver level.
	 * (may possibly send a renew response)
	 * 
	 * @param renewMessage
	 * @param outMessage
	 * @throws SavanException
	 */
	void handleRenewRequest(SavanMessageContext renewMessage, MessageContext outMessage) throws SavanException;
	
	/**
	 * Handles an EndSubscription request at the MessageReceiver level.
	 * (may possibly send a EndSubscription response)
	 * 
	 * @param renewMessage
	 * @param outMessage
	 * @throws SavanException
	 */
	void handleEndSubscriptionRequest(SavanMessageContext renewMessage, MessageContext outMessage) throws SavanException;
	
	/**
	 * Handles a GetStatus request at the MessageReceiver level.
	 * (may possibly send a GetStatus response).
	 * 
	 * @param renewMessage
	 * @param outMessage
	 * @throws SavanException
	 */
	void handleGetStatusRequest (SavanMessageContext renewMessage, MessageContext outMessage) throws SavanException;
}
