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

package org.apache.rahas;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.util.XmlSchemaDateFormat;

import javax.xml.namespace.QName;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

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
 * @see org.apache.rahas.TokenStorage
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
    private OMElement previousToken;
    
    /**
     * The RequestedAttachedReference element
     * NOTE : The oasis-200401-wss-soap-message-security-1.0 spec allows 
     * an extensibility mechanism for wsse:SecurityTokenReference and 
     * wsse:Reference. Hence we cannot limit to the 
     * wsse:SecurityTokenReference\wsse:Reference case and only hold the URI and 
     * the ValueType values.
     */
    private OMElement attachedReference;
    
    /**
     * The RequestedUnattachedReference element
     * NOTE : The oasis-200401-wss-soap-message-security-1.0 spec allows 
     * an extensibility mechanism for wsse:SecurityTokenRefence and 
     * wsse:Reference. Hence we cannot limit to the 
     * wsse:SecurityTokenReference\wsse:Reference case and only hold the URI and 
     * the ValueType values.
     */
    private OMElement unattachedReference;
    
    /**
     * A bag to hold any other properties
     */
    private Properties  properties;

    /**
     * A flag to assist the TokenStorage
     */
    private boolean changed;
    
    /**
     * The secret associated with the Token
     */
    private byte[] secret;
    
    /**
     * Created time
     */
    private Date created;
    
    /**
     * Expiration time
     */
    private Date expires;
    
    public Token(String id,
                 OMElement tokenElem,
                 Date created,
                 Date expires) throws TrustException {
        this.id = id;
        this.token = new StAXOMBuilder(DOOMAbstractFactory.getOMFactory(),
                tokenElem.getXMLStreamReader()).getDocumentElement();
        this.created = created;
        this.expires = expires;
    }

    public Token(String id,
                 OMElement tokenElem,
                 OMElement lifetimeElem) throws TrustException {
        this.id = id;
        this.token = new StAXOMBuilder(DOOMAbstractFactory.getOMFactory(),
                tokenElem.getXMLStreamReader()).getDocumentElement();
        this.processLifeTime(lifetimeElem);
    }
    
    /**
     * @param lifetimeElem
     * @throws TrustException 
     */
    private void processLifeTime(OMElement lifetimeElem) throws TrustException {
        try {
            DateFormat zulu = new XmlSchemaDateFormat();
            OMElement createdElem =
                    lifetimeElem.getFirstChildWithName(new QName(WSConstants.WSU_NS,
                                                                 WSConstants.CREATED_LN));
            this.created = zulu.parse(createdElem.getText());
            
            OMElement expiresElem =
                    lifetimeElem.getFirstChildWithName(new QName(WSConstants.WSU_NS,
                                                                 WSConstants.EXPIRES_LN));
            this.expires = zulu.parse(expiresElem.getText());
        } catch (OMException e) {
            throw new TrustException("lifeTimeProcessingError",
                                     new String[]{lifetimeElem.toString()}, e);
        } catch (ParseException e) {
            throw new TrustException("lifeTimeProcessingError",
                                     new String[]{lifetimeElem.toString()}, e);
        }
    }

    /**
     * @return Returns the chnaged.
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * @param chnaged The chnaged to set.
     */
    public void setChanged(boolean chnaged) {
        this.changed = chnaged;
    }
    
    /**
     * @return Returns the properties.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties The properties to set.
     */
    public void setProperties(Properties properties) {
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
    public OMElement getPreviousToken() {
        return previousToken;
    }

    /**
     * @param presivousToken The presivousToken to set.
     */
    public void setPreviousToken(OMElement presivousToken) {
        this.previousToken = new StAXOMBuilder(DOOMAbstractFactory.getOMFactory(),
                presivousToken.getXMLStreamReader()).getDocumentElement();
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
            this.attachedReference = new StAXOMBuilder(DOOMAbstractFactory
                    .getOMFactory(), attachedReference.getXMLStreamReader())
                    .getDocumentElement();
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
            this.unattachedReference = new StAXOMBuilder(DOOMAbstractFactory
                    .getOMFactory(), unattachedReference.getXMLStreamReader())
                    .getDocumentElement();
        }
    }

    /**
     * @return Returns the created.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return Returns the expires.
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * @param expires The expires to set.
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
