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
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.DocLitnonWrappedProxy;
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.Invoke;
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.ObjectFactory;
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.ProxyDocLitUnwrappedService;
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.ReturnType;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.Future;

/**
 * This test cases will use proxy NON wrapped wsdl to invoke methods
 * on a deployed Server Endpoint.
 */
public class ProxyNonWrappedTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    QName serviceName = new QName("http://doclitnonwrapped.proxy.test.org", "ProxyDocLitUnwrappedService");
    private QName portName = new QName("http://org.apache.axis2.proxy.doclitwrapped", "ProxyDocLitWrappedPort");

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("ProxyDocLitUnwrappedService.DocLitnonWrappedImplPort");
    }

    @Test
    public void testInvoke() throws Exception {
        TestLogger.logger.debug("-----------------------------------");
        TestLogger.logger.debug(">>Testing Sync Inovoke on Proxy DocLit non-wrapped");
        ObjectFactory factory = new ObjectFactory();
        Invoke invokeObj = factory.createInvoke();
        invokeObj.setInvokeStr("test request for twoWay Operation");
        Service service = Service.create(null, serviceName);
        assertNotNull(service);
        DocLitnonWrappedProxy proxy = service.getPort(portName, DocLitnonWrappedProxy.class);
        assertNotNull(proxy);
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
        ReturnType response = proxy.invoke(invokeObj);
        assertNotNull(response);
        TestLogger.logger.debug(">>Response =" + response.getReturnStr());

        
        // Try again to verify
        response = proxy.invoke(invokeObj);
        assertNotNull(response);
        TestLogger.logger.debug(">>Response =" + response.getReturnStr());

        TestLogger.logger.debug("-------------------------------------");
    }
    
    @Test
    public void testNullInvoke() throws Exception {
        TestLogger.logger.debug("-----------------------------------");
        TestLogger.logger.debug(">>Testing Sync Invoke on Proxy DocLit bare with a null parameter");
        ObjectFactory factory = new ObjectFactory();
        Invoke invokeObj = null;
        Service service = Service.create(null, serviceName);
        assertNotNull(service);
        DocLitnonWrappedProxy proxy = service.getPort(portName, DocLitnonWrappedProxy.class);
        assertNotNull(proxy);
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
        ReturnType response = proxy.invoke(invokeObj);
        assertNull(response);
        
        // Try again
        response = proxy.invoke(invokeObj);
        assertNull(response);

        TestLogger.logger.debug("-------------------------------------");
    }
    
    @Test
    public void testInvokeAsyncCallback(){
        try{
            TestLogger.logger.debug("---------------------------------------");
            ObjectFactory factory = new ObjectFactory();
            //create input object to web service operation
            Invoke invokeObj = factory.createInvoke();
            invokeObj.setInvokeStr("test request for twoWay Async Operation");
            //Create Service
            ProxyDocLitUnwrappedService service = new ProxyDocLitUnwrappedService();
            //Create proxy
            DocLitnonWrappedProxy proxy = service.getDocLitnonWrappedImplPort();
            TestLogger.logger.debug(">>Invoking Binding Provider property");
            //Setup Endpoint url -- optional.
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());
            TestLogger.logger.debug(">> Invoking Proxy Asynchronous Callback");
            AsyncHandler<ReturnType> handler = new AsyncCallback();
            //Invoke operation Asynchronously.
            Future<?> monitor = proxy.invokeAsync(invokeObj, handler);
            await(monitor);
            
            
            // Try again
            TestLogger.logger.debug(">> Invoking Proxy Asynchronous Callback");
            handler = new AsyncCallback();
            //Invoke operation Asynchronously.
            monitor = proxy.invokeAsync(invokeObj, handler);
            await(monitor);
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    @Test
    public void testInvokeAsyncPolling(){
        
    }

}

