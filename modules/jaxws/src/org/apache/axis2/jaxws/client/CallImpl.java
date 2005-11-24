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
package org.apache.axis2.jaxws.client;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.URI;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.JAXRPCContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.ParameterMode;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.jaxws.description.ParameterDesc;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingConstants;

/**
 * @author sunja07
 * Class CallImpl
 * <documentation> to be completed </documentation>
 * 
 * Forget not that Call instance is MUTABLE. i.e. the configuration of call
 * instance can be changed and it can be re-used for some other need.
 */
public class CallImpl extends BindingProviderImpl implements javax.xml.ws.Call {

	/**
	 * Field targetEndpointAddress
	 * The location where the webservice should be found
	 */
	private static String targetEndpointAddress;
	
	/**
	 * Field operationName 
	 * The name of the operation to invoke at the service endpoint
	 */
	private QName operationName;
	
	/**
	 * Field portTypeName
	 * The name of the corresponding port type in the wsdl 
	 */
	private static QName portTypeName;
	
	/**
	 * Field returnType
	 * The xml return type to expect from the method invocation
	 */
	private QName returnType=null;
	
	/**
	 * Field returnTypeClass
	 * The java class into which return value will be stuffed.
	 */
	private Class returnTypeClass;
	
	/**
	 * Field paramAndReturnSpecRequired
	 * A boolean flag that should be populated from the wsdl info, if
	 * the operation corresponding to this Call object has to have the
	 * parameters added explicitly and return type specified explicitly
	 * 
	 * This might change to become a hashmap with key being the operation
	 * name.
	 */
	protected static boolean paramAndReturnSpecRequired=false;
	protected static HashMap<QName,Boolean> paramAndRetReqMap = new HashMap<QName,Boolean>();
	
	/**
	 * Field propertyBag
	 * A hashmap that contains the configured values of standard properties
	 * allowed in the setProperty method.
	 */
	private static HashMap<String,Object> propertyBag = new HashMap<String,Object>();
	
	/**
	 * Field outputParams
	 * A hashmap of {name, value} for the output parameters of the last 
	 * invoked operation. The parameter names in the returned Map are of type 
	 * java.lang.String.
	 */
	private static HashMap<String,ParameterDesc> outputParams = new HashMap<String,ParameterDesc>();
	
	/**
	 * Field inputParams
	 * A hashmap of {name, value} pairs of the input parameters of the
	 * current operation. Value would be a parameter description object.
	 * Name would be of type String
	 */
	private HashMap<String,ParameterDesc> inputParams = new HashMap<String,ParameterDesc>();
	
	/**
	 * Field service object from which this call instance is created.
	 */
	private transient ServiceImpl service;
	/**
	 * 
	 */
	public CallImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

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
	public boolean isParameterAndReturnSpecRequired(QName operationName)
			throws IllegalArgumentException {
		//Who is going to set this requirement. Will it be specified in the
		//wsdl? I need clarification on the use case of this flag.
		
		//Since operationName is specifically being provided to us
		//I don't think we should hold a single boolean datamember with this
		//call object. We should either check on the fly some datastructure 
		//and return the decision, I guess OR better would be to have a map
		//that can return the decision taking operationName as the key.
		return (paramAndRetReqMap.get(operationName)!=null)
				? paramAndRetReqMap.get(operationName)
				: true; //if not specifically mentioned we would give 
						//benifit of doubt in favour.
	}

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
	 * @throws javax.xml.ws.WebServiceException This exception may be thrown if 
	 * the method isParameterAndReturnSpecRequired returns false for this 
	 * operation.
	 * @throws java.lang.IllegalArgumentException If any illegal parameter 
	 * name or XML type is specified
	 */
	public void addParameter(String paramName, QName xmlType,
			ParameterMode parameterMode) throws WebServiceException,
			IllegalArgumentException {
		
		if(isParameterAndReturnSpecRequired(this.operationName)==false) {
			throw new WebServiceException("This operation is configured not to " +
					"specify parameter and return type information ");
		}
		
		ParameterDesc param = new ParameterDesc();
		//ideally namespace of param should ideally be the namespace of the 
		//method i.e. operationName namespace. But for now, lets create a
		//dummy QName
		param.setName(new QName(paramName));
		param.setMode(parameterMode);
		param.setXmlType(xmlType);
		
		Class javaType;
		if(service.isJAXB_USAGE()) {
			javaType = getJAXBObjectClassForQName(xmlType);
		} else {
			javaType = getTypeMappingClassForQName(xmlType);
		}
		param.setJavaType(javaType);
		
		if(parameterMode.equals(ParameterMode.IN)) {
			inputParams.put(paramName,param);
		} else if (parameterMode.equals(ParameterMode.INOUT)) {
			inputParams.put(paramName,param);
			outputParams.put(paramName,param);
		} else {
			outputParams.put(paramName,param);
		}
		
		//Since addParameter is called we would mandate returnType to be set,
		//even if it isn't set otherwise
		paramAndRetReqMap.put(this.operationName, Boolean.TRUE);
	}

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
	 * @throws javax.xml.ws.WebServiceException 
	 * 1.This exception may be thrown if this method is invoked when the 
	 * method isParameterAndReturnSpecRequired returns false.
	 * 2.If specified XML type and Java type mapping is not valid. For 
	 * example, TypeMappingRegistry has no serializers for this mapping. 
	 */
	public void addParameter(String paramName, QName xmlType, Class javaType,
			ParameterMode parameterMode) throws IllegalArgumentException,
			UnsupportedOperationException, WebServiceException {

		if(isParameterAndReturnSpecRequired(this.operationName)==false) {
			throw new WebServiceException("This operation is configured not to " +
					"specify parameter and return type information ");
		}
		
		ParameterDesc param = new ParameterDesc();
		//actually namespace of param should ideally be the namespace of the 
		//method i.e. operationName namespace. But for now, lets create a
		//dummy QName
		param.setName(new QName(paramName));
		param.setMode(parameterMode);
		param.setXmlType(xmlType);
		param.setJavaType(javaType);
		//should there be a check above if the xmlType, javaType pair is valid?
		
		if(parameterMode.equals(ParameterMode.IN)) {
			inputParams.put(paramName,param);
		} else if (parameterMode.equals(ParameterMode.INOUT)) {
			inputParams.put(paramName,param);
			outputParams.put(paramName,param);
		} else {
			outputParams.put(paramName,param);
		}
		
		//Since addParameter is called we would mandate returnType to be set,
		//even if it isn't set otherwise
		paramAndRetReqMap.put(this.operationName, Boolean.TRUE);
	}

	/**
	 * Method getParameterTypeByName
	 * Gets the XML type of a parameter by name 
	 * @param paramName Name of the parameter
	 * @return Returns XML type for the specified parameter
	 */
	public QName getParameterTypeByName(String paramName) {
		//A parameter added would either be put into inputParams
		//or outputParams or both. Lets check with inputParams first
		//and if futile move to look in outputParams
		if(inputParams.containsKey(paramName)) {
			return inputParams.get(paramName).getXmlType();
		} else if(outputParams.containsKey(paramName)) {
			return outputParams.get(paramName).getXmlType();
		}
		//if not found in both, return null
		return null;
	}

	/**
	 * Method setReturnType
	 * Sets the return type for a specific operation. Invoking 
	 * setReturnType(null) removes the return type for this Call object. 
	 * @param xmlType XML data type of the return value
	 * @throws javax.xml.ws.WebServiceException This exception may be thrown when 
	 * the method isParameterAndReturnSpecRequired returns false. 
	 * @throws java.lang.IllegalArgumentException If an illegal XML type is 
	 * specified 
	 */
	public void setReturnType(QName xmlType) throws WebServiceException,
			IllegalArgumentException, UnsupportedOperationException {
		if(operationName==null) {
			throw new WebServiceException("Can't set returnType. Try setting the " +
					"operationName prior to calling setReturnType");
		}
		if(isParameterAndReturnSpecRequired(operationName)==false) {
			throw new WebServiceException("Call instance is configured not to " +
					"specify Parameter and ReturnType");
		}
		
		//TODO identify if the QName is valid, if not, throw IllegalArgumentException
		this.returnType = xmlType;
		if(service.isJAXB_USAGE()){
			Class jaxbBindedObjectClass = getJAXBObjectClassForQName(xmlType);
			returnTypeClass = jaxbBindedObjectClass;
		}
		else { //no JAXB_USAGE, so try getting things from registered TypeMapping
		//TODO get the java type mapped to this xmlType QName and set it as the 
		//returnTypeClass
		
		//come to think of this, we don't need to identify the mapped javaType
		//and set the returnTypeClass with it. Actually in the invoke method itself
		//we will get hold of the corresponding deserializer and ask it to
		//prepare the object out of the response OMElement. How does that sound!?!
		
		}

	}
	
	//This can be a utils class method
	public Class getJAXBObjectClassForQName(QName xmlType) {
		//This is a black box for now
		return Object.class;
	}
	
	public Class getTypeMappingClassForQName(QName xmlType) {
		//This is also small black box for now
		return Object.class;
	}

	/**
	 * Method setReturnType
	 * Sets the return type for a specific operation.
	 * @param xmlType XML data type of the return value
	 * @param javaType Java Class of the return value
	 * @throws java.lang.UnsupportedOperationException If this method is not 
	 * supported 
	 * @throws java.lang.IllegalArgumentException If an illegal XML type is 
	 * specified
	 * @throws javax.xml.ws.WebServiceException
	 * 1. This exception may be thrown if this method is invoked when the 
	 * method isParameterAndReturnSpecRequired returns false.
	 * 2. If XML type and Java type cannot be mapped using the standard type 
	 * mapping or TypeMapping registry
	 */
	public void setReturnType(QName xmlType, Class javaType)
			throws UnsupportedOperationException, IllegalArgumentException,
			WebServiceException {
		if(operationName==null) {
			throw new WebServiceException("Can't set returnType. Try setting the " +
					"operationName prior to calling setReturnType");
		}
		if(isParameterAndReturnSpecRequired(operationName)==false) {
			throw new WebServiceException("Call instance is configured not to " +
					"specify Parameter and ReturnType");
		}
		
		//TODO identify if the QName is valid, if not, throw IllegalArgumentException
		
		this.returnType = xmlType;
		
		if(service.isJAXB_USAGE()) { //JAXB is used
			//check if the JAXB bound class is compatible with the javaType
			//class mentioned here. If not throw JAXRPCException
			Class jaxbBindedJavaClass = getJAXBObjectClassForQName(xmlType);
			if(javaType.isAssignableFrom(jaxbBindedJavaClass)) {
				this.returnTypeClass = javaType;
			}
			else
				throw new WebServiceException("Set return type java class can't be cast " +
						"from underlying JAXB databinding object");
		}
		else {//no JAXB.
			//check if the typeMapping has a registration for this xmlType
			//and javaType pair. If not, throw JAXRPCException
			if(!service.getTypeMappingRegistry().
					getTypeMapping(ENCODINGSTYLE_URI_PROPERTY).
					isRegistered(javaType,xmlType))
				throw new WebServiceException("Invalid javaType for xmlType. " +
						"Underlying type mapping has no corresponding pair registered");
		}
	}

	/**
	 * Method getReturnType
	 * Gets the return type for a specific operation
	 * @return Returns the XML type for the return value
	 */
	public QName getReturnType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method removeAllParameters
	 * Removes all specified parameters from this Call instance. Note that 
	 * this method removes only the parameters and not the return type. The 
	 * setReturnType(null) is used to remove the return type.
	 * @throws javax.xml.ws.WebServiceException This exception may be thrown If 
	 * this method is called when the method isParameterAndReturnSpecRequired 
	 * returns false for this Call's operation.
	 */
	public void removeAllParameters() throws WebServiceException {
		inputParams.clear();
		outputParams.clear();
	}

	/**
	 * Method getOperationName
	 * Gets the name of the operation to be invoked using this Call instance.
	 * @return Qualified name of the operation
	 */
	public QName getOperationName() {
		return operationName;
	}

	/**
	 * Method setOperationName
	 * Sets the name of the operation to be invoked using this Call instance. 
	 * @param opName QName of the operation to be invoked using the 
	 * Call instance
	 */
	public void setOperationName(QName opName) {
		//All datastructures referring to existing operation may be cleared
		//Or else it could be a unnecessary memory hold up.
		inputParams.clear();
		operationName = opName;
		
		if(jaxRpcPhase != null){
			OperationDescription od = new OperationDescription(opName);
			java.util.ArrayList phaseList = new java.util.ArrayList();
			phaseList.add(jaxRpcPhase);
			od.setRemainingPhasesInFlow(phaseList);
			od.setPhasesOutFlow(phaseList);
			sContext.getServiceConfig().addOperation(od);
		}
	}

	/**
	 * Method getPortTypeName
	 * Gets the qualified name of the port type.
	 * @return Qualified name of the port type
	 */
	public QName getPortTypeName() {
		return portTypeName;
	}

	/**
	 * Method setPortTypeName
	 * Sets the qualified name of the port type. 
	 * @param portType Qualified name of the port type
	 */
	public void setPortTypeName(QName portType) {
		portTypeName = portType;
	}

	/**
	 * Method setTargetEndpointAddress
	 * Sets the address of the target service endpoint. This address must 
	 * correspond to the transport specified in the binding for this Call 
	 * instance.
	 * @param address Address of the target service endpoint; specified as an 
	 * URI
	 */
	public void setTargetEndpointAddress(String address) {
		targetEndpointAddress = address;
	}

	/**
	 * Method getTargetEndpointAddress
	 * Gets the address of a target service endpoint. 
	 * @return Address of the target service endpoint as an URI
	 */
	public String getTargetEndpointAddress() {
		return targetEndpointAddress;
	}

	/**
	 * Method setProperty
	 * Sets the value for a named property. JAX-RPC specification specifies a 
	 * standard set of properties that may be passed to the Call.setProperty 
	 * method.
	 * @param name Name of the property
	 * @param value Value of the property
	 * @throws javax.xml.ws.WebServiceException
	 * 1. If an optional standard property name is specified, however this 
	 * Call implementation class does not support the configuration of this 
	 * property.
	 * 2. If an invalid (or unsupported) property name is specified or if a 
	 * value of mismatched property type is passed.
	 * 3. If there is any error in the configuration of a valid property.
	 */
	public void setProperty(String name, Object value) throws WebServiceException {
		// TODO Auto-generated method stub
		
		//Here a long if-elseif...-else list of construct will come, checking
		//the name of the key with the standard property names acceptable
		//and putting the value of the property wherever there is a match or
		//else throwing a JAXRPCException
		if(name.equals(ENDPOINT_ADDRESS_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals(SESSION_MAINTAIN_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals(SOAPACTION_URI_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals(SOAPACTION_USE_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals(PASSWORD_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals(USERNAME_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals(JAXB_CONTEXT_PROPERTY)) {
			propertyBag.put(name,value);
		}else if(name.equals("javax.xml.rpc.soap.operation.style")) {
			//This is an optional property
			//If we don't support this we may throw a JAXRPCException
		}else if(name.equals("javax.xml.rpc.encodingstyle.namespace.uri")) {
			//This is an optional property
			//If we don't support this we may throw a JAXRPCException
		}else {
			//we may chose to ignore and add that into the bag or throw
			//a JAXRPCException. I'm good at the later choice :-)
			throw new WebServiceException("Property " + name +" not supported");
		}

	}

	/**
	 * Method getProperty
	 * Gets the value of a named property. 
	 * @param name Name of the property
	 * @return Value of the named property
	 * @throws javax.xml.ws.WebServiceException if an invalid or unsupported 
	 * property name is passed.
	 */
	public Object getProperty(String name) {
		return propertyBag.get(name);
	}

	/**
	 * Method removeProperty
	 * Removes a named property.
	 * @param name Name of the property
	 * @throws javax.xml.ws.WebServiceException if an invalid or unsupported 
	 * property name is passed.
	 */
	public void removeProperty(String name) {
		//do the error check of invalid or unsupported property name
		
		//if error check passed delete the property from the bag
		propertyBag.remove(name);
	}

	/**
	 * Method getPropertyNames
	 * Gets the names of configurable properties supported by this Call object.
	 * @return Iterator for the property names
	 */
	public Iterator getPropertyNames() {
		return propertyBag.values().iterator();
	}

	/**
	 * Method invoke
	 * Invokes a specific operation using a synchronous request-response 
	 * interaction mode.
	 * @param inputParams Object[]--Parameters for this invocation. This 
	 * includes only the input params
	 * @return Returns the return value or null
	 * @throws java.rmi.RemoteException if there is any error in the remote 
	 * method invocation
	 * @throws javax.xml.ws.soap.SOAPFaultException Indicates a SOAP fault
	 * @throws javax.xml.ws.WebServiceException 
	 * 1. If there is an error in the configuration of the Call object
	 * 2. If inputParams do not match the required parameter set (as specified
	 *    through the addParameter invocations or in the corresponding WSDL)
	 * 3. If parameters and return type are incorrectly specified 
	 */
	public Object invoke(Object[] inputParameters) throws RemoteException,
			SOAPFaultException, WebServiceException {

		//check if the call instance is properly configured. If not throw
		//a JAXRPCException.
		boolean throwJAXRPCException=false;
		if(operationName==null) {
			throwJAXRPCException=true;
		} else if(targetEndpointAddress==null) { 
			throwJAXRPCException=true;
		}else if(isParameterAndReturnSpecRequired(operationName) &&
					(inputParams == null && outputParams == null) && 
					returnType==null) {
			throwJAXRPCException=true;
			
			//TODO Here (in a seperate else) check if paramandreturnspecrequired is 
			//not set or is false for this
			//operation, then we should reflect upon the parameters using wsdl
			//information and typemapping registry configuration
		} else if (inputParameters.length != inputParams.size()){
			throwJAXRPCException=true;
		} else {
			//any other checks???
		}
		
		if(throwJAXRPCException) {
			throw new WebServiceException("Call instance not fully configured");
		}
		
		//I'll try to create an OMElement that would wrap the input params
		//and use that to invoke the invokeBlocking() method of Axis2's call
		//implementation.
		//I'm not sure if the way I wrap the contents into an OMElement should
		//keep track of any properties set on the call object viz. style and
		//use. For now am just wrapping each element as a child in the method
		//element
		OMFactory fac = OMAbstractFactory.getOMFactory();
		String operationNS = operationName.getNamespaceURI();
		OMNamespace omNS = (operationNS == null || operationNS == "")
					? fac.createOMNamespace("http://jaxwsforaxis2.org","ns1")
					: fac.createOMNamespace(operationNS, "ns1");
		OMElement methodElement = fac.createOMElement(operationName.getLocalPart(),omNS);
		for(int i=0; i<inputParameters.length;i++) {
			OMElement paramElement = fac.createOMElement("param"+String.valueOf(i),omNS);
			paramElement.addChild(fac.createText(inputParameters[i].toString()));
			methodElement.addChild(paramElement);
		}
		
		//TODO axis2Call object should actually be gotten somehow from underlying Axis2 engine
		// may have to change the following line
		org.apache.axis2.clientapi.Call axis2Call = null;
		if(clientHome != null){
			axis2Call = new org.apache.axis2.clientapi.Call(clientHome);
			axis2Call.engageModule(new QName("logging"));
		}
		else if(sContext != null){
			axis2Call = new org.apache.axis2.clientapi.Call(sContext);
		}else{
			axis2Call = new org.apache.axis2.clientapi.Call();
		}
		axis2Call.setTo(new EndpointReference(targetEndpointAddress));
		OMElement response = axis2Call.invokeBlocking(operationName.getLocalPart(),methodElement);
		
		//Now the job of extracting the return value out of the OMElement and
		//populating it into the ReturnType.
		//As 'response' we already get the contents of soap body.
		OMElement returnValue = response.getFirstElement();
		//corresponding to the returnType set, we should get a java object
		//instantiated and fill in the contents into the datamembers and return 
		//that object
		Object returnObject = getReturnObject(returnType);
		
		// ---
		// some solid code will have to go here
		// ---
		
		//I can't comment about the implementation of this method at this 
		//point, but from what the outputParams hashMap should contain I
		//think after the successful(?) invocation we should clear the 
		//outputParams hashMap and fill it with latest operation output
		//parameters.
		
		outputParams.clear();
		//populateOutputParams(operationName); // a private method to be coded
		
		return response;
		
	}
	
	public Object getReturnObject(QName returnType) {
		
		//if there has been a class registered associated with this QName
		//return that class
		if (returnTypeClass != null) {
			//Object returnObject = 
		}
		
		return null;
	}

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
	 * @throws javax.xml.ws.soap.SOAPFaultException Indicates a SOAP fault
	 * @throws javax.xml.ws.WebServiceException 
	 * 1. If there is an error in the configuration of the Call object
	 * 2. If inputParams do not match the required parameter set (as specified 
	 *    through the addParameter invocations or in the corresponding WSDL)
	 * 3. If parameters and return type are incorrectly specified
	 */
	public Object invoke(QName operationName, Object[] inputParams)
			throws RemoteException {
		
		//check if the call instance is properly configured. If not throw
		//a JAXRPCException.
		
		//---
		// some solid code will have to go here
		// ---
		
		//I can't comment about the implementation of this method at this 
		//point, but from what the outputParams hashMap should contain I
		//think after the successful(?) invocation we should clear the 
		//outputParams hashMap and fill it with latest operation output
		//parameters.
		
		outputParams.clear();
		//populateOutputParams(operationName); // a private method to be coded
		
		// TODO Auto-generated method stub
		return null;
		
	}

	/**
	 * Method invokeOneWay
	 * @param inputParams Object[]--Parameters for this invocation. This 
	 * includes only the input params.
	 * @throws javax.xml.ws.WebServiceException if there is an error in the 
	 * configuration of the Call object (example: a non-void return type has 
	 * been incorrectly specified for the one-way call) or if there is any 
	 * error during the invocation of the one-way remote call
	 */
	public void invokeOneWay(Object[] inputParameters) throws WebServiceException {
		if(returnType!=null && !returnType.getLocalPart().equals("void")) {
			throw new WebServiceException("invokeOneWay should not have a non-void" +
					" return type set.");
		}
		try {
			OMFactory fac = OMAbstractFactory.getOMFactory();
			String operationNS = operationName.getNamespaceURI();
			OMNamespace omNS = (operationNS == null || operationNS == "")
						? fac.createOMNamespace("http://jaxwsforaxis2.org","ns1")
						: fac.createOMNamespace(operationNS, "ns1");
			OMElement methodElement = fac.createOMElement(operationName.getLocalPart(),omNS);
			for(int i=0; i<inputParameters.length;i++) {
				OMElement paramElement = fac.createOMElement("param"+String.valueOf(i),omNS);
				paramElement.addChild(fac.createText(inputParameters[i].toString()));
				methodElement.addChild(paramElement);
			}
			//TODO axis2Call object should actually be gotten somehow from underlying Axis2 engine
			// may have to change the following line
			org.apache.axis2.clientapi.Call axis2Call = new org.apache.axis2.clientapi.Call();
			axis2Call.setTo(new EndpointReference(targetEndpointAddress));
			
			//a callback object that does nothing. We know we don't have anything
			//on response cycle for this fire-n-forget kind of invokeOneWay method.
			Callback callback = new Callback() {
				public void onComplete(AsyncResult async) {};
				public void reportError(Exception e) {};
			};
			axis2Call.invokeNonBlocking(operationName.getLocalPart(),methodElement,callback);
			
		}catch(Exception e) {
			throw new WebServiceException(e);
		}
	}

	/**
	 * Method getOutputParams
	 * Returns a Map of {name, value} for the output parameters of the last 
	 * invoked operation. The parameter names in the returned Map are of type 
	 * java.lang.String.
	 * @return Map Output parameters for the last Call.invoke(). Empty Map is 
	 * returned if there are no output parameters.
	 * @throws WebServiceException If this method is invoked for a one-way 
	 * operation or is invoked before any invoke method has been called.
	 */
	public Map getOutputParams() throws WebServiceException {
		return (Map)outputParams;
	}

	/**
	 * Method getOutputValues
	 * Returns a List values for the output parameters of the last invoked 
	 * operation.
	 * @return java.util.List Values for the output parameters. An empty List 
	 * is returned if there are no output values.
	 * @throws WebServiceException If this method is invoked for a one-way 
	 * operation or is invoked before any invoke method has been called.
	 */
	public List getOutputValues() throws WebServiceException {
		return (List)outputParams.values();
	}

	/**
	 * @return Returns the service.
	 */
	public ServiceImpl getService() {
		return service;
	}

	/**
	 * @param service The service to set.
	 */
	public void setService(ServiceImpl service) {
		this.service = service;
	}

}
