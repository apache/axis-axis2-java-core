/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.wsdl.extensions.impl;

import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.wsdl.impl.WSDLExtensibilityElementImpl;
import org.w3c.dom.Element;

/**
 * @author chathura@opensource.lk
 *
 */
public class SchemaImpl extends WSDLExtensibilityElementImpl implements ExtensionConstants, Schema {
	
	private Element elelment;
	
	public SchemaImpl(){
		type = SCHEMA;
	}
	
	/**
	 * 
	 * @return The schema Element as a DOM element
	 */
	public Element getElelment() {
		return elelment;
	}
	
	/**
	 * Sets the Schema Element as a DOM Element.
	 * @param elelment
	 */
	public void setElelment(Element elelment) {
		this.elelment = elelment;
	}
}
