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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class SOAPBindingProviderTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    private QName serviceName = new QName("http://SOAPBindingProvider.provider.jaxws.axis2.apache.org", "SOAPBindingProviderService");
    private QName portName =  new QName("http://SOAPBindingProvider.provider.jaxws.axis2.apache.org", "SOAPBindingProviderPort");
    
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


    String request = "<invokeOp>Hello World</invokeOp>";

    @Test
    public void testSoap11Request() throws Exception {
        System.out.println("---------------------------------------");
        
        Dispatch<SOAPMessage> dispatch=getDispatch();
        String soapMessage = getSOAP11Message();
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage(null, new ByteArrayInputStream(soapMessage.getBytes()));
        Object obj = dispatch.invoke(message);
        assertTrue(obj!=null && obj instanceof SOAPMessage);
        assertTrue(getVersionURI(message).equals(SOAP11_NS_URI));
    }

    @Test
    public void testSoap12Request() throws Exception {
        System.out.println("---------------------------------------");
        
        Dispatch<SOAPMessage> dispatch=getDispatch();
        String soapMessage = getSOAP12Message();
        System.out.println("soap message ="+soapMessage);
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        MimeHeaders header = new MimeHeaders();
        header.addHeader("Content-Type", "application/soap+xml");
        SOAPMessage message = factory.createMessage(header, new ByteArrayInputStream(soapMessage.getBytes()));
        Object obj = dispatch.invoke(message);
        assertTrue(obj!=null && obj instanceof SOAPMessage);
        assertTrue(getVersionURI(message).equals(SOAP12_NS_URI));
        System.out.println("Provider endpoint was able to receive both SOAP 11 and SOAP 12 request");
    }
    
    private Dispatch<SOAPMessage> getDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        String endpointUrl = server.getEndpoint("SOAPBindingProviderService.SOAPBindingProviderPort");
        svc.addPort(portName, null, endpointUrl);
        Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        BindingProvider p = (BindingProvider) dispatch;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
        return dispatch;
    }
    
    private String getSOAP11Message() throws SOAPException, IOException{
        return SOAP11_ENVELOPE_HEAD+request+SOAP11_ENVELOPE_TAIL;
    }
    
    private String getSOAP12Message() throws SOAPException, IOException{
        return SOAP12_ENVELOPE_HEAD+request+SOAP12_ENVELOPE_TAIL;    
    }
    
    private String getVersionURI(SOAPMessage soapMessage)throws SOAPException{
        SOAPPart sp = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = sp.getEnvelope();
        return envelope.getNamespaceURI();
    }
}

