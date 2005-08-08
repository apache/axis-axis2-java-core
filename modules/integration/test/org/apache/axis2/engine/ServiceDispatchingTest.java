/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.engine;

//todo

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class ServiceDispatchingTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my",
            "NullTransport");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;
    //private Thread thisThread;
    // private SimpleHTTPServer sas;
    private ServiceContext serviceContext;
    private ServiceDescription service;

    private boolean finish = false;

    public ServiceDispatchingTest() {
        super(ServiceDispatchingTest.class.getName());
    }

    public ServiceDispatchingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
        serviceContext =
                UtilServer.getConfigurationContext().createServiceContext(
                        service.getName());

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    public void testDispatchWithURLOnly() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMElement payload = TestingUtils.createDummyOMElement();
        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call();
        call.setTo(
                new EndpointReference("http://127.0.0.1:5555/axis/services/EchoXMLService/echoOMElement"));
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        OMElement result = (OMElement) call.invokeBlocking(
                operationName.getLocalPart(), payload);
        TestingUtils.campareWithCreatedOMElement(result);
        call.close();
    }

    public void testDispatchWithURLAndSOAPAction() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMNamespace omNs = fac.createOMNamespace("http://dummyURL", "my");
        OMElement payload = fac.createOMElement("echoOMElementRequest", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Assimov, the foundation Sega"));
        payload.addChild(value);
        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call();
        call.setTo(
                new EndpointReference("http://127.0.0.1:5555/axis/services/EchoXMLService/"));
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        call.setSoapAction("echoOMElement");
        OMElement result = (OMElement) call.invokeBlocking(
                operationName.getLocalPart(), payload);
        TestingUtils.campareWithCreatedOMElement(result);
        call.close();
    }

    public void testDispatchWithSOAPBody() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMNamespace omNs = fac.createOMNamespace(
                "http://127.0.0.1:5555/axis/services/EchoXMLService", "my");
        OMElement payload = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Assimov, the foundation Sega"));
        payload.addChild(value);


        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call();
        call.setTo(
                new EndpointReference("http://127.0.0.1:5555/axis/services/"));
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        OMElement result = (OMElement) call.invokeBlocking(
                operationName.getLocalPart(), payload);
        TestingUtils.campareWithCreatedOMElement(result);
        call.close();
    }
}
