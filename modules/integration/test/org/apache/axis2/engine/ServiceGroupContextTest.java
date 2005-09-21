package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.util.RequestCounter;
import org.apache.axis2.engine.util.RequestCounterMessageReceiver;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */
public class ServiceGroupContextTest extends TestCase {
    protected EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:" +
                                                                  (5556) +
//                                                                  (UtilServer.TESTING_PORT) +
                                                                  "/axis/services/RequestCounter");
    protected Log log = LogFactory.getLog(getClass());
    protected QName serviceName = new QName("RequestCounter");
    protected QName operationName = new QName("getRequestCount");
    protected QName transportName = new QName("http://localhost/my", "NullTransport");
    protected ServiceDescription service;

    protected void setUp() throws Exception {
        UtilServer.start();
        service = Utils.createSimpleService(serviceName, new RequestCounterMessageReceiver(),
                                            RequestCounter.class.getName(), operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = TestingUtils.createDummyOMElement();

        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
        call.setWsaAction(operationName.getLocalPart());

        OMElement result = call.invokeBlocking(operationName.getLocalPart(), payload);

//        print(result);

        OMNamespace axis2Namespace = fac.createOMNamespace(Constants.AXIS2_NAMESPACE_URI,
                                                           Constants.AXIS2_NAMESPACE_PREFIX);
        SOAPEnvelope defaultEnvelope = fac.getDefaultEnvelope();
        SOAPHeaderBlock soapHeaderBlock = defaultEnvelope.getHeader().addHeaderBlock(Constants.SERVICE_GROUP_ID,
                                                                                     axis2Namespace);
        soapHeaderBlock.setText(result.getText());

        SOAPEnvelope soapEnvelope = call.invokeBlocking(operationName.getLocalPart(),
                                                        defaultEnvelope);
//        print(soapEnvelope.getBody().getFirstElement());
    }

//    private void print(OMElement result) throws XMLStreamException {
//        System.out.println("*******************************************");
//        OMOutputImpl out = new OMOutputImpl(System.out, false);
//        result.serializeWithCache(out);
//        out.flush();
//        System.out.println("*******************************************");
//
//    }
}
