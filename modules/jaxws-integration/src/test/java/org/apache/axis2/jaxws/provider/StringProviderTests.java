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

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

public class StringProviderTests extends ProviderTestCase {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    String xmlString = "<invoke>test input</invoke>";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "StringProviderService");

    private Dispatch<String> getDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, server.getEndpoint("StringProviderService.StringProviderPort"));
        
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, Service.Mode.PAYLOAD);
        
        // Force soap action because we are passing junk over the wire
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"http://stringprovider.sample.test.org/echoString");
        
        return dispatch;
        
    }
    private Dispatch<String> getDispatchOneway() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, server.getEndpoint("StringProviderService.StringProviderPort"));
        
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, Service.Mode.PAYLOAD);
        
        // Force soap action because we are passing junk over the wire
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"http://stringprovider.sample.test.org/echoStringOneway");
        
        return dispatch;
        
    }
    
    @Test
    public void testNormal() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "<invoke>hello world</invoke>";
        String response = dispatch.invoke(request);
        assertTrue(request.equals(response));
        
        // Try again to verify
        response = dispatch.invoke(request);
        assertTrue(request.equals(response));
    }
    
    @Test
    public void testEmptyString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "";
        String response = dispatch.invoke(request);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
        // Try again to verify
        response = dispatch.invoke(request);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
       
    }
    
    @Test
    public void testNullString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = null;
        String response = dispatch.invoke(request);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
        // Try again to verify
        response = dispatch.invoke(request);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
    }
    
    @Test
    public void testNonNullString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "mixedContent";
        String response = dispatch.invoke(request);
        
        // The current implementation does not send the mixedContent over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
        
        // Try again to verifify
        response = dispatch.invoke(request);
        
        // The current implementation does not send the mixedContent over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
    }
    
    @Test
    public void testCommentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "<!--comment-->";
        String response = dispatch.invoke(request);
        // The current implementation does not send the comment over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
        
        
        // Try again to verify
        response = dispatch.invoke(request);
        // The current implementation does not send the comment over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
    }
    
    @Test
    public void testTwoElementsString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "<a>hello</a><b>world</b>";
        String response = dispatch.invoke(request);
        
        // The current implementatin only sends the first element
        // So the echo'd response is just the first one.
        assertTrue("<a>hello</a>".equals(response));
        
        
        // Try again to verify
        response = dispatch.invoke(request);
        
        // The current implementatin only sends the first element
        // So the echo'd response is just the first one.
        assertTrue("<a>hello</a>".equals(response));
    }
    
    @Test
    public void testTwoElementsAndMixedContentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "mixed1<a>hello</a>mixed2<b>world</b>mixed3";
        String response = dispatch.invoke(request);
        // The current implementation only sends the first element.
        // The mixed content (mixed1) interferes and thus nothing is sent.
        assertTrue(response == null);
        
        
        // Try again to verify
        response = dispatch.invoke(request);
        // The current implementation only sends the first element.
        // The mixed content (mixed1) interferes and thus nothing is sent.
        assertTrue(response == null);
    }
    
    @Test
    public void testException() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        
        String request = "<invoke>throwWebServiceException</invoke>";
        try {
            String response = dispatch.invoke(request);
            fail("Expected Exception");
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            assertTrue(sf.getFaultString().equals("provider"));
        }
        
        // Try again to verify
        try {
            String response = dispatch.invoke(request);
            fail("Expected Exception");
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            assertTrue(sf.getFaultString().equals("provider"));
        }
    }
    /**
     * Test that for an Operation defined as two-way in WSDL returns a 
     * response even if the Provider returns null and 
     * jaxws.provider.interpretNullAsOneway property is set
     * @throws Exception
     */
    @Test
    public void testProviderReturnsNull() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<String> dispatch = getDispatch();
        String request = "<invoke>returnNull</invoke>";
        try {
            String response = dispatch.invoke(request);
            assertTrue(response == null);
        } catch (SOAPFaultException e) {
            fail("Exception caught : " + e);
}        
        // Try again to verify
        try {
            String response = dispatch.invoke(request);
            assertTrue(response == null);
        } catch (SOAPFaultException e) {
            fail("Exception caught : " + e);
        }
        
    }
    /**
     * Test that for an Operation defined as one-way in WSDL is 
     * not effected if the Provider returns null, even if the 
     * jaxws.provider.interpretNullAsOneway property is set
     * @throws Exception
     */
    @Test
    public void testProviderReturnsNullOneway() throws Exception {
        TestLogger.logger.debug("---------------------------------------");

        Dispatch<String> dispatch = getDispatchOneway();
        // Because the operation is defined in WSDL, it should not be 
        // effected by the property
        ((BindingProvider) dispatch).getRequestContext()
        .put(org.apache.axis2.jaxws.Constants.JAXWS_PROVIDER_NULL_ONEWAY, Boolean.FALSE);

        String request = "<invoke>returnNull</invoke>";
        try {
            dispatch.invokeOneWay(request);
        } catch (SOAPFaultException e) {
            fail("Exception caught : " + e);
        }

        // Try again to verify
        try {
            dispatch.invokeOneWay(request);
        } catch (SOAPFaultException e) {
            fail("Exception caught : " + e);
        }
    }

}
