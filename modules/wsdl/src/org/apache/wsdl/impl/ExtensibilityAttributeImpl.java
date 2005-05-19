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

package org.apache.wsdl.impl;

import javax.xml.namespace.QName;

import org.apache.wsdl.ExtensibilityAttribute;

/**
 * @author chathura@opensource.lk
 *
 */
public class ExtensibilityAttributeImpl implements ExtensibilityAttribute {
	
	private QName key;
	
	private QName value;
	
	/**
	 * @param key
	 * @param value
	 */
	public ExtensibilityAttributeImpl(QName key, QName value) {
		this.key = key;
		this.value = value;
	}
	public QName getKey() {
		return key;
	}
	public QName getValue() {
		return value;
	}
}
