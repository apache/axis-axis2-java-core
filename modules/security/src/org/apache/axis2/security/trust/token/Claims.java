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
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TrustException;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class Claims extends CompositeToken {

	public static final QName TOKEN = new QName(Constants.WST_NS,
			Constants.LN.CLAIMS, Constants.WST_PREFIX);

	public Claims() {
		super();
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public Claims(OMElement elem) throws TrustException {
		super(elem);
	}

	
	/**
	 * Sets the value of the wst:Claims/@Dialect
	 * Dialect attribute specifies a URI to indicate the syntax of the claims
	 * @param value
	 */
	public void setDialectAttribute(String value) {
		this.tokenElement.addAttribute(factory.createOMAttribute(
				Constants.ATTR.CLAIMS_DIALECT, null, value));
	}
	
	/**
	 * Returns the value of the wst:Claims/@Dialect
	 * @return
	 */
	public String getDialectAttribute() {
		return this.tokenElement.getAttribute(
				new QName(Constants.ATTR.CLAIMS_DIALECT)).getAttributeValue();
	}
	
	/**
	 * Add a token as a child
	 * This is provided as an extensibility mechanism to add any
	 * child element to the <code>wst:Claims</code> element
	 * @param token
	 */
	public void addToken(OMElement token) {
		this.tokenElement.addChild(token);
	}
	
	/**
	 * Retuns an interator of child elements
	 * @return
	 */
	public Iterator getChildTokens() {
		return this.tokenElement.getChildElements();
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#getToken()
	 */
	protected QName getToken() {
		return TOKEN;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#deserializeChildElement(org.apache.axis2.om.OMElement)
	 */
	protected void deserializeChildElement(OMElement element)
			throws TrustException {
		//Do nothing , since there's no constraint on what can be included here
	}

}
