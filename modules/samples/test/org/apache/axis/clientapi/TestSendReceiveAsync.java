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

package org.apache.axis.clientapi;

import java.io.InputStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.Echo;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.providers.RawXMLProvider;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jaliya
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class TestSendReceiveAsync extends TestCase {
    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("", "EchoXMLService");

    private QName operationName = new QName("http://localhost/my", "echoOMElement");

    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private MessageContext mc;

    private Thread thisThread;

    private SimpleHTTPServer sas;

    private boolean finish = false;

    private AxisService service;
    
    private ClassLoader cl;

    /**
     * @param testName
     */
    public TestSendReceiveAsync(String testName) {
        super(testName);
        cl = Thread.currentThread().getContextClassLoader();
    }

    protected void setUp() throws Exception {

        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new AxisOperation(operationName);

        service.addOperation(operation);

        Utils.createExecutionChains(service);
        UtilServer.start();
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {

        while (!finish) {
            Thread.sleep(500);
        }
        UtilServer.unDeployService(service.getName());

    }

    public void testSendReceiveAsync() throws Exception {

        SOAPEnvelope envelope = getBasicEnvelope();
        EndpointReference targetEPR =
            new EndpointReference(
                AddressingConstants.WSA_TO,
                "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis/services/EchoXMLService");
        Call call = new Call();
        call.setTo(targetEPR);
        call.setListenerTransport("http", true);

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {

                try {
                    result.getResponseEnvelope().serialize(
                        XMLOutputFactory.newInstance().createXMLStreamWriter(System.out),
                        true);
                } catch (XMLStreamException e) {
                    reportError(e);

                } finally {
                    finish = true;
                }
            }

            public void reportError(Exception e) {
                e.printStackTrace();
            }
        };
        call.sendReceiveAsync(envelope, callback);

    }

    private SOAPEnvelope getBasicEnvelope() throws Exception {

        SOAPEnvelope envelope =
            new StAXSOAPModelBuilder(
                XMLInputFactory.newInstance().createXMLStreamReader(
                    new InputStreamReader(cl.getResourceAsStream("clientapi/SimpleSOAPEnvelope.xml"))))
                .getSOAPEnvelope();

        /*   File file = new File("./target/test-classes/clientapi/SimpleSOAPEnvelope.xml");
           XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)); //put the file
        
           OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory
                   .newInstance(), xmlStreamReader);
           return (SOAPEnvelope) builder.getDocumentElement(); */
        return envelope;
    }

}
