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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.UUID;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;

import org.apache.axis2.jaxws.framework.ClientConfigurationContextBinder;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.testutils.Axis2Server;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.RespectBindingFeature;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

public class AddressingProviderTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/addressing-repo");

    @ClassRule
    public static final ClientConfigurationContextBinder binder = new ClientConfigurationContextBinder("target/client-repo");

    private QName serviceName = new QName("http://addressing.provider.jaxws.axis2.apache.org", "AddressingProviderService");
    private QName portName = new QName("http://addressing.provider.jaxws.axis2.apache.org", "AddressingProviderPort");
        
    static final String START_SOAP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
      
    static final String END_SOAP = "</soap:Body></soap:Envelope>";
    
    static final String XML_MESSAGE = "<ns2:inMessage xmlns:ns2=\"http://addressing.provider.jaxws.axis2.apache.org\">Hello</ns2:inMessage>";
        
    static final String SOAP_MESSAGE_1 = START_SOAP +
                      "<soap:Body>" +
                      XML_MESSAGE +
                      END_SOAP;
    
    static final String SOAP_MESSAGE_2 = START_SOAP +  
                      "<soap:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:To>{0}</wsa:To><wsa:MessageID>{1}</wsa:MessageID><wsa:Action>{2}</wsa:Action></soap:Header>" +
                      "<soap:Body>" +
                      XML_MESSAGE +
                      END_SOAP;
                      
    static final String ACTION = "http://addressing.provider.jaxws.axis2.apache.org/AddressingProviderInterface/In";
    
    private static String getEndpointUrl() throws Exception {
        return server.getEndpoint("AddressingProviderService.AddressingProviderPort");
    }
    
    /**
     * Inject correct wsa header (wsa:Action must be set the the action of hello operation)
     */
    @Test
    public void testInjectAddressingHeaders() throws Exception {
          
        Dispatch<SOAPMessage> dispatch = createDispatch();
             
        String msg = SOAP_MESSAGE_1;
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage(null, new ByteArrayInputStream(msg.getBytes()));
                        
        SOAPMessage response = dispatch.invoke(request);
                     
        assertResponseXML(response, "Hello Response");
        
        System.out.println(response.toString()); 
        
        // Try Again to verify
        response = dispatch.invoke(request);
        
        assertResponseXML(response, "Hello Response");
        
        System.out.println(response.toString()); 
    }
    
    /**
     * Message already contains wsa headers. Make sure there is no mismatch between 
     * SOAPAction and wsa:Action. 
     */
    @Test
    public void testWithAddressingHeaders() throws Exception {

        Dispatch<SOAPMessage> dispatch = createDispatch();
             
        String msg = MessageFormat.format(SOAP_MESSAGE_2, 
                                          getEndpointUrl(),
                                          "urn:" + UUID.randomUUID(),
                                          ACTION);
        
        System.out.println(msg);
        
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage(null, new ByteArrayInputStream(msg.getBytes()));
                        
        SOAPMessage response = dispatch.invoke(request);
                     
        assertResponseXML(response, "Hello Response");
        
        System.out.println(response.toString());
        
        // Try again to verify
        response = dispatch.invoke(request);
        
        assertResponseXML(response, "Hello Response");
        
        System.out.println(response.toString());
    }
    
    /**
     * Message already contains wsa headers. Make sure there is no mismatch between 
     * SOAPAction and wsa:Action. 
     */
    // Skipping this test until we have a way to register the addressing validator.
    @Ignore
    @Test
    public void testWithRespectBinding() throws Exception {
        Dispatch<SOAPMessage> dispatch = createDispatchWithRespectBinding();
             
        BindingProvider bp = (BindingProvider) dispatch;
        Binding binding = (Binding) bp.getBinding();
        
        WebServiceFeature addressingFeature = binding.getFeature(AddressingFeature.ID);
        assertNotNull(addressingFeature);
        assertTrue("Expecting AddressingFeature to be enabled.", addressingFeature.isEnabled());
        
        WebServiceFeature respectBindingFeature = binding.getFeature(RespectBindingFeature.ID);
        assertNotNull(respectBindingFeature);
        assertTrue("Expecting RespectBindingFeature to be enabled.", respectBindingFeature.isEnabled());
        
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpointUrl());

        String msg = MessageFormat.format(SOAP_MESSAGE_2, 
                                          getEndpointUrl(),
                                          "urn:" + UUID.randomUUID(),
                                          ACTION);
        
        System.out.println(msg);
        
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage(null, new ByteArrayInputStream(msg.getBytes()));
                        
        SOAPMessage response = dispatch.invoke(request);
                     
        assertResponseXML(response, "Hello Response");
        
        System.out.println(response.toString());
        
        // Try again to verify
        response = dispatch.invoke(request);
        
        assertResponseXML(response, "Hello Response");
        
        System.out.println(response.toString());
    }
    
    private SOAPElement assertResponseXML(SOAPMessage msg, String expectedText) throws Exception {
        assertTrue(msg != null);
        SOAPBody body = msg.getSOAPBody();
        assertTrue(body != null);
        
        Node invokeElement = (Node) body.getFirstChild();
        assertTrue(invokeElement instanceof SOAPElement);
        assertEquals("outMessage", invokeElement.getLocalName());
                
        String text = invokeElement.getValue();
        
        System.out.println("Received: " + text);
        assertEquals("Found ("+ text + ") but expected (" + expectedText + ")", expectedText, text);
        
        return (SOAPElement) invokeElement;
    }
    
    private Dispatch<SOAPMessage> createDispatch() throws Exception {
        URL wsdlURL = getWsdl();
        assertNotNull(wsdlURL);
        Service svc = Service.create(wsdlURL, serviceName);
        
        WebServiceFeature[] wsf = {new AddressingFeature(true)};
        
        Dispatch<SOAPMessage> dispatch =
            svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE, wsf);
        
        BindingProvider p = (BindingProvider) dispatch;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpointUrl());

        return dispatch;
    }
    
    private Dispatch<SOAPMessage> createDispatchWithRespectBinding() throws Exception {
        URL wsdlURL = getWsdl();
        assertNotNull(wsdlURL);
        Service svc = Service.create(wsdlURL, serviceName);
        
        WebServiceFeature[] wsf = {new AddressingFeature(true), new RespectBindingFeature(true)};
        
        Dispatch<SOAPMessage> dispatch =
            svc.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE, wsf);
        
        return dispatch;
    }
    
    private URL getWsdl() throws Exception {
        String wsdlLocation = "/src/test/servicejars/AddressingProvider/META-INF/AddressingProvider.wsdl";
        String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
        wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();    
        File file = new File(wsdlLocation);
        return file.toURI().toURL();
    }
    
}
