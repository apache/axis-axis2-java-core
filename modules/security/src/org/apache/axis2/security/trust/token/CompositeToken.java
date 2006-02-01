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

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMText;
import org.apache.axis2.security.trust.TrustException;

public abstract class CompositeToken extends AbstractToken {

	public CompositeToken() {
		super();
	}
	
	/**
	 * @param elem
	 * @throws TrustException
	 */
	public CompositeToken(OMElement elem) throws TrustException {
		super(elem);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.security.trust.token.AbstractToken#setElementTextValue(org.apache.ws.commons.om.OMText)
	 */
	protected void setElementTextValue(OMText textNode) throws TrustException {
		throw new TrustException(TrustException.INVALID_REQUEST,
				TrustException.DESC_TEXT_IN_COMPOSITE_ELEM,
				new Object[]{this.getToken().getNamespaceURI(),
				this.getToken().getLocalPart(),
				textNode.getText()});
	}

}
