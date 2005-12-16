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
import org.apache.axis2.security.trust.TrustException;

public class RequestSecurityTokenResponse extends CompositeToken {

	public RequestSecurityTokenResponse() {
		super();
	}

	/**
	 * @param elem
	 * @throws TrustException
	 */
	public RequestSecurityTokenResponse(OMElement elem) throws TrustException {
		super(elem);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#getToken()
	 */
	protected QName getToken() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#deserializeChildElement(org.apache.axis2.om.OMElement)
	 */
	protected void deserializeChildElement(OMElement element)
			throws TrustException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

}
