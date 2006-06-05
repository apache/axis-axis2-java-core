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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;

public class SavanMessageContext {

	MessageContext messageContext = null;
	
	public SavanMessageContext (MessageContext messageContext) {
		this.messageContext = messageContext;
	}
	
	public int getProtocolVersion () throws SavanException {
		Integer version = (Integer) messageContext.getProperty(SavanConstants.PROTOCOL_VERSION);
		if (version==null)
			throw new SavanException ("Protocol version is not set in the SavanMessageContext");
		
		return version.intValue();
	}
	
	public void setProtocolVersion (int protocolVersion) {
		messageContext.setProperty(SavanConstants.PROTOCOL_VERSION,new Integer (protocolVersion));
	}
	
	public void setMessageType (int type) {
		messageContext.setProperty(SavanConstants.MESSAGE_TYPE, new Integer (type));
	}
	
	public int getMessageType () {
		Integer typeInt = (Integer) messageContext.getProperty(SavanConstants.MESSAGE_TYPE);
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
}
