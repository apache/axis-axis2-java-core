/*
* Copyright 2004,2005,2006 The Apache Software Foundation.
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
*
*
*/

package org.apache.axis2.deployment;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;


public class TargetResolverServiceTest extends UtilServerBasedTestCase implements TestConstants {

    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");

    // 2 special urls that the TestTargetResolver will modify into ones that can be targeted
    EndpointReference targetEPR = new EndpointReference(
            "trtest://" + (UtilServer.TESTING_PORT) + "/axis2/services/EchoXMLService/echoOMElement");

    EndpointReference replyTo = new EndpointReference(
            "http://ws.apache.org/new/anonymous/address");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService echoService;
    protected AxisService rrService;

    public static Test suite() {
        return getTestSetup2(new TestSuite(TargetResolverServiceTest.class), Constants.TESTING_PATH + "deployment_repo");
    }

    protected void setUp() throws Exception {
        echoService = Utils.createSimpleService(serviceName,
                new RawXMLINOutMessageReceiver(),
                Echo.class.getName(),
                operationName);
        UtilServer.deployService(echoService);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public static AxisService createSimpleServiceforClient(QName serviceName,
                                                           String className,
                                                           QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, className));

        AxisOperation axisOp = new OutInAxisOperation(opName);

        axisOp.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public void testEchoToReplyTo() throws Exception {
        OMElement method = createEchoOMElement("this message should not cause a fault.");
        ServiceClient sender = null;
        try {
            sender = createServiceClient();
            OMElement result = sender.sendReceive(operationName, method);
            assertEquals("echoOMElementResponse", result.getLocalName());
        } finally {
            if (sender != null)
                sender.cleanup();
        }
    }

    private OMElement createEchoOMElement(String text) {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText(text);
        method.addChild(value);

        return method;
    }

    private ServiceClient createServiceClient() throws AxisFault {
        AxisService service =
                createSimpleServiceforClient(serviceName,
                        Echo.class.getName(),
                        operationName);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext(Constants.TESTING_PATH + "deployment_repo");
        ServiceClient sender ;

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction(operationName.getLocalPart());
        options.setReplyTo(replyTo);

        sender = new ServiceClient(configcontext, service);
        sender.setOptions(options);
        sender.engageModule("addressing");

        return sender;
    }
}
