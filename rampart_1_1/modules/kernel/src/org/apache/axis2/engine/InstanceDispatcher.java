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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.context.*;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

/**
 * By the time the control comes to this handler, the dispatching must have happened
 * so that the message context contains the AxisServiceGroup, AxisService and
 * AxisOperation.
 * This will then try to find the Contexts of ServiceGroup, Service and the Operation.
 */
public class InstanceDispatcher extends AbstractHandler {
    private static final QName SERVICE_GROUP_QNAME = new QName(Constants.AXIS2_NAMESPACE_URI,
            Constants.SERVICE_GROUP_ID, Constants.AXIS2_NAMESPACE_PREFIX);

    /**
     * Post Condition : All the Contexts must be populated.
     *
     * @param msgContext
     * @throws org.apache.axis2.AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        ServiceContext serviceContext = msgContext.getServiceContext();

        if ((msgContext.getOperationContext() != null)
                && (serviceContext != null)) {
            msgContext.setServiceGroupContextId(
                    ((ServiceGroupContext) serviceContext.getParent()).getId());

            return InvocationResponse.CONTINUE;        
        }

        // try to extract sgcId from the message
        extractServiceGroupContextId(msgContext);

        //trying to get service context from Session context
        fillContextsFromSessionContext(msgContext);

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
            operationContext = new OperationContext(axisOperation);

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
        // setting myEPR
        String transportURL = (String) msgContext.getProperty(Constants.Configuration.TRANSPORT_IN_URL);
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
        String scope = service.getScope();
        if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope)) {
            if (sessionContext == null) {
                Object obj = msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
                if (obj != null) {
                    sessionContext = (SessionContext) getSessionContext((HttpServletRequest) obj);
                    msgContext.setSessionContext(sessionContext);
                }
            }
        }
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

        if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope) && sessionContext != null) {
            ServiceContext serviceContext = sessionContext.getServiceContext(service);
            //found the serviceContext from session context , so adding that into msgContext
            if (serviceContext != null) {
                msgContext.setServiceContext(serviceContext);
            }
        }
    }

    private void extractServiceGroupContextId(MessageContext msgContext) throws AxisFault {
        if (!msgContext.isHeaderPresent()) {
            return;
        }
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
        if (soapHeader != null) {
            OMElement serviceGroupId = soapHeader.getFirstChildWithName(SERVICE_GROUP_QNAME);
            if (serviceGroupId != null) {
                String groupId = serviceGroupId.getText();
                ServiceGroupContext serviceGroupContext = msgContext.getConfigurationContext().
                        getServiceGroupContext(groupId, msgContext);
                if (serviceGroupContext == null) {
                    throw new AxisFault(Messages.getMessage(
                            "invalidservicegrouoid", groupId));
                }
                msgContext.setServiceGroupContextId(serviceGroupId.getText());
            }
        }
    }

    private Object getSessionContext(HttpServletRequest httpServletRequest) {
        Object sessionContext =
                httpServletRequest.getSession(true).getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
        if (sessionContext == null) {
            sessionContext = new SessionContext(null);
            httpServletRequest.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                    sessionContext);
        }
        return sessionContext;
    }
}
