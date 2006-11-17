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
import javax.xml.ws.Service;

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
     * Simple test that ensures that we can echo a string to an rpc/lit web service
     */
    public void testSimple() throws Exception {
        try{ 
            RPCLit proxy = getProxy();
            String request = "This is a test...";
           
            String response = proxy.testSimple(request);
            assert(response != null);
            assert(response.equals(request));
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
}
