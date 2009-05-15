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


import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Tests Dispatch<SOAPMessage> client and a Provider<SOAPMessage> service
 * with mustUnderstand attribute header.
 */
public class SoapMessageMUProviderTests extends AbstractTestCase {
    public static final QName serviceName =
            new QName("http://ws.apache.org/axis2", "SoapMessageMUProviderService");
    public static final QName portName =
            new QName("http://ws.apache.org/axis2", "SimpleProviderServiceSOAP11port0");
    public static final String endpointUrl =
            "http://localhost:6060/axis2/services/SoapMessageMUProviderService.SimpleProviderServiceSOAP11port0";

    public static final String bindingID = SOAPBinding.SOAP11HTTP_BINDING;
    public static final Service.Mode mode = Service.Mode.MESSAGE;

    public static Test suite() {
        return getTestSetup(new TestSuite(SoapMessageMUProviderTests.class));
    }

    /**
     * Test soap message with no MU headers
     */
    public void testNoMustUnderstandHeaders() throws Exception {
        System.out.println("testNoMustUnderstandHeaders");
        // create a service
        Service svc = Service.create(serviceName);
        svc.addPort(portName, bindingID, endpointUrl);

        javax.xml.ws.Dispatch<SOAPMessage> dispatch = null;
        dispatch = svc.createDispatch(portName, SOAPMessage.class, mode);

        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");

        SOAPMessage message = AttachmentUtil.toSOAPMessage(AttachmentUtil.msgEnvPlain);

        SOAPMessage response = dispatch.invoke(message);

        String string = AttachmentUtil.toString(response);
        assertTrue(string.equalsIgnoreCase(AttachmentUtil.XML_HEADER
                                           + AttachmentUtil.msgEnvPlain));
            
        // Try a second time
        response = dispatch.invoke(message);

        string = AttachmentUtil.toString(response);
        assertTrue(string.equalsIgnoreCase(AttachmentUtil.XML_HEADER
                                           + AttachmentUtil.msgEnvPlain));
    }

    /**
     * Test the mustUnderstand soap header attribute on the client's
     * outbound soap message for headers that are not understood.  Should cause an 
     * exception.
     */
    public void testClientRequestNotUnderstoodHeaders() {
        System.out.println("testClientRequestNotUnderstoodHeaders");
        // create a service
        Service svc = Service.create(serviceName);
        svc.addPort(portName, bindingID, endpointUrl);

        javax.xml.ws.Dispatch<SOAPMessage> dispatch = null;
        dispatch = svc.createDispatch(portName, SOAPMessage.class, mode);

        //force SOAPAction to match with wsdl action                        
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");

        SOAPMessage message = AttachmentUtil.toSOAPMessage(AttachmentUtil.msgEnvMU);

        try {
            dispatch.invoke(message);
            fail("Should have received fault for not understood headers on request");
        } catch (Exception e) {
            // Expected path
        }
        
        // Try a second time
        try {
            dispatch.invoke(message);
            fail("Should have received fault for not understood headers on request");
        } catch (Exception e) {
            // Expected path
        }
    }

    /**
     * Test the mustUnderstand soap header attribute on the server's
     * outbound soap message (i.e. the inbound response to the client) for headers that
     * are not understood.  Should cause an exception.
     */
    public void testClientResponseNotUnderstoodHeaders() {
        System.out.println("testClientResponseNotUnderstoodHeaders");
        // create a service
        Service svc = Service.create(serviceName);
        svc.addPort(portName, bindingID, endpointUrl);

        javax.xml.ws.Dispatch<SOAPMessage> dispatch = null;
        dispatch = svc.createDispatch(portName, SOAPMessage.class, mode);

        //force SOAPAction to match with wsdl action                        
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");

        SOAPMessage message = AttachmentUtil.toSOAPMessage(AttachmentUtil.msgEnv);

        try {
            dispatch.invoke(message);
            fail("Should have received fault for not understood headers on response");
        } catch (Exception e) {
            // Expected path
        }
        
        // Try a second time
        try {
            dispatch.invoke(message);
            fail("Should have received fault for not understood headers on response");
        } catch (Exception e) {
            // Expected path
        }
    }

    /**
     * Test the mustUnderstand soap header attribute on the client's
     * outbound soap message for headers that should be understood.  Should not cause an 
     * exception.
     */
    public void testClientRequestUnderstoodHeaders() throws Exception {
        System.out.println("testClientRequestUnderstoodHeaders");
        // create a service
        Service svc = Service.create(serviceName);
        svc.addPort(portName, bindingID, endpointUrl);

        javax.xml.ws.Dispatch<SOAPMessage> dispatch = null;
        dispatch = svc.createDispatch(portName, SOAPMessage.class, mode);

        //force SOAPAction to match with wsdl action                        
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");

        SOAPMessage message = AttachmentUtil.toSOAPMessage(AttachmentUtil.msgEnvMU_understood);

        dispatch.invoke(message);

        // Try a second time
        dispatch.invoke(message);
    }

    /**
     * Test the mustUnderstand soap header attribute on the server's
     * outbound soap message (i.e. the inbound response to the client) for headers that
     * are understood.  Should not cause an exception.
     */
    public void testClientResponseUnderstoodHeaders() throws Exception {
        System.out.println("testClientResponseUnderstoodHeaders");
        // create a service
        Service svc = Service.create(serviceName);
        svc.addPort(portName, bindingID, endpointUrl);

        javax.xml.ws.Dispatch<SOAPMessage> dispatch = null;
        dispatch = svc.createDispatch(portName, SOAPMessage.class, mode);

        //force SOAPAction to match with wsdl action                        
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        ((BindingProvider) dispatch).getRequestContext()
                                    .put(BindingProvider.SOAPACTION_URI_PROPERTY, "echoString");

        SOAPMessage message = AttachmentUtil.toSOAPMessage(AttachmentUtil.msgEnv_understood);

        dispatch.invoke(message);

        // Try a second time
        dispatch.invoke(message);
    }
}
