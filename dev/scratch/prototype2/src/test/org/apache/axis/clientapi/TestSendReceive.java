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

import java.io.File;
import java.io.FileReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReferenceType;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.Echo;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.impl.description.ParameterImpl;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.description.SimpleAxisServiceImpl;
import org.apache.axis.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;
import org.apache.axis.impl.providers.RawXMLProvider;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestSendReceive extends AbstractTestCase {
    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("", "EchoXMLService");

    private QName operationName = new QName("http://localhost/my", "echoOMElement");

    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private EngineRegistry engineRegistry;

    private MessageContext mc;

    private Thread thisThread = null;

    private SimpleHTTPReceiver sas;

    public TestSendReceive() {
        super(TestSendReceive.class.getName());
    }

    public TestSendReceive(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        engineRegistry = EngineUtils.createMockRegistry(serviceName, operationName, transportName);

        AxisService service = new SimpleAxisServiceImpl(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        Parameter classParam = new ParameterImpl("className", Echo.class.getName());
        service.addParameter(classParam);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);

        service.addOperation(operation);

        EngineUtils.createExecutionChains(service);
        engineRegistry.addService(service);

        sas = EngineUtils.startServer(engineRegistry);
    }

    protected void tearDown() throws Exception {
        sas.stop();
    }

    public void testSendReceive() throws Exception {
        
        SOAPEnvelope envelope = getBasicEnvelope();
        EndpointReferenceType targetEPR = new EndpointReferenceType(
                AddressingConstants.WSA_TO,"http://127.0.0.1:"+EngineUtils.TESTING_PORT+"/axis/services/EchoXMLService");
        Call call = new Call();
        call.setTo(targetEPR);
        SOAPEnvelope responseEnv = call.sendReceive(envelope);

        SimpleOMSerializer sOMSerializer = new SimpleOMSerializer();
        sOMSerializer.serialize(responseEnv, XMLOutputFactory.newInstance().createXMLStreamWriter(
                System.out));

    }

    private SOAPEnvelope getBasicEnvelope() throws Exception {
        File file = new File("./target/test-classes/clientapi/SimpleSOAPEnvelope.xml");
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)); //put the file

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory
                .newInstance(), xmlStreamReader);
        return (SOAPEnvelope) builder.getDocumentElement();
    }

}