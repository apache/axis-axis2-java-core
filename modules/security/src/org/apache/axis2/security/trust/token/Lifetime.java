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
package org.apache.axis2.security.trust.token;

import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TrustException;
import org.apache.ws.commons.om.OMElement;

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Lifetime extends CompositeToken {

    public static final QName TOKEN = new QName(Constants.WST_NS,
			Constants.LN.LIFE_TIME, Constants.WST_PREFIX);
	
    private Created created;
    private Expires expires;
    
	public Lifetime(String created, String expires) {
		super();
		
		this.created = new Created();
		this.created.setValue(created);
		this.tokenElement.addChild(this.created.tokenElement);
		
		this.expires = new Expires();
		this.expires.setValue(expires);
		this.tokenElement.addChild(this.expires.tokenElement);
	}
	
	/**
	 * 
	 * @param lifeTime Lifetime in milliseconds
	 */
	public Lifetime(long lifeTime) {
        SimpleDateFormat sdtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdtf.setTimeZone(TimeZone.getTimeZone("GMT"));

        Calendar rightNow = Calendar.getInstance();
        Calendar expires = Calendar.getInstance();
        
		this.created = new Created();
		this.created.setValue(sdtf.format(rightNow.getTime()));
		this.tokenElement.addChild(this.created.tokenElement);
		
		this.expires = new Expires();
		long exp = rightNow.getTime().getTime() + lifeTime;
        expires.setTimeInMillis(exp);
		this.expires.setValue(sdtf.format(expires.getTime()));
		this.tokenElement.addChild(this.expires.tokenElement);
		
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public Lifetime(OMElement elem) throws TrustException {
		super(elem);
	}
	
    /**
     * Retuns the value of the <code>wsu:Created</code> child element. 
     * @return Returns String.
     */
    public String getCreated() {
    	if(this.created != null)
    		return this.created.getValue();
    	else
    		return null;
    }

    /**
     * Returns the value of the <code>wsu:Expires</code> element.
     * @return Returns String.
     */
    public String getExpires() {
    	if(this.expires != null)
    		return this.expires.getValue();
    	else
    		return null;
    }

    
    /**
     * Sets the value of the <code>wsu:Created</code>element.
     * @param value
     */
    public void setCreated(String value) {
    	if(this.created != null)
    		this.created.setValue(value);
    	else { 
    		this.created = new Created();
    		this.created.setValue(value);
    		this.tokenElement.addChild(this.created.tokenElement);
    	}
    }
    
    /**
     * Sets the value of the <code>wsu:Expires</code> element.
     * @param value
     */
    public void setExpires(String value) {
    	if(this.expires != null)
    		this.expires.setValue(value);
    	else { 
    		this.expires = new Expires();
    		this.expires.setValue(value);
    		this.tokenElement.addChild(this.expires.tokenElement);
    	}
    }
    
	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#getToken()
	 */
	protected QName getToken() {
		return TOKEN;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#deserializeChildElement(org.apache.ws.commons.om.OMElement)
	 */
	protected void deserializeChildElement(OMElement element)
			throws TrustException {
		QName el = new QName(element.getNamespace().getName(), element
				.getLocalName());
		if(el.equals(Created.TOKEN)) {
			this.created = new Created(element);
		} else if(el.equals(Expires.TOKEN)) {
			this.expires = new Expires(element);
		} else {
        	throw new TrustException(TrustException.INVALID_REQUEST,
        			TrustException.DESC_INCORRECT_CHILD_ELEM,
					new Object[] {
        			TOKEN.getPrefix(),TOKEN.getLocalPart(),
					el.getNamespaceURI(),el.getLocalPart()});
		}
	}

}
