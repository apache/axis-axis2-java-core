/**
 * Copyright 2001-2004 The Apache Software Foundation. <p/>Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at <p/>
 * http://www.apache.org/licenses/LICENSE-2.0 <p/>Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.axis.clientapi;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.SimpleAxisOperationImpl;
import org.apache.axis.engine.Echo;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.providers.RawXMLProvider;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.FileReader;

public class TestSendReceive extends AbstractTestCase {
    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("", "EchoXMLService");

    private QName operationName = new QName("http://localhost/my", "echoOMElement");

    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private EngineRegistry engineRegistry;

    private MessageContext mc;

    private Thread thisThread;

    private SimpleHTTPServer sas;

    public TestSendReceive() {
        super(TestSendReceive.class.getName());
    }

    public TestSendReceive(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        engineRegistry = EngineUtils.createMockRegistry(serviceName, operationName, transportName);

        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);

        service.addOperation(operation);

        EngineUtils.createExecutionChains(service);
        engineRegistry.addService(service);

        sas = EngineUtils.startServer(engineRegistry);
    }

    protected void tearDown() throws Exception {
        EngineUtils.stopServer();
    }

    public void testSendReceive() throws Exception {

        SOAPEnvelope envelope = getBasicEnvelope();
        EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + EngineUtils.TESTING_PORT + "/axis/services/EchoXMLService");
        Call call = new Call();
        call.setTo(targetEPR);
        SOAPEnvelope responseEnv = call.sendReceive(envelope);
        responseEnv.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out), true);

    }


    private SOAPEnvelope getBasicEnvelope() throws Exception {

        SOAPEnvelope envelope = new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(getTestResourceFile("clientapi/SimpleSOAPEnvelope.xml")))).getSOAPEnvelope();
        return envelope;
    }

}