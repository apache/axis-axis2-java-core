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
import org.apache.axis2.context.*;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.handlers.AbstractHandler;

import javax.xml.namespace.QName;

/**
 * This the base class for all dispatchers, it is a Handler which has a one
 * traget, that is to find the Service a given SOAP message is targeted to.
 * <p/>
 * Axis2 service dispatching is model via a Chain of diapatchers, each trying to
 * Diaptach but let go without throwing a execption in case they fail.
 */
public abstract class AbstractDispatcher extends AbstractHandler {
    /**
     * Field NAME
     */
    public static final QName NAME =
            new QName("http://axis.ws.apache.org",
                    "AbstractDispatcher");

    /**
     * Constructor Dispatcher
     */
    private ConfigurationContext engineContext;

    public AbstractDispatcher() {
        init(new HandlerDescription(NAME));
    }

    //just to put the parent
    public abstract void initDispatcher();

    /**
     * This is final, obivously not for overiding
     *
     * @param msgctx
     * @throws org.apache.axis2.AxisFault
     */
    public final void invoke(MessageContext msgctx) throws AxisFault {

        // first check we can dispatch using the relates to
        if (msgctx.getRelatesTo() != null) {
            String relatesTo = msgctx.getRelatesTo().getValue();
            if (relatesTo != null || "".equals(relatesTo)) {
                OperationContext operationContext = msgctx.getSystemContext().getOperationContext(relatesTo);
                if (operationContext != null) {
                    msgctx.setAxisOperation(operationContext.getAxisOperation());
                    msgctx.setOperationContext(operationContext);
                    msgctx.setServiceContext((ServiceContext) operationContext.getParent());
                    msgctx.setAxisService(((ServiceContext) operationContext.getParent()).getAxisService());
                    msgctx.getAxisOperation().registerOperationContext(msgctx, operationContext);
                    msgctx.setServiceGroupContextId(((ServiceGroupContext) msgctx.getServiceContext().getParent()).getId());
                }
            }
            return;
        }


        AxisService axisService = msgctx.getAxisService();
        if (axisService == null) {
            axisService = findService(msgctx);
            if (axisService != null) {
                msgctx.setAxisService(axisService);
                // TODO Chinthaka : set the Service Group Context to the message Context
            }
        }

        if (msgctx.getAxisService() != null && msgctx.getAxisOperation() == null) {
            AxisOperation axisOperation = findOperation(axisService, msgctx);
            if (axisOperation != null) {
                msgctx.setAxisOperation(axisOperation);
            }
        }
    }

    /**
     * Give the diaptacher turn to find the Service
     *
     * @param messageContext
     * @return
     * @throws AxisFault
     */
    public abstract AxisService findService(
            MessageContext messageContext) throws AxisFault;

    /**
     * Give the diaptacher turn to find the Operation
     *
     * @param service
     * @param messageContext
     * @return
     * @throws AxisFault
     */
    public abstract AxisOperation findOperation(
            AxisService service, MessageContext messageContext) throws AxisFault;

}
