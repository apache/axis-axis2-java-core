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
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

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
        log.debug(Messages.getMessage("checkingoperation",
                messageContext.getWSAAction()));

        String action = messageContext.getWSAAction();

        if (action != null) {
            AxisOperation axisOperation = service.getOperationByAction(action);
            AxisEndpoint axisEndpoint = service.getEndpoint((String) messageContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME));
            AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisEndpoint.getBinding().getChild(axisOperation.getName());
            messageContext.setProperty(Constants.AXIS_BINDING_OPERATION,axisBindingOperation);
            return axisOperation;
        }

        return null;
    }

    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        AxisService axisService = null;

        if (toEPR != null) {
            if (toEPR.hasAnonymousAddress()) {
                return null;
            }
            
            String address = toEPR.getAddress();
            log.debug(Messages.getMessage("checkingserviceforepr", address));

            QName serviceName;
            String[] values = Utils.parseRequestURLForServiceAndOperation(address,
                    messageContext.getConfigurationContext().getServiceContextPath());
            if (values == null) {
                return null;
            }

            log.debug(Messages.getMessage("checkingserviceforepr", values[0]));

            if (values[0] != null) {
                serviceName = new QName(values[0]);

                AxisConfiguration registry =
                        messageContext.getConfigurationContext().getAxisConfiguration();

                axisService = registry.getService(serviceName.getLocalPart());

                // If the axisService is not null we get the binding that the request came to add
                // add it as a property to the messageContext
                if (axisService != null) {
                    String endpointName = values[0].substring(values[0].indexOf(".")+1);
                    messageContext.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME, endpointName);
                }
            }
        }

        return axisService;
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

            log.debug(Messages.getMessage("checkingrelatesto",
                    relatesTo));

            if ((relatesTo != null) || "".equals(relatesTo)) {
                OperationContext operationContext =
                        msgctx.getConfigurationContext().getOperationContext(msgctx.getRelatesTo().getValue());

                if (operationContext != null) {
                    operationContext.addMessageContext(msgctx);
                    msgctx.setAxisOperation(operationContext.getAxisOperation());
                    msgctx.setOperationContext(operationContext);
                    msgctx.setServiceContext((ServiceContext) operationContext.getParent());
                    msgctx.setAxisService(
                            ((ServiceContext) operationContext.getParent()).getAxisService());
                    //InstanceDispatcher do this again , so let it go
                    //msgctx.getAxisOperation().registerOperationContext(msgctx, operationContext);
                    msgctx.setServiceGroupContextId(
                            ((ServiceGroupContext) msgctx.getServiceContext().getParent()).getId());
                }
            }

            return InvocationResponse.CONTINUE;
        }

        return super.invoke(msgctx);
    }
}
