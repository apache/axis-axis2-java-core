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
package org.apache.axis.engine;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;

import javax.xml.namespace.QName;

/**
 * Class Dispatcher
 */
public class RequestURIBasedDispatcher extends AbstractDispatcher {
    /**
     * Field NAME
     */
    public static final QName NAME =
        new QName("http://axis.ws.apache.org", "RequestURIBasedDispatcher");
    QName serviceName = null;
    QName operatoinName = null;

    /**
     * Constructor Dispatcher
     */
    public RequestURIBasedDispatcher() {
        init(new HandlerDescription(NAME));
    }

    public OperationDescription findOperation(
        ServiceDescription service,
        MessageContext messageContext)
        throws AxisFault {
        if (operatoinName != null) {
            OperationDescription axisOp = service.getOperation(operatoinName);
            return axisOp;
        }
        return null;

    }

    /* (non-Javadoc)
     * @see org.apache.axis.engine.AbstractDispatcher#findService(org.apache.axis.context.MessageContext)
     */
    public ServiceDescription findService(MessageContext messageContext)
        throws AxisFault {
        final String URI_ID_STRING = "/services";
        if (messageContext.isServerSide()) {

            EndpointReference toEPR = messageContext.getTo();
            if (toEPR != null) {
                String filePart = toEPR.getAddress();

                int index = filePart.lastIndexOf(URI_ID_STRING);
                String serviceStr = null;
                if (-1 != index) {
                    serviceStr =
                        filePart.substring(index + URI_ID_STRING.length() + 1);

                    ConfigurationContext engineContext =
                        messageContext.getSystemContext();

                    if ((index = serviceStr.indexOf('/')) > 0) {
                        serviceName = new QName(serviceStr.substring(0, index));
                        operatoinName =
                            new QName(serviceStr.substring(index + 1));
                    } else {
                        serviceName = new QName(serviceStr);
                    }

                    AxisConfiguration registry =
                        messageContext.getSystemContext().getAxisConfiguration();
                    return registry.getService(serviceName);
                }
            }
        }
        return null;

    }
}
