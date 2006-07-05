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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.Duration;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.filters.Filter;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscription.ExpirationBean;
import org.apache.savan.subscription.SubscriptionProcessor;
import org.apache.savan.util.UtilFactory;
import org.apache.savan.util.CommonUtil;

public class EventingSubscriptionProcessor extends SubscriptionProcessor {

	public void init (SavanMessageContext smc) throws SavanException {
		//setting the subscriber_id as a property if possible.
		
		String id = getSubscriberID(smc);
		if (id!=null) {
			smc.setProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID,id);
		}
	}
	
	public Subscriber getSubscriberFromMessage(SavanMessageContext smc) throws SavanException {

		Protocol protocol = smc.getProtocol();
		if (protocol==null)
			throw new SavanException ("Protocol not found");
		
		UtilFactory utilFactory = protocol.getUtilFactory();
		
		SOAPEnvelope envelope = smc.getEnvelope();
		if (envelope==null)
			return null;
		
		Subscriber subscriber = utilFactory.createSubscriber();  //eventing only works on leaf subscriber for now.
		if (!(subscriber instanceof EventingSubscriber)) {
			String message = "Eventing protocol only support EventingSubscribers (or subtypes) as Subscribers";
			throw new SavanException (message);
		}
		
		EventingSubscriber eventingSubscriber = (EventingSubscriber) subscriber;
		String id = UUIDGenerator.getUUID();
		smc.setProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID,id);
	
		eventingSubscriber.setId(id);
		
		SOAPBody body = envelope.getBody();
		OMElement subscribeElement = body.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Subscribe));
		if (subscribeElement==null)
			throw new SavanException ("'Subscribe' element is not present");
		
		OMElement endToElement = subscribeElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.EndTo));
		if (endToElement!=null) {
			EndpointReference endToEPR = new EndpointReference ("");
			endToEPR.fromOM(endToElement);
			
			eventingSubscriber.setEndToEPr(endToEPR);
		}
		
		OMElement deliveryElement = subscribeElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Delivery));
		if (deliveryElement==null)
			throw new SavanException ("Delivery element is not present");
		
		OMElement notifyToElement = deliveryElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.NotifyTo));
		if (notifyToElement==null)
			throw new SavanException ("NotifyTo element is null");
		
		EndpointReference notifyToEPr = new EndpointReference ("");
		notifyToEPr.fromOM(notifyToElement);
		
		OMAttribute deliveryModeAttr = deliveryElement.getAttribute(new QName (EventingConstants.ElementNames.Mode));
		String deliveryMode = null;
		if (deliveryModeAttr!=null) {
			deliveryMode = deliveryModeAttr.getAttributeValue().trim();
		} else {
			deliveryMode = EventingConstants.DEFAULT_DELIVERY_MODE;
		}
		
		Delivery delivery = new Delivery ();
		delivery.setDeliveryEPR(notifyToEPr);
		delivery.setDeliveryMode(deliveryMode);
		
		eventingSubscriber.setDelivery(delivery);
		
		OMElement expiresElement = subscribeElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Expires));
		if (expiresElement!=null) {
			String expiresText = expiresElement.getText();

			if (expiresText==null){
				String message = "Expires Text is null";
				throw new SavanException (message);
			}
			
			expiresText = expiresText.trim();
			
			ExpirationBean expirationBean = getExpirationBeanFromString(expiresText);
			Date expiration = null;
			if (expirationBean.isDuration()) {
				Calendar calendar = Calendar.getInstance();
				CommonUtil.addDurationToCalendar(calendar,expirationBean.getDurationValue());
				expiration = calendar.getTime();
			} else
				expiration = expirationBean.getDateValue();
			
			
			if (expiration==null) {
				String message = "Cannot understand the given date-time value for the Expiration";
				throw new SavanException (message);
			}
			
			eventingSubscriber.setSubscriptionEndingTime(expiration);
		}
		
		OMElement filterElement = subscribeElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Filter));
		if (filterElement!=null) {
			OMNode filterNode = filterElement.getFirstOMChild();
			OMAttribute dialectAttr = filterElement.getAttribute(new QName (EventingConstants.ElementNames.Dialect));
			Filter filter = null;
			if (dialectAttr==null) {
				filter = utilFactory.createFilter (EventingConstants.DEFAULT_FILTER_DIALECT);
			} else {
				filter = utilFactory.createFilter (dialectAttr.getAttributeValue());
			}
			if (filter==null)
				throw new SavanException ("Cant find a suitable message filter");
			
			filter.setUp (filterNode);
			
			eventingSubscriber.setFilter(filter);
		}
		
		return eventingSubscriber;
	}

	public void pauseSubscription(SavanMessageContext pauseSubscriptionMessage) throws SavanException {
		throw new UnsupportedOperationException ("Eventing specification does not support this type of messages");
	}

	public void resumeSubscription(SavanMessageContext resumeSubscriptionMessage) throws SavanException {
		throw new UnsupportedOperationException ("Eventing specification does not support this type of messages");
	}

	public ExpirationBean getExpirationBean(SavanMessageContext renewMessage) throws SavanException {

		SOAPEnvelope envelope = renewMessage.getEnvelope();
		SOAPBody body = envelope.getBody();
		
		ExpirationBean expirationBean = null;
		
		OMElement renewElement = body.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Renew));
		if (renewElement==null) {
			String message = "Renew element not present in the assumed Renew Message";
			throw new SavanException (message);
		}
		
		OMElement expiresElement = renewElement.getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Expires));
		if (expiresElement!=null) {
			String expiresText = expiresElement.getText().trim();
			expirationBean = getExpirationBeanFromString(expiresText);
		}
		
		String subscriberID = getSubscriberID(renewMessage);
		if (subscriberID==null) {
			String message = "Cannot find the subscriber ID";
			throw new SavanException (message);
		}
		
		renewMessage.setProperty(EventingConstants.TransferedProperties.SUBSCRIBER_UUID,subscriberID);
		
		expirationBean.setSubscriberID(subscriberID);
		return expirationBean;
	}

	public String getSubscriberID(SavanMessageContext smc) throws SavanException {
		SOAPEnvelope envelope = smc.getEnvelope();
		SOAPHeader header = envelope.getHeader();
		if (header==null) {
			return null;
		}
		
		OMElement ideltifierElement = envelope.getHeader().getFirstChildWithName(new QName (EventingConstants.EVENTING_NAMESPACE,EventingConstants.ElementNames.Identifier));
		if (ideltifierElement==null) {
			return null;
		}
		
		String identifier = ideltifierElement.getText().trim();
		return identifier;
	}
	
	private ExpirationBean getExpirationBeanFromString (String expiresStr) throws SavanException {

		ExpirationBean bean = new ExpirationBean ();
		
		//expires can be a duration or a date time.
		//Doing the conversion using the ConverUtil helper class.
		
		Date date = null;
		boolean isDuration = CommonUtil.isDuration(expiresStr);
		
		if (isDuration) {
			try {
				bean.setDuration(true);
				Duration duration = ConverterUtil.convertToDuration(expiresStr);
				bean.setDurationValue(duration);
			} catch (IllegalArgumentException e) {
				String message = "Cannot convert the Expiration value to a valid duration";
				throw new SavanException (message,e);
			}
		} else {
			try {
			    Calendar calendar = ConverterUtil.convertTodateTime(expiresStr);
			    date = calendar.getTime();
			    bean.setDateValue(date);
			} catch (Exception e) {
				String message = "Cannot convert the Expiration value to a valid DATE/TIME";
				throw new SavanException (message,e);
			}
		}
		
		return bean;
	}

	public void doProtocolSpecificEndSubscription(Subscriber subscriber, String reason, ConfigurationContext configurationContext) throws SavanException {
		String SOAPVersion = (String) subscriber.getProperty(EventingConstants.Properties.SOAPVersion);
		if (SOAPVersion==null) 
			throw new SavanException ("Cant find the SOAP version of the subscriber");
		
		SOAPFactory factory = null;
		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP11Factory();
		else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
			factory = OMAbstractFactory.getSOAP12Factory();
		else
			throw new SavanException ("The subscriber has a unknown SOAP version property set");
		
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		
		
	}
}
