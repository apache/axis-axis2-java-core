/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.dispatch.server;

import org.apache.axis2.jaxws.TestLogger;

import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.soap.SOAPBinding;

/**
 * A Provider&lt;String&gt; implementation used to test sending and 
 * receiving SOAP 1.2 messages.
 */
@WebServiceProvider(
        serviceName="SOAP12ProviderService", 
        wsdlLocation="META-INF/SOAP12ProviderService.wsdl", 
        targetNamespace="http://org/apache/axis2/jaxws/test/SOAP12")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class SOAP12Provider implements Provider<String> {

    private static final String sampleResponse = 
        "<test:echoStringResponse xmlns:test=\"http://org/apache/axis2/jaxws/test/SOAP12\">" +
        "<test:output>SAMPLE REQUEST MESSAGE</test:output>" +
        "</test:echoStringResponse>";
    
    public String invoke(String obj) {
        TestLogger.logger.debug(">> request received");
        TestLogger.logger.debug(obj);
        return sampleResponse;
    }

}
