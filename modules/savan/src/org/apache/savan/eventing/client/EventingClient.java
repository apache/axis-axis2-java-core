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

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.savan.eventing.EventingConstants;

public class EventingClient {

	ServiceClient serviceClient = null;
	HashMap subscriptionDataMap = null;
	
	public EventingClient (ServiceClient serviceClient) {
		this.serviceClient = serviceClient;
		subscriptionDataMap = new HashMap (); 
	}
	
	public void subscribe (EventingClientBean bean, String subscriptionID) throws Exception {
		
		Options options = serviceClient.getOptions();
		if (options==null) {
			options = new Options ();
			serviceClient.setOptions(options);
		}
		
		String SOAPVersion = options.getSoapVersionURI();
		if (SOAPVersion==null) 
			SOAPVersion = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		
		SOAPEnvelope envelope = createSubscriptionEnvelope(bean,SOAPVersion);
		
		String oldAction = options.getAction();
		String action = EventingConstants.Actions.Subscribe;
		options.setAction(action);
		OMElement subscriptionResponse =  serviceClient.sendReceive (envelope.getBody().getFirstElement());
		SubscriptionResponseData subscriptionResponseData = getSubscriptionResponseData (subscriptionResponse);
		
		subscriptionDataMap.put(subscriptionID,subscriptionResponseData);
		
		options.setAction(oldAction);
	}
	
	public void renewSubscription (long renewAmount, String subscriptionID) throws Exception {
		SubscriptionResponseData data = (SubscriptionResponseData) subscriptionDataMap.get(subscriptionID);
		EndpointReference managerEPR = data.getSubscriptionManager();
		if (managerEPR==null)
			throw new Exception ("Manager EPR is not set");
		
		Options options = serviceClient.getOptions();
		if (options==null) {
			options = new Options ();
			serviceClient.setOptions(options);
		}
		
		String SOAPVersion = options.getSoapVersionURI();
		if (SOAPVersion==null) 
			SOAPVersion = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		
		SOAPEnvelope envelope = createRenewSubscriptionEnvelope(renewAmount,SOAPVersion);
		
		String oldAction = options.getAction();
		String action = EventingConstants.Actions.Renew;
		options.setAction(action);
		
		EndpointReference oldTo = serviceClient.getOptions().getTo();
		options.setTo(managerEPR);
		
		OMElement renewResponse =  serviceClient.sendReceive (envelope.getBody().getFirstElement());

		options.setAction(oldAction);
		options.setTo(oldTo);
	}
	
	public void unsubscribe (String subscriptionID) throws Exception {
		SubscriptionResponseData data = (SubscriptionResponseData) subscriptionDataMap.get(subscriptionID);
		EndpointReference managerEPR = data.getSubscriptionManager();
		if (managerEPR==null)
			throw new Exception ("Manager EPR is not set");
		
		Options options = serviceClient.getOptions();
		if (options==null) {
			options = new Options ();
			serviceClient.setOptions(options);
		}
		
		String SOAPVersion = options.getSoapVersionURI();
		if (SOAPVersion==null) 
			SOAPVersion = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		
		SOAPEnvelope envelope = createUnsubscriptionEnvelope(SOAPVersion);
		
		String oldAction = options.getAction();
		String action = EventingConstants.Actions.Unsubscribe;
		options.setAction(action);
		
		EndpointReference oldTo = serviceClient.getOptions().getTo();
		options.setTo(managerEPR);
		
		OMElement unsubscribeResponse =  serviceClient.sendReceive (envelope.getBody().getFirstElement());

		options.setAction(oldAction);
		options.setTo(oldTo);
	}
	
	public SubscriptionStatus getSubscriptionStatus (String subscriptionID) throws Exception {
		SubscriptionResponseData data = (SubscriptionResponseData) subscriptionDataMap.get(subscriptionID);
		EndpointReference managerEPR = data.getSubscriptionManager();
		if (managerEPR==null)
			throw new Exception ("Manager EPR is not set");
		
		Options options = serviceClient.getOptions();
		if (options==null) {
			options = new Options ();
			serviceClient.setOptions(options);
		}
		
		String SOAPVersion = options.getSoapVersionURI();
		if (SOAPVersion==null) 
			SOAPVersion = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
		
		SOAPEnvelope envelope = createGetStatusEnvelope(SOAPVersion);
		
		String oldAction = options.getAction();
		String action = EventingConstants.Actions.GetStatus;
		options.setAction(action);
		
		EndpointReference oldTo = serviceClient.getOptions().getTo();
		options.setTo(managerEPR);
		
		OMElement getStatusResponse =  serviceClient.sendReceive (envelope.getBody().getFirstElement());
		SubscriptionStatus subscriptionStatus = getSubscriptionStatus (getStatusResponse);
		
		options.setAction(oldAction);
		options.setTo(oldTo);
		
		return subscriptionStatus;
	}
	
	private SubscriptionResponseData getSubscriptionResponseData (OMElement responseMessagePayload) {
		SubscriptionResponseData data = new SubscriptionResponseData ();
		
		OMElement subscriberManagerElement = responseMessagePayload.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.SubscriptionManager));
		EndpointReference managerEPR = new EndpointReference ("");
		managerEPR.fromOM(subscriberManagerElement);
		data.setSubscriptionManager(managerEPR);
		
		OMElement expiresElement = responseMessagePayload.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Expires));
		if (expiresElement!=null) {
			String text = expiresElement.getText();
			long expiresValue = Long.parseLong(text);
			data.setExpires(expiresValue);
		}
		
		return data;
	}
	
	private SubscriptionStatus getSubscriptionStatus (OMElement getStatusResponseElement) throws Exception {
		SubscriptionStatus subscriptionStatus = new SubscriptionStatus ();
		
		OMElement expiresElementElement = getStatusResponseElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Expires));
		if (expiresElementElement!=null) {
			String valueStr = expiresElementElement.getText();
			long expires = Long.parseLong(valueStr);
			subscriptionStatus.setExpirationTime(expires);
		}
		
		return subscriptionStatus;
	}
	
	private SOAPEnvelope createSubscriptionEnvelope (EventingClientBean bean, String SOAPVersion) throws Exception{
		SOAPFactory factory = null;
		
		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP11Factory();
		else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP12Factory();
		else throw new Exception ("Unknown SOAP version");
		
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		SOAPBody body = envelope.getBody();
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
		
		OMElement subscriptionElement = factory.createOMElement(EventingConstants.ElementNames.Subscribe,ens);
		
		EndpointReference endToEPR = bean.getEndToEPR();
		if (bean.getEndToEPR()!=null) {
			//TODO when Axis2 get corrected, use the method which takes an addressing version as a param.
			OMElement endToElement = endToEPR.toOM(EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.EndTo,EventingConstants.EVENTING_PREFIX);
			subscriptionElement.addChild(endToElement);
		}
		
		EndpointReference deliveryEPR = bean.getDeliveryEPR();
		if (deliveryEPR==null)
			throw new Exception ("Delivery EPR is not set");
		
		OMElement deliveryElement = factory.createOMElement(EventingConstants.ElementNames.Delivery,ens);
		OMElement notifyToElement = deliveryEPR.toOM(EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.NotifyTo,EventingConstants.EVENTING_PREFIX);

		deliveryElement.addChild(notifyToElement);
		subscriptionElement.addChild(deliveryElement);
		
		//TODO add Expires,Filter elements.
		body.addChild(subscriptionElement);
		
		return envelope;
	}
	
	private SOAPEnvelope createRenewSubscriptionEnvelope (long renewAmount, String SOAPVersion) throws Exception{
		SOAPFactory factory = null;
		
		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP11Factory();
		else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP12Factory();
		else throw new Exception ("Unknown SOAP version");
		
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		SOAPBody body = envelope.getBody();
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
		OMElement renewElement = factory.createOMElement(EventingConstants.ElementNames.Renew,ens);
		OMElement expiresElement = factory.createOMElement(EventingConstants.ElementNames.Expires,ens);
		expiresElement.setText(new Long (renewAmount).toString());
		renewElement.addChild(expiresElement);
		
		body.addChild(renewElement);
		
		return envelope;
	}
	
	private SOAPEnvelope createUnsubscriptionEnvelope (String SOAPVersion) throws Exception {
		SOAPFactory factory = null;
		
		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP11Factory();
		else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP12Factory();
		else throw new Exception ("Unknown SOAP version");
		
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		SOAPBody body = envelope.getBody();
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
		OMElement unsubscribeElement = factory.createOMElement(EventingConstants.ElementNames.Unsubscribe,ens);
		body.addChild(unsubscribeElement);
		
		return envelope;
	}

	private SOAPEnvelope createGetStatusEnvelope (String SOAPVersion) throws Exception {
		SOAPFactory factory = null;
		
		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP11Factory();
		else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP12Factory();
		else throw new Exception ("Unknown SOAP version");
		
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		SOAPBody body = envelope.getBody();
		
		OMNamespace ens = factory.createOMNamespace(EventingConstants.EVENTING_NAMESPACE,EventingConstants.EVENTING_PREFIX);
		OMElement getStatusElement = factory.createOMElement(EventingConstants.ElementNames.GetStatus,ens);
		body.addChild(getStatusElement);
		
		return envelope;
	}
	
	private class SubscriptionResponseData {

		EndpointReference subscriptionManager = null;
		
		long expires = -1;

		public long getExpires() {
			return expires;
		}

		public EndpointReference getSubscriptionManager() {
			return subscriptionManager;
		}

		public void setExpires(long expires) {
			this.expires = expires;
		}

		public void setSubscriptionManager(EndpointReference subscriptionManager) {
			this.subscriptionManager = subscriptionManager;
		}
		
		
	}
	
}
