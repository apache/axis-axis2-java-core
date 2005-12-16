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

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TrustException;
import org.apache.ws.security.message.token.SecurityTokenReference;

public class CancelRequestSecurityToken extends RequestSecurityToken {

	CancelTarget cancelTarget;
	
	public CancelRequestSecurityToken() {
		super(Constants.REQ_TYPE.CANCEL_SECURITY_TOKEN);
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public CancelRequestSecurityToken(OMElement elem) throws TrustException {
		super(elem);
	}
	
	/**
	 * Sets the cancel target with the target token
	 * @param targetToken
	 */
	public void setCancelTarget(OMElement targetToken) {
		if(this.cancelTarget == null) {
			this.cancelTarget = new CancelTarget();
			this.tokenElement.addChild(this.cancelTarget.tokenElement);
		}
		
		this.cancelTarget.setCancelTarget(targetToken);
	}
	
	/**
	 * Sets the cancel target with a security token reference
	 * @param securityTokenReference
	 * @throws TrustException
	 */
	public void setCancelTarget(SecurityTokenReference securityTokenReference) throws TrustException {
		if(this.cancelTarget == null) {
			this.cancelTarget = new CancelTarget();
			this.tokenElement.addChild(this.cancelTarget.tokenElement);
		}
		this.cancelTarget.setCancelTarget(securityTokenReference);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.RequestSecurityToken#handleSpecificChildren(org.apache.axis2.om.OMElement)
	 */
	protected void handleSpecificChildren(OMElement element) throws TrustException {
		QName el =  new QName(element.getNamespace().getName(), element.getLocalName());
		
		if(el.equals(CancelTarget.TOKEN)) {
			this.cancelTarget = new CancelTarget(element);
		} else {
        	throw new TrustException(TrustException.INVALID_REQUEST,
        			TrustException.DESC_INCORRECT_CHILD_ELEM,
					new Object[] {
        			TOKEN.getPrefix(),TOKEN.getLocalPart(),
					el.getNamespaceURI(),el.getLocalPart()});
		}
	}

}
