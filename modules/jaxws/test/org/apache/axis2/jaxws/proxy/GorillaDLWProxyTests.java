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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.dispatch.DispatchTestConstants;
import org.apache.axis2.jaxws.proxy.gorilla_dlw.sei.GorillaInterface;
import org.test.proxy.rpclit.ObjectFactory;
import org.test.proxy.rpclit.Enum;
import org.test.proxy.rpclit.ComplexAll;

import junit.framework.TestCase;

public class GorillaDLWProxyTests extends TestCase {

    private QName serviceName = new QName(
            "http://org.apache.axis2.jaxws.proxy.gorilla_dlw", "GorillaService");
    private String axisEndpoint = "http://localhost:8080/axis2/services/GorillaService";
    private QName portName = new QName("http://org.apache.axis2.jaxws.proxy.rpclit",
            "GorillaPort");
    private String wsdlLocation = "test/org/apache/axis2/jaxws/proxy/gorilla_dlw/META-INF/gorilla_dlw.wsdl";
    
    /**
     * Utility method to get the proxy
     * @return GorillaInterface proxy
     * @throws MalformedURLException
     */
    public GorillaInterface getProxy() throws MalformedURLException {
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURL(); 
        Service service = Service.create(null, serviceName);
        Object proxy =service.getPort(portName, GorillaInterface.class);
        BindingProvider p = (BindingProvider)proxy; 
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        
        return (GorillaInterface)proxy;
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
     * Simple test that ensures that we can echo a string.
     * If this test fails, it usually means that there are connection
     * problems or deploy problems.
     */
    public void testEchoString() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            String request = "Hello World";
           
            String response = proxy.echoString(request);
            assertTrue(response != null);
            assert(response.equals(request));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    /**
     * Tests that we can echo a null
     */
    public void testEchoStringNull() throws Exception {
        try{ 
            GorillaInterface proxy = getProxy();
            String request = null;  // Null is an appropriate input
           
            String response = proxy.echoString(request);
            assertTrue(response == null);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
}