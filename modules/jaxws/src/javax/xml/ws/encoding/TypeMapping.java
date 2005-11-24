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

package javax.xml.rpc.encoding;

import javax.xml.rpc.JAXRPCException;

/**
 * public interface TypeMapping
 * <p>
 * The javax.xml.rpc.encoding.TypeMapping is the base interface for the representation of a type mapping. A TypeMapping
 *  implementation class may support one or more encoding styles.
 * <p>
 * For its supported encoding styles, a TypeMapping instance maintains a set of tuples of the type {Java type,
 * SerializerFactory, DeserializerFactory, XML type}.
 * @version 1.0
 * @author shaas02
 *
 */
public interface TypeMapping {
	
	/**
	 * Returns the encodingStyle URIs (as String[]) supported by this TypeMapping instance. A TypeMapping that contains only
	 *  encoding style independent serializers and deserializers returns null from this method.
	 * @return Array of encodingStyle URIs for the supported encoding styles
	 */
	java.lang.String[] getSupportedEncodings();
	
	/**
	 * Sets the encodingStyle URIs supported by this TypeMapping instance. A TypeMapping that contains only encoding
	 * independent serializers and deserializers requires null as the parameter for this method.
	 * @param encodingStyleURIs - Array of encodingStyle URIs for the supported encoding styles
	 */
	void setSupportedEncodings(java.lang.String[] encodingStyleURIs);
	
	/**
	 * Checks whether or not type mapping between specified XML type and Java type is registered.
	 * @param javaType - Class of the Java type
	 * @param xmlType - Qualified name of the XML data type
	 * @return boolean; true if type mapping between the specified XML type and Java type is registered; otherwise false
	 */
	boolean isRegistered(java.lang.Class javaType,
			javax.xml.namespace.QName xmlType);
	
	/**
	 * Registers SerializerFactory and DeserializerFactory for a specific type mapping between an XML type and Java type.
	 * This method replaces any existing registered SerializerFactory DeserializerFactory instances.
	 * 
	 * @param javaType - Class of the Java type
	 * @param xmlType  - Qualified name of the XML data type
	 * @param sf - SerializerFactory
	 * @param dsf - DeserializerFactory
	 * @throws JAXRPCException  - If any error during the registration
	 */
	void register(java.lang.Class javaType,
			javax.xml.namespace.QName xmlType,
			SerializerFactory sf,
			DeserializerFactory dsf)throws JAXRPCException;
	
	/**
	 * Gets the SerializerFactory registered for the specified pair of Java type and XML data type.
	 * @param javaType  - Class of the Java type
	 * @param xmlType  - Qualified name of the XML data type
	 * @return Registered SerializerFactory or null  if there is no registered factory
	 */
	SerializerFactory getSerializer(java.lang.Class javaType,
			javax.xml.namespace.QName xmlType);
	
	/**
	 * Gets the DeserializerFactory registered for the specified pair of Java type and XML data type.
	 * @param javaType  - Class of the Java type
	 * @param xmlType  - Qualified name of the XML data type
	 * @return Registered DeserializerFactory or null  if there is no registered factory
	 */
	DeserializerFactory getDeserializer(java.lang.Class javaType,
			javax.xml.namespace.QName xmlType);
	
	/**
	 * Removes the SerializerFactory registered for the specified pair of Java type and XML data type.
	 * @param javaType
	 * @param xmlType
	 * @throws JAXRPCException - If there is error in removing the registered SerializerFactory
	 */
	void removeSerializer(java.lang.Class javaType,
			javax.xml.namespace.QName xmlType)throws JAXRPCException;
	
	/**
	 * Removes the DeserializerFactory registered for the specified pair of Java type and XML data type.
	 * @param javaType
	 * @param xmlType
	 * @throws JAXRPCException - If there is error in removing the registered DeserializerFactory
	 */
	void removeDeserializer(java.lang.Class javaType,
			javax.xml.namespace.QName xmlType)throws JAXRPCException;

}
