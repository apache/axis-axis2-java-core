/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.engine;

//todo

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.*;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.testUtils.SimpleJavaProvider;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.Socket;

public class MessageWithServerTest extends AbstractTestCase {
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("", "EchoService");
    private QName operationName = new QName("http://ws.apache.org/axis2", "echoVoid");
    private QName transportName = new QName("", "NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread;
    private SimpleHTTPServer sas;

    public MessageWithServerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        AxisService service = new AxisService(serviceName);
        service.setInFlow(new MockFlow("service inflow", 4));
        service.setOutFlow(new MockFlow("service outflow", 5));
        service.setFaultFlow(new MockFlow("service faultflow", 1));
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);

        service.setProvider(new SimpleJavaProvider());

        AxisModule m1 = new AxisModule(new QName("", "A Mdoule 1"));
        m1.setInFlow(new MockFlow("service module inflow", 4));
        m1.setFaultFlow(new MockFlow("service module faultflow", 1));
        service.addModule(m1.getName());

        AxisOperation operation = new SimpleAxisOperationImpl(operationName);
        service.addOperation(operation);

        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }


    public void testEchoStringServer() throws Exception {
        File file = getTestResourceFile("soap/soapmessage.txt");
        FileInputStream in = new FileInputStream(file);

        Socket socket = new Socket("127.0.0.1", EngineUtils.TESTING_PORT);
        OutputStream out = socket.getOutputStream();
        byte[] buf = new byte[1024];
        int index = -1;
        while ((index = in.read(buf)) > 0) {
            out.write(buf, 0, index);
        }


        InputStream respose = socket.getInputStream();
        Reader rReader = new InputStreamReader(respose);
        char[] charBuf = new char[1024];
        while ((index = rReader.read(charBuf)) > 0) {
            log.info(new String(charBuf));
        }

        in.close();
        out.close();

        rReader.close();
        socket.close();
    }
}
