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
import java.net.ServerSocket;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.engine.OperationImpl;
import org.apache.axis.impl.engine.ServiceImpl;
import org.apache.axis.impl.providers.RawXMLProvider;
import org.apache.axis.impl.registry.ParameterImpl;
import org.apache.axis.impl.transport.http.SimpleAxisServer;
import org.apache.axis.registry.EngineRegistry;
import org.apache.axis.registry.Operation;
import org.apache.axis.registry.Parameter;
import org.apache.axis.registry.Service;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class SimpleAxisServerTest extends AbstractTestCase{
    private QName serviceName = new QName("","EchoXMLService");
    private QName operationName = new QName("http://localhost/my","echoOMElement");
    private QName transportName = new QName("http://localhost/my","NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread = null;
    private SimpleAxisServer sas;
    private int testingPort = 7777;
    private int testCount = 0;
    private AxisEngine engine;
    
    public SimpleAxisServerTest(){
        super(SimpleAxisServerTest.class.getName());
    }

    public SimpleAxisServerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        engineRegistry = Utils.createMockRegistry(serviceName,operationName,transportName);
        Service service = new ServiceImpl(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        Parameter classParam = new ParameterImpl("className",EchoXML.class.getName());
        service.addParameter(classParam);
        service.setProvider(new RawXMLProvider());
        Operation operation = new OperationImpl(operationName,service);
        
        service.addOperation(operation);
        
        ExecutionChain inchain = new ExecutionChain();
        inchain.addPhase(new Phase(Constants.PHASE_SERVICE));
        EngineUtils.addHandlers(service.getInFlow(),inchain,Constants.PHASE_SERVICE);
        service.setInputExecutionChain(inchain);
        
        ExecutionChain outchain = new ExecutionChain();
        outchain.addPhase(new Phase(Constants.PHASE_SERVICE));
        EngineUtils.addHandlers(service.getOutFlow(),outchain,Constants.PHASE_SERVICE);
        service.setOutExecutionChain(outchain);
        
        ExecutionChain faultchain = new ExecutionChain();
        
        faultchain.addPhase(new Phase(Constants.PHASE_SERVICE));
        
        EngineUtils.addHandlers(service.getFaultFlow(),faultchain,Constants.PHASE_SERVICE);
        service.setFaultExecutionChain(outchain);
        
        engineRegistry.addService(service);
        
        engine = new AxisEngine(engineRegistry);
    }

    protected void tearDown() throws Exception {
    }


    public void testEchoXMLSync() throws Exception{
        ServerSocket serverSoc = new ServerSocket(testingPort);
        sas = new SimpleAxisServer(engine);
        sas.setServerSocket(serverSoc);
        thisThread = new Thread(sas);
        thisThread.setDaemon(true);
        thisThread.start();
        sas.stop();
        Thread.sleep(1000);
        serverSoc = new ServerSocket(testingPort);
        sas = new SimpleAxisServer(engine);
        sas.setServerSocket(serverSoc);
        thisThread = new Thread(sas);
        thisThread.setDaemon(true);
        thisThread.start();
        sas.stop();            
        Thread.sleep(1000);
        serverSoc = new ServerSocket(testingPort);
        sas = new SimpleAxisServer(engine);
        sas.setServerSocket(serverSoc);
        thisThread = new Thread(sas);
        thisThread.setDaemon(true);
        thisThread.start();
        sas.stop();            
    }
}
