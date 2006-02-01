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

import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.om.OMText;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TrustException;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Iterator;

public abstract class AbstractToken {
	
	protected abstract QName getToken();
	protected OMFactory factory = OMAbstractFactory.getOMFactory();
	protected OMElement tokenElement;
	protected OMNamespace wstNamespace;
	
	public AbstractToken() {
		QName token = this.getToken();
		this.tokenElement = factory.createOMElement(token.getLocalPart(), token.getNamespaceURI(),Constants.WST_PREFIX);
		wstNamespace = factory.createOMNamespace(Constants.WST_NS, Constants.WST_PREFIX);
	}

	public AbstractToken(OMElement elem) throws TrustException {
		QName token = this.getToken();
        QName el =  new QName(elem.getNamespace().getName(), elem.getLocalName());
        if (!el.equals(token))
            throw new TrustException(TrustException.INVALID_REQUEST, "badTokenType", new Object[]{el});
        
        this.tokenElement = elem;
        this.parse(this.tokenElement);
	}

	/**
	 * This is called for each of the  immediate 
	 * child elements of type <code>OMNode.ELEMENT_NODE</code> of this token * 
	 * @param element
	 * @throws TrustException
	 */
	private void parse(OMElement element) throws TrustException {
		Iterator children = element.getChildElements();
		while (children.hasNext()) {
			OMNode child = (OMNode) children.next();
			switch (child.getType()) {
				case OMNode.ELEMENT_NODE :
					this.deserializeChildElement((OMElement)child);
					break;
				case Node.TEXT_NODE :
					this.setElementTextValue((OMText)child);
					break;
			}
			
		}
		
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}
	

	/**
	 * This is called for each of the  immediate 
	 * child elements of type <code>OMNode.ELEMENT_NODE</code> of this token 
	 * @param element
	 */
	protected abstract void deserializeChildElement(OMElement element)throws TrustException;
	
	/**
	 * This is called with a <code>OMText</code> node of the
	 * current element
	 * @param textNode
	 */
	protected abstract void setElementTextValue(OMText textNode) throws TrustException;
	
}
