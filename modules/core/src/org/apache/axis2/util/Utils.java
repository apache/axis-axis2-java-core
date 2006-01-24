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
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.*;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class Utils {

    public static void addHandler(Flow flow, Handler handler, String phaseName) {
        HandlerDescription handlerDesc = new HandlerDescription();
        PhaseRule rule = new PhaseRule(phaseName);

        handlerDesc.setRules(rule);
        handler.init(handlerDesc);
        handlerDesc.setHandler(handler);
        flow.addHandler(handlerDesc);
    }

    public static MessageContext createOutMessageContext(MessageContext inMessageContext) {
        MessageContext newmsgCtx = new MessageContext();

        newmsgCtx.setConfigurationContext(inMessageContext.getConfigurationContext());
        newmsgCtx.setSessionContext(inMessageContext.getSessionContext());
        newmsgCtx.setTransportIn(inMessageContext.getTransportIn());
        newmsgCtx.setTransportOut(inMessageContext.getTransportOut());

        Options oldOptions =
                inMessageContext.getOptions();

        newmsgCtx.setMessageID(UUIDGenerator.getUUID());
        newmsgCtx.setTo(oldOptions.getReplyTo());
        newmsgCtx.setFaultTo(oldOptions.getFaultTo());
        newmsgCtx.setFrom(oldOptions.getTo());
        newmsgCtx.setRelatesTo(
                new RelatesTo(
                        oldOptions.getMessageId(),
                        AddressingConstants.Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE));
        newmsgCtx.setWSAAction(oldOptions.getAction());
        newmsgCtx.setOperationContext(inMessageContext.getOperationContext());
        newmsgCtx.setServiceContext(inMessageContext.getServiceContext());
        newmsgCtx.setProperty(MessageContext.TRANSPORT_OUT,
                inMessageContext.getProperty(MessageContext.TRANSPORT_OUT));
        newmsgCtx.setProperty(Constants.OUT_TRANSPORT_INFO,
                inMessageContext.getProperty(Constants.OUT_TRANSPORT_INFO));

        // Setting the charater set encoding
        newmsgCtx.setProperty(MessageContext.CHARACTER_SET_ENCODING,
                inMessageContext.getProperty(MessageContext.CHARACTER_SET_ENCODING));
        newmsgCtx.setDoingREST(inMessageContext.isDoingREST());
        newmsgCtx.setDoingMTOM(inMessageContext.isDoingMTOM());
        newmsgCtx.setServerSide(inMessageContext.isServerSide());
        newmsgCtx.setServiceGroupContextId(inMessageContext.getServiceGroupContextId());

        return newmsgCtx;
    }

    public static AxisService createSimpleService(QName serviceName, String className, QName opName)
            throws AxisFault {
        return createSimpleService(serviceName, new RawXMLINOutMessageReceiver(), className,
                opName);
    }

    public static AxisService createSimpleServiceforClient(QName serviceName, String className, QName opName)
            throws AxisFault {
        return createSimpleServiceforClient(serviceName, new RawXMLINOutMessageReceiver(), className,
                opName);
    }

    public static AxisService createSimpleService(QName serviceName,
                                                  MessageReceiver messageReceiver, String className, QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS, className));

        AxisOperation axisOp = new InOutAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLService.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public static AxisService createSimpleServiceforClient(QName serviceName,
                                                           MessageReceiver messageReceiver,
                                                           String className,
                                                           QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS, className));

        AxisOperation axisOp = new OutInAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLService.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public static ServiceContext fillContextInformation(AxisService axisService,
                                                        ConfigurationContext configurationContext) {

        // 2. if null, create new opCtxt
        // fill the service group context and service context info
        return fillServiceContextAndServiceGroupContext(axisService, configurationContext);
    }

    private static ServiceContext fillServiceContextAndServiceGroupContext(AxisService axisService,
                                                                           ConfigurationContext configurationContext) {
        String serviceGroupContextId = UUIDGenerator.getUUID();
        ServiceGroupContext serviceGroupContext = new ServiceGroupContext(configurationContext,
                (AxisServiceGroup) axisService.getParent());

        serviceGroupContext.setId(serviceGroupContextId);
        configurationContext.registerServiceGroupContext(serviceGroupContext);

        return new ServiceContext(axisService, serviceGroupContext);
    }

    /**
     * Break a full path into pieces
     *
     * @param path
     * @return an array where element [0] always contains the service, and element 1, if not null, contains
     *         the path after the first element. all ? parameters are discarded.
     */
    public static String[] parseRequestURLForServiceAndOperation(String path) {
        String[] values = new String[2];

        // TODO. This is kind of brittle. Any service with the name /services would cause fun.
        int index = path.lastIndexOf(Constants.REQUEST_URL_PREFIX);
        String service;

        if (-1 != index) {
            int serviceStart = index + Constants.REQUEST_URL_PREFIX.length();

            service = path.substring(serviceStart + 1);

            int queryIndex = service.indexOf('?');

            if (queryIndex > 0) {
                service = service.substring(0, queryIndex);
            }

            int operationIndex = service.indexOf('/');

            if (operationIndex > 0) {
                values[0] = service.substring(0, operationIndex);
                values[1] = service.substring(operationIndex + 1);
            } else {
                values[0] = service;
            }
        }

        return values;
    }

    public static ConfigurationContext getNewConfigurationContext(String repositry)
            throws Exception {
        File file = new File(repositry);
        if (!file.exists()) {
            throw new Exception("repository directory " + file.getAbsolutePath()
                    + " does not exists");
        }
        File axis2xml = new File(file, "axis.xml");
        String axis2xmlString = null;
        if(axis2xml.exists()){
            axis2xmlString = axis2xml.getName();
        }
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(file.getAbsolutePath(),axis2xmlString);
    }

    public static String getParameterValue(Parameter param) {
        if (param == null) {
            return null;
        } else {
            return (String) param.getValue();
        }
    }

    /**
     * To get the name of the module , where archive name is combination of module name + its version
     * The format of the module version will be like follow
     * moduleName-00.0000 as an exmple addressing-01.0001.aar
     */

    private static String getModuleName(String moduleName) {
        char version_seperator = '-';
        int version_index = moduleName.indexOf(version_seperator);
        if (version_index > 0) {
            return moduleName.substring(0, version_index);
        } else {
            return moduleName;
        }
    }

    private static String getModuleVersion(String moduleName) {
        char version_seperator = '-';
        int version_index = moduleName.indexOf(version_seperator);
        if (version_index > 0) {
            return moduleName.substring(version_index + 1, moduleName.length());
        } else {
            return null;
        }
    }


    public static QName getModuleName(String name, String versionID) {
        String moduleName ;
        if(versionID!=null && !versionID.equals("")){
           moduleName = name + "-" + versionID;
        } else {
            moduleName = name;
        }
        return new QName(moduleName);
    }

    public static void calculateDefaultModuleVersion(HashMap modules, AxisConfiguration axisConfig) {
        Iterator allModules = modules.values().iterator();
        HashMap defaultModules = new HashMap();
        while (allModules.hasNext()) {
            AxisModule axisModule = (AxisModule) allModules.next();
            QName moduleName = axisModule.getName();
            String moduleNameString = getModuleName(moduleName.getLocalPart());
            String moduleVersionString = getModuleVersion(moduleName.getLocalPart());
            String currentDefaultVerison = (String) defaultModules.get(moduleNameString);
            if (currentDefaultVerison != null) {
                if (isLatest(moduleVersionString, currentDefaultVerison)) {
                    defaultModules.put(moduleNameString, moduleVersionString);
                }
            } else {
                defaultModules.put(moduleNameString, moduleVersionString);
            }

        }
        Iterator def_mod_itr = defaultModules.keySet().iterator();
        while (def_mod_itr.hasNext()) {
            String moduleName = (String) def_mod_itr.next();
            axisConfig.addDefaultModuleVersion(moduleName, (String) defaultModules.get(moduleName));
        }
    }

    public static boolean isLatest(String moduleVersion, String currentDefaultVersion) {
        float m_version = Float.parseFloat(moduleVersion);
        float m_c_vresion = Float.parseFloat(currentDefaultVersion);
        return m_version > m_c_vresion;
    }
}
