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
import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.providers.RawXMLProvider;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;

public class SimpleAxisServerTest extends AbstractTestCase{
    private QName serviceName = new QName("","EchoXMLService");
    private QName operationName = new QName("http://localhost/my","echoOMElement");
    private QName transportName = new QName("http://localhost/my","NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread = null;
    private SimpleHTTPReceiver sas;
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
        engineRegistry = EngineUtils.createMockRegistry(serviceName,operationName,transportName);
        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);
        
        service.addOperation(operation);
        
        EngineUtils.createExecutionChains(service);
        engineRegistry.addService(service);
        
        engine = new AxisEngine(engineRegistry);
    }

    protected void tearDown() throws Exception {
    }


    public void testEchoXMLSync() throws Exception{
        EngineUtils.startServer(engineRegistry);
        EngineUtils.stopServer();  
        Thread.sleep(1000);
        EngineUtils.startServer(engineRegistry);
        EngineUtils.stopServer();  
        Thread.sleep(1000);
        EngineUtils.startServer(engineRegistry);
        EngineUtils.stopServer();  
    }
}
