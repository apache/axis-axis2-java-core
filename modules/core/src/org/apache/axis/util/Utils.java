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

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.ParameterImpl;
import org.apache.axis.description.PhaseRule;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.axis.receivers.AbstractMessageReceiver;
import org.apache.axis.receivers.RawXMLINOutMessageRecevier;
import org.apache.wsdl.WSDLService;

public class Utils {
    
    public static void addHandler(Flow flow,Handler handler,String phaseName){
        HandlerDescription handlerDesc = new HandlerDescription();
        PhaseRule rule = new PhaseRule(phaseName);
        handlerDesc.setRules(rule);
        handler.init(handlerDesc);
        handlerDesc.setHandler(handler);
        flow.addHandler(handlerDesc);
    }

   
//    public static void addPhasesToServiceFromFlow(
//        ServiceContext serviceContext,
//        String phaseName,
//        Flow flow,
//        int flowtype)
//        throws AxisFault {
//                ArrayList faultchain = new ArrayList();
//                Phase p = new Phase(Constants.PHASE_SERVICE);
//                faultchain.add(p);
//                addHandlers(flow, p);
//                serviceContext.setPhases(faultchain, flowtype);
//    }

//    public static void createExecutionChains(ServiceContext serviceContext) throws AxisFault {
//        ServiceDescription service = serviceContext.getServiceConfig();
//        addPhasesToServiceFromFlow(
//            serviceContext,
//            Constants.PHASE_SERVICE,
//            service.getInFlow(),
//            AxisConfiguration.INFLOW);
//        addPhasesToServiceFromFlow(
//            serviceContext,
//            Constants.PHASE_SERVICE,
//            service.getOutFlow(),
//            AxisConfiguration.OUTFLOW);
//        addPhasesToServiceFromFlow(
//            serviceContext,
//            Constants.PHASE_SERVICE,
//            service.getFaultInFlow(),
//            AxisConfiguration.FAULT_IN_FLOW);
//    }

    public static ServiceDescription createSimpleService(
        QName serviceName,
        MessageReceiver messageReceiver,
        String className,
        QName opName) {
        ServiceDescription service = new ServiceDescription(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS, className));
        
        OperationDescription axisOp = new OperationDescription(opName);
        axisOp.setMessageReciever(messageReceiver);
        axisOp.setStyle(WSDLService.STYLE_RPC);
        service.addOperation(axisOp);
        return service;
    }

//    public static ServiceContext createServiceContext(
//        ServiceDescription service,
//        ConfigurationContext engineContext)
//        throws AxisFault {
//        ServiceContext serviceContext = new ServiceContext(service, engineContext);
//        createExecutionChains(serviceContext);
//        return serviceContext;
//    }

    public static ServiceDescription createSimpleService(
        QName serviceName,
        String className,
        QName opName) {
        return createSimpleService(
            serviceName,
            new RawXMLINOutMessageRecevier(),
            className,
            opName);
    }

//    public static void addHandlers(Flow flow, Phase phase) throws AxisFault {
//        if (flow != null) {
//            int handlerCount = flow.getHandlerCount();
//            for (int i = 0; i < handlerCount; i++) {
//                phase.addHandler(flow.getHandler(i).getHandler());
//            }
//        }
//    }
    public static void resolvePhases(AxisConfiguration axisconfig,ServiceDescription serviceDesc) throws AxisFault, PhaseException{
        PhaseResolver pr = new PhaseResolver(axisconfig,serviceDesc);
        pr.buildchains();
    }
    
    public static String getParameterValue(Parameter param){
        if(param == null){
            return null;
        }else{
            return (String)param.getValue();
        }
    }
    
    public static void configureMessageContextForHTTP(String contextType,String soapAction, MessageContext msgCtx) throws AxisFault{
        if(Constants.SOAP.MTOM_CONTENT_TYPE.equals(contextType)){
            if(Constants.VALUE_TRUE.equals(msgCtx.getProperty(Constants.Configuration.ENABLE_MTOM))){
                msgCtx.setProperty(Constants.Configuration.DO_MTOM,Constants.VALUE_TRUE);
            }else{
                throw new AxisFault("MTOTM Not supported");
            }
        }else  if(Constants.SOAP.SOAP_12_CONTENT_TYPE.equals(contextType)){
            //TODO what to do with 1.2 for REST
        }else  if(contextType != null && contextType.indexOf(Constants.SOAP.SOAP_11_CONTENT_TYPE) > -1){
            if((soapAction == null || soapAction.length() == 0) 
            && Constants.VALUE_TRUE.equals(msgCtx.getProperty(Constants.Configuration.ENABLE_REST))){
                msgCtx.setProperty(Constants.Configuration.DO_REST,Constants.VALUE_TRUE);
            }
        }
    
    }
    
    
}
