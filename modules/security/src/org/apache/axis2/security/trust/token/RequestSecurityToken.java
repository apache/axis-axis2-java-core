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


public abstract class RequestSecurityToken extends CompositeToken {

	protected TokenType tokenTypeElement;
	protected RequestType requestTypeElement;
	
	public static final QName TOKEN = new QName(Constants.WST_NS,
			Constants.LN.REQUESTED_SECURITY_TOKEN, Constants.WST_PREFIX);
	
	public RequestSecurityToken(String requestType) {
		super();
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public RequestSecurityToken(OMElement elem) throws TrustException {
		super(elem);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#getToken()
	 */
	protected QName getToken() {
		return TOKEN;
	}
	
	/**
	 * Sets the <code>wst:RequestType</code> value of this <code>wst:RequestSecurityToken</code>
	 * @param requestType The <code>wst:RequestType</code> uri as a <code>String
	 */
	public void setRequestType(String requestType) {
	  	if(this.requestTypeElement == null) {
	  		this.requestTypeElement = new RequestType();
	  		this.tokenElement.addChild(this.requestTypeElement.tokenElement);
	  	}

		this.requestTypeElement.setValue(requestType);

	}
	
	/**
	 * Returns the request type if it is set
	 * @return
	 */
	public String getRequestType() {
		if(this.requestTypeElement != null) {
			return this.requestTypeElement.getValue();
		}
		return null;
	}
	
	/**
	 * Sets the <code>wst:TokenType</code> value of this <code>wst:RequestSecurityToken</code>
	 * @param tokenType The <code>wst:TokenType</code> uri as a <code>String</code>
	 */
	public void setTokenType(String tokenType) {
	  	if(this.tokenTypeElement == null) { 
	  		this.tokenTypeElement = new TokenType();
	  		this.tokenElement.addChild(this.tokenTypeElement.tokenElement);
	  	}

	  	this.tokenTypeElement.setValue(tokenType);
	}
	
	/**
	 * Returns the token type is set
	 * @return
	 */
	public String getTokenType() {
		if(this.tokenTypeElement != null) {
			return tokenTypeElement.getValue();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#deserializeChildElement(org.apache.axis2.om.OMElement)
	 */
	protected void deserializeChildElement(OMElement element)
			throws TrustException {
		QName el =  new QName(element.getNamespace().getName(), element.getLocalName());
		if(el.equals(RequestType.TOKEN)) {
			this.requestTypeElement = new RequestType(element);
		} else if(el.equals(TokenType.TOKEN)) {
			this.tokenTypeElement = new TokenType(element);
		} else {
			this.handleSpecificChildren(element);
		}
	}

	/**
	 * This is provided as an extensibility mechanism to add any
	 * child element to the <code>wst:RequestSecyrityToken</code> element
	 * @param childToken
	 */
	public void addToken(OMElement childToken) {
		this.tokenElement.addChild(childToken);
	}
	
	/**
	 * Returns the requested token if available
	 * @param token
	 * @return
	 */
	public OMElement getToken(QName tokenQName) {
		return this.tokenElement.getFirstChildWithName(tokenQName);
	}
	
	/**
	 * /wst:RequestSecurityToken/@Context
	 * This URI specifies an identifier/context for this request.
	 * @param contextAttrValue
	 */
	public void setContextAttr(String contextAttrValue) {
		this.tokenElement.addAttribute(Constants.ATTR.CONTEXT,
				contextAttrValue, wstNamespace);
	}
	
	public String getContextAttrValue() {
		return this.tokenElement.getAttribute(
				new QName(wstNamespace.getName(), Constants.ATTR.CONTEXT,
						wstNamespace.getPrefix())).getAttributeValue();
	}
	
	/**
	 * This is provided as an extensibility mechnism to 
	 * ass any attrbute to the <code>wst:RequestSecyrityToken</code> element
	 * @param attribute Name of the attr
	 * @param value Attr value
	 * @param namespace Attr namespace
	 */
	public void addAttribute(String attribute, String value,
			OMNamespace namespace) {
		this.tokenElement.addAttribute(attribute, value, namespace);
	}
	
	/**
	 * This is to be used to retrieve the value of the 
	 * custom attrbutes added to the 
	 * <code>wst:RequestSecyrityToken</code>
	 * @param attribute
	 * @return
	 */
	public String getAttributeValue(QName attribute) {
		return this.tokenElement.getAttribute(attribute).getAttributeValue();
	}
	
	public OMElement getRequestElement() {
		return this.tokenElement;
	}
	
	/**
	 * @param element Specific child element
	 */
	protected abstract void handleSpecificChildren(OMElement element) throws TrustException;

}
