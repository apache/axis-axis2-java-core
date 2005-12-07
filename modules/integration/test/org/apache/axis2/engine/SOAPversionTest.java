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

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.MyInOutMEPClient;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class SOAPversionTest extends TestCase implements TestConstants {

    private Log log = LogFactory.getLog(getClass());
    QName assumedServiceName = new QName("AnonymousService");


    private AxisService service;

    private boolean finish = false;

    protected void setUp() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }


    public void testSOAP11() throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/integrationRepo");
        Options options = new Options();
        inOutMEPClient.setClientOptions(options);
        options.setSoapVersionURI(
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);

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
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/integrationRepo");
        Options options = new Options();
        inOutMEPClient.setClientOptions(options);
        options.setSoapVersionURI(
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);

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
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/integrationRepo");
        Options options = new Options();
        inOutMEPClient.setClientOptions(options);
        options.setSoapVersionURI(
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);

        SOAPEnvelope result =
                inOutMEPClient.invokeBlockingWithEnvelopeOut(
                        operationName.getLocalPart(), payload);
//        assertEquals("SOAP Version received is not compatible", SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI, result.getNamespace().getName());

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
