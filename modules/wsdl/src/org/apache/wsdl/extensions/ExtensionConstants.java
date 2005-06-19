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

package org.apache.wsdl.extensions;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 *
 */
public interface ExtensionConstants {
	
	/**
	 * The Type name for the SOAP Address defined in the Port/Endpoint
	 */
	public static final QName SOAP_ADDRESS =  new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
	
	public static final QName SOAP_OPERATION = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation");
	
	public static final QName SCHEMA = new QName("http://www.w3.org/2001/XMLSchema", "schema");
	

}
