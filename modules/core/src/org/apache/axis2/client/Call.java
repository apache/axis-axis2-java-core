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


package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * This class is used to invoke INOUT web services and serves as a convenience
 * class to work with INOUT MEP.
 */
public class Call extends InOutMEPClient {
    protected AxisOperation axisOperationTemplate;
    protected MessageContext lastResponseMsgCtx;

    /**
     * @throws AxisFault
     */
    public Call() throws AxisFault {
        super(null);
        assumeServiceContext(null);
    }

    /**
     * @param service
     * @see InOutMEPClient constructer
     */
    public Call(ServiceContext service) {
        super(service);
    }

    /**
     * Creates call object with client home.
     *
     * @param clientHome
     * @throws AxisFault
     */
    public Call(String clientHome) throws AxisFault {
        super(null);
        assumeServiceContext(clientHome);
    }

    protected void assumeServiceContext(String clientHome) throws AxisFault {
        super.assumeServiceContext(clientHome);

        AxisService axisService = serviceContext.getAxisService();

        axisOperationTemplate = new OutInAxisOperation();
        axisOperationTemplate.setName(new QName("TemplateOperation"));

        AxisConfiguration axisConfiguration =
                serviceContext.getConfigurationContext().getAxisConfiguration();
        PhasesInfo info = axisConfiguration.getPhasesInfo();

        // to set the operation flows
        if (info != null) {
            info.setOperationPhases(axisOperationTemplate);
        }

        axisService.addOperation(axisOperationTemplate);
    }

    /**
     * Creates an operation description if it is null and copies the flows from
     * the template operation.
     *
     * @param opDesc
     * @param axisOp
     */
    protected AxisOperation createOpDescAndFillInFlowInformation(AxisOperation opDesc,
                                                                 String axisOp, int mepURL)
            throws AxisFault {
        if (opDesc == null) {

            // if the operation is not already defined we will copy the
            // created Phases from the template operation to this Operation
            opDesc = AxisOperationFactory.getAxisOperation(mepURL);
            opDesc.setName(new QName(axisOp));
            opDesc.setRemainingPhasesInFlow(axisOperationTemplate.getRemainingPhasesInFlow());
            opDesc.setPhasesOutFlow(axisOperationTemplate.getPhasesOutFlow());
            opDesc.setPhasesInFaultFlow(axisOperationTemplate.getPhasesInFaultFlow());
            opDesc.setPhasesOutFaultFlow(axisOperationTemplate.getPhasesOutFaultFlow());
            serviceContext.getAxisService().addOperation(opDesc);
        }

        return opDesc;
    }

    /**
     * Invokes the blocking/synchronous call.
     * 
     *
     * @param axisop - this is used to identify the operation on the client side explicitly.
     * @param toSend - data to be sent (OMElement).
     * @return Returns OMElement.
     * @throws AxisFault
     */
    public OMElement invokeBlocking(String axisop, OMElement toSend) throws AxisFault {
        AxisOperation opDesc = serviceContext.getAxisService().getOperation(new QName(axisop));

        opDesc = createOpDescAndFillInFlowInformation(opDesc, axisop,
                WSDLConstants.MEP_CONSTANT_OUT_IN);
        opDesc.setParent(serviceContext.getAxisService());

        MessageContext msgctx = prepareTheSOAPEnvelope(toSend);

        this.lastResponseMsgCtx = super.invokeBlocking(opDesc, msgctx);

        SOAPEnvelope resEnvelope = lastResponseMsgCtx.getEnvelope();

        return resEnvelope.getBody().getFirstElement();
    }

    /**
     * Invokes the blocking/synchronous call.
     *
     * @param axisop   - this is used to identify the operation on the client side explicitly.
     * @param envelope - data to be sent (SOAPEnvelope).
     * @return Returns SOAPEnvelope.
     * @throws AxisFault
     */
    public SOAPEnvelope invokeBlocking(String axisop, SOAPEnvelope envelope) throws AxisFault {
        AxisOperation opDesc = serviceContext.getAxisService().getOperation(new QName(axisop));

        opDesc = createOpDescAndFillInFlowInformation(opDesc, axisop,
                WSDLConstants.MEP_CONSTANT_OUT_IN);

        MessageContext msgctx = new MessageContext();
        msgctx.setConfigurationContext(serviceContext.getConfigurationContext());

        if ((envelope == null) || (envelope.getBody() == null)) {
            throw new AxisFault("SOAP envelope or SOAP Body can not be null");
        }

        msgctx.setEnvelope(envelope);
        this.lastResponseMsgCtx = super.invokeBlocking(opDesc, msgctx);

        return lastResponseMsgCtx.getEnvelope();
    }

    /**
     * Invokes the nonblocking/asynchronous call.
     *
     * @param axisop
     * @param toSend   -  data to be sent (OMElement).
     * @param callback
     * @throws AxisFault
     */
    public void invokeNonBlocking(String axisop, OMElement toSend, Callback callback)
            throws AxisFault {
        AxisOperation opDesc = serviceContext.getAxisService().getOperation(new QName(axisop));

        opDesc = createOpDescAndFillInFlowInformation(opDesc, axisop,
                WSDLConstants.MEP_CONSTANT_OUT_IN);

        MessageContext msgctx = prepareTheSOAPEnvelope(toSend);

        // call the underline implementation
        super.invokeNonBlocking(opDesc, msgctx, callback);
    }

    /**
     * Invokes the nonblocking/asynchronous call.
     *
     * @param axisop
     * @param envelope -  data to be sent (SOAPEnvelope).
     * @param callback
     * @throws AxisFault
     */
    public void invokeNonBlocking(String axisop, SOAPEnvelope envelope, Callback callback)
            throws AxisFault {
        AxisOperation opDesc = serviceContext.getAxisService().getOperation(new QName(axisop));

        opDesc = createOpDescAndFillInFlowInformation(opDesc, axisop,
                WSDLConstants.MEP_CONSTANT_OUT_IN);

        MessageContext msgctx = new MessageContext();
        msgctx.setConfigurationContext(serviceContext.getConfigurationContext());

        if ((envelope == null) || (envelope.getBody() == null)) {
            throw new AxisFault("SOAP envelope or SOAP Body can not be null");
        }

        msgctx.setEnvelope(envelope);

        // call the underline implementation
        super.invokeNonBlocking(opDesc, msgctx, callback);
    }

    /**
     * Get the MessageContext of the response.
     *
     * @return Returns message context.
     */
    public MessageContext getResponseMessageContext() {
        return lastResponseMsgCtx;
    }
}
