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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageWithServerTest extends AbstractTestCase{
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("","EchoService");
    private QName operationName = new QName("http://ws.apache.org/axis2","echoVoid");
    private QName transportName = new QName("","NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread = null;
    private SimpleHTTPReceiver sas;

    public MessageWithServerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        engineRegistry = EngineUtils.createMockRegistry(serviceName,operationName,transportName);
        sas = EngineUtils.startServer(engineRegistry);
    }

    protected void tearDown() throws Exception {
        EngineUtils.stopServer();  
        Thread.sleep(1000);
    }


    public void testEchoStringServer() throws Exception{
    	File file = getTestResourceFile("soap/soapmessage.txt");
    	FileInputStream in = new FileInputStream(file);
    	
    	Socket socket = new Socket("127.0.0.1",EngineUtils.TESTING_PORT);
    	OutputStream out = socket.getOutputStream();
    	byte[]  buf = new byte[1024];
    	int index = -1;
    	while((index = in.read(buf)) > 0){
    		out.write(buf,0,index);
    	}

    	
    	InputStream respose = socket.getInputStream();
    	Reader rReader = new InputStreamReader(respose);
    	char[] charBuf = new char[1024];
    	while((index = rReader.read(charBuf)) > 0){
    	        log.info(new String(charBuf));
    	}
    	
    	in.close();
    	out.close();

        rReader.close();
    	socket.close();
    }
}
