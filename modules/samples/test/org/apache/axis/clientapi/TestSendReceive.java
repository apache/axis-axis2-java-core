///*
// * Copyright 2004,2005 The Apache Software Foundation.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *      http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.axis.clientapi;
//
//import junit.framework.TestCase;
//import org.apache.axis.addressing.AddressingConstants;
//import org.apache.axis.addressing.EndpointReference;
//import org.apache.axis.context.MessageContext;
//import org.apache.axis.description.AxisDescWSDLComponentFactory;
//import org.apache.axis.description.GlobalDescription;
//import org.apache.axis.description.OperationDescription;
//import org.apache.axis.description.ServiceDescription;
//import org.apache.axis.engine.AxisConfiguration;
//import org.apache.axis.engine.AxisFault;
//import org.apache.axis.integration.UtilServer;
//import org.apache.axis.phaseresolver.PhaseException;
//import org.apache.axis.phaseresolver.PhaseResolver;
//import org.apache.axis.soap.SOAPEnvelope;
//import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
//import org.apache.axis.transport.http.SimpleHTTPServer;
//import org.apache.axis.util.Utils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import javax.xml.namespace.QName;
//import javax.xml.stream.XMLInputFactory;
//import javax.xml.stream.XMLOutputFactory;
//import java.io.FileReader;
//
//public class TestSendReceive extends TestCase {
//    private Log log = LogFactory.getLog(getClass());
//
//    private QName serviceName = new QName("", "EchoXMLService");
//
//    private QName operationName =
//        new QName("http://localhost/my", "echoOMElement");
//
//    private QName transportName =
//        new QName("http://localhost/my", "NullTransport");
//
//    private MessageContext mc;
//
//    private Thread thisThread;
//    private ServiceDescription service;
//
//    private SimpleHTTPServer sas;
//
//    public TestSendReceive() {
//        super(TestSendReceive.class.getName());
//    }
//
//    public TestSendReceive(String testName) {
//        super(testName);
//    }
//
//    protected void setUp() throws Exception {
//        service =
//            Utils.createSimpleService(
//                serviceName,
//                org.apache.axis.engine.Echo.class.getName(),operationName);
//
//       
//
//        UtilServer.start();
//        UtilServer.deployService(Utils.createServiceContext(service,null));
//    }
//
//    protected void tearDown() throws Exception {
//        UtilServer.unDeployService(service.getName());
//        UtilServer.stop();
//    }
//
//    public void testSendReceive() throws Exception {
//
//        SOAPEnvelope envelope = getBasicEnvelope();
//        EndpointReference targetEPR =
//            new EndpointReference(
//                AddressingConstants.WSA_TO,
//                "http://127.0.0.1:"
//                    + UtilServer.TESTING_PORT
//                    + "/axis/services/EchoXMLService");
//        Call call = new Call();
//        call.setTo(targetEPR);
//        SOAPEnvelope responseEnv = (SOAPEnvelope)call.invokeBlocking("echoOMElement",envelope);
//        responseEnv.serializeWithCache(
//            XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
//
//    }
//
//    private SOAPEnvelope getBasicEnvelope() throws Exception {
//
//        SOAPEnvelope envelope =
//            new StAXSOAPModelBuilder(
//                XMLInputFactory.newInstance().createXMLStreamReader(
//                    new FileReader("src/test-resources/clientapi/SimpleSOAPEnvelope.xml")))
//                .getSOAPEnvelope();
//        return envelope;
//    }
//    
//
//
//}