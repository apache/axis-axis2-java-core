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
 *
 *  Runtime state of the engine
 */
package org.apache.axis2.clientapi;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * This class is the pretty convineance class for the user without see the comlplexites of Axis2.
 */
public class Call extends InOutMEPClient {

    private HashMap properties;
    protected static OperationDescription operationTemplate;
    private MessageContext lastResponseMessage;

    /**
     * this is a convenience Class, here the Call will assume a Annoynmous Service.
     *
     * @throws AxisFault
     */

    public Call() throws AxisFault {
        super(assumeServiceContext(null));
    }

    /**
     * This is used to create call object with client home , using onky this constructor it can
     * able to engage modules  , addning client side parameters
     *
     * @param clientHome
     * @throws AxisFault
     */
    public Call(String clientHome) throws AxisFault {
        super(assumeServiceContext(clientHome));
    }

    /**
     * @param service
     * @see InOutMEPClient constructer
     */
    public Call(ServiceContext service) {
        super(service);
    }

    /**
     * Invoke the blocking/Synchronous call
     *
     * @param axisop
     * @param toSend - This should be OM Element (payload)
     * @return
     * @throws AxisFault
     */

    public OMElement invokeBlocking(String axisop, OMElement toSend) throws AxisFault {

        OperationDescription axisConfig =
                serviceContext.getServiceConfig().getOperation(new QName(axisop));
        if (axisConfig == null) {
            axisConfig = new OperationDescription(new QName(axisop));
            axisConfig.setRemainingPhasesInFlow(operationTemplate.getRemainingPhasesInFlow());
            axisConfig.setPhasesOutFlow(operationTemplate.getPhasesOutFlow());
            axisConfig.setPhasesInFaultFlow(operationTemplate.getPhasesInFaultFlow());
            axisConfig.setPhasesOutFaultFlow(operationTemplate.getPhasesOutFaultFlow());
            serviceContext.getServiceConfig().addOperation(axisConfig);
        }

//        if (axisConfig == null) {
//            axisConfig = new OperationDescription(new QName(axisop));
//            serviceContext.getServiceConfig().addOperation(axisConfig);
//        }
        MessageContext msgctx = prepareTheSystem(toSend);

        this.lastResponseMessage = super.invokeBlocking(axisConfig, msgctx);
        SOAPEnvelope resEnvelope = lastResponseMessage.getEnvelope();
        return resEnvelope.getBody().getFirstElement();
    }

    /**
     * Invoke the nonblocking/Asynchronous call
     *
     * @param axisop
     * @param toSend   -  This should be OM Element (payload)
     *                 invocation behaves accordingly
     * @param callback
     * @throws AxisFault
     */

    public void invokeNonBlocking(String axisop, OMElement toSend, Callback callback)
            throws AxisFault {
        OperationDescription axisConfig =
                serviceContext.getServiceConfig().getOperation(new QName(axisop));
        if (axisConfig == null) {
            axisConfig = new OperationDescription(new QName(axisop));
            axisConfig.setRemainingPhasesInFlow(operationTemplate.getRemainingPhasesInFlow());
            axisConfig.setPhasesOutFlow(operationTemplate.getPhasesOutFlow());
            axisConfig.setPhasesInFaultFlow(operationTemplate.getPhasesInFaultFlow());
            axisConfig.setPhasesOutFaultFlow(operationTemplate.getPhasesOutFaultFlow());
            serviceContext.getServiceConfig().addOperation(axisConfig);
        }
        MessageContext msgctx = prepareTheSystem(toSend);

        super.invokeNonBlocking(axisConfig, msgctx, callback);
    }

    /**
     * Assume the values for the ConfigurationContext and ServiceContext to make the NON WSDL cases simple.
     *
     * @return ServiceContext that has a ConfigurationContext set in and has assumed values.
     * @throws AxisFault
     */
    protected static ServiceContext assumeServiceContext(String clinetHome) throws AxisFault {
        ConfigurationContext sysContext = null;
        if (ListenerManager.configurationContext == null) {
            ConfigurationContextFactory efac = new ConfigurationContextFactory();
            sysContext = efac.buildClientConfigurationContext(clinetHome);
        } else {
            sysContext = ListenerManager.configurationContext;
        }

        //create new service
        QName assumedServiceName = new QName("AnonnoymousService");
        ServiceDescription axisService = new ServiceDescription(assumedServiceName);
        operationTemplate = new OperationDescription(new QName("TemplateOperatin"));
        axisService.addOperation(operationTemplate);
        sysContext.getAxisConfiguration().addService(axisService);
        ServiceContext service = sysContext.createServiceContext(assumedServiceName);
        return service;
    }

    /**
     * @param key
     * @return
     */
    public Object get(String key) {
        return serviceContext.getProperty(key);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public void set(String key, Object value) {
        serviceContext.getEngineContext().setProperty(key, value);
    }

    /**
     * @return
     */
    public MessageContext getLastResponseMessage() {
        return lastResponseMessage;
    }

}
