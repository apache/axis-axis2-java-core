/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.server.dispatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
/*
 * This class is used to map xml/annotation data with java. We use mapper to retrive various java art effects by looking in Service and SEI annotations.
 * If no annotation are specified we derive defaults.
 */

public interface Mapper {
	
	/**
	 * Looks at the SOAPBinding annotation figures out if the request is BARE or Wrapped and accordingly returns Parameter objects for the java method.
	 * @param mc
	 * @param javaMethod
	 * @return
	 */
	public Object[] getInputParameterData(MessageContext mc, Method javaMethod) throws JAXBException, MessageException, XMLStreamException, JAXBWrapperException;
	
	/**
	 * Creates output message block that can be serialized as response from server.
	 * @param mc
	 * @param response
	 * @return
	 */
	public Block getOutputParameterBlock(MessageContext mc, Object response, Method method)throws JAXBException, ClassNotFoundException, JAXBWrapperException, MessageException;
	
	/**
	 * Reads java method from Message context operationName. 
     * Find the Java method that corresponds to the WSDL operation that was 
     * targeted by the Axis2 Dispatchers.
     * @param mc
	 * @return
	 */
	public Method getJavaMethod(MessageContext mc, Class serviceImplClass);
	
	/**
	 * @param javaMethod
	 * @param args
	 * @return
	 * creates a message context given a java method and input arguments.
	 */
	public MessageContext getMessageContext(Method javaMethod, Object[] args);
	
	/**
	 * Reads input parameter names for java method.
	 * @param method
	 * @param objects
	 * @return
	 */
	public ArrayList<String> getParamNames(Method method, Object[] objects);
	
	/**
	 * creates name value pair for input parameters, skips AsyncHandler object from input parameter. 
	 * @param objects
	 * @param names
	 * @return
	 */
	public Map<String, Object> getParamValues(Object[] objects, ArrayList<String> names);
}
