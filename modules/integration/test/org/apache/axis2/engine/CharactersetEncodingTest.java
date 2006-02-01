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
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Testing character encoding support
 */
public class CharactersetEncodingTest extends TestCase implements TestConstants {

    private Log log = LogFactory.getLog(getClass());

    private AxisService service;

    public CharactersetEncodingTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "chunking-enabledRepository");

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


    private void runTest(String value, String expected) {

        try {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
            OMElement payload = fac.createOMElement("echoOMElement", omNs);
            OMElement text = fac.createOMElement("Text", omNs);
            text.addChild(fac.createText(text, value));
            payload.addChild(text);

//            Call call = new Call(
//                    Constants.TESTING_PATH + "chunking-enabledRepository");
            Options options = new Options();
//            call.setClientOptions(options);
            options.setProperty(MessageContext.CHARACTER_SET_ENCODING, "utf-16");

            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
//            OMElement resultElem = call.invokeBlocking(operationName.getLocalPart(), payload);

            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(Constants.TESTING_PATH + "chunking-enabledRepository",null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);

            OMElement resultElem = sender.sendReceive(payload);


            assertNotNull("Result is null", resultElem);
            String result = ((OMElement) resultElem.getFirstOMChild()).getText();

            assertNotNull("Result value is null", result);

            assertEquals("Expected result not received.", expected, result);

//            call.close();

        } catch (AxisFault e) {
            log.error(e, e);
            assertFalse("Failure in processing", true);
        }
    }


    private void runtest(String value) throws Exception {
        runTest(value, value);
    }

    public void testSimpleString() throws Exception {
        runtest("a simple string");
    }

    public void testStringWithApostrophes() throws Exception {
        runtest("this isn't a simple string");
    }

    public void testStringWithEntities() throws Exception {
        runTest("&amp;&lt;&gt;&apos;&quot;", "&amp;&lt;&gt;&apos;&quot;");
    }

    public void testStringWithRawEntities() throws Exception {
        runTest("&<>'\"", "&<>'\"");
    }

    public void testStringWithLeadingAndTrailingSpaces() throws Exception {
        runtest("          centered          ");
    }

    public void testWhitespace() throws Exception {
        runtest(" \n \t "); // note: \r fails
    }

    public void testFrenchAccents() throws Exception {
        runtest("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    public void testGermanUmlauts() throws Exception {
        runtest(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    public void testWelcomeUnicode() throws Exception {
        // welcome in several languages
        runtest(
                "Chinese (trad.) : \u6b61\u8fce  ");
    }

    public void testWelcomeUnicode2() throws Exception {
        // welcome in several languages
        runtest(
                "Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    public void testWelcomeUnicode3() throws Exception {
        // welcome in several languages
        runtest(
                "Japanese : \u3088\u3046\u3053\u305d");
    }

}
