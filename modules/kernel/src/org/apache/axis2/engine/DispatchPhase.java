package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;

import java.util.ArrayList;
import java.util.Iterator;
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
        }

        AxisService service = msgContext.getAxisService();
        AxisOperation operation = msgContext.getAxisOperation();
        // If we're configured to do so, check the service for a single op...
        if (operation == null &&
                JavaUtils.isTrue(service.getParameterValue("supportSingleOperation"))) {
            Iterator ops = service.getOperations();
            // If there's exactly one, that's the one we want.  If there's more, forget it.
            if (ops.hasNext()) {
                operation = (AxisOperation)ops.next();
                if (ops.hasNext()) {
                    operation = null;
                }
            }
        }

        // If we still don't have an operation, fault.
        if (operation == null) {
            throw new AxisFault(Messages.getMessage("operationnotfoundforepr",
                                                    ((toEPR != null) ? toEPR.getAddress()
                                                            : ""), msgContext.getWSAAction()));
        }

        msgContext.setAxisOperation(operation);
        
        validateTransport(msgContext);

        loadContexts(service, msgContext);

        if (msgContext.getOperationContext() == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullOperationContext"));
        }

        if (msgContext.getServiceContext() == null) {
            throw new AxisFault(Messages.getMessage("cannotBeNullServiceContext"));
        }

        // TODO - review this
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

    private void loadContexts(AxisService service, MessageContext msgContext) throws AxisFault {
        String scope = service == null ? null : service.getScope();
        ServiceContext serviceContext = msgContext.getServiceContext();

        if ((msgContext.getOperationContext() != null)
                && (serviceContext != null)) {
            msgContext.setServiceGroupContextId(
                    ((ServiceGroupContext)serviceContext.getParent()).getId());
            return;
        }
        if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope)) {
            fillContextsFromSessionContext(msgContext);
        }

        AxisOperation axisOperation = msgContext.getAxisOperation();
//        if (axisOperation == null) {
//            return;
//        }
        OperationContext operationContext =
                axisOperation.findForExistingOperationContext(msgContext);

        if (operationContext != null) {
            // register operation context and message context
//            axisOperation.registerOperationContext(msgContext, operationContext);
            axisOperation.registerMessageContext(msgContext, operationContext);

            serviceContext = (ServiceContext) operationContext.getParent();
            ServiceGroupContext serviceGroupContext =
                    (ServiceGroupContext) serviceContext.getParent();

            msgContext.setServiceContext(serviceContext);
            msgContext.setServiceGroupContext(serviceGroupContext);
            msgContext.setServiceGroupContextId(serviceGroupContext.getId());
        } else {    // 2. if null, create new opCtxt
            operationContext = ContextFactory.createOperationContext(axisOperation, serviceContext);

            axisOperation.registerMessageContext(msgContext, operationContext);
            if (serviceContext != null) {
                // no need to added to configuration conetxt , since we are happy in
                //  storing in session context
                operationContext.setParent(serviceContext);
            } else {
                // fill the service group context and service context info
                msgContext.getConfigurationContext().fillServiceContextAndServiceGroupContext(
                        msgContext);
            }
        }
        serviceContext = msgContext.getServiceContext();
        if (serviceContext != null) {
            serviceContext.setMyEPR(msgContext.getTo());
        }
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

    private void fillContextsFromSessionContext(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getAxisService();
        if (service == null) {
            throw new AxisFault(Messages.getMessage("unabletofindservice"));
        }
        SessionContext sessionContext = msgContext.getSessionContext();
        if (sessionContext == null) {
            TransportListener listener = msgContext.getTransportIn().getReceiver();
            sessionContext = listener.getSessionContext(msgContext);
            if (sessionContext == null) {
                createAndFillContexts(service, msgContext, sessionContext);
                return;
            }
        }
        String serviceGroupName = msgContext.getAxisServiceGroup().getServiceGroupName();
        ServiceGroupContext serviceGroupContext = sessionContext.getServiceGroupContext(
                serviceGroupName);
        if (serviceGroupContext != null) {
            //setting service group context
            msgContext.setServiceGroupContext(serviceGroupContext);
            // setting Service conetxt
            msgContext.setServiceContext(
                    ContextFactory.createServiceContext(serviceGroupContext, service));
        } else {
            createAndFillContexts(service, msgContext, sessionContext);
        }
        ServiceContext serviceContext = sessionContext.getServiceContext(service);
        //found the serviceContext from session context , so adding that into msgContext
        if (serviceContext != null) {
            msgContext.setServiceContext(serviceContext);
            serviceContext.setProperty(HTTPConstants.COOKIE_STRING, sessionContext.getCookieID());
        }
    }

    private void createAndFillContexts(AxisService service,
                                       MessageContext msgContext,
                                       SessionContext sessionContext) throws AxisFault {
        ServiceGroupContext serviceGroupContext;
        AxisServiceGroup axisServiceGroup = (AxisServiceGroup) service.getParent();
        serviceGroupContext = ContextFactory.createServiceGroupContext(
                msgContext.getConfigurationContext(), axisServiceGroup);

        msgContext.setServiceGroupContext(serviceGroupContext);
        ServiceContext serviceContext =
                ContextFactory.createServiceContext(serviceGroupContext, service);
        msgContext.setServiceContext(serviceContext);
        if (sessionContext != null) {
            sessionContext.addServiceContext(serviceContext);
            sessionContext.addServiceGroupContext(serviceGroupContext);
        }
    }
}
