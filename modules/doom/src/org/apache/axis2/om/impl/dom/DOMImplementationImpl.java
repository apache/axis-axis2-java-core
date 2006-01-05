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
package org.apache.axis2.om.impl.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class DOMImplementationImpl implements DOMImplementation {

	public boolean hasFeature(String arg0, String arg1) {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype)
			throws DOMException {
		//TODO Handle docType stuff
		DocumentImpl doc = new DocumentImpl();
		
		new ElementImpl(doc,DOMUtil.getLocalName(qualifiedName),new NamespaceImpl(namespaceURI, DOMUtil.getPrefix(qualifiedName)));
		
		return doc;
	}

	public DocumentType createDocumentType(String qualifiedName, String publicId, 
			String systemId) throws DOMException {
		// TODO 
		throw new UnsupportedOperationException("TODO");
	}

	/*
	 * DOM-Level 3 methods
	 */
	
	public Object getFeature(String arg0, String arg1) {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

}
