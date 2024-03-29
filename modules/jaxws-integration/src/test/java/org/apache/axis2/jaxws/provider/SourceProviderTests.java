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

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

public class SourceProviderTests extends ProviderTestCase {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private QName serviceName = new QName("http://ws.apache.org/axis2", "SourceProviderService");
    private String xmlDir = "xml";

    private Dispatch<Source> getDispatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, server.getEndpoint("SourceProviderService.SourceProviderPort"));
        
        Dispatch<Source> dispatch = svc
                .createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        
        // Force soap action because we are passing junk over the wire
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"test");
        
        return dispatch;
        
    }
    
    private Source getSource(String text) {
        if (text == null) {
            return null;
        } else {
            ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
            return new StreamSource((InputStream) stream);
        }
        
    }
    
    private String getString(Source source) throws Exception {
        if (source == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        Result result = new StreamResult(writer);
        t.transform(source, result);
        return writer.getBuffer().toString();
        
    }
    
    @Test
    public void testNormal() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<test>hello world</test>";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        assertTrue(response.contains(request));
        
        // Try again to verify
        requestSource = getSource(request);
        responseSource = dispatch.invoke(requestSource);
        response = getString(responseSource);
        
        assertTrue(response.contains(request));
    }
    
    @Test
    public void testEmptyString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
        // Try again to verify
        requestSource = getSource(request);
        responseSource = dispatch.invoke(requestSource);
        response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
    }
    
    @Test
    public void testNullSource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        Source responseSource = dispatch.invoke(null);
        String response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
        
        // Try again to verify
        responseSource = dispatch.invoke(null);
        response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
    }
    
    @Test
    public void testEmptySource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        Source responseSource = dispatch.invoke(new StreamSource());
        String response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
        
        // Try again to verify
        responseSource = dispatch.invoke(new StreamSource());
        response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
    }
    
    @Test
    public void testNonNullString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "mixedContent";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        // The current implementation does not send the mixedContent over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
        
        
        // Try again to verify
        requestSource = getSource(request);
        responseSource = dispatch.invoke(requestSource);
        response = getString(responseSource);
        
        // The current implementation does not send the mixedContent over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
    }
    
    @Test
    public void testCommentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<!--comment-->";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        // The current implementation does not send the comment over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
        
        
        // Try again to verify
        requestSource = getSource(request);
        responseSource = dispatch.invoke(requestSource);
        response = getString(responseSource);
        // The current implementation does not send the comment over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
    }
    
   
    @Test
    public void testProviderReturnsNull() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<test>ReturnNull</test>";
        Source requestSource = getSource(request);
        try {
            requestSource = getSource(request);
            dispatch.invokeOneWay(requestSource);
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }
        
        // Try again to verify
        try {
            requestSource = getSource(request);
            dispatch.invokeOneWay(requestSource);
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }

    }

    @Test
    public void testProviderEmptySource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<test>ReturnEmpty</test>";
        Source requestSource = getSource(request);
        try {
            requestSource = getSource(request);
            Source responseSource = dispatch.invoke(requestSource);
            //Expecting empty response payload back. Nothing underneath soap body.
            assertNull(responseSource);
        }catch(Exception e){
            e.printStackTrace();
            fail("Caught exception " + e);
        }

    }

    @Test
    public void testTwoElementsString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<a>hello</a><b>world</b>";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        // The current implementatin only sends the first element
        // So the echo'd response is just the first one.
        assertTrue(response.contains("<a>hello</a>"));
        
        
        // Try again to verify
        requestSource = getSource(request);
        responseSource = dispatch.invoke(requestSource);
        response = getString(responseSource);
        
        // The current implementatin only sends the first element
        // So the echo'd response is just the first one.
        assertTrue(response.contains("<a>hello</a>"));
    }
    
    @Test
    public void testTwoElementsAndMixedContentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "mixed1<a>hello</a>mixed2<b>world</b>mixed3";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        // The current implementation only sends the first element.
        // The mixed content (mixed1) interferes and thus nothing is sent.
        assertTrue(response == null);
        
        
        // Try again to verify
        requestSource = getSource(request);
        responseSource = dispatch.invoke(requestSource);
        response = getString(responseSource);
        // The current implementation only sends the first element.
        // The mixed content (mixed1) interferes and thus nothing is sent.
        assertTrue(response == null);
    }
    
    @Test
    public void testException() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<test>throwWebServiceException</test>";
        try {
            Source requestSource = getSource(request);
            Source responseSource = dispatch.invoke(requestSource);
            String response = getString(responseSource);
            fail("Expected Exception");
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            assertTrue(sf.getFaultString().equals("provider"));
        }
        
        // Try again to verify
        try {
            Source requestSource = getSource(request);
            Source responseSource = dispatch.invoke(requestSource);
            String response = getString(responseSource);
            fail("Expected Exception");
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            assertTrue(sf.getFaultString().equals("provider"));
        }
    }
    
    @Test
    public void testUserGeneratedSOAPFault() throws Exception {
        System.out.println("---------------------------------------");
        
        Dispatch<Source> dispatch = getDispatch();
        String request = "<test>throwUserGeneratedFault</test>";
        try {
            Source requestSource = getSource(request);
            Source responseSource = dispatch.invoke(requestSource);
            String response = getString(responseSource);
            fail("Expected Exception");
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            assertTrue(sf.getFaultString().equals("userGeneratedFaultTest"));
        }
    }

    
    @Test
    public void testProviderSource(){
        try{
            String resourceDir = new File(providerResourceDir, xmlDir).getAbsolutePath();
            String fileName = resourceDir+File.separator+"web.xml";

            File file = new File(fileName);
            InputStream inputStream = new FileInputStream(file);
            StreamSource xmlStreamSource = new StreamSource(inputStream);

            Service svc = Service.create(serviceName);
            svc.addPort(portName,null, server.getEndpoint("SourceProviderService.SourceProviderPort"));
            Dispatch<Source> dispatch = svc.createDispatch(portName, Source.class, null);
            TestLogger.logger.debug(">> Invoking Source Provider Dispatch");
            Source response = dispatch.invoke(xmlStreamSource);

            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            
            
            // Try again to verify
            inputStream = new FileInputStream(file);
            xmlStreamSource = new StreamSource(inputStream);
            TestLogger.logger.debug(">> Invoking Source Provider Dispatch");
            response = dispatch.invoke(xmlStreamSource);

            TestLogger.logger.debug(">> Response [" + response.toString() + "]");

        }catch(Exception e){
        	e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
}
