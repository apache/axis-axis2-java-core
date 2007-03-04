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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Dispatches based on the SOAPAction.
 */
public class SOAPActionBasedDispatcher extends AbstractDispatcher {

    /**
     * Field NAME
     */
    public static final String NAME = "SOAPActionBasedDispatcher";
    private static final Log log = LogFactory.getLog(SOAPActionBasedDispatcher.class);

    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {
        String action = messageContext.getSoapAction();

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(messageContext.getLogIDString() +
                    " Checking for Operation using SOAPAction : " + action);
        }
        if (action != null) {
            AxisOperation op = service.getOperationBySOAPAction(action);

            if (op == null) {
                op = service.getOperationByAction(action);
            }

            /*
             * HACK: Please remove this when we add support for custom action
             * uri
             */
            if ((op == null) && (action.lastIndexOf('/') != -1)) {
                op = service.getOperation(new QName(action.substring(action.lastIndexOf('/'),
                                                                     action.length())));
            }

            return op;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug(messageContext.getLogIDString() +
                    " Checking for Service using SOAPAction is a TODO item");
        }
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
