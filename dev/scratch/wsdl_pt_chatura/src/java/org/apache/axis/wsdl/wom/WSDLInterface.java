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
package org.apache.axis.wsdl.wom;

import java.net.URI;
import java.util.List;

/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLInterface {
	public List getFeatures();

	public void setFeatures(List features);

	public List getProperties();

	public void setProperties(List properties);

	public List getDefinedOperations(WSDLInterface wsdlInterface);

	public List getDefinedOperations();

	/**
	 * @return
	 */
	public List getFaults();

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @return
	 */
	public List getOperations();

	/**
	 * @return
	 */
	public List getSuperInterfaces();

	/**
	 * @return
	 */
	public URI getTargetnamespace();

	/**
	 * @param list
	 */
	public void setFaults(List list);

	/**
	 * @param string
	 */
	public void setName(String string);

	/**
	 * @param list
	 */
	public void setOperations(List list);

	/**
	 * @param list
	 */
	public void setSuperInterfaces(List list);

	/**
	 * @param uri
	 */
	public void setTargetnamespace(URI uri);
}