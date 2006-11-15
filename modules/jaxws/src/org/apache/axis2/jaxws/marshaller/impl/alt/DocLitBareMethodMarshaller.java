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
package org.apache.axis2.jaxws.marshaller.impl.alt;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.MethodMarshallerImpl;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DocLitBareMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitBareMethodMarshaller.class);
    protected ServiceDescription serviceDesc = null;
    protected EndpointDescription endpointDesc = null;
    protected OperationDescription operationDesc = null;
    protected Protocol protocol = Protocol.soap11;
    
    
    public DocLitBareMethodMarshaller(ServiceDescription serviceDesc, EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol) {
        super();
        this.serviceDesc = serviceDesc;
        this.endpointDesc = endpointDesc;
        this.operationDesc = operationDesc;
        this.protocol = protocol;
    }

    public Object demarshalResponse(Message message, Object[] inputArgs)
            throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // TODO Add Real Code
            throw new UnsupportedOperationException();
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Object[] demarshalRequest(Message message)
            throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // TODO Add Real Code
            throw new UnsupportedOperationException();
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalResponse(Object returnObject, Object[] holderObjects)
            throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // TODO Add Real Code
            throw new UnsupportedOperationException();
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalRequest(Object[] object) throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // TODO Add Real Code
            throw new UnsupportedOperationException();
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable) throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // TODO Add Real Code
            throw new UnsupportedOperationException();
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Object demarshalFaultResponse(Message message) throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // TODO Add Real Code
            throw new UnsupportedOperationException();
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

}
