/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.engine.registry.MockFlow;
import org.apache.axis.providers.SimpleJavaProvider;
import org.apache.axis.registry.ConcreateParameter;
import org.apache.axis.registry.EngineRegistry;
import org.apache.axis.registry.Module;
import org.apache.axis.registry.Parameter;
import org.apache.axis.registry.SimpleEngineRegistry;

/**
 * @author hemapani@opensource.lk
 */
public class EngineTest extends TestCase{
    private QName serviceName = new QName("","EchoService");
    private QName operationName = new QName("","echoVoid");
    private QName transportName = new QName("","NullTransport");
    private EngineRegistry engineRegistry;
    private MessageContext mc;
    
    public EngineTest() {
        super();
    }
    
    public EngineTest(String arg0) {
        super(arg0);
    }
    protected void setUp() throws Exception {
        Global global = new SimpleGlobal();
        global.setInFlow(new MockFlow("globel inflow",4));
        global.setOutFlow(new MockFlow("globel outflow",2));
        global.setFaultFlow(new MockFlow("globel faultflow",1));
        engineRegistry = new SimpleEngineRegistry(global);
        
        Transport transport = new SimpleTransport(transportName);
        transport.setInFlow(new MockFlow("transport inflow",4));
        transport.setOutFlow(new MockFlow("transport outflow",2));
        transport.setFaultFlow(new MockFlow("transport faultflow",1));
        engineRegistry.addTransport(transport);
        
        Service service = new SimpleService(serviceName);
        service.setInFlow(new MockFlow("service inflow",4));
        service.setOutFlow(new MockFlow("service outflow",5));
        service.setFaultFlow(new MockFlow("service faultflow",1));
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        Parameter classParam = new ConcreateParameter("className",EchoService.class.getName());
        service.addParameter(classParam);
         
        service.setProvider(new SimpleJavaProvider());
        
        Module m1 = new SimpleModule(new QName("","A Mdoule 1"));
        m1.setInFlow(new MockFlow("service module inflow",4));
        m1.setFaultFlow(new MockFlow("service module faultflow",1));
        service.addModule(m1);
        
        Operation operation = new SimpleOperation(operationName,service);
        operation.setInFlow(new MockFlow("inflow",4));
        
        service.addOperation(operation);
        engineRegistry.addService(service);
        
        mc = new MessageContext(engineRegistry);
        mc.setCurrentTansport(transportName);
        mc.setCurrentService(serviceName);
        mc.setCurrentOperation(operationName);
    }

    public void testSend()throws Exception{
        AxisEngine engine = new AxisEngine(engineRegistry);
        engine.send(mc);
    }
    public void testRecive()throws Exception{
        AxisEngine engine = new AxisEngine(engineRegistry);
        engine.recive(mc);
    }
    protected void tearDown() throws Exception {
    }

}
