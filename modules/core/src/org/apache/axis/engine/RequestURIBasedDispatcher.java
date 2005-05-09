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

import javax.xml.namespace.QName;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;

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
        init(new HandlerMetadata(NAME));
    }

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
        throws AxisFault {
        if (operatoinName != null) {
            AxisOperation axisOp = service.getOperation(operatoinName);
            return axisOp;
        }
        return null;

    }

    /* (non-Javadoc)
     * @see org.apache.axis.engine.AbstractDispatcher#findService(org.apache.axis.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        final String URI_ID_STRING = "/services";
        if (messageContext.isServerSide()) {

            EndpointReference toEPR = messageContext.getTo();
            String filePart = toEPR.getAddress();

            int index = filePart.lastIndexOf(URI_ID_STRING);
            String serviceStr = null;
            if (index > 0) {
                serviceStr = filePart.substring(index + URI_ID_STRING.length() + 1);

                EngineContext engineContext = messageContext.getEngineContext();

                if ((index = serviceStr.indexOf('/')) > 0) {
                    serviceName = new QName(serviceStr.substring(0, index));
                    operatoinName = new QName(serviceStr.substring(index + 1));
                } else {
                    serviceName = new QName(serviceStr);
                }

                EngineConfiguration registry = messageContext.getEngineContext().getEngineConfig();
                return registry.getService(serviceName);
            }
        }
        return null;

    }
}
