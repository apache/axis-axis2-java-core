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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.description.MockFlow;
import org.apache.axis.description.SimpleAxisOperationImpl;
import org.apache.axis.providers.SimpleJavaProvider;
import org.apache.axis.transport.http.SimpleHTTPReceiver;

public class EngineUtils {
    public static final int TESTING_PORT = 4444;
    public static final String FAILURE_MESSAGE = "Intentional Faliure";
    private static int index = 0; 
    private static SimpleHTTPReceiver sas;
    
    public static void addHandlers(Flow flow,Phase phase) throws AxisFault{
        if(flow != null){
            int handlerCount = flow.getHandlerCount();
            for(int i = 0;i<handlerCount;i++){
                phase.addHandler(flow.getHandler(i).getHandler());
            }
        }
    }
    
    public static synchronized SimpleHTTPReceiver startServer(EngineRegistry engineRegistry) throws IOException{
        ServerSocket serverSoc = null;
        if(sas == null){
        }else{
            sas.stop();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        serverSoc = new ServerSocket(TESTING_PORT);
        sas = new SimpleHTTPReceiver(engineRegistry);

        sas.setServerSocket(serverSoc);
        Thread thisThread = new Thread(sas);
        thisThread.setDaemon(true);
        thisThread.start();
        return sas;
    }
    
    public static synchronized void stopServer() throws IOException{
        if(sas != null){
            sas.stop();
            sas = null;
        }
    }
    
    
    public static void addHandler(Flow flow, Handler handler){
        HandlerMetaData hmd = new HandlerMetaData();
        hmd.setName(new QName("",String.valueOf(index)));
        index++;
        hmd.setHandler(handler);
        flow.addHandler(hmd);
    }
    
    public static void createExecutionChains(AxisService service) throws AxisFault{
        addPhasesToServiceFromFlow(service,Constants.PHASE_SERVICE,service.getInFlow(),EngineRegistry.INFLOW);
        addPhasesToServiceFromFlow(service,Constants.PHASE_SERVICE,service.getOutFlow(),EngineRegistry.OUTFLOW);
        addPhasesToServiceFromFlow(service,Constants.PHASE_SERVICE,service.getFaultFlow(),EngineRegistry.FAULTFLOW);
    }
    public static EngineRegistry createMockRegistry(QName serviceName,QName operationName,QName transportName) throws AxisFault{
        EngineRegistry engineRegistry = null;
        AxisGlobal global = new AxisGlobal();
        engineRegistry = new org.apache.axis.engine.EngineRegistryImpl(global);
        

        
        AxisService service = new AxisService(serviceName);
        service.setInFlow(new MockFlow("service inflow",4));
        service.setOutFlow(new MockFlow("service outflow",5));
        service.setFaultFlow(new MockFlow("service faultflow",1));
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);
        
        service.setProvider(new SimpleJavaProvider());
        
        AxisModule m1 = new AxisModule(new QName("","A Mdoule 1"));
        m1.setInFlow(new MockFlow("service module inflow",4));
        m1.setFaultFlow(new MockFlow("service module faultflow",1));
        service.addModule(m1.getName());
        
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);
        service.addOperation(operation);
        
        engineRegistry.addService(service);
        //create Execution Chains
        addPhasesToServiceFromFlow(service,Constants.PHASE_SERVICE,service.getInFlow(),EngineRegistry.INFLOW);
        addPhasesToServiceFromFlow(service,Constants.PHASE_SERVICE,service.getOutFlow(),EngineRegistry.OUTFLOW);
        addPhasesToServiceFromFlow(service,Constants.PHASE_SERVICE,service.getFaultFlow(),EngineRegistry.FAULTFLOW);
        return engineRegistry;
    }
    
    public static void addPhasesToServiceFromFlow(AxisService service, String phaseName, Flow flow,int flowtype) throws AxisFault{
        ArrayList faultchain = new ArrayList();
        Phase p = new Phase(Constants.PHASE_SERVICE);
        faultchain.add(p);
        EngineUtils.addHandlers(flow,p);
        service.setPhases(faultchain,flowtype);
    }

}
