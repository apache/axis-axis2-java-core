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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TrustException;

import javax.xml.namespace.QName;

public class BinarySecret extends ValueToken {

    public static final QName TOKEN = new QName(Constants.WST_NS,
			Constants.LN.BINARY_SECRET, Constants.WST_PREFIX);
    
	public BinarySecret() {
		super();
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public BinarySecret(OMElement elem) throws TrustException {
		super(elem);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#getToken()
	 */
	protected QName getToken() {
		return TOKEN;
	}
	
	/**
	 * Sets the value of the .../wst:BinarySecret/@Type attribute
	 * @param type
	 */
	public void setTypeAttribute(String type) {
		this.tokenElement.addAttribute(Constants.ATTR.BINARY_SECRET_TYPE, type,
				null);
	}
	
	/**
	 * Retuns the value of the .../wst:BinarySecret/@Type attribute
	 * @return
	 */
	public String getTypeAttribute() {
		return this.tokenElement.getAttribute(
				new QName(Constants.ATTR.BINARY_SECRET_TYPE))
				.getAttributeValue();
	}
	
	/**
	 * Adds the given attribute
	 * @param attribute
	 * @param value
	 * @param namespace
	 */
	public void addAttribute(String attribute, String value,
			OMNamespace namespace) {
		this.tokenElement.addAttribute(attribute, value, namespace);
	}
	
	/**
	 * Returns the value of the requested attribute
	 * @param attribute
	 * @return
	 */
	public String getAttributeValue(QName attribute) {
		return this.tokenElement.getAttribute(attribute).getAttributeValue();
	}

}
