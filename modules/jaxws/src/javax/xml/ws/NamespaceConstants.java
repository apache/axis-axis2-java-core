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

package javax.xml.ws;

/**
 * Class NamespaceConstants
 * Constants used in JAX-RPC for namespace prefixes and URIs
 * 
 * @version 1.0
 * @author sunja07
 */
public class NamespaceConstants {

	/**
	 * Namespace prefix for SOAP Envelope 
	 */
	public static final java.lang.String NSPREFIX_SOAP_ENVELOPE = "soapenv";
	
	/**
	 * Namespace prefix for SOAP Encoding 
	 */
	public static final java.lang.String NSPREFIX_SOAP_ENCODING = "soapenc";
	
	/**
	 * Namespace prefix for XML Schema XSD 
	 */
	public static final java.lang.String NSPREFIX_SCHEMA_XSD = "xsd";
	
	/**
	 * Namespace prefix for XML Schema XSI 
	 */
	public static final java.lang.String NSPREFIX_SCHEMA_XSI = "xsi";
	
	/**
	 * Nameapace URI for SOAP 1.1 Envelope 
	 */
	public static final java.lang.String NSURI_SOAP_ENVELOPE = 
		"http://schemas.xmlsoap.org/soap/envelope/";
	
	/**
	 * Nameapace URI for SOAP 1.1 Encoding 
	 */
	public static final java.lang.String NSURI_SOAP_ENCODING = 
		"http://schemas.xmlsoap.org/soap/encoding/";
	
	/**
	 * Nameapace URI for SOAP 1.1 next actor role 
	 */
	public static final java.lang.String NSURI_SOAP_NEXT_ACTOR = 
		"http://schemas.xmlsoap.org/soap/actor/next";
	
	/**
	 * Namespace URI for XML Schema XSD 
	 */
	public static final java.lang.String NSURI_SCHEMA_XSD = 
		"http://www.w3.org/2001/XMLSchema";
	
	/**
	 * Namespace URI for XML Schema XSI 
	 */
	public static final java.lang.String NSURI_SCHEMA_XSI = 
		"http://www.w3.org/2001/XMLSchema-instance";
	
	/**
	 * Empty Constructor
	 */
	public NamespaceConstants() {}
}
