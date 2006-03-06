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

package org.apache.axis2.swa;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.llom.OMTextImpl;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class EchoRawSwATest extends TestCase {

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoSwAService");

    private QName operationName = new QName("echoAttachment");

    private ServiceContext serviceContext;

    private AxisService service;

    private boolean finish = false;

    private OMTextImpl expectedTextData;

    public EchoRawSwATest() {
        super(EchoRawSwATest.class.getName());
    }

    public EchoRawSwATest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-enabledRepository");
        service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(
                        AbstractMessageReceiver.SERVICE_CLASS, EchoSwA.class
                                .getName()));
        AxisOperation axisOp = new OutInAxisOperation(operationName);
        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setStyle(WSDLService.STYLE_DOC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    public void testEchoXMLSync() throws Exception {
        Socket socket = new Socket("127.0.0.1", 5555);
        OutputStream outStream = socket.getOutputStream();
        InputStream inStream = socket.getInputStream();
        InputStream requestMsgInStream = getResourceAsStream("/org/apache/axis2/swa/swainput.bin");
        int data;
        while ((data = requestMsgInStream.read()) != -1) {
            outStream.write(data);
        }
        outStream.flush();
        socket.shutdownOutput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
        StringBuffer sb = new StringBuffer();

        String response = reader.readLine();
        while (null != response) {
            try {
                sb.append(response.trim());
                response = reader.readLine();
            } catch (SocketException e) {
                break;
            }
        }

        assertTrue(sb.toString().indexOf(
                "Apache Axis2 - The NExt Generation Web Services Engine") > 0);
        assertTrue(sb.toString().indexOf("multipart/related") > 0);
    }

    private InputStream getResourceAsStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }

    private void compareWithCreatedOMText(OMText actualTextData) {
        String originalTextValue = expectedTextData.getText();
        String returnedTextValue = actualTextData.getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }
}
