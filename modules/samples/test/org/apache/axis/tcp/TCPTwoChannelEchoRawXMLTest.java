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

package org.apache.axis.tcp;



import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Echo;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.integration.UtilsTCPServer;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.util.Utils;

public class TCPTwoChannelEchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference(AddressingConstants.WSA_TO,
                    "tcp://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/EchoXMLService/echoOMElement");
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

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
                   Utils.createSimpleService(
                       serviceName,
                       org.apache.axis.engine.Echo.class.getName(),
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
        value.addChild(fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);
        
        return method;
    }

    public void testEchoXMLCompleteASync() throws Exception {
            ServiceDescription service =
                Utils.createSimpleService(
                    serviceName,
            Echo.class.getName(),
                    operationName);

            

            OMFactory fac = OMAbstractFactory.getOMFactory();

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.setText("Isaac Assimov, the foundation Sega");
            method.addChild(value);

            org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call(serviceContext);
            // call.engageModule(new QName(Constants.MODULE_ADDRESSING));

            try {
                call.setTo(targetEPR);
                call.setTransportInfo(Constants.TRANSPORT_TCP, Constants.TRANSPORT_TCP, true);
                Callback callback = new Callback() {
                    public void onComplete(AsyncResult result) {
                        try {
                            result.getResponseEnvelope().serialize(
                                XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
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

                call.invokeNonBlocking(operationName.getLocalPart(), method, callback);
                int index = 0;
                while (!finish) {
                    Thread.sleep(1000);
                    index++;
                    if (index > 10) {
                        throw new AxisFault("Server is shutdown as the Async response take too longs time");
                    }
                }
            } finally {
                call.close();
            }

        }
}
