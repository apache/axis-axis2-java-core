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

/**
 * public interface Serializer
 * extends java.io.Serializable
 * <p>
 * The javax.xml.rpc.encoding.Serializer interface defines the base interface for serializers. A Serializer converts a Java object to an
 * XML representation using a specific XML processing mechanism and based on the specified type mapping and encoding style.
 * @version 1.0
 * @author shaas02
 *
 */
public interface Serializer extends Serializable {

	/**
	 * Gets the type of the XML processing mechanism and representation used by this Serializer.
	 * 
	 * @return XML processing mechanism type
	 */
	java.lang.String getMechanismType();
}
