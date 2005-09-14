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
package org.apache.axis2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.*;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;

public class Utils {

    public static void addHandler(Flow flow,
                                  Handler handler,
                                  String phaseName) {
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

    public static ServiceDescription createSimpleService(QName serviceName,
                                                         MessageReceiver messageReceiver,
                                                         String className,
                                                         QName opName) throws AxisFault {
        ServiceDescription service = new ServiceDescription(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(
                new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                        className));

        OperationDescription axisOp = new OperationDescription(opName);
        axisOp.setMessageReceiver(messageReceiver);
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

    public static ServiceDescription createSimpleService(QName serviceName,
                                                         String className,
                                                         QName opName) throws AxisFault {
        return createSimpleService(serviceName,
                new RawXMLINOutMessageReceiver(),
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
    public static void resolvePhases(AxisConfiguration axisconfig,
                                     ServiceDescription serviceDesc)
            throws AxisFault, PhaseException {
        PhaseResolver pr = new PhaseResolver(axisconfig, serviceDesc);
        pr.buildchains();
    }

    public static String getParameterValue(Parameter param) {
        if (param == null) {
            return null;
        } else {
            return (String) param.getValue();
        }
    }


    public static String[] parseRequestURLForServiceAndOperation(
            String filePart) {
        String[] values = new String[2];

        int index = filePart.lastIndexOf(Constants.REQUEST_URL_PREFIX);
        String serviceStr = null;
        if (-1 != index) {
            serviceStr =
                    filePart.substring(
                            index + Constants.REQUEST_URL_PREFIX.length() + 1);
            if ((index = serviceStr.indexOf('/')) > 0) {
                values[0] = serviceStr.substring(0, index);
                int lastIndex = serviceStr.indexOf('?');
                if (lastIndex >= 0) {
                    values[1] = serviceStr.substring(index + 1, lastIndex);
                } else {
                    values[1] = serviceStr.substring(index + 1);
                }
            } else {
                values[0] = serviceStr;
            }
        }
        return values;
    }

    public static void extractServiceGroupAndServiceInfo(String filePart, MessageContext messageContext) throws AxisFault {
        String[] values = parseRequestURLForServiceAndOperation(
                filePart);
        String serviceNameAndGroup = values[0];
        if (serviceNameAndGroup != null) {
            String[] serviceNameAndGroupStrings = serviceNameAndGroup.split(":");
            AxisConfiguration registry =
                    messageContext.getSystemContext().getAxisConfiguration();
            if (serviceNameAndGroupStrings[0] != null) {
                ServiceGroupDescription serviceGroup = registry.getServiceGroup(serviceNameAndGroupStrings[0]);
                String serviceNameStr = "";
                if (serviceNameAndGroupStrings.length == 1) {
                    // This means user has not given a service name.
                    // the notations is ...../axis2/services/<ServiceGroupName>
                    serviceNameStr = serviceNameAndGroupStrings[0];
                }
                ServiceDescription serviceDescription = registry.getService(serviceNameStr);
                if (serviceGroup != null && serviceDescription != null) {
                    messageContext.setServiceGroupDescription(serviceGroup);
                    messageContext.setServiceDescription(serviceDescription);
                }
            }
        }
    }

    public static ServiceContext fillContextInformation(OperationDescription operationDesc, ServiceDescription serviceDesc, ConfigurationContext configurationContext) throws AxisFault {
        MessageContext msgContext;
        //  2. if null, create new opCtxt
        OperationContext operationContext =
                OperationContextFactory.createOperationContext(operationDesc.getAxisSpecifMEPConstant(), operationDesc);

        //  fill the service group context and service context info
        return fillServiceContextAndServiceGroupContext(serviceDesc, configurationContext);

    }

    private static ServiceContext fillServiceContextAndServiceGroupContext(ServiceDescription serviceDesc, ConfigurationContext configurationContext) throws AxisFault {
        String serviceGroupContextId = UUIDGenerator.getUUID();
        ServiceGroupContext serviceGroupContext = new ServiceGroupContext(configurationContext,serviceDesc.getParent());
        serviceGroupContext.fillServiceContexts();
        serviceGroupContext.setId(serviceGroupContextId);
        configurationContext.registerServiceGroupContext(serviceGroupContext);
        ServiceContext serviceContext = new ServiceContext(serviceDesc, serviceGroupContext);
        return serviceContext;
    }

}
