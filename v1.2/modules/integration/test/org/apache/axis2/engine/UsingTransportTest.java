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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;


public class UsingTransportTest extends UtilServerBasedTestCase implements TestConstants {

    QName assumedServiceName = new QName("AnonymousService");


    private AxisService service = null;


    public static Test suite() {
        return getTestSetup(new TestSuite(UsingTransportTest.class));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(TestConstants.serviceName, Echo.class.getName(),
                                            TestConstants.operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(TestConstants.serviceName);
        UtilServer.unDeployClientService();
    }


    public void testValidTransport() throws AxisFault {
        String title = "testValidTransport(): ";

        OMElement payload = createEnvelope();

        ConfigurationContext configCtx =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

        ServiceClient servClient = new ServiceClient(configCtx, null);

        Options options = new Options();
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        servClient.setOptions(options);

        OperationClient opClinet = servClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        String ocType = opClinet.getClass().getName();
        System.out.println("\n" + title + "Using OperationClient [" + ocType + "]");
        opClinet.addMessageContext(prepareTheSOAPEnvelope(payload, options));
        System.out.println(title + "Invoking OperationClient [" + ocType + "] execute()");
        opClinet.execute(true);

        SOAPEnvelope result =
                opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();

        assertEquals("SOAP Version received is not compatible",
                     SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                     result.getNamespace().getNamespaceURI());
    }


    public void testInvalidTransport() throws AxisFault {
        String title = "testInvalidTransport(): ";
        OMElement payload = createEnvelope();

        ConfigurationContext configCtx =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

        ServiceClient servClient = new ServiceClient(configCtx, null);

        Options options = new Options();
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        // set the endpoint with "htp" instead of "http" 
        EndpointReference invalidTargetEPR = new EndpointReference(
                "htp://127.0.0.1:5556/axis2/services/EchoXMLService/echoOMElement");
        options.setTo(invalidTargetEPR);

        servClient.setOptions(options);

        OperationClient opClinet = servClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        String ocType = opClinet.getClass().getName();
        System.out.println(title + "Using OperationClient [" + ocType + "]");

        MessageContext msgCtx = prepareTheSOAPEnvelope(payload, options);

        opClinet.addMessageContext(msgCtx);

        try {
            System.out.println(title + "Invoking OperationClient [" + ocType + "] execute()");
            opClinet.execute(true);
        }
        catch (Exception ex) {
            String exName = ex.getClass().getName();

            System.out.println(title +
                    "Expecting an exception of type [org.apache.axis2.AxisFault].   Caught exception  [" +
                    exName + "]    error [" + ex.getMessage() + "]");
            ex.printStackTrace();

            assertTrue("Expecting an exception of type [org.apache.axis2.AxisFault]",
                       (ex instanceof org.apache.axis2.AxisFault));
        }

    }


    private MessageContext prepareTheSOAPEnvelope(OMElement toSend, Options options)
            throws AxisFault {
        MessageContext msgctx = new MessageContext();

        SOAPFactory sf = getSOAPFactory(options);
        SOAPEnvelope se = sf.getDefaultEnvelope();

        if (toSend != null) {
            se.getBody().addChild(toSend);
        }

        msgctx.setEnvelope(se);
        return msgctx;
    }


    private SOAPFactory getSOAPFactory(Options options) {
        String soapVersionURI = options.getSoapVersionURI();

        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            // if its not SOAP 1.2 just assume SOAP 1.1
            return OMAbstractFactory.getSOAP11Factory();
        }
    }


    private OMElement createEnvelope() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");

        OMElement method = fac.createOMElement("echoOMElement", omNs);

        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));

        method.addChild(value);

        return method;
    }


}
