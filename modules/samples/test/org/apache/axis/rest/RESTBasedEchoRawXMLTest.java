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

package org.apache.axis.rest;

//todo

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.ParameterImpl;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisConfigurationImpl;
import org.apache.axis.engine.Echo;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.impl.llom.OMOutputer;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RESTBasedEchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference(AddressingConstants.WSA_TO,
                    "http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;
    //private Thread thisThread;
   // private SimpleHTTPServer sas;
    private ServiceContext serviceContext;
    private ServiceDescription service;

    private boolean finish = false;
    
   
    private Thread thread;
    
    private final MessageInformation messageInfo = new MessageInformation();

    public RESTBasedEchoRawXMLTest() {
        super(RESTBasedEchoRawXMLTest.class.getName());
    }

    public RESTBasedEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        Parameter parameter = new ParameterImpl(Constants.Configuration.ENABLE_REST,"true");
        ((AxisConfigurationImpl)UtilServer.getConfigurationContext().getAxisConfiguration()).addParameter(parameter);
        service =
                Utils.createSimpleService(serviceName,
        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
        serviceContext =
                UtilServer.getConfigurationContext().createServiceContext(service.getName());
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
        value.addChild(fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);
        
        return method;
    }

//    public void testEchoXMLASync() throws Exception {
//                OMElement payload = createEnvelope();
//
//        org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();
//
//        call.setTo(targetEPR);
//        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
//
//        Callback callback = new Callback() {
//            public void onComplete(AsyncResult result) {
//                try {
//                    result.getResponseEnvelope().serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
//                } catch (XMLStreamException e) {
//                    reportError(e);
//                } finally {
//                    finish = true;
//                }
//            }
//
//            public void reportError(Exception e) {
//                e.printStackTrace();
//                finish = true;
//            }
//        };
//
//        call.invokeNonBlocking(operationName.getLocalPart(), payload, callback);
//        int index = 0;
//        while (!finish) {
//            Thread.sleep(1000);
//            index++;
//            if(index > 10 ){
//                throw new AxisFault("Server is shutdown as the Async response take too longs time");
//            }
//        }
//
//
//        log.info("send the reqest");
//    }

    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();

        org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
        call.set(Constants.Configuration.DO_REST,"true");
        OMElement result =
                (OMElement) call.invokeBlocking(operationName.getLocalPart(), payload);
        result.serializeWithCache(new OMOutputer(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out)));
        
        System.out.println(messageInfo.requestMessage);
        call.close();
    }
    
    public class MessageInformation{
        private String requestMessage = null;
           private String responseMessage = null;
    }
}
