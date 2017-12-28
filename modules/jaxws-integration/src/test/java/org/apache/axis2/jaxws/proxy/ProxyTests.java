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

package org.apache.axis2.jaxws.proxy;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.proxy.doclitwrapped.DocLitWrappedProxy;
import org.apache.axis2.jaxws.proxy.doclitwrapped.ProxyDocLitWrappedService;
import org.apache.axis2.jaxws.proxy.doclitwrapped.ReturnType;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

public class ProxyTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");
    
    private QName serviceName = new QName(
            "http://doclitwrapped.proxy.test.org", "ProxyDocLitWrappedService");
    private QName portName = new QName("http://doclitwrapped.proxy.test.org",
            "DocLitWrappedProxyImplPort");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+"src/test/java/org/apache/axis2/jaxws/proxy/doclitwrapped/META-INF/ProxyDocLitWrapped.wsdl";
    private boolean runningOnAxis = true;

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("ProxyDocLitWrappedService.DocLitWrappedProxyImplPort");
    }

    @Test
    public void testMultipleServiceCalls() throws Exception {
        if(!runningOnAxis){
            return;
        }
        TestLogger.logger.debug("---------------------------------------");
        String request = new String("some string request");
        TestLogger.logger.debug("Service Call #1");
        ProxyDocLitWrappedService service1 = new ProxyDocLitWrappedService();
        DocLitWrappedProxy proxy1 = service1.getDocLitWrappedProxyImplPort();
        BindingProvider p1 =    (BindingProvider)proxy1;
        p1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
        String response1 = proxy1.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response1);
        TestLogger.logger.debug("---------------------------------------");

        TestLogger.logger.debug("Service Call #2");
        ProxyDocLitWrappedService service2 = new ProxyDocLitWrappedService();
        DocLitWrappedProxy proxy2 = service2.getDocLitWrappedProxyImplPort();
        BindingProvider p2 =    (BindingProvider)proxy2;
        p2.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
        String response2 = proxy2.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response2);
        TestLogger.logger.debug("---------------------------------------");
    }
    
    @Test
    public void testInvokeWithNullParam() throws Exception {
        if(!runningOnAxis){
            return;
        }
        TestLogger.logger.debug("---------------------------------------");
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(null, serviceName); 
        Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
        TestLogger.logger.debug(">>Invoking Binding Provider property");
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

        DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
        TestLogger.logger.debug(">> Invoking Proxy Synchronously");
        String request = null;
        String response = dwp.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response);

        // Try again
        response = dwp.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response);
        TestLogger.logger.debug("---------------------------------------");
    }
    
    @Test
    public void testInvoke() throws Exception {
        if(!runningOnAxis){
            return;
        }
        TestLogger.logger.debug("---------------------------------------");
        
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(null, serviceName);
        String request = new String("some string request"); 
        Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
        TestLogger.logger.debug(">>Invoking Binding Provider property");
        BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            
        DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
        TestLogger.logger.debug(">> Invoking Proxy Synchronously");
        String response = dwp.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response);
        
        // Try again
        response = dwp.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response);
        TestLogger.logger.debug("---------------------------------------");
    }

    @Test
    public void testInvokeWithWSDL() throws Exception {
        if(!runningOnAxis){
            return;
        }
        TestLogger.logger.debug("---------------------------------------");
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(wsdlUrl, serviceName);
        String request = new String("some string request"); 
        Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
        TestLogger.logger.debug(">>Invoking Binding Provider property");
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            
        DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
        TestLogger.logger.debug(">> Invoking Proxy Synchronously");
        String response = dwp.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response);
        
        // Try again
        response = dwp.invoke(request);
        TestLogger.logger.debug("Proxy Response =" + response);
        TestLogger.logger.debug("---------------------------------------");
    }
    
    @Test
    public void testInvokeAsyncCallback() throws Exception {
        if(!runningOnAxis){
            return;
        }
        TestLogger.logger.debug("---------------------------------------");
        
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(null, serviceName);
        String request = new String("some string request"); 
        Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
        TestLogger.logger.debug(">>Invoking Binding Provider property");
        BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            
        DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
        TestLogger.logger.debug(">> Invoking Proxy Asynchronous Callback");
        AsyncHandler handler = new AsyncCallback();
        Future<?> response = dwp.invokeAsync(request, handler);
        
        // Try again
        handler = new AsyncCallback();
        response = dwp.invokeAsync(request, handler);
        TestLogger.logger.debug("---------------------------------------");
    }
    
    @Test
    public void testInvokeAsyncPolling() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(null, serviceName);
        DocLitWrappedProxy proxy =service.getPort(portName, DocLitWrappedProxy.class);
        
        String request = new String("some string request");

        TestLogger.logger.debug(">> Invoking Binding Provider property");
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

        TestLogger.logger.debug(">> Invoking Proxy with async polling request");
        Response<ReturnType> asyncResponse = proxy.invokeAsync(request);

        await(asyncResponse);
        
        ReturnType response = asyncResponse.get();
        assertNotNull(response);
        
        // Try again
        asyncResponse = proxy.invokeAsync(request);

        await(asyncResponse);
        
        response = asyncResponse.get();
        assertNotNull(response);
    }
    
    @Test
    public void testTwoWay() throws Exception {
        if(runningOnAxis){
            return;
        }
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(null, serviceName);
        String request = new String("some string request"); 
        
        Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            
        DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;  
        String response = dwp.twoWay(request);
        System.out.println("Response =" + response);
        
        // Try again
        response = dwp.twoWay(request);
        System.out.println("Response =" + response);
    }
    
    @Test
    public void testOneWay(){
        
    }
    
    @Test
    public void testHolder(){
        
    }
    
    @Test
    public void testTwoWayAsyncCallback() throws Exception {
        if(runningOnAxis){
            return;
        }
        File wsdl= new File(wsdlLocation); 
        URL wsdlUrl = wsdl.toURI().toURL(); 
        Service service = Service.create(null, serviceName);
        
        String request = new String("some string request"); 
        
        Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
        BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            
        DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
        AsyncHandler handler = new AsyncCallback();
        Future<?> response = dwp.twoWayAsync(request, handler);
        
        // Try again
        handler = new AsyncCallback();
        response = dwp.twoWayAsync(request, handler);
    }
    
    @Test
    public void testAsyncPooling(){
        
    }
}

