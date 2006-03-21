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

import java.util.HashMap;

import org.apache.ws.commons.om.OMElement;

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
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class Token {
    
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
     * @return Returns the chnaged.
     */
    protected boolean isChnaged() {
        return chnaged;
    }

    /**
     * @param chnaged The chnaged to set.
     */
    protected void setChnaged(boolean chnaged) {
        this.chnaged = chnaged;
    }

    /**
     * Create a new token
     * @param id
     */
    public Token(String id){
        this.id = id;
    }
    
    public Token(String id, OMElement tokenElem) {
        this.id = id;
        this.token = tokenElem; 
    }

    /**
     * @return Returns the properties.
     */
    protected HashMap getProperties() {
        return properties;
    }

    /**
     * @param properties The properties to set.
     */
    protected void setProperties(HashMap properties) {
        this.properties = properties;
    }

    /**
     * @return Returns the state.
     */
    protected int getState() {
        return state;
    }

    /**
     * @param state The state to set.
     */
    protected void setState(int state) {
        this.state = state;
    }

    /**
     * @return Returns the token.
     */
    protected OMElement getToken() {
        return token;
    }

    /**
     * @param token The token to set.
     */
    protected void setToken(OMElement token) {
        this.token = token;
    }

    /**
     * @return Returns the id.
     */
    protected String getId() {
        return id;
    }

    /**
     * @return Returns the presivousToken.
     */
    protected OMElement getPresivousToken() {
        return presivousToken;
    }

    /**
     * @param presivousToken The presivousToken to set.
     */
    protected void setPresivousToken(OMElement presivousToken) {
        this.presivousToken = presivousToken;
    }

    /**
     * @return Returns the secret.
     */
    protected byte[] getSecret() {
        return secret;
    }

    /**
     * @param secret The secret to set.
     */
    protected void setSecret(byte[] secret) {
        this.secret = secret;
    }
    
    
}
