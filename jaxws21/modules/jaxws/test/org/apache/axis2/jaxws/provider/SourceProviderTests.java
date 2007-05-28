/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.provider;

import org.apache.axis2.jaxws.TestLogger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

public class SourceProviderTests extends ProviderTestCase {

    private String endpointUrl = "http://localhost:8080/axis2/services/SourceProviderService";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SourceProviderService");
    private String xmlDir = "xml";


    protected void setUp() throws Exception {
            super.setUp();
    }

    protected void tearDown() throws Exception {
            super.tearDown();
    }

    public SourceProviderTests(String name) {
        super(name);
    }
    
    private Dispatch<Source> getDispatch() {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
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
    
    public void testNormal() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<test>hello world</test>";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        assertTrue(response.contains(request));
    }
    
    public void testEmptyString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
        
        //assertTrue(request.equals(response));
    }
    
    public void testNullSource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        Source responseSource = dispatch.invoke(null);
        String response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
    }
    
    public void testEmptySource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        Source responseSource = dispatch.invoke(new StreamSource());
        String response = getString(responseSource);
        
        // The current belief is that this should return a null indicating
        // the nothing is echo'ed 
        assertTrue(response == null);
    }
    
    public void testNonNullString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "mixedContent";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        // The current implementation does not send the mixedContent over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
    }
    
    public void testCommentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<!--comment-->";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        // The current implementation does not send the comment over the wire, so the
        // expectation is that the echo'd response is null
        assertTrue(response == null);
    }
    
    public void testTwoElementsString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "<a>hello</a><b>world</b>";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        
        // The current implementatin only sends the first element
        // So the echo'd response is just the first one.
        assertTrue(response.contains("<a>hello</a>"));
    }
    
    public void testTwoElementsAndMixedContentString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        Dispatch<Source> dispatch = getDispatch();
        
        String request = "mixed1<a>hello</a>mixed2<b>world</b>mixed3";
        Source requestSource = getSource(request);
        Source responseSource = dispatch.invoke(requestSource);
        String response = getString(responseSource);
        // The current implementation only sends the first element.
        // The mixed content (mixed1) interferes and thus nothing is sent.
        assertTrue(response == null);
    }
    
    public void testException() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
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
    }
    
    public void testProviderSource(){
        try{
        	String resourceDir = new File(providerResourceDir, xmlDir).getAbsolutePath();
        	String fileName = resourceDir+File.separator+"web.xml";
        	
        	File file = new File(fileName);
        	InputStream inputStream = new FileInputStream(file);
        	StreamSource xmlStreamSource = new StreamSource(inputStream);
        	
        	Service svc = Service.create(serviceName);
        	svc.addPort(portName,null, endpointUrl);
        	Dispatch<Source> dispatch = svc.createDispatch(portName, Source.class, null);
            TestLogger.logger.debug(">> Invoking Source Provider Dispatch");
        	Source response = dispatch.invoke(xmlStreamSource);

            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
        	
        }catch(Exception e){
        	e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
}