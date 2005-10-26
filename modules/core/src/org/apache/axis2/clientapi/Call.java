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

package org.apache.axis2.clientapi;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * This class should be used only to invoke INOUT web services and will serve as a more convenient
 * class to work with INOUT MEP.
 */
public class Call extends InOutMEPClient {

    protected static AxisOperation axisOperationTemplate;
    protected MessageContext lastResponseMessage;

    /**
     * @throws AxisFault
     */

    public Call() throws AxisFault {
        super(assumeServiceContext(null));
    }

    /**
     * This is used to create call object with client home , using only this constructor it can
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
     * @param axisop - this will be used to identify the operation in the client side, without dispatching
     * @param toSend - This should be OM Element (payload)
     * @return
     * @throws AxisFault
     */

    public OMElement invokeBlocking(String axisop, OMElement toSend)
            throws AxisFault {

        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(new QName(axisop));
        opDesc = createOpDescAndFillInFlowInformation(opDesc,axisop,WSDLConstants.MEP_CONSTANT_OUT_IN);
        opDesc.setParent(serviceContext.getAxisService());
        MessageContext msgctx = prepareTheSOAPEnvelope(toSend);

        this.lastResponseMessage = super.invokeBlocking(opDesc, msgctx);
        SOAPEnvelope resEnvelope = lastResponseMessage.getEnvelope();
        return resEnvelope.getBody().getFirstElement();
    }

    /**
     * Invoke the blocking/Synchronous call
     *
     * @param axisop - this will be used to identify the operation in the client side, without dispatching
     * @param envelope - This should be SOAPEnvelope
     * @return
     * @throws AxisFault
     */
    public SOAPEnvelope invokeBlocking(String axisop, SOAPEnvelope envelope)
            throws AxisFault {

        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(new QName(axisop));
        opDesc = createOpDescAndFillInFlowInformation(opDesc,axisop, WSDLConstants.MEP_CONSTANT_OUT_IN);

        MessageContext msgctx = new MessageContext(serviceContext.getConfigurationContext());
        if(envelope == null || envelope.getBody() == null){
            throw new AxisFault("SOAP envelope or SOAP Body can not be null");
        }
        msgctx.setEnvelope(envelope);

        this.lastResponseMessage = super.invokeBlocking(opDesc, msgctx);
        return lastResponseMessage.getEnvelope();
    }

    /**
     * Invoke the nonblocking/Asynchronous call
     *
     * @param axisop
     * @param toSend   -  This should be OM Element (payload)
     *                 invocation behaves accordingly
     * @param callback
     * @throws org.apache.axis2.AxisFault
     */

    public void invokeNonBlocking(
            String axisop,
            OMElement toSend,
            Callback callback)
            throws AxisFault {
        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(new QName(axisop));
        opDesc = createOpDescAndFillInFlowInformation(opDesc,axisop,WSDLConstants.MEP_CONSTANT_OUT_IN);
        MessageContext msgctx = prepareTheSOAPEnvelope(toSend);
        //call the underline implementation
        super.invokeNonBlocking(opDesc, msgctx, callback);
    }
    /**
     * Invoke the nonblocking/Asynchronous call
     *
     * @param axisop
     * @param envelope   -  This should be a SOAP Envelope
     *                 invocation behaves accordingly
     * @param callback
     * @throws org.apache.axis2.AxisFault
     */

    public void invokeNonBlocking(
            String axisop,
            SOAPEnvelope envelope,
            Callback callback)
            throws AxisFault {
        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(new QName(axisop));
        opDesc = createOpDescAndFillInFlowInformation(opDesc,axisop,WSDLConstants.MEP_CONSTANT_OUT_IN);

        MessageContext msgctx = new MessageContext(serviceContext.getConfigurationContext());
        if(envelope == null || envelope.getBody() == null){
            throw new AxisFault("SOAP envelope or SOAP Body can not be null");
        }
        msgctx.setEnvelope(envelope);
        //call the underline implementation
        super.invokeNonBlocking(opDesc, msgctx, callback);
    }

    /**
     * This method create a operation desc if it null and copy the flows from the template operation
     * @param opDesc
     * @param axisOp
     */
    protected AxisOperation createOpDescAndFillInFlowInformation(
            AxisOperation opDesc,
            String axisOp , int mepURL) throws AxisFault {
        if (opDesc == null) {
            //if the operation is not alrady define we will copy the
            //crated Phases from the templete operation to the this Operation

//            opDesc = new AxisOperation(new QName(axisOp));
            opDesc = AxisOperationFactory.getOperetionDescription(mepURL);
            opDesc.setName(new QName(axisOp));
            opDesc.setRemainingPhasesInFlow(
                    axisOperationTemplate.getRemainingPhasesInFlow());
            opDesc.setPhasesOutFlow(axisOperationTemplate.getPhasesOutFlow());
            opDesc.setPhasesInFaultFlow(
                    axisOperationTemplate.getPhasesInFaultFlow());
            opDesc.setPhasesOutFaultFlow(
                    axisOperationTemplate.getPhasesOutFaultFlow());
            serviceContext.getAxisService().addOperation(opDesc);
        }
        return opDesc;
    }

    /**
     * Assume the values for the ConfigurationContext and ServiceContext to make the NON WSDL cases simple.
     *
     * @return ServiceContext that has a ConfigurationContext set in and has assumed values.
     * @throws org.apache.axis2.AxisFault
     */
    protected static ServiceContext assumeServiceContext(String clientHome)
            throws AxisFault {
        ConfigurationContext sysContext = null;
        //we are trying to keep one configuration Context at the Client side. That make it easier to
        //manage the TransportListeners. But using the static referance is bit crude!.
        if (ListenerManager.configurationContext == null) {
            ConfigurationContextFactory efac =
                    new ConfigurationContextFactory();
            sysContext = efac.buildClientConfigurationContext(clientHome);
            // ListenerManager.configurationContext = sysContext;
        } else {
            sysContext = ListenerManager.configurationContext;
        }
        QName assumedServiceName = new QName("AnonymousService");
        AxisService axisService =sysContext.getAxisConfiguration().getService("AnonymousService");
        if(axisService == null) {
            //we will assume a Service and operations
            axisService = new AxisService(assumedServiceName);
//        axisOperationTemplate = new AxisOperation(new QName("TemplateOperation"));
            axisOperationTemplate = new   OutInAxisOperation(new QName("TemplateOperation"));

            PhasesInfo info =((AxisConfigurationImpl)sysContext.getAxisConfiguration()).getPhasesinfo();
            //to set the operation flows
            if(info != null){
                info.setOperationPhases(axisOperationTemplate);
            }
            axisService.addOperation(axisOperationTemplate);
            sysContext.getAxisConfiguration().addService(axisService);
        }
        return axisService.getParent().getServiceGroupContext(sysContext).getServiceContext(
                assumedServiceName.getLocalPart());
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
     */
    public void set(String key, Object value) {
        serviceContext.getConfigurationContext().setProperty(key, value);
    }

    /**
     * @return
     */
    public MessageContext getLastResponseMessage() {
        return lastResponseMessage;
    }

}
