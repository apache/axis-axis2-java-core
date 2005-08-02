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

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CallUnregisteredServiceTest extends TestCase {
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("", "EchoXMLService");
    private QName operationName = new QName("http://localhost/my",
            "echoOMElement");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;
    private Thread thisThread;
    private SimpleHTTPServer sas;

    public CallUnregisteredServiceTest() {
        super(CallUnregisteredServiceTest.class.getName());
    }

    public CallUnregisteredServiceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

    public void testEchoXMLSync() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

//            SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
            OMNamespace omNs = fac.createOMNamespace("http://localhost/my",
                    "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.addChild(
                    fac.createText(value,
                            "Isaac Assimov, the foundation Sega"));
            method.addChild(value);
//            reqEnv.getBody().addChild(method);

            Call call = new Call();
            EndpointReference targetEPR =
                    new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis/services/EchoXMLService1");
            call.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    false);
            call.setTo(targetEPR);
            SOAPEnvelope resEnv =
                    (SOAPEnvelope) call.invokeBlocking(
                            operationName.getLocalPart(), method);

            SOAPBody sb = resEnv.getBody();
            if (sb.hasFault()) {
                throw new AxisFault(
                        sb.getFault().getReason().getSOAPText().getText());
            }
            fail("The test must fail due to wrong service Name");

        } catch (AxisFault e) {
            assertTrue(e.getMessage().indexOf("Service Not found") >= 0);
            tearDown();
            return;
        }

    }
}
