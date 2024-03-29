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
package org.apache.axis2.jaxws.provider.soapbinding.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class StringProviderTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");
    
    private QName serviceName = new QName("http://StringProvider.soapbinding.provider.jaxws.axis2.apache.org", "SOAPBindingStringProviderService");
    private QName portName =  new QName("http://StringProvider.soapbinding.provider.jaxws.axis2.apache.org", "SOAPBindingStringProviderPort");

    private static final String SOAP11_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP12_NS_URI = "http://www.w3.org/2003/05/soap-envelope";
    public static final String SOAP11_ENVELOPE_HEAD = "<?xml version='1.0' encoding='utf-8'?>" + 
    "<soapenv:Envelope xmlns:soapenv=\"" + SOAP11_NS_URI + "\">" +
    "<soapenv:Header />" + 
    "<soapenv:Body>";

    public static final String SOAP12_ENVELOPE_HEAD = 
        "<?xml version='1.0' encoding='utf-8'?>" + 
        "<soapenv:Envelope xmlns:soapenv=\"" + SOAP12_NS_URI + "\">" +
        "<soapenv:Header />" + 
        "<soapenv:Body>";

    public static final String SOAP11_ENVELOPE_TAIL = 
        "</soapenv:Body>" + 
        "</soapenv:Envelope>";

    public static final String SOAP12_ENVELOPE_TAIL = 
        "</soapenv:Body>" + 
        "</soapenv:Envelope>";
    
/*
 * This test case makes sure that we receive a soap11 response for a soap11 request.
 */
    @Test
    public void testsoap11request(){
        System.out.println("---------------------------------------");
        try{
            Service svc = Service.create(serviceName);
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING,
                    server.getEndpoint("SOAPBindingStringProviderService.SOAPBindingStringProviderPort"));

            Dispatch<String> dispatch =
                svc.createDispatch(portName, String.class, Service.Mode.MESSAGE);
            String xmlMessage = SOAP11_ENVELOPE_HEAD+"<invokeOp>soap11 request</invokeOp>"+SOAP11_ENVELOPE_TAIL;
            String response = dispatch.invoke(xmlMessage);

            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapMessage = factory.createMessage(null, new ByteArrayInputStream(response.getBytes()));
            assertTrue(getVersionURI(soapMessage).equals(SOAP11_NS_URI));
        }catch(Exception e){
            System.out.println("Failure while sending soap 11 request");
            System.out.println(e.getMessage());
            fail();
        }

    }

    private String getVersionURI(SOAPMessage soapMessage)throws SOAPException{
        SOAPPart sp = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = sp.getEnvelope();
        return envelope.getNamespaceURI();
    }
}
