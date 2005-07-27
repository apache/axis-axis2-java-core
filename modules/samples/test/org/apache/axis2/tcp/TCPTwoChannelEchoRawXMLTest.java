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

package org.apache.axis2.tcp;


import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilsTCPServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class TCPTwoChannelEchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference(AddressingConstants.WSA_TO,
                    "tcp://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/EchoXMLService/echoOMElement");
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my",
            "NullTransport");

    private MessageContext mc;
    private SimpleHTTPServer sas;
    private ServiceDescription service;
    private ServiceContext serviceContext;

    private boolean finish = false;

    public TCPTwoChannelEchoRawXMLTest() {
        super(TCPTwoChannelEchoRawXMLTest.class.getName());
    }

    public TCPTwoChannelEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilsTCPServer.start();

        
        //create and deploy the service
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilsTCPServer.deployService(service);

        ServiceDescription service =
                Utils.createSimpleService(serviceName,
                        org.apache.axis2.engine.Echo.class.getName(),
                        operationName);
        serviceContext = UtilServer.createAdressedEnabledClientSide(service);
    }

    protected void tearDown() throws Exception {
        UtilsTCPServer.stop();
    }

    private OMElement createEnvelope() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);

        return method;
    }

    public void testEchoXMLCompleteASync() throws Exception {
        ServiceDescription service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);


        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Assimov, the foundation Sega");
        method.addChild(value);

        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call(
                serviceContext);
        call.engageModule(new QName(Constants.MODULE_ADDRESSING));

        try {
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_TCP,
                    Constants.TRANSPORT_TCP,
                    true);
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        result.getResponseEnvelope().serialize(XMLOutputFactory.newInstance()
                                .createXMLStreamWriter(System.out));
                    } catch (XMLStreamException e) {
                        reportError(e);
                    } finally {
                        finish = true;
                    }
                }

                public void reportError(Exception e) {
                    e.printStackTrace();
                    finish = true;
                }
            };

            call.invokeNonBlocking(operationName.getLocalPart(),
                    method,
                    callback);
            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Server is shutdown as the Async response take too longs time");
                }
            }
        } finally {
            call.close();
        }

    }
}
