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


package org.apache.axis2.transport.local;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPTransportUtils;

import java.io.OutputStream;

/**
 * LocalResponder
 */
public class LocalResponder extends AbstractHandler implements TransportSender {
    LocalTransportSender sender;

    public LocalResponder(LocalTransportSender sender) {
        this.sender = sender;
    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {
    }

    public void stop() {
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    /**
     * Method invoke
     *
     * @param msgContext the active MessageContext
     * @throws AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        // Check for the REST behaviour, if you desire rest beahaviour
        // put a <parameter name="doREST" value="true"/> at the axis2.xml
        msgContext.setDoingMTOM(HTTPTransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(HTTPTransportUtils.doWriteSwA(msgContext));

        OutputStream out;
        EndpointReference epr = null;

        if (msgContext.getTo() != null && !msgContext.getTo().hasAnonymousAddress()) {
            epr = msgContext.getTo();
        }

        if (epr != null) {
            if (!epr.hasNoneAddress()) {
                out = sender.getResponse();
                TransportUtils.writeMessage(msgContext, out);
            }
        } else {
            out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

            if (out != null) {
                TransportUtils.writeMessage(msgContext, out);
            } else {
                throw new AxisFault(
                        "Both the TO and Property MessageContext.TRANSPORT_OUT is Null, No where to send");
            }
        }

        // TODO fix this, we do not set the value if the operation context is
        // not available
        if (msgContext.getOperationContext() != null) {
            msgContext.getOperationContext().setProperty(Constants.RESPONSE_WRITTEN,
                                                         Constants.VALUE_TRUE);
        }
        return InvocationResponse.CONTINUE;
    }
}
