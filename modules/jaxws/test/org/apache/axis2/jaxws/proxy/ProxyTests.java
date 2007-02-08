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
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.proxy.doclitwrapped.sei.DocLitWrappedProxy;
import org.apache.axis2.jaxws.proxy.doclitwrapped.sei.ProxyDocLitWrappedService;
import org.test.proxy.doclitwrapped.ReturnType;

public class ProxyTests extends TestCase {
    private QName serviceName = new QName(
            "http://doclitwrapped.proxy.test.org", "ProxyDocLitWrappedService");
    private String axisEndpoint = "http://localhost:8080/axis2/services/ProxyDocLitWrappedService";
    private QName portName = new QName("http://doclitwrapped.proxy.test.org",
            "ProxyDocLitWrappedPort");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+"test/org/apache/axis2/jaxws/proxy/doclitwrapped/META-INF/ProxyDocLitWrapped.wsdl";
    private boolean runningOnAxis = true;
    
    public void testMultipleServiceCalls(){
        try{
            if(!runningOnAxis){
                return;
            }
            System.out.println("---------------------------------------");
            System.out.println("test:" +getName());
            String request = new String("some string request");
            System.out.println("Service Call #1");
            ProxyDocLitWrappedService service1 = new ProxyDocLitWrappedService();
            DocLitWrappedProxy proxy1 = service1.getProxyDocLitWrappedPort();
            BindingProvider p1 =    (BindingProvider)proxy1;
            p1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
            String response1 = proxy1.invoke(request);
            System.out.println("Proxy Response =" + response1);
            System.out.println("---------------------------------------");
            
            System.out.println("Service Call #2");
            ProxyDocLitWrappedService service2 = new ProxyDocLitWrappedService();
            DocLitWrappedProxy proxy2 = service2.getProxyDocLitWrappedPort();
            BindingProvider p2 =    (BindingProvider)proxy2;
            p2.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
            String response2 = proxy2.invoke(request);
            System.out.println("Proxy Response =" + response2);
            System.out.println("---------------------------------------");
            
        }catch(Exception e){
            //fail(getName() + " failed");
            e.printStackTrace();
        }
    }
    
    public void testInvokeWithNullParam(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            System.out.println("---------------------------------------");
            System.out.println("Test Name: "+getName());
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            System.out.println(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);

            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            System.out.println(">> Invoking Proxy Synchronously");
            String request = null;
            String response = dwp.invoke(request);
            System.out.println("Proxy Response =" + response);
            System.out.println("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    public void testInvoke(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            System.out.println("---------------------------------------");
            
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            String request = new String("some string request"); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            System.out.println(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            System.out.println(">> Invoking Proxy Synchronously");
            String response = dwp.invoke(request);
            System.out.println("Proxy Response =" + response);
            System.out.println("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }

    public void testInvokeWithWSDL(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            System.out.println("---------------------------------------");
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(wsdlUrl, serviceName);
            String request = new String("some string request"); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            System.out.println(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            System.out.println(">> Invoking Proxy Synchronously");
            String response = dwp.invoke(request);
            System.out.println("Proxy Response =" + response);
            System.out.println("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testInvokeAsyncCallback(){
        try{ 
            if(!runningOnAxis){
                return;
            }
            System.out.println("---------------------------------------");
            
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            String request = new String("some string request"); 
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
            System.out.println(">>Invoking Binding Provider property");
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            System.out.println(">> Invoking Proxy Asynchronous Callback");
            AsyncHandler handler = new AsyncCallback();
            Future<?> response = dwp.invokeAsync(request, handler);
            System.out.println("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testInvokeAsyncPolling() {
        try { 
            System.out.println("---------------------------------------");
            
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            DocLitWrappedProxy proxy =service.getPort(portName, DocLitWrappedProxy.class);
            
            String request = new String("some string request"); 
            
            System.out.println(">> Invoking Binding Provider property");
            BindingProvider p = (BindingProvider) proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
                
            System.out.println(">> Invoking Proxy with async polling request");
            Response<ReturnType> asyncResponse = proxy.invokeAsync(request);

            while (!asyncResponse.isDone()) {
                System.out.println(">> Async invocation still not complete");
                Thread.sleep(1000);
            }
            
            ReturnType response = asyncResponse.get();
            assertNotNull(response);
        }
        catch(Exception e) { 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testTwoWay(){
        /*
        try{ 
            if(runningOnAxis){
                return;
            }
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            String request = new String("some string request"); 
            
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,wasEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;  
            String response = dwp.twoWay(request);
            System.out.println("Response =" + response);
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
        */
    }
    
    public void testOneWay(){
        
    }
    
    public void testHolder(){
        
    }
    
    public void testTwoWayAsyncCallback(){
        /*
        try{ 
            if(runningOnAxis){
                return;
            }
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            Service service = Service.create(null, serviceName);
            
            String request = new String("some string request"); 
            
            Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,wasEndpoint);
                
            DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
            AsyncHandler handler = new AsyncCallback();
            Future<?> response = dwp.twoWayAsync(request, handler);
            
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
        */
    }
    
    public void testAsyncPooling(){
        
    }
}

