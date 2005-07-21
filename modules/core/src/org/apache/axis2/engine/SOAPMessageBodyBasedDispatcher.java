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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

/**
 * Dispatches based on the namespace URI of the first child of
 * the Body.
 */
public class SOAPMessageBodyBasedDispatcher extends AbstractDispatcher {
    /**
     * Field NAME
     */
    public static final QName NAME =
            new QName("http://axis.ws.apache.org",
                    "SOAPMessageBodyBasedDispatcher");
    QName serviceName = null;
    QName operatoinName = null;

    /**
     * Constructor Dispatcher
     */
    public SOAPMessageBodyBasedDispatcher() {
        init(new HandlerDescription(NAME));
    }

    public OperationDescription findOperation(ServiceDescription service,
                                              MessageContext messageContext)
            throws AxisFault {
        OMElement bodyFirstChild = messageContext.getEnvelope().getBody()
                .getFirstElement();
        operatoinName = new QName(bodyFirstChild.getLocalName());

        OperationDescription axisOp = service.getOperation(operatoinName);
        return axisOp;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public ServiceDescription findService(MessageContext messageContext) throws AxisFault {
        final String URI_ID_STRING = "/services";
        OMElement bodyFirstChild = messageContext.getEnvelope().getBody()
                .getFirstElement();
        OMNamespace ns = bodyFirstChild.getNamespace();
        if (ns != null) {
            String filePart = ns.getName();

            String[] values = Utils.parseRequestURLForServiceAndOperation(
                    filePart);
            if (values[1] != null) {
                operatoinName = new QName(values[1]);
            }
            if (values[0] != null) {
                serviceName = new QName(values[0]);
                AxisConfiguration registry =
                        messageContext.getSystemContext().getAxisConfiguration();
                return registry.getService(serviceName);
            }
        }
        return null;

    }
}
