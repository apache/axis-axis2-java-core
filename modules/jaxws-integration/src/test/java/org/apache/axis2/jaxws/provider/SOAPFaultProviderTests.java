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

package org.apache.axis2.jaxws.provider;

import static org.junit.Assert.fail;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * 
 */
public class SOAPFaultProviderTests extends ProviderTestCase {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");
    
    public QName portName =
        new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");
    private QName serviceName = new QName("http://ws.apache.org/axis2", "StringProviderService");

    private static final String sampleSoapEnvelopeHeader =
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body>";

    private static final String sampleSoapEnvelopeFooter = "</soap:Body>" + "</soap:Envelope>";

    private static final String soapFaultBodyContent = 
        "<soap:Fault>"
        + "<faultcode>soap:SOAPFaultProviderTests</faultcode>"
        + "<faultstring>WSA-CP2-GENERATED-FAULT: FAULT01</faultstring>"
        + "</soap:Fault>";
    

    private Dispatch<String> getDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("StringProviderService.StringProviderPort"));

        // Use Message mode so we can build up the entire SOAP envelope containing the fault
        Dispatch<String> dispatch =
                svc.createDispatch(portName, String.class, Service.Mode.MESSAGE);

        // Force soap action because we are passing junk over the wire
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://stringprovider.sample.test.org/echoString");

        return dispatch;
    }

    @Test
    public void testFault() throws Exception {
        System.out.println("---------------------------------------");

        Dispatch<String> dispatch = getDispatch();
        String request = sampleSoapEnvelopeHeader + soapFaultBodyContent + sampleSoapEnvelopeFooter;
        // Test that a Provider receives the full fault.  If not, it will throw an exception
        try {
            String response = dispatch.invoke(request);
        }
        catch (Exception e) {
            fail("Caught unexpected exception " + e.toString());
        }
        
        // Try again
        // Test that a Provider receives the full fault.  If not, it will throw an exception
        try {
            String response = dispatch.invoke(request);
        }
        catch (Exception e) {
            fail("Caught unexpected exception " + e.toString());
        }
    }

}
