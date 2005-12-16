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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
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
    public static final QName NAME = new QName("http://ws.apache.org/axis2/",
            "AddressingBasedDispatcher");
    private Log log = LogFactory.getLog(getClass());

    // TODO this logic needed to be improved, as the Dispatching is almost garentnee to fail
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        log.debug("Checking for Operation using WSAAction : " + messageContext.getWSAAction());

        String action = messageContext.getWSAAction();

        if (action != null) {
            return service.getOperationByAction(action);
        }

        return null;
    }

    // TODO this logic needed to be improved, as the Dispatching is almost garentnee to fail
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        AxisService service = null;

        if (toEPR != null) {
            String address = toEPR.getAddress();

            log.debug("Checking for Service using toEPR's address : " + address);

            if (Final.WSA_ANONYMOUS_URL.equals(address)
                    || Submission.WSA_ANONYMOUS_URL.equals(address)) {
                return null;
            }

            QName serviceName = new QName(address);
            String[] values = Utils.parseRequestURLForServiceAndOperation(address);

            log.debug("Checking for Service using toEPR : " + values[0]);

            if (values[0] != null) {
                serviceName = new QName(values[0]);

                AxisConfiguration registry =
                        messageContext.getConfigurationContext().getAxisConfiguration();

                return registry.getService(serviceName.getLocalPart());
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
    public void invoke(MessageContext msgctx) throws AxisFault {

        // first check we can dispatch using the relates to
        if (msgctx.getRelatesTo() != null) {
            log.debug("Checking RelatesTo : " + msgctx.getRelatesTo());

            String relatesTo = msgctx.getRelatesTo().getValue();

            if ((relatesTo != null) || "".equals(relatesTo)) {
                OperationContext operationContext =
                        msgctx.getConfigurationContext().getOperationContext(relatesTo);

                if (operationContext != null) {
                    msgctx.setAxisOperation(operationContext.getAxisOperation());
                    msgctx.setOperationContext(operationContext);
                    msgctx.setServiceContext((ServiceContext) operationContext.getParent());
                    msgctx.setAxisService(
                            ((ServiceContext) operationContext.getParent()).getAxisService());
                    msgctx.getAxisOperation().registerOperationContext(msgctx, operationContext);
                    msgctx.setServiceGroupContextId(
                            ((ServiceGroupContext) msgctx.getServiceContext().getParent()).getId());
                }
            }

            return;
        }

        super.invoke(msgctx);
    }
}
