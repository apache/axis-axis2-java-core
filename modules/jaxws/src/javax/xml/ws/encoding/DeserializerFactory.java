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
 * public interface DeserializerFactory
 * extends java.io.Serializable
 * <p>
 * The javax.xml.rpc.encoding.DeserializerFactory is a factory of deserializers. A DeserializerFactory is registered with a
 * TypeMapping instance as part of the TypeMappingRegistry.
 * @version 1.0
 * @author shaas02
 * @see <code>Serializer</code>
 */
public interface DeserializerFactory extends Serializable {

	/**
	 * Returns a Deserializer for the specified XML processing mechanism type.
	 * 
	 * @param mechanismType - XML processing mechanism type [TBD: definition of valid constants]
	 * @return
	 * @throws JAXRPCException - If DeserializerFactory does not support the specified XML processing mechanism
	 */
	Deserializer getDeserializerAs(java.lang.String mechanismType) throws JAXRPCException;
	
	/**
	 * Returns a list of all XML processing mechanism types supported by this DeserializerFactory.
	 * 
	 * @return List of unique identifiers for the supported XML processing mechanism types
	 */
	java.util.Iterator getSupportedMechanismTypes();
}
