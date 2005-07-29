package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.util.MyInOutMEPClient;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.io.File;

import junit.framework.TestCase;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public class FaultHandlingTest extends TestCase{
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
    protected String testResourceDir = "test-resources";


    private boolean finish = false;

    /**
     * @param testName
     */
    public FaultHandlingTest(String testName) {
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
        serviceContext =
                UtilServer.getConfigurationContext().createServiceContext(service.getName());

    }

//    public void testInvalidSOAPMessage() throws AxisFault, XMLStreamException {
//        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
//
//        SOAPEnvelope soapEnvelope = fac.createSOAPEnvelope();
//        fac.createSOAPHeader(soapEnvelope);
//        fac.createSOAPHeader(soapEnvelope);
//        fac.createSOAPBody(soapEnvelope);
//
//        MyInOutMEPClient inOutMEPClient = getMyInOutMEPClient();
//
//        SOAPEnvelope result =
//                inOutMEPClient.invokeBlockingWithEnvelopeOut(operationName.getLocalPart(), soapEnvelope);
//
//        OMOutputImpl output = new OMOutputImpl(System.out, false);
//        result.serialize(output);
//        output.flush();
//
//        inOutMEPClient.close();
//    }

    private MyInOutMEPClient getMyInOutMEPClient() throws AxisFault {
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient();
        inOutMEPClient.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        inOutMEPClient.setTo(targetEPR);
        inOutMEPClient.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        return inOutMEPClient;
    }

    private SOAPEnvelope createEnvelope(String fileName) throws Exception {
        if (fileName == "" || fileName == null) {
            throw new Exception("A SOAP file name must be provided !!");
        }
        XMLStreamReader parser = XMLInputFactory.newInstance()
                .createXMLStreamReader(new FileReader(getTestResourceFile(fileName)));

        return (SOAPEnvelope) new StAXSOAPModelBuilder(parser, null).getDocumentElement();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

}
