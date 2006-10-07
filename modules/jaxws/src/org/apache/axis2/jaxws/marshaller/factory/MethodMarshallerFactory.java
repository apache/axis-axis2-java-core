/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.marshaller.factory;

import javax.jws.soap.SOAPBinding;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.DocLitBareMethodMarshallerImpl;
import org.apache.axis2.jaxws.marshaller.impl.DocLitWrappedMethodMarshallerImpl;
import org.apache.axis2.jaxws.message.Protocol;


public class MethodMarshallerFactory {

	public MethodMarshallerFactory() {
		super();
		
	}
	
	public MethodMarshaller createDocLitMethodMarshaller(SOAPBinding.ParameterStyle style, ServiceDescription serviceDesc, EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol){
		if(style == SOAPBinding.ParameterStyle.WRAPPED){
			return new DocLitWrappedMethodMarshallerImpl(serviceDesc, endpointDesc, operationDesc, protocol);
		}
		if(style == SOAPBinding.ParameterStyle.BARE){
			return new DocLitBareMethodMarshallerImpl(serviceDesc, endpointDesc, operationDesc, protocol);
		}
		return null;
	}
	
	public MethodMarshaller createRPCLitMethodMarshaller(SOAPBinding.ParameterStyle style, ServiceDescription serviceDesc, EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol){
		throw new UnsupportedOperationException("RPC/LIT not supported");
	}
}
