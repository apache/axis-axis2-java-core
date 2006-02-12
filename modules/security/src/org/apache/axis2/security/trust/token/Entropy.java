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

public class Entropy extends CompositeToken {

	public static final QName TOKEN = new QName(Constants.WST_NS,
			Constants.LN.ENTROPY, Constants.WST_PREFIX);
	
	private BinarySecret binarySecret;
	
	public Entropy() {
		super();
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public Entropy(OMElement elem) throws TrustException {
		super(elem);
	}

	/**
	 * Sets the binary secret value
	 * @param type The type uri of the binary secret as a <code>String</code>
	 * @param secretValue The binary secret value as a <code>String</code>
	 */
	public void setBinarySecret(String type, String secretValue) {
		this.binarySecret = new BinarySecret();
		this.binarySecret.setTypeAttribute(type);
		this.binarySecret.setValue(secretValue);
	}
	
	public BinarySecret getBinarySecret() {
		return this.binarySecret;
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
        QName el =  new QName(element.getNamespace().getName(), element.getLocalName());
        
        if(el.equals(BinarySecret.TOKEN)) {
        	this.binarySecret = new BinarySecret(element);
        } else {
        	throw new TrustException(TrustException.INVALID_REQUEST,
        			TrustException.DESC_INCORRECT_CHILD_ELEM,
					new Object[] {
        			TOKEN.getPrefix(),TOKEN.getLocalPart(),
					el.getNamespaceURI(),el.getLocalPart()});
        }
	}

}
