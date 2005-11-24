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

package javax.xml.rpc;

/**
 * Interface JAXRPCContext
 * The JAXRPCContext interface provides an extensible property mechanism that 
 * may be used to configure BindingProvider instances and to pass imformation 
 * between applications and handlers.
 * 
 * @version 1.0
 * @author sunja07
 */
public interface JAXRPCContext {
	/**
	 * Standard property: Map of attachments to a message, key is the MIME 
	 * Content-ID, value is a DataHandler.
	 * Type: java.util.Map
	 */
	static final java.lang.String MESSAGE_ATTACHMENTS = 
		"javx.xml.rpc.binding.attachments";

	/**
	 * Standard property: input source for WSDL document.
	 * Type: org.xml.sax.InputSource
	 */
	static final java.lang.String WSDL_DESCRIPTION = 
		"javax.xml.rpc.wsdl.description";

	/**
	 * Standard property: name of WSDL service.
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_SERVICE = 
		"javax.xml.rpc.wsdl.service";

	/**
	 * Standard property: name of WSDL port.
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_PORT = 
		"javax.xml.rpc.wsdl.port";

	/**
	 * Standard property: name of wsdl interface (2.0) or port type (1.1).
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_INTERFACE = 
		"javax.xml.rpc.wsdl.interface";

	/**
	 * Standard property: name of WSDL operation.
	 * Type: javax.xml.namespace.QName
	 */
	static final java.lang.String WSDL_OPERATION = 
		"javax.xml.rpc.wsdl.operation";

	/**
	 * Method setProperty
	 * Sets the name and value of a property associated with the context. If 
	 * the context contains a value of the same property, the old value is 
	 * replaced.
	 * @param name Name of the property associated with the context
	 * @param value Value of the property
	 * @throws java.lang.IllegalArgumentException If some aspect of the 
	 * property is prevents it from being stored in the context
	 */
	void setProperty(java.lang.String name,
            java.lang.Object value) throws java.lang.IllegalArgumentException;

	/**
	 * Method removeProperty
	 * Removes a property (name-value pair) from the context
	 * @param name Name of the property to be removed
	 * @throws java.lang.IllegalArgumentException if an illegal property name 
	 * is specified
	 */
	void removeProperty(java.lang.String name) throws 
	java.lang.IllegalArgumentException;

	/**
	 * Method getProperty
	 * Gets the value of a specific property from the MessageContext
	 * @param name Name of the property whose value is to be retrieved
	 * @return Value of the property
	 * @throws java.lang.IllegalArgumentException if an illegal property name 
	 * is specified
	 */
	java.lang.Object getProperty(java.lang.String name) throws 
	java.lang.IllegalArgumentException;

	/**
	 * Method getPropertyNames
	 * Returns an Iterator view of the names of the properties in this context
	 * @return Iterator for the property names
	 */
	java.util.Iterator getPropertyNames();
}
