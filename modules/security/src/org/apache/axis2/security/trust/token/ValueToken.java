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
import org.apache.axis2.om.OMText;
import org.apache.axis2.security.trust.TrustException;

/**
 * This is the base class for the elements that carries a
 * value in the element
 * Example:
 * 	<wsu:Created>...</wsu:Created>
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public abstract class ValueToken extends AbstractToken {
	
	protected OMText valueText;
	
	public ValueToken() {
		super();
	}
	
	public ValueToken(OMElement elem) throws TrustException {
		super(elem);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#deserializeChildElement(org.apache.axis2.om.OMElement)
	 */
	protected void deserializeChildElement(OMElement element)
			throws TrustException {
		//There cannot be any children in this token
		throw new TrustException(TrustException.INVALID_REQUEST,
				TrustException.DESC_CHILD_IN_VALUE_ELEM,
				new Object[] {
				this.getToken().getNamespaceURI(),this.getToken().getLocalPart(),
				element.getNamespace().getName(),element.getLocalName()});

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#setElementTextValue(org.apache.axis2.om.OMText)
	 */
	protected void setElementTextValue(OMText textNode) {
		this.valueText = textNode;
	}
	
	/**
	 * Returns the value of the token
	 * @return
	 */
	public String getValue() {
		if(this.valueText != null) 
			return this.valueText.getText();
		else
			return null;
	}
	
	/**
	 * Sets the value of the token
	 * @param value
	 */
	public void setValue(String value) {
    	if(this.valueText != null)
    		this.valueText.detach();
    	
    	this.valueText = factory.createText(value);
        this.tokenElement.addChild(this.valueText);
	}
	
}
