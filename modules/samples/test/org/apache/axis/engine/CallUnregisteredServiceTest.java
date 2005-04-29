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
 
package org.apache.axis.engine;

//todo

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.context.MessageContext;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CallUnregisteredServiceTest extends TestCase{
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("", "EchoXMLService");
    private QName operationName = new QName("http://localhost/my", "echoOMElement");
    
    private EngineConfiguration engineRegistry;
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
            OMFactory fac = OMFactory.newInstance();

            SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
            OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.addChild(fac.createText(value,"Isaac Assimov, the foundation Sega"));
            method.addChild(value);
            reqEnv.getBody().addChild(method);

            Call call = new Call();
            EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + (UtilServer.TESTING_PORT) + "/axis/services/EchoXMLService1");
            call.setTransport(Constants.TRANSPORT_HTTP);
            call.setTo(targetEPR);
            call.setTransport(Constants.TRANSPORT_HTTP);
            SOAPEnvelope resEnv = call.sendReceiveSync(reqEnv);

            SOAPBody sb = resEnv.getBody();
            if (sb.hasFault()) {
                throw new AxisFault(sb.getFault().getReason().getSOAPText().getText());
            }
            fail("The test must fail due to wrong service Name");

        } catch (AxisFault e) {
            tearDown();
            return;
        }

    }
}
