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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.engine.Global;
import org.apache.axis.engine.Operation;
import org.apache.axis.engine.Service;
import org.apache.axis.engine.SimpleGlobal;
import org.apache.axis.engine.SimpleModule;
import org.apache.axis.engine.SimpleOperation;
import org.apache.axis.engine.SimpleService;
import org.apache.axis.engine.SimpleTransport;
import org.apache.axis.engine.Transport;
import org.apache.axis.engine.context.MessageContext;
import org.apache.axis.engine.exec.Constants;
import org.apache.axis.engine.exec.ExecutionChain;
import org.apache.axis.engine.registry.ConcreateParameter;
import org.apache.axis.engine.registry.EchoService;
import org.apache.axis.engine.registry.EngineRegistry;
import org.apache.axis.engine.registry.MockFlow;
import org.apache.axis.engine.registry.Module;
import org.apache.axis.engine.registry.Parameter;
import org.apache.axis.engine.registry.SimpleEngineRegistry;
import org.apache.axis.providers.SimpleJavaProvider;
import org.apache.axis.providers.SyncProvider;
import org.apache.axis.transport.http.SimpleAxisServer;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class EchoTest extends AbstractTestCase{
    private QName serviceName = new QName("","EchoService");
    private QName operationName = new QName("http://ws.apache.org/axis2","echoVoid");
    private QName transportName = new QName("","NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread = null;
    private SimpleAxisServer sas;
    private int testingPort = 1234;

    public EchoTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        engineRegistry = Utils.createMockRegistry(serviceName,operationName,transportName);
        
        mc = new MessageContext(engineRegistry);
        mc.setCurrentTansport(transportName);
        mc.setCurrentService(serviceName);
        mc.setCurrentOperation(operationName);

        AxisEngine engine = new AxisEngine(engineRegistry);
        sas = new SimpleAxisServer(engine);
        sas.setServerSocket(new ServerSocket(testingPort));
        thisThread = new Thread(sas);
        thisThread.start();
    }

    protected void tearDown() throws Exception {
    	sas.stop();
    }


    public void testEchoStringServer() throws Exception{
    	File file = new File("src/samples/soap/sample1.txt");
    	FileInputStream in = new FileInputStream(file);
    	
    	Socket socket = new Socket("127.0.0.1",testingPort);
    	System.out.println("scoket created");
    	OutputStream out = socket.getOutputStream();
    	byte[]  buf = new byte[1024];
    	int index = -1;
    	while((index = in.read(buf)) > 0){
    		out.write(buf,0,index);
    	}
    	System.out.println("Message Send");
    	
    	InputStream respose = socket.getInputStream();
    	Reader rReader = new InputStreamReader(respose);
    	char[] charBuf = new char[1024];
    	while((index = rReader.read(charBuf)) > 0){
        	System.out.println(charBuf);    		
    	}
    	
    	in.close();
    	out.close();
    	rReader.close();
    	socket.close();
    }
}
