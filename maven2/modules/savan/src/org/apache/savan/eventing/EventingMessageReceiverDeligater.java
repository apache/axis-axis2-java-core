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

import java.util.Calendar;
import java.util.Date;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.eventing.subscribers.EventingLeafSubscriber;
import org.apache.savan.messagereceiver.MessageReceiverDeligater;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.AbstractSubscriber;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.util.CommonUtil;


public class EventingMessageReceiverDeligater implements MessageReceiverDeligater {

	public void handleSubscriptionRequest(SavanMessageContext subscriptionMessage, MessageContext outMessage) throws SavanException {
		
		if (outMessage==null)
			throw new SavanException ("Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
		
		MessageContext subscriptionMsgCtx = subscriptionMessage.getMessageContext();
		
		SOAPEnvelope outMessageEnvelope = outMessage.getEnvelope();
		SOAPFactory factory = null;
		
		if (outMessageEnvelope!=null) {
			factory = (SOAPFactory) outMessageEnvelope.getOMFactory();
		} else {
			factory = (SOAPFactory) subscriptionMsgCtx.getEnvelope().getOMFactory();
			outMessageEnvelope = factory.getDefaultEnvelope();
			
			try {
				outMessage.setEnvelope(outMessageEnvelope);
			} catch (AxisFault e) {
				throw new SavanException (e);
			}
		}
		
		//setting the action
		outMessage.getOptions().setAction(EventingConstants.Actions.SubscribeResponse);
			
		//sending the subscription response message.
		String address = subscriptionMsgCtx.getOptions().getTo().getAddress();
		EndpointReference subscriptionManagerEPR = new EndpointReference (address);

		String id = (String) subscriptionMessage.getProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID);
		if (id==null)
			throw new SavanException ("Subscription UUID is not set");
		
		subscriptionManagerEPR.addReferenceParameter(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Identifier,EventingConstants.EVENTING_PREFIX),id);
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
		OMElement subscribeResponseElement = factory.createOMElement(EventingConstants.ElementNames.SubscribeResponse,ens);
		OMElement subscriptionManagerElement = null;
		try {
			subscriptionManagerElement = EndpointReferenceHelper.toOM(subscribeResponseElement.getOMFactory(), subscriptionManagerEPR, new QName(EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.SubscriptionManager,EventingConstants.EVENTING_PREFIX), AddressingConstants.Submission.WSA_NAMESPACE);
		} catch (AxisFault e) {
			throw new SavanException (e);
		}
		
		//TODO set expires
		
		subscribeResponseElement.addChild(subscriptionManagerElement);
		outMessageEnvelope.getBody().addChild(subscribeResponseElement);
		
		//setting the message type
		outMessage.setProperty(SavanConstants.MESSAGE_TYPE,new Integer (SavanConstants.MessageTypes.SUBSCRIPTION_RESPONSE_MESSAGE));
	
	}
	
	public void handleRenewRequest(SavanMessageContext renewMessage, MessageContext outMessage) throws SavanException {
		
		if (outMessage==null)
			throw new SavanException ("Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
		
		MessageContext subscriptionMsgCtx = renewMessage.getMessageContext();
		
		SOAPEnvelope outMessageEnvelope = outMessage.getEnvelope();
		SOAPFactory factory = null;
		
		if (outMessageEnvelope!=null) {
			factory = (SOAPFactory) outMessageEnvelope.getOMFactory();
		} else {
			factory = (SOAPFactory) subscriptionMsgCtx.getEnvelope().getOMFactory();
			outMessageEnvelope = factory.getDefaultEnvelope();
			
			try {
				outMessage.setEnvelope(outMessageEnvelope);
			} catch (AxisFault e) {
				throw new SavanException (e);
			}
		}
		
		//setting the action
		outMessage.getOptions().setAction(EventingConstants.Actions.RenewResponse);
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
			
		//sending the Renew Response message.
		OMElement renewResponseElement = factory.createOMElement(EventingConstants.ElementNames.RenewResponse,ens);
		String subscriberID = (String) renewMessage.getProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID);
		if (subscriberID==null) {
			String message = "SubscriberID TransferedProperty is not set";
			throw new SavanException (message);
		}

		SubscriberStore store = CommonUtil.getSubscriberStore(renewMessage.getMessageContext().getAxisService());
		Subscriber subscriber = store.retrieve(subscriberID);
		EventingLeafSubscriber eventingSubscriber = (EventingLeafSubscriber) subscriber;
		if (eventingSubscriber==null) {
			String message = "Cannot find the AbstractSubscriber with the given ID";
			throw new SavanException (message);
		}
		
		Date expiration = eventingSubscriber.getSubscriptionEndingTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(expiration);
		
		String expiresValue = ConverterUtil.convertToString(calendar);
		
		if (expiresValue!=null) {
			OMElement expiresElement = factory.createOMElement(EventingConstants.ElementNames.Expires,ens);
			renewResponseElement.addChild(expiresElement);
		}
		
		outMessageEnvelope.getBody().addChild(renewResponseElement);
		
		//setting the message type
		outMessage.setProperty(SavanConstants.MESSAGE_TYPE,new Integer (SavanConstants.MessageTypes.RENEW_RESPONSE_MESSAGE));
	}

	public void handleEndSubscriptionRequest(SavanMessageContext renewMessage, MessageContext outMessage) throws SavanException {
		
		if (outMessage==null)
			throw new SavanException ("Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
		
		MessageContext subscriptionMsgCtx = renewMessage.getMessageContext();
		
		//setting the action
		outMessage.getOptions().setAction(EventingConstants.Actions.UnsubscribeResponse);
		
		SOAPEnvelope outMessageEnvelope = outMessage.getEnvelope();
		SOAPFactory factory = null;
		
		if (outMessageEnvelope!=null) {
			factory = (SOAPFactory) outMessageEnvelope.getOMFactory();
		} else {
			factory = (SOAPFactory) subscriptionMsgCtx.getEnvelope().getOMFactory();
			outMessageEnvelope = factory.getDefaultEnvelope();
			
			try {
				outMessage.setEnvelope(outMessageEnvelope);
			} catch (AxisFault e) {
				throw new SavanException (e);
			}
		}	
		
		//setting the message type
		outMessage.setProperty(SavanConstants.MESSAGE_TYPE,new Integer (SavanConstants.MessageTypes.UNSUBSCRIPTION_RESPONSE_MESSAGE));
	}

	public void handleGetStatusRequest(SavanMessageContext getStatusMessage, MessageContext outMessage) throws SavanException {

		if (outMessage==null)
			throw new SavanException ("Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
		
		MessageContext subscriptionMsgCtx = getStatusMessage.getMessageContext();
		
		String id = (String) getStatusMessage.getProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID);
		if (id==null)
			throw new SavanException ("Cannot fulfil request. AbstractSubscriber ID not found");
		
		//setting the action
		outMessage.getOptions().setAction(EventingConstants.Actions.UnsubscribeResponse);
		
		SOAPEnvelope outMessageEnvelope = outMessage.getEnvelope();
		SOAPFactory factory = null;
		
		if (outMessageEnvelope!=null) {
			factory = (SOAPFactory) outMessageEnvelope.getOMFactory();
		} else {
			factory = (SOAPFactory) subscriptionMsgCtx.getEnvelope().getOMFactory();
			outMessageEnvelope = factory.getDefaultEnvelope();
			
			try {
				outMessage.setEnvelope(outMessageEnvelope);
			} catch (AxisFault e) {
				throw new SavanException (e);
			}
		}
		
		SubscriberStore store = CommonUtil.getSubscriberStore(getStatusMessage.getMessageContext().getAxisService());
		
		
		if (store==null) {
			throw new SavanException ("AbstractSubscriber Store was not found");
		}
		
		EventingLeafSubscriber subscriber = (EventingLeafSubscriber) store.retrieve(id);
		if (subscriber==null) {
			throw new SavanException ("AbstractSubscriber not found");
		}
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
		OMElement getStatusResponseElement = factory.createOMElement(EventingConstants.ElementNames.GetStatusResponse,ens);
		
		Date expires = subscriber.getSubscriptionEndingTime();
		if (expires!=null) {
			OMElement expiresElement = factory.createOMElement(EventingConstants.ElementNames.Expires,ens);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(expires);
			String expirationString = ConverterUtil.convertToString(calendar);
			expiresElement.setText(expirationString);
			getStatusResponseElement.addChild(expiresElement);
		}
		
		outMessageEnvelope.getBody().addChild(getStatusResponseElement);
		
		//setting the message type
		outMessage.setProperty(SavanConstants.MESSAGE_TYPE,new Integer (SavanConstants.MessageTypes.GET_STATUS_RESPONSE_MESSAGE));
	}
	
	

}
