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

import javax.xml.namespace.QName;

import org.apache.axis.core.AxisFault;
import org.apache.axis.core.EngineUtils;
import org.apache.axis.core.Global;
import org.apache.axis.core.Operation;
import org.apache.axis.core.Service;
import org.apache.axis.core.SimpleGlobal;
import org.apache.axis.core.SimpleModule;
import org.apache.axis.core.SimpleOperation;
import org.apache.axis.core.SimpleService;
import org.apache.axis.core.SimpleTransport;
import org.apache.axis.core.Transport;
import org.apache.axis.core.context.MessageContext;
import org.apache.axis.core.exec.Constants;
import org.apache.axis.core.exec.ExecutionChain;
import org.apache.axis.core.exec.Phase;
import org.apache.axis.core.registry.ConcreateParameter;
import org.apache.axis.core.registry.EngineRegistry;
import org.apache.axis.core.registry.Module;
import org.apache.axis.core.registry.Parameter;
import org.apache.axis.core.registry.SimpleEngineRegistry;
import org.apache.axis.engine.registry.EchoService;
import org.apache.axis.engine.registry.MockFlow;
import org.apache.axis.providers.SimpleJavaProvider;
import org.apache.axis.providers.SyncProvider;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class Utils {
    public static EngineRegistry createMockRegistry(QName serviceName,QName operationName,QName transportName) throws AxisFault{
        EngineRegistry engineRegistry = null;
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
         
        service.setProvider(new SyncProvider(new SimpleJavaProvider()));
        
        Module m1 = new SimpleModule(new QName("","A Mdoule 1"));
        m1.setInFlow(new MockFlow("service module inflow",4));
        m1.setFaultFlow(new MockFlow("service module faultflow",1));
        service.addModule(m1);
        
        Operation operation = new SimpleOperation(operationName,service);
        operation.setInFlow(new MockFlow("inflow",4));
        
        service.addOperation(operation);
        engineRegistry.addService(service);
        //create Execution Chains
        ExecutionChain inchain = new ExecutionChain();
        inchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
        inchain.addPhase(new Phase(Constants.PHASE_GLOBAL));
        inchain.addPhase(new Phase(Constants.PHASE_SERVICE));
        EngineUtils.addHandlers(transport.getInFlow(),inchain,Constants.PHASE_TRANSPORT);
        EngineUtils.addHandlers(global.getInFlow(),inchain,Constants.PHASE_GLOBAL);
        EngineUtils.addHandlers(service.getInFlow(),inchain,Constants.PHASE_SERVICE);
        service.setInputExecutionChain(inchain);
        
        ExecutionChain outchain = new ExecutionChain();
        outchain.addPhase(new Phase(Constants.PHASE_SERVICE));
        outchain.addPhase(new Phase(Constants.PHASE_GLOBAL));
        outchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
        EngineUtils.addHandlers(service.getInFlow(),outchain,Constants.PHASE_SERVICE);
        EngineUtils.addHandlers(global.getInFlow(),outchain,Constants.PHASE_GLOBAL);
        EngineUtils.addHandlers(transport.getInFlow(),outchain,Constants.PHASE_TRANSPORT);
        service.setOutExecutionChain(outchain);
        
        ExecutionChain faultchain = new ExecutionChain();
        
        faultchain.addPhase(new Phase(Constants.PHASE_SERVICE));
        faultchain.addPhase(new Phase(Constants.PHASE_GLOBAL));
        faultchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
        
        EngineUtils.addHandlers(service.getInFlow(),faultchain,Constants.PHASE_SERVICE);
        EngineUtils.addHandlers(global.getInFlow(),faultchain,Constants.PHASE_GLOBAL);
        EngineUtils.addHandlers(transport.getInFlow(),faultchain,Constants.PHASE_TRANSPORT);
        service.setFaultExecutionChain(outchain);
        return engineRegistry;
    }
}
