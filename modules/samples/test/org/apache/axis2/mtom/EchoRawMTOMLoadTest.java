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

package org.apache.axis2.mtom;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.attachments.ByteArrayDataSource;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

public class EchoRawMTOMLoadTest extends TestCase {
    private EndpointReference targetEPR = new EndpointReference(
            AddressingConstants.WSA_TO,
            "http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis/services/EchoXMLService/echoOMElement");

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoOMElement");

    private QName transportName = new QName("http://localhost/my",
            "NullTransport");

    private AxisConfiguration engineRegistry;

    private MessageContext mc;

    private ServiceContext serviceContext;

    private ServiceDescription service;

    private boolean finish = false;
    
    byte[] expectedByteArray;

    public EchoRawMTOMLoadTest() {
        super(EchoRawMTOMLoadTest.class.getName());
    }

    public EchoRawMTOMLoadTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-enabledRepository");
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
        serviceContext = UtilServer.getConfigurationContext()
                .createServiceContext(service.getName());
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    private OMElement createEnvelope() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoOMElement", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        expectedByteArray = new byte[]{13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
                                      98};
        for (int i = 0; i < 4; i++) {
            OMElement subData = fac.createOMElement("subData", omNs);
            DataHandler dataHandler = new DataHandler(
                    new ByteArrayDataSource(expectedByteArray));
            OMText textData = new OMTextImpl(dataHandler, true);
            //OMText textData = new OMTextImpl("Thilina Gunarathne");
            subData.addChild(textData);
            data.addChild(subData);
            //System.out.println("Creating blobs "+i);
        }

        rpcWrapEle.addChild(data);
        return rpcWrapEle;
    }

    public void testEchoXMLSync() throws Exception {
        for (int i = 0; i < 10; i++) {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

            OMElement payload = createEnvelope();

            org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call();
            call.setTo(targetEPR);
            call.set(Constants.Configuration.ENABLE_MTOM,
                    Constants.VALUE_TRUE);
            call.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP, false);
            call.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            OMElement result = call.invokeBlocking(operationName
                    .getLocalPart(),
                    payload);
            OMElement ele = (OMElement) result.getFirstChild();
            OMElement ele1 = (OMElement) ele.getFirstChild();
            OMText binaryNode = (OMText) ele1.getFirstChild();
            DataHandler actualDataHandler = binaryNode.getDataHandler();
            ByteArrayInputStream inStream = (ByteArrayInputStream)actualDataHandler.getContent();
            byte[] actualByteArray = new byte[11];
            inStream.read(actualByteArray);
            assertEquals(expectedByteArray[0],actualByteArray[0]);
            assertEquals(expectedByteArray[0],actualByteArray[0]);       
            System.out.println(i);
        }
    }

}