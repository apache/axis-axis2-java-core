/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.marshaller;

import javax.jws.WebParam.Mode;


/**
 * Stores Method Parameter as Name and Value. Method Parameter can be an input Method Parameter or output Method parameter.
 * input Method Parameter is a input to a java Method.
 * output Method Parameter is a return parameter from a java Method.
 */
public class MethodParameter {
	
	private String name = null;
	private Object value = null;
	private Mode mode = null;
	private Class type = null;
	private Class actualType = null; //If parameter is a GenericType, this property stores the actual Type
	private boolean isHolder = false;
	
	public MethodParameter(String name, Object value, Mode mode) {
		super();
		this.name = name;
		this.value = value;
		this.mode = mode;
	}
	public MethodParameter(String name, Object value, Mode mode, Class type, Class actualType, boolean isHolder) {
		this(name,value,mode);
		this.type = type;
		this.isHolder = isHolder;
		this.actualType = actualType;
	}
	public String getName() {
		return name;
	}
	public Object getValue() {
		return value;
	}
	public Mode getMode() {
		return mode;
	}
	public Class getType() {
		return type;
	}
	public void setType(Class type) {
		this.type = type;
	}
	public Class getActualType() {
		return this.actualType;
	}
	public void setActualType(Class actualType) {
		this.actualType = actualType;
	}
	public boolean isHolder() {
		return isHolder;
	}
	public void setHolder(boolean isHolder) {
		this.isHolder = isHolder;
	}
}
