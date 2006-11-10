/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.marshaller;

import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.message.Message;

/**
 * This class marshals and unmarshals method invocations.
 * 
 * If there are any problems, a WebServiceException is thrown.  (Each of the methods is guranteed to catch any unchecked exception and wrap
 * it in a WebServiceException).
 */
public interface MethodMarshaller {
	
	/**
	 * This method converts java Objects in to a Message. Used on Client side to convert input method object to Message that is sent on wire.
	 * 
	 * NONWRAP CASE:
	 * creates a request message. The input object to a non wrapped wsdl will be a object (mainly a JAXB Object) that will
	 * have all the payload data or method parameter data already setup. So the message will be created by converting input object in to a JAXBBlock and
	 * attaching the Block Message to soap body.
	 * 
	 * WRAP CASE:
	 * 
	 * create request message. It reads RequestWrapper annotation from OperationDescription and reads the class name, then reads
	 * all the webParam annotation on the method and uses JAXBWrapTool to wrap the request as jaxbObject. Create JAXBblock from the jaxbObject
	 * reads Biniding provider properties and set them on request message and return request message.
	 * @param object
	 * @return
	 */
	public Message marshalRequest(Object[] object) throws WebServiceException; 
	
	/**
	 * This method creates Message from a returnObject and input parameters of holder type. This is a case where we have method with return
	 * type and input parameters as holders. Used on Server side to convert service methods return type to Message that is then sent on wire.
	 * @param jaxbObject
	 * @return
	 */
	public Message marshalResponse(Object returnObject, Object[] holderObjects)throws WebServiceException;
	
	/**
	 * This method creates Fault Message from a Throbale input parameter. 
	 * Used on Server side to convert Exceptions to Fault Message that is then sent on wire.
	 * @param jaxbObject
	 * @return
	 */
	public Message marshalFaultResponse(Throwable throwable) throws WebServiceException;
	/**
	 * This method converts Message to java objects. Used on Server Side to this extract method input parameters from message and invokes method on service
	 * with found input parameters on ServiceEndpoint.
	 * @param message
	 * @return
	 */
	public Object[] demarshalRequest(Message message)throws WebServiceException;
	
	/**
	 * This method converts Message to Object. Used on Client side when converting response message from Server to ResponseWrapper/return type of method that
	 * Client uses to map.
	 * 
	 * NONWRAP CASE:
	 * creates return result that client expects from the method call. This method reads the method return type
	 * or uses webResult annotation and creates JAXBBlock from the response context and returns the business object associated with the JAXBBlock.
	 * 
	 * WRAP CASE:
	 * creates return result that client expects from the method call. It reads response wrapper annotation then reads OM from the
	 * response message context and creates JAXBBlock from the OMElement on messageContext. It then reads the webresult annotation to gather the return parameter
	 * name and creates the result object for it by reading the property object from JAXBBlock's business object using PropertyDescriptor. 
	 * 
	 * @param message
	 * @return
	 */
	public Object demarshalResponse(Message message, Object[] inputArgs) throws WebServiceException;
	
    /**
	 * This method converts Fault Message to fault java objects. Used on Client Side to extract Fault Object expected by client from message.
	 * @param message
	 * @return
	 */
	public Object demarshalFaultResponse(Message message) throws WebServiceException;
	
	
}
