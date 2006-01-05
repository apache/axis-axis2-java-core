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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * Message Sender is similar to the Call. Call is just a wrapper that provides a simple API.
 */
public class MessageSender extends InOnlyMEPClient {
    protected static AxisOperation axisOperationTemplate;

    public MessageSender() throws AxisFault {
        this((String) null);
    }

    /**
     * Constructs a Message Sender from a Service Context.
     *
     * @param service
     */
    public MessageSender(ServiceContext service) {
        super(service);
    }

    /**
     * Constructs a Message Sender from a specified repository.
     *
     * @param clientHome repository location
     * @throws AxisFault
     */
    public MessageSender(String clientHome) throws AxisFault {
        super(null);
        assumeServiceContext(clientHome);
    }

    /**
     * create a default service Context if the users are not interested in the lower levels of control
     *
     * @throws AxisFault
     */
    protected void assumeServiceContext(String clientHome) throws AxisFault {
        super.assumeServiceContext(clientHome);
        axisOperationTemplate = new OutOnlyAxisOperation(new QName("TemplateOperation"));

        AxisService axisService = serviceContext.getAxisService();
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
     * Sends a SOAP elvelope created from an OMElement.
     *
     * @param opName
     * @param toSend
     * @throws AxisFault
     */
    public void send(String opName, OMElement toSend) throws AxisFault {
        SOAPEnvelope envelope = createDefaultSOAPEnvelope();

        if (toSend != null) {
            envelope.getBody().addChild(toSend);
        }

        this.send(opName, envelope);
    }

    public void send(String opName, SOAPEnvelope soapEnvelope) throws AxisFault {
        if (soapEnvelope == null) {
            throw new AxisFault("Can not send null SOAP envelope");
        }

        AxisOperation axisOp = serviceContext.getAxisService().getOperation(new QName(opName));

        if (axisOp == null) {

            // todo I just assumed mep is alwas in-out , this has to improve : Deepal
            axisOp = new OutOnlyAxisOperation(new QName(opName));
            serviceContext.getAxisService().addOperation(axisOp);
            axisOp = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_ONLY);
            axisOp.setName(new QName(opName));
            axisOp.setRemainingPhasesInFlow(axisOperationTemplate.getRemainingPhasesInFlow());
            axisOp.setPhasesOutFlow(axisOperationTemplate.getPhasesOutFlow());
            axisOp.setPhasesInFaultFlow(axisOperationTemplate.getPhasesInFaultFlow());
            axisOp.setPhasesOutFaultFlow(axisOperationTemplate.getPhasesOutFaultFlow());
            serviceContext.getAxisService().addOperation(axisOp);
        }

        MessageContext msgctx = new MessageContext();
        msgctx.setConfigurationContext(serviceContext.getConfigurationContext());

        msgctx.setEnvelope(soapEnvelope);
        super.send(axisOp, msgctx);
    }
}
