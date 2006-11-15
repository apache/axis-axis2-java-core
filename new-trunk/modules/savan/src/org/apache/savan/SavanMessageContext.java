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

package org.apache.savan;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.storage.SubscriberStore;

/**
 * This encaptulates a Axis2 Message Context.
 * Provide some easy methods to access Savan specific properties easily.
 */
public class SavanMessageContext {

	MessageContext messageContext = null;
	
	public SavanMessageContext (MessageContext messageContext) {
		this.messageContext = messageContext;
	}
	
	public void setMessageType (int type) {
		messageContext.setProperty(SavanConstants.MESSAGE_TYPE, new Integer (type));
	}
	
	public int getMessageType () {
		Integer typeInt = (Integer) messageContext.getProperty(SavanConstants.MESSAGE_TYPE);
		if (typeInt==null) {
			typeInt = new Integer (SavanConstants.MessageTypes.UNKNOWN);
			messageContext.setProperty(SavanConstants.MESSAGE_TYPE,typeInt);
		}
		
		return typeInt.intValue();
	}
	
	public ConfigurationContext getConfigurationContext () {
		return messageContext.getConfigurationContext();
	}
	
	public Object getProperty (String key) {
		return messageContext.getProperty(key);
	}
	
	public void setProperty (String key, Object val) {
		messageContext.setProperty(key,val);
	}
	
	public SOAPEnvelope getEnvelope () {
		return messageContext.getEnvelope();
	}
	
	public MessageContext getMessageContext () {
		return messageContext;
	}
	
	public SubscriberStore getSubscriberStore () {
		Parameter parameter = messageContext.getParameter(SavanConstants.SUBSCRIBER_STORE);
		SubscriberStore subscriberStore = null;
		if (parameter!=null) {
			parameter = messageContext.getParameter(SavanConstants.SUBSCRIBER_STORE);
			subscriberStore = (SubscriberStore) parameter.getValue();
		}
		
		return subscriberStore;
	}
	
	public void setSubscriberStore (SubscriberStore store) throws SavanException  {
		Parameter parameter = new Parameter ();
		parameter.setName(SavanConstants.SUBSCRIBER_STORE);
		parameter.setValue(store);
		
		try {
			messageContext.getAxisService().addParameter(parameter);
		} catch (AxisFault e) {
			String message = "Could not add the AbstractSubscriber Store parameter";
			throw new SavanException (message,e);
		}
	}
	
	public void setProtocol (Protocol protocol) {
		messageContext.setProperty(SavanConstants.PROTOCOL, protocol);
	}
	
	public Protocol getProtocol () {
		return (Protocol) messageContext.getProperty(SavanConstants.PROTOCOL);
	}
	
}
