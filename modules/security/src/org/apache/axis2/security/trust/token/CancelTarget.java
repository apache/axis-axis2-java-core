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
import org.apache.axis2.security.util.Axis2Util;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.SecurityTokenReference;

import javax.xml.namespace.QName;

public class CancelTarget extends CompositeToken {

    public static final QName TOKEN = new QName(Constants.WST_NS, Constants.LN.CANCEL_TARGET, Constants.WST_PREFIX);
    
    private OMElement targetToken;
    private SecurityTokenReference securityTokenReference;
    
    public CancelTarget(SecurityTokenReference securityTokenReference) throws TrustException {
		super();
		this.securityTokenReference = securityTokenReference;
		this.tokenElement.addChild(Axis2Util.toOM(this.securityTokenReference.getElement()));
	}
    
    /**
     * This can be used when we are creating a new CancelTaget which will
     * refer to a targetToken rather than a SecurityTokenReference 
     */
    public CancelTarget() {
    	super();
    }

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public CancelTarget(OMElement elem) throws TrustException {
		super(elem);
	}
	
	/**
	 * Sets the cancel target element 
	 * @param targetToken
	 */
	public void setCancelTarget(OMElement targetToken) {
		if(this.securityTokenReference != null) {
			this.tokenElement.getFirstChildWithName(
					new QName(WSConstants.WSSE_NS,
							SecurityTokenReference.SECURITY_TOKEN_REFERENCE))
					.detach();
		}
		if(this.targetToken != null) {
			this.tokenElement.detach();
		}
		
		this.targetToken = targetToken;
		this.tokenElement.addChild(this.targetToken);
	}
	
	/**
	 * Sets the cancel target security token reference
	 * @param securityTokenReference
	 * @throws TrustException
	 */
	public void setCancelTarget(SecurityTokenReference securityTokenReference) throws TrustException {
		if(this.targetToken != null) {
			this.targetToken.detach();
		}
		if(this.securityTokenReference != null) {
			this.tokenElement.getFirstChildWithName(
					new QName(WSConstants.WSSE_NS,
							SecurityTokenReference.SECURITY_TOKEN_REFERENCE))
					.detach();
		}
		
		this.securityTokenReference = securityTokenReference;
		this.tokenElement.addChild(Axis2Util.toOM(this.securityTokenReference.getElement()));
	}
	
	/**
	 * Returns the security token reference to the token to be cancelled
	 * @return
	 */
	public SecurityTokenReference getSecurityTokenReference() {
		return securityTokenReference;
	}
	
	/**
	 * Returns the target token to be cancelled
	 * @return
	 */
	public OMElement getTargetToken() {
		return targetToken;
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
		QName el =  new QName(element.getNamespace().getName(), element.getLocalName());
		
		if(el.equals(new QName(WSConstants.WSSE_NS,SecurityTokenReference.SECURITY_TOKEN_REFERENCE)) && this.targetToken == null) {
			try {
				this.securityTokenReference = new SecurityTokenReference(Axis2Util.toDOM(element));
			} catch (WSSecurityException wsse) {
				throw new TrustException(wsse.getMessage(),wsse);
			}
		} else if(this.securityTokenReference == null) {
			this.targetToken = element;
		}
	}

}
