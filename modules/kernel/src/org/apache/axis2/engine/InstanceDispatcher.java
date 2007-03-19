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


package org.apache.axis2.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.namespace.QName;

/**
 * By the time the control comes to this handler, the dispatching must have happened
 * so that the message context contains the AxisServiceGroup, AxisService and
 * AxisOperation.
 * This will then try to find the Contexts of ServiceGroup, Service and the Operation.
 */
public class InstanceDispatcher extends AbstractHandler {
    private static final QName SERVICE_GROUP_QNAME = new QName(Constants.AXIS2_NAMESPACE_URI,
                                                               Constants.SERVICE_GROUP_ID,
                                                               Constants.AXIS2_NAMESPACE_PREFIX);

    /**
     * Post Condition : All the Contexts must be populated.
     *
     * @param msgContext MessageContext
     * @throws org.apache.axis2.AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getAxisService();
        String scope = service == null ? null : service.getScope();
        ServiceContext serviceContext = msgContext.getServiceContext();

        if ((msgContext.getOperationContext() != null)
                && (serviceContext != null)) {
            msgContext.setServiceGroupContextId(
                    ((ServiceGroupContext) serviceContext.getParent()).getId());

            return InvocationResponse.CONTINUE;
        }
        if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope)) {
            fillContextsFromSessionContext(msgContext);
        } else if (Constants.SCOPE_SOAP_SESSION.equals(scope)) {
            extractServiceGroupContextId(msgContext);
        }

        AxisOperation axisOperation = msgContext.getAxisOperation();
        // 1. look up opCtxt using mc.addressingHeaders.relatesTo[0]
        if (axisOperation == null) {
            return InvocationResponse.CONTINUE;
        }
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
        return InvocationResponse.CONTINUE;
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

    private void extractServiceGroupContextId(MessageContext msgContext) throws AxisFault {
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
        if (soapHeader != null) {
            OMElement serviceGroupId = soapHeader.getFirstChildWithName(SERVICE_GROUP_QNAME);
            if (serviceGroupId != null) {
                String groupId = serviceGroupId.getText();
                ServiceGroupContext serviceGroupContext = msgContext.getConfigurationContext().
                        getServiceGroupContextFromSoapSessionTable(groupId, msgContext);
                if (serviceGroupContext == null) {
                    throw new AxisFault(Messages.getMessage(
                            "invalidservicegrouoid", groupId));
                }
                msgContext.setServiceGroupContextId(serviceGroupId.getText());
            }
        }
    }
}
