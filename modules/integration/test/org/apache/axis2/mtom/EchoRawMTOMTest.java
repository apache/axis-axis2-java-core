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

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.attachments.utils.ImageDataSource;
import org.apache.ws.commons.attachments.utils.ImageIO;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.llom.OMTextImpl;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;

import javax.activation.DataHandler;
import java.awt.*;
import java.io.InputStream;

public class EchoRawMTOMTest extends TestCase implements TestConstants {


    private Log log = LogFactory.getLog(getClass());

    private AxisService service;

    private OMTextImpl expectedTextData;

    private boolean finish = false;

    public EchoRawMTOMTest() {
        super(EchoRawMTOMTest.class.getName());
    }

    public EchoRawMTOMTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-enabledRepository");
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    protected OMElement createEnvelope() throws Exception {

        DataHandler expectedDH;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoOMElement", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        Image expectedImage;
        expectedImage =
                new ImageIO()
                        .loadImage(getResourceAsStream("org/apache/axis2/mtom/test.jpg"));
        ImageDataSource dataSource = new ImageDataSource("test.jpg",
                expectedImage);
        expectedDH = new DataHandler(dataSource);
        expectedTextData = new OMTextImpl(expectedDH, true, fac);
        data.addChild(expectedTextData);
        rpcWrapEle.addChild(data);
        return rpcWrapEle;

    }

    public void testEchoXMLASync() throws Exception {
        OMElement payload = createEnvelope();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(MessageContext.CHARACTER_SET_ENCODING, MessageContext.UTF_16);

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                SOAPEnvelope envelope = result.getResponseEnvelope();

                OMElement ele = (OMElement) envelope.getBody().getFirstElement().getFirstOMChild();
                OMText binaryNode = (OMText) ele.getFirstOMChild();

                // to the assert equal
                compareWithCreatedOMText(binaryNode);
                finish = true;
            }

            public void onError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }
        };
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo",null);
        ServiceClient sender = new ServiceClient(configContext, null);
        options.setAction(Constants.AXIS2_NAMESPACE_URI+"/"+operationName.getLocalPart());
        sender.setOptions(options);

        sender.sendReceiveNonblocking(payload, callback);

        int index = 0;
        while (!finish) {
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response take too long to complete");
            }
        }
    }

    public void testEchoXMLSync() throws Exception {
        OMElement payload = createEnvelope();
        Options options = new Options();
//        options.setProperty(MessageContext.CHARACTER_SET_ENCODING, "UTF-16");
        //options.setTimeOutInMilliSeconds(-1);
        //options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(Integer.MAX_VALUE));
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo",null);
        ServiceClient sender = new ServiceClient(configContext,null);
        options.setAction(Constants.AXIS2_NAMESPACE_URI+"/"+operationName.getLocalPart());
        sender.setOptions(options);
        options.setTo(targetEPR);
        OMElement result = sender.sendReceive(payload);

        // result.serializeAndConsume(new
        // OMOutput(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out)));
        OMElement ele = (OMElement) result.getFirstOMChild();
        OMText binaryNode = (OMText) ele.getFirstOMChild();

        // to the assert equal
        compareWithCreatedOMText(binaryNode);

        // Save the image
        DataHandler actualDH;
        actualDH = (DataHandler) binaryNode.getDataHandler();
        new ImageIO().loadImage(actualDH.getDataSource()
                .getInputStream());
//        FileOutputStream imageOutStream = new FileOutputStream("target/testout.jpg");
//        new ImageIO().saveImage("image/jpeg", actualObject, imageOutStream);

    }

    protected InputStream getResourceAsStream(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(path);
    }

    protected void compareWithCreatedOMText(OMText actualTextData) {
        String originalTextValue = expectedTextData.getText();
        String returnedTextValue = actualTextData.getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }
}