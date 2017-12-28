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

package org.apache.axis2.jaxws.dispatch;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Future;

public class SOAPMessageDispatchTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

	private QName serviceName = new QName(
			"http://org.apache.axis2.proxy.doclitwrapped", "ProxyDocLitWrappedService");
	private QName portName = new QName("http://org.apache.axis2.proxy.doclitwrapped",
	"ProxyDocLitWrappedPort");
	
	String messageResource = "test-resources" + File.separator  + "xml" + File.separator +"soapmessage.xml";
	
    private static String getEndpoint() throws Exception {
        return server.getEndpoint("ProxyDocLitWrappedService.DocLitWrappedProxyImplPort");
    }

    @Test
    public void testSOAPMessageSyncMessageMode() throws Exception {

        String basedir = new File(System.getProperty("basedir",".")).getAbsolutePath();
        String messageResource = new File(basedir, this.messageResource).getAbsolutePath();

        TestLogger.logger.debug("---------------------------------------");
        //Initialize the JAX-WS client artifacts
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, getEndpoint());
        Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName,
                                                            SOAPMessage.class, Service.Mode.MESSAGE);

        //Create SOAPMessage Object no attachments here.
        FileInputStream inputStream = new FileInputStream(messageResource);
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msgObject = factory.createMessage(null, inputStream);

        //Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking Async Dispatch");
        SOAPMessage response = dispatch.invoke(msgObject);

        assertNotNull("dispatch invoke returned null", response);
        response.writeTo(System.out);
        
        // Invoke a second time to verify
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking Async Dispatch");
        response = dispatch.invoke(msgObject);

        assertNotNull("dispatch invoke returned null", response);
        response.writeTo(System.out);
    }

    @Test
    public void testSOAPMessageAsyncCallbackMessageMode() throws Exception {

        String basedir = new File(System.getProperty("basedir",".")).getAbsolutePath();
        String messageResource = new File(basedir, this.messageResource).getAbsolutePath();

        TestLogger.logger.debug("---------------------------------------");
        //Initialize the JAX-WS client artifacts
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, getEndpoint());
        Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName,
                                                            SOAPMessage.class, Service.Mode.MESSAGE);

        //Create SOAPMessage Object no attachments here.
        FileInputStream inputStream = new FileInputStream(messageResource);
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msgObject = factory.createMessage(null, inputStream);

        AsyncCallback<SOAPMessage> ac = new AsyncCallback<SOAPMessage>();
        //Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        Future<?> monitor = dispatch.invokeAsync(msgObject, ac);

        assertNotNull("dispatch invokeAsync returned null Future<?>", monitor);
        await(monitor);

        SOAPMessage response = ac.getValue();
        assertNotNull("dispatch invoke returned null", response);
        response.writeTo(System.out);
        
        // Invoke a second time to verify
        ac = new AsyncCallback<SOAPMessage>();
        //Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        monitor = dispatch.invokeAsync(msgObject, ac);

        assertNotNull("dispatch invokeAsync returned null Future<?>", monitor);
        await(monitor);

        response = ac.getValue();
        assertNotNull("dispatch invoke returned null", response);
        response.writeTo(System.out);
    }

    @Test
    public void testSOAPMessageAsyncPollingMessageMode() throws Exception {

        String basedir = new File(System.getProperty("basedir",".")).getAbsolutePath();
        String messageResource = new File(basedir, this.messageResource).getAbsolutePath();

        TestLogger.logger.debug("---------------------------------------");
        //Initialize the JAX-WS client artifacts
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, getEndpoint());
        Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName,
                                                            SOAPMessage.class, Service.Mode.MESSAGE);

        //Create SOAPMessage Object no attachments here.
        FileInputStream inputStream = new FileInputStream(messageResource);
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msgObject = factory.createMessage(null, inputStream);

        //Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        Response<SOAPMessage> asyncResponse = dispatch.invokeAsync(msgObject);

        assertNotNull("dispatch invokeAsync returned null Response", asyncResponse);
        await(asyncResponse);

        SOAPMessage response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        response.writeTo(System.out);
        
        
        // Invoke a second time to verify
        // Invoke the Dispatch
        TestLogger.logger.debug(">> Invoking sync Dispatch");
        asyncResponse = dispatch.invokeAsync(msgObject);

        assertNotNull("dispatch invokeAsync returned null Response", asyncResponse);
        await(asyncResponse);

        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        response.writeTo(System.out);
    }

    /**
     * Tests that HTTP connections are properly released if {@link Dispatch#invokeOneWay(Object)} is
     * used to invoke a service that actually uses the in-out MEP. This is a regression test for
     * AXIS2-5062.
     * 
     * @throws Exception
     */
    @Test
    public void testConnectionReleaseForInvokeOneWayWithMEPMismatch() throws Exception {
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, getEndpoint());
        Dispatch<SOAPMessage> dispatch = svc.createDispatch(portName,
                                                            SOAPMessage.class, Service.Mode.MESSAGE);
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPBody().addBodyElement(new QName("urn:test", "test"));
        // If HTTP connections are not properly released, then this will end up with a
        // ConnectionPoolTimeoutException.
        for (int i=0; i<200; i++) {
            dispatch.invokeOneWay(message);
        }
    }
}
