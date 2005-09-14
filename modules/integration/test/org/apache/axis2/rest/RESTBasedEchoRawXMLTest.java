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

package org.apache.axis2.rest;


import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;


public class RESTBasedEchoRawXMLTest extends TestCase {
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


    private Thread thread;

    private final MessageInformation messageInfo = new MessageInformation();
     private ConfigurationContext config;

    public RESTBasedEchoRawXMLTest() {
        super(RESTBasedEchoRawXMLTest.class.getName());
    }

    public RESTBasedEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        config = UtilServer.start();
        Parameter parameter = new ParameterImpl(
                Constants.Configuration.ENABLE_REST, "true");
        UtilServer.getConfigurationContext()
                .getAxisConfiguration().addParameter(parameter);
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
        serviceContext =
                service.getParent().getServiceGroupContext(config).getServiceContext(service.getName().getLocalPart());
//                
//         Runnable runnable = new Runnable() {
//            public void run() {
//                try {
//                    ServerSocket socket = new ServerSocket(UtilServer.TESTING_PORT+345);
//                    Socket clientSocket = socket.accept();
//                    
//                    InputStream in = clientSocket.getInputStream();
//                    OutputStream out = clientSocket.getOutputStream();
//                    
//                    
//                    byte[] byteBuff = new byte[in.available()];
//                    in.read(byteBuff);
//                    messageInfo.requestMessage = new String(byteBuff);
//                    
//                    Socket toServer = new Socket();
//                    toServer.connect(new InetSocketAddress(UtilServer.TESTING_PORT));
//                    OutputStream toServerOut = toServer.getOutputStream();
//                    toServerOut.write(messageInfo.requestMessage.getBytes());
//                    toServerOut.flush();
//                    
//                    InputStream fromServerIn = toServer.getInputStream();
//                    byteBuff = new byte[fromServerIn.available()];
//                    fromServerIn.read(byteBuff);
//                    messageInfo.responseMessage = new String(byteBuff);
//                    out.write(messageInfo.responseMessage.getBytes());
//                    Thread.sleep(30000);
//                    out.flush();
//                    
//                    toServer.close();
//                    clientSocket.close();
//                    socket.close();
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//            }
//        };
//        thread = new Thread(runnable);
//        thread.start();


    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
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


    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();

        org.apache.axis2.clientapi.Call call =
                new org.apache.axis2.clientapi.Call("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        call.setDoREST(true);
        OMElement result =
                call.invokeBlocking(operationName.getLocalPart(),
                        payload);
        result.serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(
                                System.out));

        call.close();
    }

    public class MessageInformation {
        private String requestMessage = null;
        private String responseMessage = null;
    }
}
