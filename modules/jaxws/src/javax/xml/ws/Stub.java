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
 * Interface Stub
 * The interface javax.xml.rpc.Stub is the common base interface for the stub 
 * classes. All generated stub classes are required to implement the 
 * javax.xml.rpc.Stub interface. An instance of a stub class represents a 
 * client side proxy or stub instance for the target service endpoint.
 * The javax.xml.rpc.Stub interface provides an extensible property mechanism 
 * for the dynamic configuration of a stub instance.
 * 
 * @version 1.0
 * @author sunja07
 */
public interface Stub extends BindingProvider{
	
	/**
	 * Method _setProperty
	 * Sets the name and value of a configuration property for this Stub 
	 * instance. This method is retained for backwards compatibility, new code 
	 * should use getRequestContext().setProperty(...) instead. If the Stub 
	 * instances contains a value of the same property, the old value is 
	 * replaced.
	 * Note that the _setProperty method may not perform validity check on a 
	 * configured property value. An example is the standard property for the 
	 * target service endpoint address that is not checked for validity in the 
	 * _setProperty method. In this case, stub configuration errors are 
	 * detected at the remote method invocation.
	 * 
	 * @param name - Name of the configuration property
	 * @param value - Value of the property 
	 * @throws WebServiceException - 
	 * 1. If an optional standard property name is specified, however this Stub
	 *  implementation class does not support the configuration of this 
	 *  property. 
	 * 2. If an invalid or unsupported property name is specified or if a value
	 *  of mismatched property type is passed. 
	 * 3. If there is any error in the configuration of a valid property.
	 */
	void _setProperty(java.lang.String name, java.lang.Object value) throws 
	WebServiceException;
	
	/**
	 * Method _getProperty
	 * Gets the value of a specific configuration property. This method is 
	 * retained for backwards compatibility, new code should use 
	 * getRequestContext().getProperty(...) instead.
	 * 
	 * @param name - Name of the property whose value is to be retrieved 
	 * @return Value of the configuration property 
	 * @throws WebServiceException - if an invalid or unsupported property name 
	 * is passed.
	 */
	java.lang.Object _getProperty(java.lang.String name) throws 
	WebServiceException;
	
	/**
	 * Method _getPropertyNames
	 * Returns an Iterator view of the names of the properties that can be 
	 * configured on this stub instance. This method is retained for backwards 
	 * compatibility, new code should use getRequestContext().getPropertyNames(...)
	 * instead.
	 * 
	 * @return Iterator for the property names of the type java.lang.String
	 */
	java.util.Iterator _getPropertyNames();

}
