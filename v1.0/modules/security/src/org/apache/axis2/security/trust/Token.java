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

package org.apache.axis2.security.trust;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * This represents a security token which can have either one of 4 states.
 * <ul>
 * <li>ISSUED</li>
 * <li>EXPIRED</li>
 * <li>CACELLED</li>
 * <li>RENEWED</li>
 * </ul>
 * Also this holds the <code>OMElement</code>s representing the token in its 
 * present state and the previous state.
 * 
 * These tokens are stired using the storage mechanism provided via the 
 * <code>TokenStorage</code> interface.
 * @see org.apache.axis2.security.trust.TokenStorage
 */
public class Token {
    
    private static Document dummyDoc = new OMDOMFactory().getDocument();
    
    public final static int ISSUED = 1;
    public final static int EXPIRED = 2;
    public final static int CANCELLED = 3;
    public final static int RENEWED = 4;
    
    /**
     * Token identifier
     */
    private String id;
    
    /**
     * Current state of the token
     */
    private int state = -1;
    
    /**
     * The actual token in its current state
     */
    private OMElement token;
    
    /**
     * The token in its previous state
     */
    private OMElement presivousToken;
    
    /**
     * The RequestedAttachedReference element
     * NOTE : The oasis-200401-wss-soap-message-security-1.0 spec allows 
     * an extensibility mechanism for wsse:SecurityTokenRefence and 
     * wsse:Refernce. Hence we cannot limit to the 
     * wsse:SecurityTokenRefence\wsse:Refernce case and only hold the URI and 
     * the ValueType values.
     */
    private OMElement attachedReference;
    
    /**
     * The RequestedUnattachedReference element
     * NOTE : The oasis-200401-wss-soap-message-security-1.0 spec allows 
     * an extensibility mechanism for wsse:SecurityTokenRefence and 
     * wsse:Refernce. Hence we cannot limit to the 
     * wsse:SecurityTokenRefence\wsse:Refernce case and only hold the URI and 
     * the ValueType values.
     */
    private OMElement unattachedReference;
    
    /**
     * A bag to hold anyother properties
     */
    private HashMap properties;

    /**
     * A flag to assist the TokenStorage
     */
    private boolean chnaged;
    
    /**
     * The secret associated with the Token
     */
    private byte[] secret;
    

    /**
     * Create a new token
     * @param id
     */
    public Token(String id){
        this.id = id;
    }
    
    public Token(String id, OMElement tokenElem) {
        this.id = id;
        this.token = (OMElement)dummyDoc.importNode((Element)tokenElem, true);
    }

    /**
     * @return Returns the chnaged.
     */
    public boolean isChnaged() {
        return chnaged;
    }

    /**
     * @param chnaged The chnaged to set.
     */
    public void setChnaged(boolean chnaged) {
        this.chnaged = chnaged;
    }
    
    /**
     * @return Returns the properties.
     */
    public HashMap getProperties() {
        return properties;
    }

    /**
     * @param properties The properties to set.
     */
    public void setProperties(HashMap properties) {
        this.properties = properties;
    }

    /**
     * @return Returns the state.
     */
    public int getState() {
        return state;
    }

    /**
     * @param state The state to set.
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return Returns the token.
     */
    public OMElement getToken() {
        return token;
    }

    /**
     * @param token The token to set.
     */
    public void setToken(OMElement token) {
        this.token = token;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Returns the presivousToken.
     */
    public OMElement getPresivousToken() {
        return presivousToken;
    }

    /**
     * @param presivousToken The presivousToken to set.
     */
    public void setPresivousToken(OMElement presivousToken) {
        this.presivousToken = presivousToken;
    }

    /**
     * @return Returns the secret.
     */
    public byte[] getSecret() {
        return secret;
    }

    /**
     * @param secret The secret to set.
     */
    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    /**
     * @return Returns the attachedReference.
     */
    public OMElement getAttachedReference() {
        return attachedReference;
    }

    /**
     * @param attachedReference The attachedReference to set.
     */
    public void setAttachedReference(OMElement attachedReference) {
        if(attachedReference != null) {
            this.attachedReference = (OMElement) dummyDoc.importNode(
                (Element) attachedReference, true);
        }
    }

    /**
     * @return Returns the unattachedReference.
     */
    public OMElement getUnattachedReference() {
        return unattachedReference;
    }

    /**
     * @param unattachedReference The unattachedReference to set.
     */
    public void setUnattachedReference(OMElement unattachedReference) {
        if(unattachedReference != null) {
            this.unattachedReference = (OMElement) dummyDoc.importNode(
                (Element) unattachedReference, true);
        }
    }
}
