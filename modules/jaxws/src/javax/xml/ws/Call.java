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
 * Interface Call
 * The javax.xml.rpc.Call interface provides support for the dynamic 
 * invocation of a service endpoint. The javax.xml.rpc.Service interface acts 
 * as a factory for the creation of Call instances.
 * Once a Call instance is created, various setter and getter methods may be 
 * used to configure this Call instance.
 * 
 * @version 1.0
 * @author sunja07
 */
public interface Call extends BindingProvider{
	
	/**
	 * Standard property for operation style. This property is set to "rpc" 
	 * if the operation style is rpc; "document" if the operation style is 
	 * document.
	 */
	static final java.lang.String OPERATION_STYLE_PROPERTY = 
		"javax.xml.rpc.soap.operation.style";
	
	/**
	 * Standard property for encoding Style: Encoding style specified as a 
	 * namespace URI. The default value is the SOAP 1.1 encoding 
	 * http://schemas.xmlsoap.org/soap/encoding/
	 */
	static final java.lang.String ENCODINGSTYLE_URI_PROPERTY = 
		"javax.xml.rpc.encodingstyle.namespace.uri";
	
	/**
	 * Method isParameterAndReturnSpecRequired
	 * Indicates whether addParameter and setReturnType methods are to be 
	 * invoked to specify the parameter and return type specification for a 
	 * specific operation.
	 * @param operationName Qualified name of the operation
	 * @return Returns true if the Call implementation class requires 
	 * addParameter and setReturnType to be invoked in the client code for 
	 * the specified operation. This method returns false otherwise.
	 * @throws java.lang.IllegalArgumentException - If invalid operation name 
	 * is specified
	 */
	boolean isParameterAndReturnSpecRequired(
			javax.xml.namespace.QName operationName) 
	throws java.lang.IllegalArgumentException;
	
	/**
	 * Method addParameter
	 * Adds a parameter type and mode for a specific operation. Note that the 
	 * client code may not call any addParameter and setReturnType methods 
	 * before calling the invoke method. In this case, the Call implementation 
	 * class determines the parameter types by using reflection on parameters, 
	 * using the WSDL description and configured type mapping registry.
	 * @param paramName Name of the parameter
	 * @param xmlType XML type of the parameter
	 * @param parameterMode Mode of the parameter-whether ParameterMode.IN, 
	 * ParameterMode.OUT, or ParameterMode.INOUT
	 * @throws javax.xml.rpc.JAXRPCException This exception may be thrown if 
	 * the method isParameterAndReturnSpecRequired returns false for this 
	 * operation.
	 * @throws java.lang.IllegalArgumentException If any illegal parameter 
	 * name or XML type is specified
	 */
	void addParameter(java.lang.String paramName,
            javax.xml.namespace.QName xmlType,
            ParameterMode parameterMode) throws javax.xml.rpc.JAXRPCException,
			java.lang.IllegalArgumentException;
			
	/**
	 * Method addParameter
	 * Adds a parameter type and mode for a specific operation. This method is 
	 * used to specify the Java type for either OUT or INOUT parameters. 
	 * @param paramName Name of the parameter
	 * @param xmlType XML type of the parameter
	 * @param javaType Java class of the parameter
	 * @param parameterMode Mode of the parameter-whether ParameterMode.IN, 
	 * OUT or INOUT 
	 * @throws java.lang.IllegalArgumentException If any illegal parameter 
	 * name or XML type is specified 
	 * @throws java.lang.UnsupportedOperationException If this method is not 
	 * supported
	 * @throws javax.xml.rpc.JAXRPCException 
	 * 1.This exception may be thrown if this method is invoked when the 
	 * method isParameterAndReturnSpecRequired returns false.
	 * 2.If specified XML type and Java type mapping is not valid. For 
	 * example, TypeMappingRegistry has no serializers for this mapping. 
	 */
	void addParameter(java.lang.String paramName,
            javax.xml.namespace.QName xmlType,
            java.lang.Class javaType,
            ParameterMode parameterMode) throws 
            java.lang.IllegalArgumentException,
			java.lang.UnsupportedOperationException, 
			javax.xml.rpc.JAXRPCException;
	
	/**
	 * Method getParameterTypeByName
	 * Gets the XML type of a parameter by name 
	 * @param paramName Name of the parameter
	 * @return Returns XML type for the specified parameter
	 */
	javax.xml.namespace.QName getParameterTypeByName(
			java.lang.String paramName);
	
	/**
	 * Method setReturnType
	 * Sets the return type for a specific operation. Invoking 
	 * setReturnType(null) removes the return type for this Call object. 
	 * @param xmlType XML data type of the return value
	 * @throws javax.xml.rpc.JAXRPCException This exception may be thrown when 
	 * the method isParameterAndReturnSpecRequired returns false. 
	 * @throws java.lang.IllegalArgumentException If an illegal XML type is 
	 * specified 
	 */
	void setReturnType(javax.xml.namespace.QName xmlType) throws 
	javax.xml.rpc.JAXRPCException,
	java.lang.IllegalArgumentException, 
	java.lang.UnsupportedOperationException;
	
	/**
	 * Method setReturnType
	 * Sets the return type for a specific operation.
	 * @param xmlType XML data type of the return value
	 * @param javaType Java Class of the return value
	 * @throws java.lang.UnsupportedOperationException If this method is not 
	 * supported 
	 * @throws java.lang.IllegalArgumentException If an illegal XML type is 
	 * specified
	 * @throws javax.xml.rpc.JAXRPCException
	 * 1. This exception may be thrown if this method is invoked when the 
	 * method isParameterAndReturnSpecRequired returns false.
	 * 2. If XML type and Java type cannot be mapped using the standard type 
	 * mapping or TypeMapping registry
	 */
	void setReturnType(javax.xml.namespace.QName xmlType,
            java.lang.Class javaType) throws 
            java.lang.UnsupportedOperationException,
			java.lang.IllegalArgumentException,
			javax.xml.rpc.JAXRPCException;

	/**
	 * Method getReturnType
	 * Gets the return type for a specific operation
	 * @return Returns the XML type for the return value
	 */
	javax.xml.namespace.QName getReturnType();
	
	/**
	 * Method removeAllParameters
	 * Removes all specified parameters from this Call instance. Note that 
	 * this method removes only the parameters and not the return type. The 
	 * setReturnType(null) is used to remove the return type.
	 * @throws javax.xml.rpc.JAXRPCException This exception may be thrown If 
	 * this method is called when the method isParameterAndReturnSpecRequired 
	 * returns false for this Call's operation.
	 */
	void removeAllParameters() throws javax.xml.rpc.JAXRPCException;
	
	/**
	 * Method getOperationName
	 * Gets the name of the operation to be invoked using this Call instance.
	 * @return Qualified name of the operation
	 */
	javax.xml.namespace.QName getOperationName();
	
	/**
	 * Method setOperationName
	 * Sets the name of the operation to be invoked using this Call instance. 
	 * @param operationName QName of the operation to be invoked using the 
	 * Call instance
	 */
	void setOperationName(javax.xml.namespace.QName operationName);
	
	/**
	 * Method getPortTypeName
	 * Gets the qualified name of the port type.
	 * @return Qualified name of the port type
	 */
	javax.xml.namespace.QName getPortTypeName();
	
	/**
	 * Method setPortTypeName
	 * Sets the qualified name of the port type. 
	 * @param portType Qualified name of the port type
	 */
	void setPortTypeName(javax.xml.namespace.QName portType);
	
	/**
	 * Method setTargetEndpointAddress
	 * Sets the address of the target service endpoint. This address must 
	 * correspond to the transport specified in the binding for this Call 
	 * instance.
	 * @param address Address of the target service endpoint; specified as an 
	 * URI
	 */
	void setTargetEndpointAddress(java.lang.String address);
	
	/**
	 * Method getTargetEndpointAddress
	 * Gets the address of a target service endpoint. 
	 * @return Address of the target service endpoint as an URI
	 */
	java.lang.String getTargetEndpointAddress();
	
	/**
	 * Method setProperty
	 * Sets the value for a named property. JAX-RPC specification specifies a 
	 * standard set of properties that may be passed to the Call.setProperty 
	 * method.
	 * @param name Name of the property
	 * @param value Value of the property
	 * @throws javax.xml.rpc.JAXRPCException
	 * 1. If an optional standard property name is specified, however this 
	 * Call implementation class does not support the configuration of this 
	 * property.
	 * 2. If an invalid (or unsupported) property name is specified or if a 
	 * value of mismatched property type is passed.
	 * 3. If there is any error in the configuration of a valid property.
	 */
	void setProperty(java.lang.String name,
            java.lang.Object value) throws javax.xml.rpc.JAXRPCException;
	
	/**
	 * Method getProperty
	 * Gets the value of a named property. 
	 * @param name Name of the property
	 * @return Value of the named property
	 * @throws javax.xml.rpc.JAXRPCException if an invalid or unsupported 
	 * property name is passed.
	 */
	java.lang.Object getProperty(java.lang.String name);
	
	/**
	 * Method removeProperty
	 * Removes a named property.
	 * @param name Name of the property
	 * @throws javax.xml.rpc.JAXRPCException if an invalid or unsupported 
	 * property name is passed.
	 */
	void removeProperty(java.lang.String name);
	
	/**
	 * Method getPropertyNames
	 * Gets the names of configurable properties supported by this Call object.
	 * @return Iterator for the property names
	 */
	java.util.Iterator getPropertyNames();
	
	/**
	 * Method invoke
	 * Invokes a specific operation using a synchronous request-response 
	 * interaction mode.
	 * @param inputParams Object[]--Parameters for this invocation. This 
	 * includes only the input params
	 * @return Returns the return value or null
	 * @throws java.rmi.RemoteException if there is any error in the remote 
	 * method invocation
	 * @throws javax.xml.rpc.soap.SOAPFaultException Indicates a SOAP fault
	 * @throws javax.xml.rpc.JAXRPCException 
	 * 1. If there is an error in the configuration of the Call object
	 * 2. If inputParams do not match the required parameter set (as specified
	 *    through the addParameter invocations or in the corresponding WSDL)
	 * 3. If parameters and return type are incorrectly specified 
	 */
	java.lang.Object invoke(java.lang.Object[] inputParams)
    throws java.rmi.RemoteException, javax.xml.rpc.soap.SOAPFaultException, 
    javax.xml.rpc.JAXRPCException;
	
	/**
	 * Method invoke
	 * Invokes a specific operation using a synchronous request-response 
	 * interaction mode.
	 * @param operationName QName of the operation
	 * @param inputParams Object[]--Parameters for this invocation. This 
	 * includes only the input params.
	 * @return Returns the return value or null
	 * @throws java.rmi.RemoteException if there is any error in the remote 
	 * method invocation
	 * @throws javax.xml.rpc.soap.SOAPFaultException Indicates a SOAP fault
	 * @throws javax.xml.rpc.JAXRPCException 
	 * 1. If there is an error in the configuration of the Call object
	 * 2. If inputParams do not match the required parameter set (as specified 
	 *    through the addParameter invocations or in the corresponding WSDL)
	 * 3. If parameters and return type are incorrectly specified
	 */
	java.lang.Object invoke(javax.xml.namespace.QName operationName,
            java.lang.Object[] inputParams)
            throws java.rmi.RemoteException;
	
	/**
	 * Method invokeOneWay
	 * @param inputParams Object[]--Parameters for this invocation. This 
	 * includes only the input params.
	 * @throws javax.xml.rpc.JAXRPCException if there is an error in the 
	 * configuration of the Call object (example: a non-void return type has 
	 * been incorrectly specified for the one-way call) or if there is any 
	 * error during the invocation of the one-way remote call
	 */
	void invokeOneWay(java.lang.Object[] inputParams) throws 
	javax.xml.rpc.JAXRPCException;
	
	/**
	 * Method getOutputParams
	 * Returns a Map of {name, value} for the output parameters of the last 
	 * invoked operation. The parameter names in the returned Map are of type 
	 * java.lang.String.
	 * @return Map Output parameters for the last Call.invoke(). Empty Map is 
	 * returned if there are no output parameters.
	 * @throws JAXRPCException If this method is invoked for a one-way 
	 * operation or is invoked before any invoke method has been called.
	 */
	java.util.Map getOutputParams() throws JAXRPCException;
	
	/**
	 * Method getOutputValues
	 * Returns a List values for the output parameters of the last invoked 
	 * operation.
	 * @return java.util.List Values for the output parameters. An empty List 
	 * is returned if there are no output values.
	 * @throws JAXRPCException If this method is invoked for a one-way 
	 * operation or is invoked before any invoke method has been called.
	 */
	java.util.List getOutputValues() throws JAXRPCException;
}
