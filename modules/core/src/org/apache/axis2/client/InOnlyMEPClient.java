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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.wsdl.WSDLConstants;

/**
 * This class handles In-Only (fire and forget) MEP
 */
public class InOnlyMEPClient extends MEPClient {

    public InOnlyMEPClient(ServiceContext service) {
        super(service, WSDLConstants.MEP_URI_IN_ONLY);
    }

    /**
     * Sends the SOAP Message and forgets about it. This is one way
     *
     * @param axisop
     * @param msgctx
     * @throws AxisFault
     */
    public void send(AxisOperation axisop, final MessageContext msgctx) throws AxisFault {
        prepareInvocation(axisop, msgctx);
        if (msgctx.getMessageID() == null) {
            String messageID = String.valueOf("uuid:" + UUIDGenerator.getUUID());
            msgctx.setMessageID(messageID);
        }
        msgctx.setServiceContext(serviceContext);

        //if the transport to use for sending is not specified, try to find it from the URL
        TransportOutDescription senderTransport = clientOptions.getSenderTransport();
        if (senderTransport == null) {
            senderTransport =
                    inferTransport(msgctx.getTo());
        }
        msgctx.setTransportOut(senderTransport);

        //initialize and set the Operation Context
        ConfigurationContext sysContext = serviceContext.getConfigurationContext();
        OperationContext operationContext = axisop.findOperationContext(msgctx, serviceContext);
        msgctx.setOperationContext(operationContext);
        operationContext.setProperties(clientOptions.getProperties());

        AxisEngine engine = new AxisEngine(sysContext);
        engine.send(msgctx);
    }

    protected void configureTransportInformation(MessageContext msgCtx) throws AxisFault {
        inferTransportOutDescription(msgCtx);
    }
}
