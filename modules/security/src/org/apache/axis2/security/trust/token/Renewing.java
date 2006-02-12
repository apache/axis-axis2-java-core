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
import org.apache.ws.commons.om.OMText;

import javax.xml.namespace.QName;

public class Renewing extends AbstractToken {

    public static final QName TOKEN = new QName(Constants.WST_NS,
			Constants.LN.RENEWING, Constants.WST_PREFIX);	
    
    //to request a renewable token.
    boolean isAllow;
    
    //to indicate that a renewable token is    acceptable if the requested duration exceeds the limit of the issuance service.
    boolean isOK;
    
	public Renewing() {
		super();
		//Defaults
        this.isAllow = true;
        this.isOK = false;
	}
	
	public Renewing(boolean isOK, boolean isAllow) {
		super();
		
		this.isAllow = isAllow;
		this.isOK = isOK;
		
		this.tokenElement.addAttribute(Constants.ATTR.RENEWING_ALLOW, String.valueOf(this.isAllow), null);
		this.tokenElement.addAttribute(Constants.ATTR.RENEWING_OK, String.valueOf(this.isOK), null);
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public Renewing(OMElement elem) throws TrustException {
		super(elem);
		// TODO Auto-generated constructor stub
	}

	
	
	public boolean isAllow() {
		return isAllow;
	}

	public void setAllow(boolean isAllow) {
		this.isAllow = isAllow;
		this.tokenElement.addAttribute(Constants.ATTR.RENEWING_ALLOW, String.valueOf(this.isAllow), null);
	}

	public boolean isOK() {
		return isOK;
	}

	public void setOK(boolean isOK) {
		this.isOK = isOK;
		this.tokenElement.addAttribute(Constants.ATTR.RENEWING_OK, String.valueOf(this.isOK), null);
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
		throw new TrustException(TrustException.INVALID_REQUEST,
		"There cannot be a child element in this element: " + TOKEN.getLocalPart());
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#setElementTextValue(org.apache.ws.commons.om.OMText)
	 */
	protected void setElementTextValue(OMText textNode) throws TrustException {
		throw new TrustException(TrustException.INVALID_REQUEST,
		"There cannot be a value in this element: " + TOKEN.getLocalPart());
	}

}
