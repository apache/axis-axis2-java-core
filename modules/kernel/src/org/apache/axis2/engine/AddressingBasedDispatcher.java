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
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Dispatcher based on the WS-Addressing properties.
 */
public class AddressingBasedDispatcher extends AbstractDispatcher implements AddressingConstants {

    /**
     * Field NAME
     */
    public static final String NAME = "AddressingBasedDispatcher";
    private static final Log log = LogFactory.getLog(AddressingBasedDispatcher.class);

    // TODO this logic needed to be improved, as the Dispatching is almost guaranteed to fail
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(
                    messageContext.getLogIDString() + " " + Messages.getMessage("checkingoperation",
                                                                                messageContext.getWSAAction()));
        }
        String action = messageContext.getWSAAction();

        if (action != null) {
            return service.getOperationByAction(action);
        }

        return null;
    }

    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();

        if ((toEPR == null) || (toEPR.hasAnonymousAddress())) {
            return null;
        }

        AxisService service = null;
        String address = toEPR.getAddress();
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(messageContext.getLogIDString() + " " +
                    Messages.getMessage("checkingserviceforepr", address));
        }

        ConfigurationContext configurationContext = messageContext.getConfigurationContext();
        String[] values = 
                Utils.parseRequestURLForServiceAndOperation(address,
                                                            configurationContext.
                                                                    getServiceContextPath());
        if (values == null) {
            return null;
        }

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(messageContext.getLogIDString() + " " +
                    Messages.getMessage("checkingserviceforepr", values[0]));
        }

        if (values[0] != null) {
            AxisConfiguration registry =
                    configurationContext.getAxisConfiguration();

            service = registry.getService(values[0]);

            // If the axisService is not null we get the binding that the request came to and
            // add it as a property to the messageContext
            if (service != null) {
                Map endpoints = service.getEndpoints();
                if (endpoints != null) {
                    if (endpoints.size() == 1) {
                        messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                                   endpoints.get(
                                                           service.getEndpointName()));
                    } else {
                        String endpointName = values[0].substring(values[0].indexOf(".") + 1);
                        messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME,
                                                   endpoints.get(endpointName));
                    }
                }
            }
        }

        return service;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * @param msgctx
     * @throws org.apache.axis2.AxisFault
     */
    public InvocationResponse invoke(MessageContext msgctx) throws AxisFault {
        // first check we can dispatch using the relates to
        if (msgctx.getRelatesTo() != null) {
            String relatesTo = msgctx.getRelatesTo().getValue();

            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(msgctx.getLogIDString() + " " + Messages.getMessage("checkingrelatesto",
                                                                              relatesTo));
            }
            if (relatesTo != null && !"".equals(relatesTo)) {
                OperationContext operationContext =
                        msgctx.getConfigurationContext()
                                .getOperationContext(relatesTo);

                if (operationContext != null) {
//                    if(operationContext.isComplete()){
//                        // If the dispatch happens because of the RelatesTo and the mep is complete
//                        // we should throw a more descriptive fault.
//                        throw new AxisFault(Messages.getMessage("duplicaterelatesto",relatesTo));
//                    }
                    msgctx.setAxisOperation(operationContext.getAxisOperation());
                    msgctx.setOperationContext(operationContext);
                    msgctx.setServiceContext((ServiceContext) operationContext.getParent());
                    msgctx.setAxisService(
                            ((ServiceContext) operationContext.getParent()).getAxisService());

                    // TODO : Is this necessary here?
                    msgctx.getAxisOperation().registerMessageContext(msgctx, operationContext);

                    msgctx.setServiceGroupContextId(
                            ((ServiceGroupContext) msgctx.getServiceContext().getParent()).getId());

                    if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                        log.debug(msgctx.getLogIDString() +
                                " Dispatched successfully on the RelatesTo. operation=" +
                                operationContext.getAxisOperation());
                    }
                    return InvocationResponse.CONTINUE;
                }
            }
        }
        return super.invoke(msgctx);
    }
}
