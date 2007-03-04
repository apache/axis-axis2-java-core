package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;

import java.util.ArrayList;
import java.util.List;

/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, softwar
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

public class DispatchPhase extends Phase {

    public DispatchPhase() {
    }

    public DispatchPhase(String phaseName) {
        super(phaseName);
    }

    public void checkPostConditions(MessageContext msgContext) throws AxisFault {
        EndpointReference toEPR = msgContext.getTo();

        if (msgContext.getAxisService() == null) {
            throw new AxisFault(Messages.getMessage("servicenotfoundforepr",
                                                    ((toEPR != null) ? toEPR.getAddress() : "")));
        } else if (msgContext.getAxisOperation() == null) {
            throw new AxisFault(Messages.getMessage("operationnotfoundforepr",
                                                    ((toEPR != null) ? toEPR.getAddress()
                                                            : ""), msgContext.getWSAAction()));
        }

        validateTransport(msgContext);
        if (msgContext.getOperationContext() == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullOperationContext"));
        }

        if (msgContext.getServiceContext() == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullServiceContext"));
        }

        if ((msgContext.getAxisOperation() == null) && (msgContext.getOperationContext() != null)) {
            msgContext.setAxisOperation(msgContext.getOperationContext().getAxisOperation());
        }

        if ((msgContext.getAxisService() == null) && (msgContext.getServiceContext() != null)) {
            msgContext.setAxisService(msgContext.getServiceContext().getAxisService());
        }

        //TODO: The same thing should probably happen for a IN-OUT if addressing is enabled and the replyTo/faultTo are not anonymous 
        if (msgContext.getAxisOperation().getMessageExchangePattern()
                .equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY)) {
            Object requestResponseTransport =
                    msgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
            if (requestResponseTransport != null) {
                ((RequestResponseTransport) requestResponseTransport)
                        .acknowledgeMessage(msgContext);
            }
        } else if (msgContext.getAxisOperation().getMessageExchangePattern()
                .equals(WSDL20_2004_Constants.MEP_URI_IN_OUT))
        {   // OR, if 2 way operation but the response is intended to not use the response channel of a 2-way transport
            // then we don't need to keep the transport waiting.
            Object requestResponseTransport =
                    msgContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
            if (requestResponseTransport != null) {
                if (AddressingHelper.isReplyRedirected(msgContext) &&
                        AddressingHelper.isFaultRedirected(msgContext)) {
                    ((RequestResponseTransport) requestResponseTransport)
                            .acknowledgeMessage(msgContext);
                }
            }
        }


        ArrayList operationChain = msgContext.getAxisOperation().getRemainingPhasesInFlow();
        msgContext.setExecutionChain((ArrayList) operationChain.clone());
    }

    /**
     * To check wether the incoming request has come in valid transport , simpley the transports
     * that service author wants to expose
     *
     * @param msgctx
     */
    private void validateTransport(MessageContext msgctx) throws AxisFault {
        AxisService service = msgctx.getAxisService();
        if (service.isEnableAllTransports()) {
            return;
        } else {
            List trs = service.getExposedTransports();
            String incommingTrs = msgctx.getIncomingTransportName();
            for (int i = 0; i < trs.size(); i++) {
                String tr = (String) trs.get(i);
                if (incommingTrs != null && incommingTrs.equals(tr)) {
                    return;
                }
            }
        }
        EndpointReference toEPR = msgctx.getTo();
        throw new AxisFault(Messages.getMessage("servicenotfoundforepr",
                                                ((toEPR != null) ? toEPR.getAddress() : "")));
    }
}
