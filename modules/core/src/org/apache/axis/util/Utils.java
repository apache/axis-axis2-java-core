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
package org.apache.axis.util;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.description.ParameterImpl;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.engine.SimplePhase;
import org.apache.axis.receivers.AbstractMessageReceiver;
import org.apache.axis.receivers.RawXMLINOutMessageRecevier;

public class Utils {

    public static void addHandler(Flow flow, Handler handler) {
        HandlerMetadata hmd = new HandlerMetadata();
        hmd.setHandler(handler);
        flow.addHandler(hmd);
    }
    public static void addPhasesToServiceFromFlow(
        ServiceContext serviceContext,
        String phaseName,
        Flow flow,
        int flowtype)
        throws AxisFault {
        ArrayList faultchain = new ArrayList();
        SimplePhase p = new SimplePhase(Constants.PHASE_SERVICE);
        faultchain.add(p);
        addHandlers(flow, p);
        serviceContext.setPhases(faultchain, flowtype);
    }

    public static void createExecutionChains(ServiceContext serviceContext)
        throws AxisFault {
        AxisService service = serviceContext.getServiceConfig();
        addPhasesToServiceFromFlow(
            serviceContext,
            Constants.PHASE_SERVICE,
            service.getInFlow(),
            EngineConfiguration.INFLOW);
        addPhasesToServiceFromFlow(
            serviceContext,
            Constants.PHASE_SERVICE,
            service.getOutFlow(),
            EngineConfiguration.OUTFLOW);
        addPhasesToServiceFromFlow(
            serviceContext,
            Constants.PHASE_SERVICE,
            service.getFaultInFlow(),
            EngineConfiguration.FAULT_IN_FLOW);
    }

    public static AxisService createSimpleService(
        QName serviceName,
        MessageReceiver messageReceiver,
        String className) {
        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(
            new ParameterImpl(
                AbstractMessageReceiver.SERVICE_CLASS,
            className));
        service.setMessageReceiver(messageReceiver);
        return service;
    }
    
    public static ServiceContext createServiceContext(AxisService service) throws AxisFault{
        ServiceContext serviceContext = new ServiceContext(service);
        createExecutionChains(serviceContext);
        return serviceContext;
    }

    public static AxisService createSimpleService(
         QName serviceName, String className) {
             return createSimpleService(serviceName,new RawXMLINOutMessageRecevier(),className);   
     }

    public static void addHandlers(Flow flow, SimplePhase phase)
        throws AxisFault {
        if (flow != null) {
            int handlerCount = flow.getHandlerCount();
            for (int i = 0; i < handlerCount; i++) {
                phase.addHandler(flow.getHandler(i).getHandler());
            }
        }
    }

}
