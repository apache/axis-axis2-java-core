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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAPHeader;

import javax.xml.namespace.QName;

/**
 * By the time the control comes to this handler, the dispatching must have happened
 * so that the message context contains the AxisServiceGroup, AxisService and
 * AxisOperation.
 * This will then try to find the Contexts of ServiceGroup, Service and the Operation.
 */
public class InstanceDispatcher extends AbstractHandler {

    private static final long serialVersionUID = -1928612412157492489L;


    /**
     * Post Condition : All the Contexts must be populated.
     *
     * @param msgContext
     * @throws org.apache.axis2.AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        ServiceContext serviceContext = msgContext.getServiceContext();

        if ((msgContext.getOperationContext() != null)
                && (serviceContext != null)) {
            msgContext.setServiceGroupContextId(
                    ((ServiceGroupContext) serviceContext.getParent()).getId());

            return;
        }

        // try to extract sgcId from the message
        extractServiceGroupContextId(msgContext);
        
        //trying to get service context from Session context
        fillContextsFromSessionContext(msgContext);

        AxisOperation axisOperation = msgContext.getAxisOperation();

        // 1. look up opCtxt using mc.addressingHeaders.relatesTo[0]
        if (axisOperation == null) {
            return;
        }

        OperationContext operationContext =
                axisOperation.findForExistingOperationContext(msgContext);

        if (operationContext != null) {

            // register operation context and message context
            axisOperation.registerOperationContext(msgContext, operationContext);

            serviceContext = (ServiceContext) operationContext.getParent();
            ServiceGroupContext serviceGroupContext =
                    (ServiceGroupContext) serviceContext.getParent();

            msgContext.setServiceContext(serviceContext);
            msgContext.setServiceGroupContext(serviceGroupContext);
            msgContext.setServiceGroupContextId(serviceGroupContext.getId());
        } else {    // 2. if null, create new opCtxt
            operationContext = new OperationContext(axisOperation);

            axisOperation.registerOperationContext(msgContext, operationContext);
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
        // setting myEPR
        String transportURL = (String) msgContext.getProperty(Constants.Configuration.TRANSPORT_IN_URL);
        if (serviceContext != null) {
            if (transportURL != null) {
                serviceContext.setMyEPR(msgContext.getTo());
            }
            serviceContext.setMyEPR(msgContext.getTo());
        }
    }

    private void fillContextsFromSessionContext(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getAxisService();
        if (service == null) {
            throw new AxisFault("Service not found operation terminated !!");
        }
        SessionContext sessionContext = msgContext.getSessionContext();
        String serviceGroupContextId = msgContext.getServiceGroupContextId();
        if (serviceGroupContextId != null && sessionContext != null) {
            //setting service group context which is teken from session context
            ServiceGroupContext serviceGroupContext = sessionContext.getServiceGroupContext(
                    serviceGroupContextId);
            if (serviceGroupContext != null) {
                //setting service group context
                msgContext.setServiceGroupContext(serviceGroupContext);
                // setting Service conetxt
                msgContext.setServiceContext(serviceGroupContext.getServiceContext(service));
                return;
            }
        }
        String scope = service.getScope();

        if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope) && sessionContext != null) {
            ServiceContext serviceContext = sessionContext.getServiceContext(service);
            //found the serviceContext from session context , so adding that into msgContext
            if (serviceContext != null) {
                msgContext.setServiceContext(serviceContext);
            }
        }
    }

    private void extractServiceGroupContextId(MessageContext msgContext) throws AxisFault {
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
        if (soapHeader != null) {
            OMElement serviceGroupId = soapHeader.getFirstChildWithName(new QName(Constants.AXIS2_NAMESPACE_URI,
                    Constants.SERVICE_GROUP_ID, Constants.AXIS2_NAMESPACE_PREFIX));
            if (serviceGroupId != null) {
                String groupId = serviceGroupId.getText();
                ServiceGroupContext serviceGroupContext = msgContext.getConfigurationContext().
                        getServiceGroupContext(groupId, msgContext);
                if (serviceGroupContext == null) {
//                handleNoServiceGroupContextIDCase(msgContext);
                    throw new AxisFault("Invalid Service Group Id." + groupId);
                }
                msgContext.setServiceGroupContextId(serviceGroupId.getText());
            }
        }

    }

}
