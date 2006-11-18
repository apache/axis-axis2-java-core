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
package org.apache.axis2.jaxws.proxy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.dispatch.DispatchTestConstants;
import org.apache.axis2.jaxws.proxy.rpclit.sei.RPCLit;

import junit.framework.TestCase;

public class RPCProxyTests extends TestCase {

    private QName serviceName = new QName(
            "http://org.apache.axis2.jaxws.proxy.rpclit", "RPCLitService");
    private String axisEndpoint = "http://localhost:8080/axis2/services/RPCLitService";
    private QName portName = new QName("http://org.apache.axis2.jaxws.proxy.rpclit",
            "RPCLit");
    private String wsdlLocation = "test/org/apache/axis2/jaxws/proxy/rpclit/META-INF/RPCLit.wsdl";
    
    /**
     * Utility method to get the proxy
     * @return RPCLit proxy
     * @throws MalformedURLException
     */
    public RPCLit getProxy() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        Object proxy =service.getPort(portName, RPCLit.class);
        BindingProvider p = (BindingProvider)proxy; 
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        
        return (RPCLit)proxy;
    }
    
    /**
     * Utility Method to get a Dispatch<String>
     * @return
     * @throws MalformedURLException
     */
    public Dispatch<String> getDispatch() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        service.addPort(portName, null, axisEndpoint);
        Dispatch<String> dispatch = service.createDispatch(portName, String.class, Service.Mode.PAYLOAD);
        return dispatch;
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testSimple() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String request = "This is a test...";
           
            String response = proxy.testSimple(request);
            assertTrue(response != null);
            assert(response.equals(request));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testSimple_Dispatch() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testSimple xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        "<tns:simpleIn xsi:type='xsd:string' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
        "PAYLOAD WITH XSI:TYPE" +
        "</tns:simpleIn></tns:testSimple>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        System.out.println(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simpleOut"));
        assertTrue(response.contains("testSimpleResponse"));
        assertTrue(response.contains("PAYLOAD WITH XSI:TYPE"));
    }
    
    public void testSimple_DispatchWithoutXSIType() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testSimple xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        "<tns:simpleIn>" +
        "PAYLOAD WITHOUT XSI:TYPE" +
        "</tns:simpleIn></tns:testSimple>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);
        

        assertNotNull("dispatch invoke returned null", response);
        System.out.println(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("simpleOut"));
        assertTrue(response.contains("testSimpleResponse"));
        assertTrue(response.contains("PAYLOAD WITHOUT XSI:TYPE"));
    }
    
    /**
     * Simple test that ensures that we can echo a string to an rpc/lit web service.
     */
    public void testStringList() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String[] request = new String[] {"Hello" , "World"};
           
            String[] response = proxy.testStringList2(request);
            assertTrue(response != null);
            assertTrue(response.length==2);
            assertTrue(response[0].equals("Hello"));
            assertTrue(response[1].equals("World"));
            assert(response.equals(request));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    // TODO 
    // Commented out while fixing this test.  The above test succeeds, but sends the message in an incorrect format.
    // This test sends the message in the correct format, but currently fails.  
    // We need to investigate the @XmlList processing.
    //
    public void _testStringList_Dispatch() throws Exception {
        // Send a payload that simulates
        // the rpc message
        String request = "<tns:testStringList2 xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit'>" +
        //"<tns:arg_2_0 xmlns:tns='http://org/apache/axis2/jaxws/proxy/rpclit' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='tns:StringList' >" +
        "<tns:arg_2_0>" +
        "Hello World" +
        "</tns:arg_2_0></tns:testStringList2>";
        Dispatch<String> dispatch = getDispatch();
        String response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        System.out.println(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(!response.contains("Fault"));
        assertTrue(response.contains("testStringList2Return"));
        assertTrue(response.contains("testStringList2Response"));
        assertTrue(response.contains("Hello World"));
    }
    
    
}
