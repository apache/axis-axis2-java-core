/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.security.handler.config;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This is the representation of the inflow configurations of the security
 * module
 */
public class InflowConfiguration {
	
	private HashMap action = new HashMap();
	
	/**
	 * Returns the configuration as an OMElement 
	 * @return Returns Parameter.
	 */
	public Parameter getProperty() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("", null);
		OMElement propertyElement = fac.createOMElement(
				WSSHandlerConstants.INFLOW_SECURITY, ns);
		
		OMElement actionElem = fac.createOMElement(
				WSSHandlerConstants.ACTION, ns);
		
		// Get the set of kes of the selected action
		Iterator keys = action.keySet().iterator();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			// Create an element with the name of the key
			OMElement elem = fac.createOMElement(key, ns);
			// Set the text value of the element
			elem.setText((String) action.get(key));
			// Add the element as a child of this action element
			actionElem.addChild(elem);
		}
		
		propertyElement.addChild(actionElem);
		
		ParameterImpl param = new ParameterImpl();
		param.setParameterElement(propertyElement);
		
		return param;
	}

	/**
	 * Returns the action items.
	 * @return Returns String.
	 */
	public String getActionItems() {
		return (String)this.action.get(WSSHandlerConstants.ACTION_ITEMS);
	}

	/**
	 * Sets the action items
	 * @param actionItems
	 */
	public void setActionItems(String actionItems) {
		this.action.put(WSSHandlerConstants.ACTION_ITEMS, actionItems);
	}

	/**
	 * Returns the decryption property file
	 * @return
	 */
	public String getDecryptionPropFile() {
		return (String)this.action.get(WSHandlerConstants.DEC_PROP_FILE);
	}

	/**
	 * Sets the decryption property file
	 * @param decryptionPropFile
	 */
	public void setDecryptionPropFile(String decryptionPropFile) {
		this.action.put(WSHandlerConstants.DEC_PROP_FILE,decryptionPropFile);
	}

	/**
	 * Returns the password callback class name
	 * @return
	 */
	public String getPasswordCallbackClass() {
		return (String)this.action.get(WSHandlerConstants.PW_CALLBACK_CLASS);
	}

	/**
	 * Sets the password callback class name
	 * @param passwordCallbackClass
	 */
	public void setPasswordCallbackClass(String passwordCallbackClass) {
		this.action.put(WSHandlerConstants.PW_CALLBACK_CLASS,passwordCallbackClass);
	}

	/**
	 * Returns the signature property file
	 * @return Returns String.
	 */
	public String getSignaturePropFile() {
		return (String)this.action.get(WSHandlerConstants.SIG_PROP_FILE);
	}

	/**
	 * Sets the signature property file
	 * @param signaturePropFile
	 */
	public void setSignaturePropFile(String signaturePropFile) {
		this.action.put(WSHandlerConstants.SIG_PROP_FILE, signaturePropFile);
	}
	
	/**
	 * Sets whether signature confirmation should be enabled or not
	 * @param value
	 */
	public void setEnableSignatureConfirmation(boolean value) {
		this.action.put(
				WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION, value?"true":"false");
	}
	
	/**
	 * Returns whether signature confirmation should be enabled or not
	 * @return Returns String.
	 */
	public String getEnableSignatureConfirmation() {
		return (String) this.action
				.get(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION);
	}
	
}
