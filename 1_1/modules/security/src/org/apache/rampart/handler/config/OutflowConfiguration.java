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

package org.apache.rampart.handler.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.description.Parameter;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This is the representation of the outflow configurations of the security
 * module.
 * 
 * @deprecated
 */
public class OutflowConfiguration {

	private HashMap[] actionList;

	private int currentAction = 0;

	/**
	 * Creates a default outflow configuration instance with an action.
	 */
	public OutflowConfiguration() {
		this.actionList = new HashMap[1];
		this.actionList[0] = new HashMap();
	}

	/**
	 * Creates a new outflow configuration instance with the given number of
	 * actions.
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
	 * @return Returns Parameter.
	 */
	public Parameter getProperty() {
		

        for (int i = 0; i < actionList.length; i++) {
            HashMap action = actionList[i];
            
            if (! action.keySet().contains("items")) {
                return null;
            }
        }
                
		OMFactory fac = OMAbstractFactory.getOMFactory();
        //TODO: Find the constants for "Parameter" and "name"
        OMElement paramElement = fac.createOMElement("Parameter",null);
		paramElement.addAttribute(fac.createOMAttribute("name", null ,WSSHandlerConstants.OUTFLOW_SECURITY));

		
		for (int i = 0; i < this.actionList.length; i++) {
			// Create the action element
			OMElement actionElem = fac.createOMElement(
					WSSHandlerConstants.ACTION, null);

			// Get the current action
			HashMap action = this.actionList[i];

			// Get the set of kes of the selected action
			Iterator keys = action.keySet().iterator();

			while (keys.hasNext()) {
				String key = (String) keys.next();
                String value = (String) action.get(key);
                if(value != null && value.length() > 0) {
                    // Create an element with the name of the key
    				OMElement elem = fac.createOMElement(key, null);
    				// Set the text value of the element
                    elem.setText(value);
    				// Add the element as a child of this action element
    				actionElem.addChild(elem);
                }
			}
			
			paramElement.addChild(actionElem);
		}
		
		Parameter param = new Parameter();
		param.setParameterElement(paramElement);
        param.setValue(paramElement);
        param.setName(WSSHandlerConstants.OUTFLOW_SECURITY);
		return param;
	}

	/**
	 * Moves to the next action. If this is called when the current action is the
	 * last action then the current action will not change.
	 * 
	 * @throws Exception
	 */
	public void nextAction() {
		if (currentAction < this.actionList.length - 1) {
			this.currentAction++;
		}
	}

	/**
	 * Moves to previous action. If this is called when the current action is the
	 * first option then then the current action will not change.
	 * 
	 * @throws Exception
	 */
	public void previousAction() {
		if (this.currentAction > 0) {
			this.currentAction--;
		}
	}

	/**
	 * Sets the action items.
	 * 
	 * @param actionItems
	 */
	public void setActionItems(String actionItems) {
		this.actionList[this.currentAction].put(
				WSSHandlerConstants.ACTION_ITEMS, actionItems);
	}

	/**
	 * Returns the action items.
	 * @return Returns String.
	 */
	public String getActionItems() {
		return (String) this.actionList[this.currentAction]
				.get(WSSHandlerConstants.ACTION_ITEMS);
	}
	
	/**
	 * Sets the user of the current action.
	 * 
	 * @param user
	 */
	public void setUser(String user) {
		this.actionList[this.currentAction].put(WSHandlerConstants.USER, user);
	}

	/**
	 * Returns the user of the current action.
	 * @return Returns String.
	 */
	public String getUser() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.USER);
	}
	
	/**
	 * Sets the name of the password callback class of the current action.
	 * 
	 * @param passwordCallbackClass
	 */
	public void setPasswordCallbackClass(String passwordCallbackClass) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.PW_CALLBACK_CLASS, passwordCallbackClass);
	}

	/**
	 * Returns the name of the password callback class of the current action.
	 * @return Returns String.
	 */
	public String getPasswordCallbackClass() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.PW_CALLBACK_CLASS);
	}
	
	/**
	 * Sets the signature property file of the current action.
	 * 
	 * @param signaturePropFile
	 */
	public void setSignaturePropFile(String signaturePropFile) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.SIG_PROP_FILE, signaturePropFile);
	}

    /**
     * Sets the signature property ref key of the current action.
     * 
     * @param signaturePropRefId
     */
    public void setSignaturePropRefId(String signaturePropRefId) {
        this.actionList[this.currentAction].put(
                WSHandlerConstants.SIG_PROP_REF_ID, signaturePropRefId);
    }
    
	/**
	 * Returns the signature property file of the current action.
	 * @return Returns String.
	 */
	public String getSignaturePropFile() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SIG_PROP_FILE);
	}
	
	/**
	 * Sets the signatue key identifier of the current action.
	 * 
	 * @param signatureKeyIdentifier
     * Valid values:
     * <ul>
     * <li>X509KeyIdentifier - {@link WSSHandlerConstants#X509_KEY_IDENTIFIER}</li>
     * <li>SKIKeyIdentifier - {@link WSSHandlerConstants#SKI_KEY_IDENTIFIER}</li>
     * <li>IssuerSerial - {@link WSSHandlerConstants#ISSUER_SERIAL}</li>
     * <li>DirectReference - {@link WSSHandlerConstants#BST_DIRECT_REFERENCE}</li>
     * <li>Thumbprint - {@link WSSHandlerConstants#THUMBPRINT_IDENTIFIER}</li>
     * </ul> 
	 */
	public void setSignatureKeyIdentifier(String signatureKeyIdentifier) {
		this.actionList[this.currentAction].put(WSHandlerConstants.SIG_KEY_ID,
				signatureKeyIdentifier);
	}

	/**
	 * Returns the signatue key identifier of the current action.
	 * @return Returns String.
	 */
	public String getSignatureKeyIdentifier() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SIG_KEY_ID);
	}
    
    public void setSignatureAlgorithm(String signatureAlgo) {
        this.actionList[this.currentAction].put(WSHandlerConstants.SIG_ALGO,
                signatureAlgo);
    }
    
    public String getSignatureAlgorithm() {
        return (String) this.actionList[this.currentAction]
                .get(WSHandlerConstants.SIG_ALGO);
    }
	
	/**
	 * Sets the encrypted key identifier of the current action.
     * <br/>
	 * @param encryptionKeyIdentifier
     * Valid values:
     * <ul>
     * <li>X509KeyIdentifier - {@link WSSHandlerConstants#X509_KEY_IDENTIFIER}</li>
     * <li>SKIKeyIdentifier - {@link WSSHandlerConstants#SKI_KEY_IDENTIFIER}</li>
     * <li>IssuerSerial - {@link WSSHandlerConstants#ISSUER_SERIAL}</li>
     * <li>DirectReference - {@link WSSHandlerConstants#BST_DIRECT_REFERENCE}</li>
     * <li>EmbeddedKeyName - {@link WSSHandlerConstants#EMBEDDED_KEYNAME}</li>
     * <li>Thumbprint - {@link WSSHandlerConstants#THUMBPRINT_IDENTIFIER}</li>
     * </ul> 
	 */
	public void setEncryptionKeyIdentifier(String encryptionKeyIdentifier) {
		this.actionList[this.currentAction].put(WSHandlerConstants.ENC_KEY_ID,
				encryptionKeyIdentifier);
	}

	/**
	 * Returns the encrypted key identifier of the current action.
	 * @return Returns String.
	 */
	public String getEncryptionKeyIdentifier() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_KEY_ID);
	}
	
	/**
	 * Sets the encryption user of the current action.
	 * 
	 * @param encryptionUser
	 */
	public void setEncryptionUser(String encryptionUser) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENCRYPTION_USER, encryptionUser);
	}

	/**
	 * Returns the encryption user of the current action.
	 * @return Returns String.
	 */
	public String getEncryptionUser() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENCRYPTION_USER);
	}
	
	/**
	 * Sets the signature parts of the current action.
	 * 
	 * @param signatureParts
	 */
	public void setSignatureParts(String signatureParts) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.SIGNATURE_PARTS, signatureParts);
	}
	
	/**
	 * Returns the signature parts of the current action.
	 * @return Returns String.
	 */
	public String getSignatureParts() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SIGNATURE_PARTS);
	}

	/**
	 * Sets the encryption parts of the current action.
	 * 
	 * @param encryptionParts
	 */
	public void setEncryptionParts(String encryptionParts) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENCRYPTION_PARTS, encryptionParts);
	}
	
	/**
	 * Returns the encryption parts of the current action.
	 * @return Returns String.
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
	 * Returns the password type of the current action.
	 * @return Returns String.
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
	 * Returns the encryption symmetric algorithm of the current action.
	 * @return Returns String.
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
	 * Returns the encryption key transport algorithm of the current action.
	 * @return Returns String.
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
	 * Returns the embedded key callback class of the current action.
	 * 
	 * @return Returns String.
	 */
	public String getEmbeddedKeyCallbackClass() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_CALLBACK_CLASS);
	}

	/**
	 * Sets the XPath expression to selecte the elements with content of the
	 * current action to be MTOM optimized.
	 * 
	 * @param optimizePartsXPathExpr
	 */
	public void setOptimizeParts(String optimizePartsXPathExpr) {
		this.actionList[this.currentAction].put(
				WSSHandlerConstants.OPTIMIZE_PARTS, optimizePartsXPathExpr);
	}

	/**
	 * Returns the Path expression to selecte the elements with content of the
	 * current action to be MTOM optimized.
	 * 
	 * @return Returns String.
	 */
	public String getOptimizeParts() {
		return (String) this.actionList[this.currentAction]
				.get(WSSHandlerConstants.OPTIMIZE_PARTS);
	}
	
	/**
	 * Sets the SAML property file of the current action.
	 * @param samlPropFile
	 */
	public void setSamlPropFile(String samlPropFile) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.SAML_PROP_FILE, samlPropFile);
	}
	
	/**
	 * Returns the SAML property file of the current action.
	 * @return Returns String.
	 */
	public String getSamlPropFile() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.SAML_PROP_FILE);
	}
	
	/**
	 * Sets the encryption property file.
	 * @param encPropFile
	 */
	public void setEncryptionPropFile(String encPropFile) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENC_PROP_FILE, encPropFile);
	}
	
    /**
     * Sets the encryption property ref key of the current action.
     * 
     * @param encryptionPropRefId
     */
    public void setEncryptionPropRefId(String encryptionPropRefId) {
        this.actionList[this.currentAction].put(
                WSHandlerConstants.ENC_PROP_REF_ID, encryptionPropRefId);
    }
    
	/**
	 * Returns the encryption property file. 
	 * @return Returns String.
	 */
	public String getEncryptionPropFile() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_PROP_FILE);
	}

    /**
     * Enable/Disable PrecisionInMilliseconds
     * @param value
     */
    public void setPrecisionInMilliseconds(boolean value) {
        this.actionList[this.currentAction].put(
                WSHandlerConstants.TIMESTAMP_PRECISION, value?"true":"false");
    }
    
    /**
     * Returns whether PrecisionInMilliseconds is enabled or not
     * @return Returns String.
     */
    public String getPrecisionInMilliseconds() {
        return (String) this.actionList[this.currentAction]
                .get(WSHandlerConstants.TIMESTAMP_PRECISION);
    }
    
	/**
	 * Option to add additional elements in the username token element.
	 * Example: Nonce and Create elements
	 * @param addUTElements
	 */
	public void setAddUTElements(String addUTElements) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ADD_UT_ELEMENTS, addUTElements);
	}
	
	/**
	 * Returns the additional elements to be added to the username token element.
	 */
	public String getAddUTElements() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ADD_UT_ELEMENTS);
	}
	
	/**
	 * Sets the text of the key name that needs to be sent.
	 * @param embeddedKeyName
	 */
	public void setEmbeddedKeyName(String embeddedKeyName) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENC_KEY_NAME, embeddedKeyName);
	}
	
	/**
	 * Returns the text of the key name that needs to be sent.
	 * @return Returns String.
	 */
	public String getEmbeddedKeyName() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENC_KEY_NAME);
	}
	
	/**
	 * Sets whether signature confirmation should be enabled or not.
	 * @param value
	 */
	public void setEnableSignatureConfirmation(boolean value) {
		this.actionList[this.currentAction].put(
				WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION, value?"true":"false");
	}
	
	/**
	 * Returns whether signature confirmation should be enabled or not
	 * @return Returns String.
	 */
	public String getEnableSignatureConfirmation() {
		return (String) this.actionList[this.currentAction]
				.get(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION);
	}
	
	/**
	 * Sets whether signature confirmation should be enabled or not
	 * @param value
	 */
	public void setPreserveOriginalEnvelope(boolean value) {
		this.actionList[this.currentAction].put(
				WSSHandlerConstants.PRESERVE_ORIGINAL_ENV, value?"true":"false");
	}
	
	/**
	 * Returns whether signature confirmation should be enabled or not.
	 * @return Returns String.
	 */
	public String getPreserveOriginalEnvelope() {
		return (String) this.actionList[this.currentAction]
				.get(WSSHandlerConstants.PRESERVE_ORIGINAL_ENV);
	}
    
    
    public void setSignAllHeadersAndBody() {
        this.actionList[this.currentAction].put(WSSHandlerConstants.SIGN_ALL_HEADERS, "true");
        this.setSignBody();
    }
    
    public void setSignBody() {
        this.actionList[this.currentAction].put(WSSHandlerConstants.SIGN_BODY, "true");
    }
    
    public void setEncryptBody() {
        this.actionList[this.currentAction].put(WSSHandlerConstants.ENCRYPT_BODY, "true");
    }
}
