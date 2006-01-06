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
import org.apache.axis2.om.OMNode;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TrustException;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class IssueRequestSecurityToken extends RequestSecurityToken {

	private AppliesTo appliesTo;
	private Claims claims;
	private Entropy entropy;
	private Lifetime lifetime;
	private KeySize keySize;
	private Renewing renewing;
	
	public IssueRequestSecurityToken() {
		super(Constants.REQ_TYPE.ISSUE_SECURITY_TOKEN);
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public IssueRequestSecurityToken(OMElement elem) throws TrustException {
		super(elem);
	}
	
	/**
	 * Sets the value of the <code>AppliesTo</code> element.
	 * @param value
	 */
	public void setAppliesTo(OMElement value) {
		if(this.appliesTo == null) {
			this.appliesTo = new AppliesTo();
			this.tokenElement.addChild(this.appliesTo.tokenElement);
		}
		
		this.appliesTo.addToken(value);
	}
	
	/**
	 * Returns the first child of the <code>AppliesTo</code> element.
	 * @return Returns OMElement.
	 */
	public OMElement getAppliesTo() {
		if(this.appliesTo != null) {
			Iterator children = this.appliesTo.tokenElement.getChildElements();
			while (children.hasNext()) {
				OMNode node = (OMNode) children.next();
				if(node.getType() == OMNode.ELEMENT_NODE) {
					//Return the first child element
					return (OMElement)node;
				}
			}
			//If an child element is not found 
			return null;
		} else {
			return null;
		}
	}
	
	/**
	 * Can be used when setting one claim in the <code>Claims</code> element
	 * @param dialectURI Dialect attribute specifies a URI to indicate the syntax of the claims
	 * @param claimElement
	 */
	public void setClaims(String dialectURI, OMElement claimElement) {
		if(this.claims == null) {
			this.claims = new Claims();
			this.tokenElement.addChild(this.claims.tokenElement);
		}
		
		this.claims.setDialectAttribute(dialectURI);
		this.claims.addToken(claimElement);
	}
	
	/**
	 * This should be used to set a ste of claims in the <code>Claims</code> element
	 * @param dialectURI Dialect attribute specifies a URI to indicate the syntax of the claims
	 * @param claimsElements Iterator of OMElements
	 * @throws TrustException
	 */
	public void addClaims(String dialectURI, Iterator claimsElements)
			throws TrustException {
		if(this.claims == null) {
			this.claims = new Claims();
		}
		
		this.claims.setDialectAttribute(dialectURI);
		while (claimsElements.hasNext()) {
			OMNode node = (OMNode) claimsElements.next();
			if(node.getType() == OMNode.ELEMENT_NODE) {
				this.claims.addToken((OMElement)node);
			}
		}
	}
	
	/**
	 * Returns the <code>Claims</code> element.
	 * @return Returns Claims.
	 */
	public Claims getClaims() {
		return this.claims;
	}
	
	
	/**
	 * Sets the <code>wst:Entropy/wst:BinarySecret</code> value and 
	 * <code>wst:Entropy/wst:BinarySecret@Type</code> of the 
	 * <code>wst:RequestSecurityToken</code>
	 * @param binarySecretType 
	 * @param entropyValue
	 */
	public void setEntropy(String binarySecretType, String entropyValue) {
		if(this.entropy == null) {
			this.entropy = new Entropy();
			this.tokenElement.addChild(this.entropy.tokenElement);
		}
		
		this.entropy.setBinarySecret(binarySecretType, entropyValue);
	}
	
	/**
	 * Sets the binary secret of the Entropy element when the its of type <code>Nonce</code>
	 * @see org.apache.axis2.security.trust.Constants.BINARY_SECRET_TYPE#NONCE_VAL
	 * @param nonceValue The nonce value
	 */
	public void setEntropyNonce(String nonceValue) {
		this.setEntropy(Constants.BINARY_SECRET_TYPE.NONCE_VAL, nonceValue);
	}
	
	/**
	 * Returns the <code>Entropy</code> element
	 * @return Returns Entropy.
	 */
	public Entropy getEntropy() {
		return this.entropy;
	}
	

	/**
	 * Adds a <code>wst:Lifetime</code> element with the given duration to the 
	 * <code>wst:RequestSecurityToken</code>
	 * @param lifetimeInMillis
	 */
	public void setLifetime(long lifetimeInMillis) {
		if(this.lifetime != null) {
			this.lifetime.tokenElement.detach();
		}
		
		this.lifetime = new Lifetime(lifetimeInMillis);
		this.tokenElement.addChild(this.lifetime.tokenElement);
	}
	
	/**
	 * Retuns the <code>Lifetime</code> element
	 * @return Returns Lifetime.
	 */
	public Lifetime getLifetime() {
		return this.lifetime;
	}
	
	/**
	 * Sets the <code>wst:KeySize</code> value of the <code>wst:RequestSecurityToken</code>
	 * @param size
	 */
	public void setKeySize(int size) {
		if(this.keySize == null) {
			this.keySize = new KeySize();
			this.tokenElement.addChild(this.keySize.tokenElement);
		}
		this.keySize.setKeySize(size);
	}
	
	/**
	 * Retuns the <code>KeySize</code> element
	 * @return Returns KeySize.
	 */
	public KeySize getKeySize() {
		return this.keySize;
	}
	
	/**
	 * Sets the <code>wst:Renewing</code> element of the 
	 * <code>wst:RequestSecurityToken</code>
	 * @param allow
	 * @param ok
	 */
	public void setRenewing(boolean allow, boolean ok) {
		if(this.renewing == null) {
			this.renewing = new Renewing();
			this.tokenElement.addChild(this.renewing.tokenElement);
		}
		this.renewing.setAllow(allow);
		this.renewing.setOK(ok);
	}
	
	/**
	 * Returns the <code>Renewing</code> element
	 * @return Returns Renewing.
	 */
	public Renewing getRenewing() {
		return this.renewing;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.RequestSecurityToken#handleSpecificChildren(org.apache.axis2.om.OMElement)
	 */
	protected void handleSpecificChildren(OMElement element)
			throws TrustException {
		QName el =  new QName(element.getNamespace().getName(), element.getLocalName());
		
		if(el.equals(AppliesTo.TOKEN)) {
			this.appliesTo = new AppliesTo(element);
		} else if(el.equals(Claims.TOKEN)) {
			this.claims = new Claims(element);
		} else if(el.equals(Entropy.TOKEN)) {
			this.entropy = new Entropy(element);
		} else if(el.equals(Lifetime.TOKEN)) {
			this.lifetime = new Lifetime(element);
		} else if(el.equals(KeySize.TOKEN)) {
			this.keySize = new KeySize(element);
		} else if(el.equals(Renewing.TOKEN)) {
			this.renewing = new Renewing(element);
		} else {
        	throw new TrustException(TrustException.INVALID_REQUEST,
        			TrustException.DESC_INCORRECT_CHILD_ELEM,
					new Object[] {
        			TOKEN.getPrefix(),TOKEN.getLocalPart(),
					el.getNamespaceURI(),el.getLocalPart()});
		}
	}

}
