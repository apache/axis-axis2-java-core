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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This the base class for all dispatchers. A dispatcher's task is
 * to find the service for an incoming SOAP message.
 * <p/>
 * In Axis2, a chain of dispatchers is setup. Each tries to
 * dispatch and returns without throwing an exception, in case, it fails.
 */
public abstract class AbstractDispatcher extends AbstractHandler {

    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://ws.apache.org/axis2/", "AbstractDispatcher");
    private Log log = LogFactory.getLog(getClass());

    public AbstractDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * Called by Axis Engine to find the operation.
     *
     * @param service
     * @param messageContext
     * @return Returns AxisOperation.
     * @throws AxisFault
     */
    public abstract AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault;

    /**
     * Called by Axis Engine to find the service.
     *
     * @param messageContext
     * @return Returns AxisService.
     * @throws AxisFault
     */
    public abstract AxisService findService(MessageContext messageContext) throws AxisFault;

    public abstract void initDispatcher();

    /**
     * @param msgctx
     * @throws org.apache.axis2.AxisFault
     */
    public void invoke(MessageContext msgctx) throws AxisFault {
        AxisService axisService = msgctx.getAxisService();

        if (axisService == null) {
            axisService = findService(msgctx);

            if (axisService != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found AxisService : " + axisService.getServiceDescription());
                }

                msgctx.setAxisService(axisService);
                // TODO Chinthaka : set the Service Group Context to the message Context
            }
        }

        if ((msgctx.getAxisService() != null) && (msgctx.getAxisOperation() == null)) {
            AxisOperation axisOperation = findOperation(axisService, msgctx);

            if (axisOperation != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found AxisOperation : " + axisOperation.getName());
                }

                msgctx.setAxisOperation(axisOperation);
            }
        }
    }
}
