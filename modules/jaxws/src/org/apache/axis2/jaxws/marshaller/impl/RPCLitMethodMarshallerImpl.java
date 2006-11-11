package org.apache.axis2.jaxws.marshaller.impl;

import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;

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
public class RPCLitMethodMarshallerImpl extends MethodMarshallerImpl {

   
    public RPCLitMethodMarshallerImpl(ServiceDescription serviceDesc,
            EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol) {
        super(serviceDesc, endpointDesc, operationDesc, protocol);
        // TODO Unsupported
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object demarshalResponse(Message message, Object[] inputArgs)
            throws WebServiceException {
        // TODO Unsupported
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] demarshalRequest(Message message)
            throws WebServiceException {
        // TODO Unsupported
        throw new UnsupportedOperationException();
    }

    @Override
    public Message marshalResponse(Object returnObject, Object[] holderObjects)
            throws WebServiceException {
        // TODO Unsupported
        throw new UnsupportedOperationException();
    }

    @Override
    public Message marshalRequest(Object[] object) throws WebServiceException {
        // TODO Unsupported
        throw new UnsupportedOperationException();
    }

}
