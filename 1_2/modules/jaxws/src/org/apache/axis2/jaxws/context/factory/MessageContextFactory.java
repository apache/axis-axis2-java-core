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
package org.apache.axis2.jaxws.context.factory;

import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.handler.ProtectedMessageContext;
import org.apache.axis2.jaxws.handler.SoapMessageContext;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

public class MessageContextFactory {

    public MessageContextFactory() {
        super();

    }

    public static WebServiceContext createWebServiceContext() {
        return new WebServiceContextImpl();
    }

    public static MessageContext createSoapMessageContext(
            org.apache.axis2.jaxws.core.MessageContext jaxwsMessageContext) {
        return new SoapMessageContext(jaxwsMessageContext);
    }

    public static MessageContext createProtectedMessageContext(
            org.apache.axis2.jaxws.core.MessageContext jaxwsMessageContext) {
        return new ProtectedMessageContext(jaxwsMessageContext);
    }
}
