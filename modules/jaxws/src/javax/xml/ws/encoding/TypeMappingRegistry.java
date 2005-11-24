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

import java.io.Serializable;
import javax.xml.rpc.JAXRPCException;

/**
 * The interface javax.xml.rpc.encoding.TypeMappingRegistry  defines a registry of TypeMapping instances for various
 * encoding styles.
 * @version 1.0
 * @author shaas02
 *
 */
public interface TypeMappingRegistry extends Serializable {

	/**
	 * Registers a TypeMapping instance with the TypeMappingRegistry. This method replaces any existing registered
	 * TypeMapping instance for the specified encodingStyleURI.
	 * 
	 * @param encodingStyleURI - An encoding style specified as an URI. An example is
	 * "http://schemas.xmlsoap.org/soap/encoding/"
	 * @param mapping - TypeMapping instance
	 * @return Previous TypeMapping associated with the specified encodingStyleURI, or null if there was no TypeMapping
	 * associated with the specified encodingStyleURI
	 * @throws JAXRPCException - If there is an error in the registration of the TypeMapping for the specified encodingStyleURI.
	 */
	TypeMapping register(java.lang.String encodingStyleURI,
			TypeMapping mapping) throws JAXRPCException;
	
	/**
	 * Registers the TypeMapping instance that is default for all encoding styles supported by the TypeMappingRegistry. A
	 * default TypeMapping  should include serializers and deserializers that are independent of and usable with any encoding
	 * style. Successive invocations of the registerDefault method replace any existing default TypeMapping instance.
	 * <p>
	 * If the default TypeMapping is registered, any other TypeMapping instances registered through the
	 * TypeMappingRegistry.register method (for a set of encodingStyle URIs) override the default TypeMapping.
	 * 
	 * @param mapping - TypeMapping instance
	 * @throws JAXRPCException - If there is an error in the registration of the default TypeMapping
	 */
	void registerDefault(TypeMapping mapping) throws JAXRPCException;
	
	/**
	 * Gets the registered default TypeMapping instance. This method returns null if there is no registered default
	 * TypeMapping in the registry.
	 * @return The registered default TypeMapping instance or null
	 */
	TypeMapping getDefaultTypeMapping();
	
	/**
	 * Returns a list of registered encodingStyle URIs in this TypeMappingRegistry instance.
	 * @return Array of the registered encodingStyle URIs
	 */
	java.lang.String[] getRegisteredEncodingStyleURIs();
	
	/**
	 * Returns the registered TypeMapping for the specified encodingStyle URI. If there is no registered TypeMapping  for the
	 * specified encodingStyleURI, this method returns null.
	 * @param encodingStyleURI  - Encoding style specified as an URI
	 * @return TypeMapping for the specified encodingStyleURI or null
	 */
	TypeMapping getTypeMapping(java.lang.String encodingStyleURI);
	
	/**
	 * Creates a new empty TypeMapping object
	 * @return TypeMapping instance
	 */
	TypeMapping createTypeMapping();
	
	/**
	 * Unregisters a TypeMapping instance, if present, from the specified encodingStyleURI.
	 * 
	 * @param encodingStyleURI  - Encoding style specified as an URI
	 * @return TypeMapping instance that has been unregistered or null if there was no TypeMapping registered for the
	 * specified encodingStyleURI
	 */
	TypeMapping unregisterTypeMapping(java.lang.String encodingStyleURI);
	
	/**
	 * Removes a TypeMapping from the TypeMappingRegistry. A TypeMapping is associated with 1 or more
	 * encodingStyleURIs. This method unregisters the specified TypeMapping instance from all associated
	 * encodingStyleURIs and then removes this TypeMapping instance from the registry.
	 * @param mapping  - TypeMapping to be removed
	 * @return true if specified TypeMapping is removed from the TypeMappingRegistry; false  if the specified TypeMapping
	 * was not in the TypeMappingRegistry
	 */
	boolean removeTypeMapping(TypeMapping mapping);
	
	/**
	 * Removes all registered TypeMappings and encodingStyleURIs from this TypeMappingRegistry.
	 */
	void clear();
}
