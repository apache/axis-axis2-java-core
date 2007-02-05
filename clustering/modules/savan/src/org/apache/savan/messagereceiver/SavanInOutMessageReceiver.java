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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.util.UtilFactory;

/**
 * InOut message deceiver for Savan. 
 * May get called for control messages depending on the protocol.
 */
public class SavanInOutMessageReceiver extends AbstractInOutSyncMessageReceiver {

	public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {

		SavanMessageContext savanInMessage = new SavanMessageContext (inMessage);
		
//		Integer protocolVersion = (Integer) inMessage.getProperty(SavanConstants.PROTOCOL_VERSION);
//		UtilFactory utilFactory = AbstractSavanUtilFactory.getUtilFactory(protocolVersion.intValue());
		
		Protocol protocol = savanInMessage.getProtocol();
		if (protocol==null) {
			throw new SavanException ("Protocol not found");
		}
		
		UtilFactory utilFactory = protocol.getUtilFactory();
		MessageReceiverDeligater deligator = utilFactory.createMessageReceiverDeligater();

		int messageType = savanInMessage.getMessageType();
		if (messageType==SavanConstants.MessageTypes.SUBSCRIPTION_MESSAGE) {
			deligator.handleSubscriptionRequest(savanInMessage,outMessage);
		} else if (messageType==SavanConstants.MessageTypes.RENEW_MESSAGE) {
			deligator.handleRenewRequest (savanInMessage,outMessage);
		} else if (messageType==SavanConstants.MessageTypes.UNSUBSCRIPTION_MESSAGE) {
			deligator.handleEndSubscriptionRequest (savanInMessage,outMessage);
		} else if (messageType==SavanConstants.MessageTypes.GET_STATUS_MESSAGE) {
			deligator.handleGetStatusRequest (savanInMessage,outMessage);
		}
		
	}

}
