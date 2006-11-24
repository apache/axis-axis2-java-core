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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Dispatches based on the namespace URI of the first child of
 * the body.
 */
public class SOAPMessageBodyBasedDispatcher extends AbstractDispatcher {

    /**
     * Field NAME
     */
    public static final String NAME = "SOAPMessageBodyBasedDispatcher";
    private static final Log log = LogFactory.getLog(SOAPMessageBodyBasedDispatcher.class);
    String serviceName = null;
    QName operationName = null;
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        OMElement bodyFirstChild = messageContext.getEnvelope().getBody().getFirstElement();

        if (bodyFirstChild == null) {
            return null;
        } else {
            if(isDebugEnabled){
                log.debug(
                        "Checking for Operation using SOAP message body's first child's local name : "
                                + bodyFirstChild.getLocalName());
            }
            operationName = new QName(bodyFirstChild.getLocalName());
        }

        return service.getOperation(operationName);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        OMElement bodyFirstChild = messageContext.getEnvelope().getBody().getFirstElement();

        if (bodyFirstChild != null) {
            OMNamespace ns = bodyFirstChild.getNamespace();

            if (ns != null) {
                String filePart = ns.getNamespaceURI();

                if(isDebugEnabled){
                    log.debug(
                            "Checking for Service using SOAP message body's first child's namespace : "
                                    + filePart);
                }
                String[] values = Utils.parseRequestURLForServiceAndOperation(filePart,
                        messageContext.getConfigurationContext().getServiceContextPath());

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
        }

        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
