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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Dispatches the service based on the information from the target endpoint URL.
 */
public class RequestURIBasedDispatcher extends AbstractDispatcher {
    private Log log = LogFactory.getLog(getClass());
    /**
     * Field NAME
     */
    public static final QName NAME =
            new QName("http://axis.ws.apache.org",
                    "RequestURIBasedDispatcher");
    String serviceName = null;
    QName operationName = null;


    public AxisOperation findOperation(AxisService service,
                                       MessageContext messageContext)
            throws AxisFault {
        log.debug("Checking for Operation using target endpoint uri fragment : " + operationName);
        if (operationName != null) {
            return service.getOperation(operationName);
        }
        return null;

    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            log.debug("Checking for Service using target endpoint address : " + toEPR.getAddress());
            String filePart = toEPR.getAddress();
            String[] values = Utils.parseRequestURLForServiceAndOperation(
                    filePart);
            if (values[1] != null) {
                operationName = new QName(values[1]);
            }
            if (values[0] != null) {
                serviceName = values[0];
                AxisConfiguration registry =
                        messageContext.getConfigurationContext().getAxisConfiguration();
                return registry.getService(serviceName);
            }
        }
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
