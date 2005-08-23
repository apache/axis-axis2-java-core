package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.util.MyInOutMEPClient;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

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

public class SOAPversionTest extends TestCase {
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

    protected void setUp() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
        serviceContext =
                UtilServer.getConfigurationContext().createServiceContext(
                        service.getName());

    }

    public void testSOAP11() throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/intregrationRepo");
        inOutMEPClient.setSoapVersionURI(
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        inOutMEPClient.setTo(targetEPR);
        inOutMEPClient.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        SOAPEnvelope result =
                inOutMEPClient.invokeBlockingWithEnvelopeOut(
                        operationName.getLocalPart(), payload);
        assertEquals("SOAP Version received is not compatible",
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                result.getNamespace().getName());
        inOutMEPClient.close();
    }

    public void testSOAP12() throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/intregrationRepo");
        inOutMEPClient.setSoapVersionURI(
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        inOutMEPClient.setTo(targetEPR);
        inOutMEPClient.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        SOAPEnvelope result =
                inOutMEPClient.invokeBlockingWithEnvelopeOut(
                        operationName.getLocalPart(), payload);
        assertEquals("SOAP Version received is not compatible",
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                result.getNamespace().getName());


        inOutMEPClient.close();
    }

    public void testSOAPfault() throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/intregrationRepo");
        inOutMEPClient.setSoapVersionURI(
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        inOutMEPClient.setTo(targetEPR);
        inOutMEPClient.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        SOAPEnvelope result =
                inOutMEPClient.invokeBlockingWithEnvelopeOut(
                        operationName.getLocalPart(), payload);
//        assertEquals("SOAP Version received is not compatible", SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, result.getNamespace().getName());
        try {
            OMOutputImpl output = new org.apache.axis2.om.impl.OMOutputImpl(System.out, false);
            result.serializeWithCache(output);
            output.flush();
        } catch (XMLStreamException e) {
            log.info(e.getMessage());
        }

        inOutMEPClient.close();
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


}
