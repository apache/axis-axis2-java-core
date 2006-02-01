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

package org.apache.axis2.mail;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.axis2.transport.mail.SimpleMailListener;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * These tests willcheck wheather the mail transport works ok with charactor set
 * encoding changes.
 */
public class MailCharSetEncodingTest extends TestCase {

    private EndpointReference targetEPR = new EndpointReference("foo@127.0.0.1"
            + "/axis2/services/EchoXMLService/echoOMElement");

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoOMElement");

    OMElement resultElem = null;

    private AxisConfiguration engineRegistry;

    private SOAPEnvelope envelope;

    private boolean finish = false;

    ServiceContext clientServiceContext;

    ConfigurationContext clientConfigContext;

    ConfigurationContext configContext;

    public MailCharSetEncodingTest() {
        super(MailCharSetEncodingTest.class.getName());
    }

    protected void setUp() throws Exception {
        configContext = UtilsMailServer.start();

        AxisService service = Utils.createSimpleService(serviceName,
                Echo.class.getName(), operationName);
        configContext.getAxisConfiguration().addService(service);

        SimpleMailListener ml = new SimpleMailListener();

        ml.init(configContext, configContext.getAxisConfiguration()
                .getTransportIn(new QName(Constants.TRANSPORT_MAIL)));
        ml.start();

    }

    public void runTest(String value) throws Exception {
        finish = false;
        resultElem = null;
        envelope = null;
        try {
            if (clientConfigContext == null) {
                clientConfigContext = UtilsMailServer
                        .createClientConfigurationContext();
                engineRegistry = clientConfigContext.getAxisConfiguration();
            }
            AxisService clientService = new AxisService(
                    serviceName.getLocalPart());
            AxisOperation clientOperation = new OutInAxisOperation(
            );
            clientOperation.setName(operationName);
            clientOperation.setMessageReceiver(new MessageReceiver() {
                public void receive(MessageContext messageCtx) {
                    envelope = messageCtx.getEnvelope();
                }
            });
            engineRegistry.removeService(serviceName.getLocalPart());
            clientService.addOperation(clientOperation);
            engineRegistry.addService(clientService);
            clientServiceContext = Utils.fillContextInformation(clientService, clientConfigContext);


            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_MAIL);
            options.setUseSeparateListener(true);
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    resultElem = result.getResponseEnvelope();
                    finish = true;
                }

                public void onError(Exception e) {
                    log.error(e.getMessage(), e);
                    finish = true;
                }
            };
            ServiceClient sender = new ServiceClient(clientConfigContext, clientService);
            sender.setOptions(options);
            options.setTo(targetEPR);
            sender.sendReceiveNonBlocking(operationName,createEnvelope(value), callback);

            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Async response is taking too long[10s+]. Server is being shut down.");
                }
            }
            // call.close();
            assertNotNull("Result is null", resultElem);
            String result = ((OMElement) resultElem.getFirstOMChild()
                    .getNextOMSibling()).getFirstElement().getFirstElement()
                    .getText();

            assertNotNull("Result value is null", result);

            assertEquals("Expected result not received.", value, result);

        } catch (AxisFault e) {
            log.error(e, e);
            throw e;
        } catch (InterruptedException e) {
            log.error(e, e);
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            throw e;

        }
    }

    public void testSimpleString() throws Exception {
        runTest("a simple string");
    }

    public void testStringWithApostrophes() throws Exception {
        runTest("this isn't a simple string");
    }

    public void testStringWithEntities() throws Exception {
        runTest("&amp;&lt;&gt;&apos;&quot;");
    }

    public void testStringWithRawEntities() throws Exception {
        runTest("&<>'\"");
    }

    public void testStringWithLeadingAndTrailingSpaces() throws Exception {
        runTest("          centered          ");
    }

    public void testWhitespace() throws Exception {
        runTest(" \n \t "); // note: \r fails
    }

    public void testFrenchAccents() throws Exception {
        runTest("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    public void testGermanUmlauts() throws Exception {
        runTest(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    public void testWelcomeUnicode() throws Exception {
        // welcome in several languages
        runTest("Chinese (trad.) : \u6b61\u8fce  ");
    }

    public void testWelcomeUnicode2() throws Exception {
        // welcome in several languages
        runTest("Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    public void testWelcomeUnicode3() throws Exception {
        // welcome in several languages
        runTest("Japanese : \u3088\u3046\u3053\u305d");
    }

    protected void tearDown() throws Exception {
        UtilsMailServer.stop();
    }

    private OMElement createEnvelope(String text) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createText(value, text));
        method.addChild(value);

        return method;
    }
}