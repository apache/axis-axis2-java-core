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
 * This is the representation of the outflow configurations of the security
 * module
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class OutflowConfiguration {

	private HashMap[] actionList;

	private int currentAction = 0;

	/**
	 * Create a default outflow configuration instance with 1 action
	 */
	public OutflowConfiguration() {
		this.actionList = new HashMap[1];
		this.actionList[0] = new HashMap();
	}

	/**
	 * Create a new outflow configuration instance with the given number of
	 * actions
	 * 
	 * @param actionCount
	 */
	public OutflowConfiguration(int actionCount) {
		this.actionList = new HashMap[actionCount];
		for (int i = 0; i < actionCount; i++) {
			this.actionList[i] = new HashMap();
		}
	}

	/**
	 * Returns the configuration as an Parameter
	 * 
	 * @return
	 */
	public Parameter getProperty() {
		
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("", null);
		OMElement propertyElement = fac.createOMElement(
				WSSHandlerConstants.OUTFLOW_SECURITY, ns);

		
		for (int i = 0; i < this.actionList.length; i++) {
			// Create the action element
			OMElement actionElem = fac.createOMElement(
					WSSHandlerConstants.ACTION, ns);

			// Get the current action
			HashMap action = this.actionList[i];

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
		}
		
		ParameterImpl param = new ParameterImpl();
		param.setParameterElement(propertyElement);
		return param;
	}

	/**
	 * Move to the next action If this is called when the current action is the
	 * last action then the current action will not change
	 * 
	 * @throws Exception
	 */
	public void nextAction() {
		if (currentAction < this.actionList.length - 1) {
			this.currentAction++;
		}
	}

	/**
	 * Move to previous action If this is called when the current action is the
	 * first option then then the current action will not change
	 * 
	 * @throws Exception
	 */
	public void previousAction() {
		if (this.currentAction > 0) {
			this.currentAction--;
		}
	}

	/**
	 * Sets the action items
	 * 
	 * @param actionItems
	 */
	public void setActionItems(String actionItems) {
		this.actionList[this.currentAction].put(
				WSSHandlerConstants.ACTION_ITEMS, actionItems);
	}

	/**
	 * Returns the action items
	 * @return
	 */
	public String getActionItems() {
		return (String) this.actionList[this.currentAction]
				.get(WSSHandlerConstants.ACTION_ITEMS);
	}
	
	/**
	 * Sets the user of the current action
	 * 
	 * @param user
	 */
	public void setUser(String user) {
		this.actionList[this.currentAction].put(WSHandlerConstants.USER, user);
	}

	/**
	 * Returns the user of the current action
	 * @return
	 */
	public String getUser() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.USER);
	}
	
	/**
	 * Sets the name of the password callback class of the current action
	 * 
	 * @param pwCallbackClass
	 */
	public void setPasswordCallbackClass(String passwordCallbackClass) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.PW_CALLBACK_CLASS, passwordCallbackClass);
	}

	/**
	 * Returns the name of the password callback class of the current action
	 * @return
	 */
	public String getPasswordCallbackClass() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.PW_CALLBACK_CLASS);
	}
	
	/**
	 * Sets the signature property file of the current action
	 * 
	 * @param signaturePropFile
	 */
	public void setSignaturePropFile(String signaturePropFile) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.SIG_PROP_FILE, signaturePropFile);
	}

	/**
	 * Returns the signature property file of the current action
	 * @return
	 */
	public String getSignaturePropFile() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SIG_PROP_FILE);
	}
	
	/**
	 * Sets the signatue key identifier of the current action
	 * 
	 * @param signatureKeyIdentifier
	 */
	public void setSignatureKeyIdentifier(String signatureKeyIdentifier) {
		this.actionList[this.currentAction].put(WSHandlerConstants.SIG_KEY_ID,
				signatureKeyIdentifier);
	}

	/**
	 * Returns the signatue key identifier of the current action
	 * @return
	 */
	public String getSignatureKeyIdentifier() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SIG_KEY_ID);
	}
	
	/**
	 * sets the encrypted key identifier of the current action
	 * 
	 * @param encryptionKeyIdentifier
	 */
	public void setEncryptionKeyIdentifier(String encryptionKeyIdentifier) {
		this.actionList[this.currentAction].put(WSHandlerConstants.ENC_KEY_ID,
				encryptionKeyIdentifier);
	}

	/**
	 * Returns the encrypted key identifier of the current action
	 * @return
	 */
	public String getEncryptionKeyIdentifier() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_KEY_ID);
	}
	
	/**
	 * Sets the encryption user of the current action
	 * 
	 * @param encryptionUser
	 */
	public void setEncryptionUser(String encryptionUser) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENCRYPTION_USER, encryptionUser);
	}

	/**
	 * Returns the encryption user of the current action
	 * @return
	 */
	public String getEncryptionUser() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENCRYPTION_USER);
	}
	
	/**
	 * Sets the signature parts of the current action
	 * 
	 * @param signatureParts
	 */
	public void setSignatureParts(String signatureParts) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.SIGNATURE_PARTS, signatureParts);
	}
	
	/**
	 * Returns the signature parts of the current action
	 * @return
	 */
	public String getSignatureParts() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SIGNATURE_PARTS);
	}

	/**
	 * Sets the encryption parts of the current action
	 * 
	 * @param signatureParts
	 */
	public void setEncryptionParts(String encryptionParts) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENCRYPTION_PARTS, encryptionParts);
	}
	
	/**
	 * Returns the encryption parts of the current action
	 * @return
	 */
	public String getEncryptionParts() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENCRYPTION_PARTS);
	}	

	/**
	 * Sets the password type of the current action
	 * 
	 * @param passwordType
	 */
	public void setPasswordType(String passwordType) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.PASSWORD_TYPE, passwordType);
	}

	/**
	 * Returns the password type of the current action
	 * @return
	 */
	public String getPasswordType() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.PASSWORD_TYPE);
	}
	
	/**
	 * Sets the encryption symmetric algorithm of the current action
	 * 
	 * @param encryptionSymAlgorithm
	 */
	public void setEncryptionSymAlgorithm(String encryptionSymAlgorithm) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENC_SYM_ALGO, encryptionSymAlgorithm);
	}

	/**
	 * Returns the encryption symmetric algorithm of the current action
	 * @return
	 */
	public String getEncryptionSymAlgorithm() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_SYM_ALGO);
	}
	
	/**
	 * Sets the encryption key transport algorithm of the current action
	 * 
	 * @param encryptionKeyTransportAlgorithm
	 */
	public void setEncryptionKeyTransportAlgorithm(
			String encryptionKeyTransportAlgorithm) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENC_KEY_TRANSPORT,
				encryptionKeyTransportAlgorithm);
	}

	/**
	 * Returns the encryption key transport algorithm of the current action
	 * @return
	 */
	public String getEncryptionKeyTransportAlgorithm() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_KEY_TRANSPORT);
	}

	/**
	 * Sets the embedded key callback class of the current action
	 * 
	 * @param embeddedKeyCallbackClass
	 */
	public void setEmbeddedKeyCallbackClass(String embeddedKeyCallbackClass) {
		this.actionList[this.currentAction]
				.put(WSHandlerConstants.ENC_CALLBACK_CLASS,
						embeddedKeyCallbackClass);
	}

	/**
	 * returns the embedded key callback class of the current action
	 * 
	 * @return
	 */
	public String getEmbeddedKeyCallbackClass() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_CALLBACK_CLASS);
	}

	/**
	 * Sets the XPath expression to selecte the elements with content of the
	 * current action to be MTOM optimized
	 * 
	 * @param optimizePartsXPathExpr
	 */
	public void setOptimizeParts(String optimizePartsXPathExpr) {
		this.actionList[this.currentAction].put(
				WSSHandlerConstants.OPTIMIZE_PARTS, optimizePartsXPathExpr);
	}

	/**
	 * Returns the Path expression to selecte the elements with content of the
	 * current action to be MTOM optimized
	 * 
	 * @return
	 */
	public String getOptimizeParts() {
		return (String) this.actionList[this.currentAction]
				.get(WSSHandlerConstants.OPTIMIZE_PARTS);
	}
	
	/**
	 * Sets the SAML property file of the current action
	 * @param samlPropFile
	 */
	public void setSamlPropFile(String samlPropFile) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.SAML_PROP_FILE, samlPropFile);
	}
	
	/**
	 * Returns the SAML property file of the current action
	 * @return
	 */
	public String getSamlPropFile() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SAML_PROP_FILE);
	}
	
	/**
	 * Sets the encryption property file
	 * @param encPropFile
	 */
	public void setEncryptionPropFile(String encPropFile) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENC_PROP_FILE, encPropFile);
	}
	
	/**
	 * Returns the encryption property file
	 * @return
	 */
	public String getEncryptionPropFile() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_PROP_FILE);
	}
	
	/**
	 * Option to add additional elements in the username token element
	 * Example: Nonce and Create elements
	 * @param addUTElements
	 */
	public void setAddUTElements(String addUTElements) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ADD_UT_ELEMENTS, addUTElements);
	}
	
	/**
	 * Returns the additional elements to be 
	 * added to the username token element
	 * @param addUTElements
	 */
	public String getAddUTElements() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ADD_UT_ELEMENTS);
	}
	
	/**
	 * Sets the text of the key name that needs to be sent
	 * @param embeddedKeyName
	 */
	public void setEmbeddedKeyName(String embeddedKeyName) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENC_KEY_NAME, embeddedKeyName);
	}
	
	/**
	 * Returns the text of the key name that needs to be sent
	 * @return
	 */
	public String getEmbeddedKeyName() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_KEY_NAME);
	}
}
