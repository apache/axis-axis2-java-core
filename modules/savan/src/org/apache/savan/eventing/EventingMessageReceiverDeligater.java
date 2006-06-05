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

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.messagereceiver.MessageReceiverDeligater;


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
			subscriptionManagerElement = subscriptionManagerEPR.toOM(EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.SubscriptionManager,EventingConstants.EVENTING_PREFIX);
		} catch (AxisFault e) {
			throw new SavanException (e);
		}
		
		//TODO set expires
		
		subscribeResponseElement.addChild(subscriptionManagerElement);
		outMessageEnvelope.getBody().addChild(subscribeResponseElement);
		
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
		String expiresValue = (String) renewMessage.getProperty(EventingConstants.TransferedProperties.EXPIRES_VALUE);
		if (expiresValue!=null) {
			OMElement expiresElement = factory.createOMElement(EventingConstants.ElementNames.Expires,ens);
			renewResponseElement.addChild(expiresElement);
		}
		
		outMessageEnvelope.getBody().addChild(renewResponseElement);
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
	}

	public void handleGetStatusRequest(SavanMessageContext getStatusMessage, MessageContext outMessage) throws SavanException {

		if (outMessage==null)
			throw new SavanException ("Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
		
		MessageContext subscriptionMsgCtx = getStatusMessage.getMessageContext();
		
		String id = (String) getStatusMessage.getProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID);
		if (id==null)
			throw new SavanException ("Cannot fulfil request. Subscriber ID not found");
		
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
		
		ConfigurationContext configurationContext = getStatusMessage.getConfigurationContext();
		HashMap subscribers = (HashMap) configurationContext.getProperty(SavanConstants.SUBSCRIBER_TABLE);
		
		if (subscribers==null) {
			throw new SavanException ("Subscriber not found");
		}
		
		EventingSubscriber subscriber = (EventingSubscriber) subscribers.get(id);
		if (subscriber==null) {
			throw new SavanException ("Subscriber not found");
		}
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);

		OMElement getStatusResponseElement = factory.createOMElement(EventingConstants.ElementNames.GetStatusResponse,ens);
		
		long expires = subscriber.getSubscriptionEndingTime();
		if (expires>0) {
			OMElement expiresElement = factory.createOMElement(EventingConstants.ElementNames.Expires,ens);
			expiresElement.setText(new Long (expires).toString());
			
			getStatusResponseElement.addChild(expiresElement);
		}
		
		outMessageEnvelope.getBody().addChild(getStatusResponseElement);
	}
	
	

}
