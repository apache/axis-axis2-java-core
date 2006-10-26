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


package org.apache.axis2.transport;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPTransportUtils;

import java.io.OutputStream;

/**
 * By the time this Class is invoked either the To EPR on the MessageContext
 * should be set or TRANSPORT_WRITER property set in the message Context with a
 * Writer. This Class would write the SOAPMessage using either of the methods in
 * the order To then Writer.
 */
public abstract class AbstractTransportSender extends AbstractHandler implements TransportSender {

    /**
     * Field NAME
     */
    public static final String NAME = "TransportSender";

    /**
     * Constructor AbstractTransportSender
     */
    public AbstractTransportSender() {
        init(new HandlerDescription(NAME));
    }

    public abstract void finalizeSendWithOutputStreamFromIncomingConnection(
            MessageContext msgContext, OutputStream out)
            throws AxisFault;

    public abstract void finalizeSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault;

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {
    }

    /**
     * Method invoke
     *
     * @param msgContext
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
                out = openTheConnection(epr, msgContext);

                OutputStream newOut = startSendWithToAddress(msgContext, out);

                if (newOut != null) {
                    out = newOut;
                }

                writeMessage(msgContext, out);
                finalizeSendWithToAddress(msgContext, out);
            }
        } else {
            out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

            if (out != null) {
                startSendWithOutputStreamFromIncomingConnection(msgContext, out);
                writeMessage(msgContext, out);
                finalizeSendWithOutputStreamFromIncomingConnection(msgContext, out);
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

    protected abstract OutputStream openTheConnection(EndpointReference epr, MessageContext msgctx)
            throws AxisFault;

    public abstract OutputStream startSendWithOutputStreamFromIncomingConnection(
            MessageContext msgContext, OutputStream out)
            throws AxisFault;

    public abstract OutputStream startSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault;

    public void writeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if ((envelope != null) && msgContext.isDoingREST()) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            try {
                OMOutputFormat format = new OMOutputFormat();

                // Pick the char set encoding from the msgContext
                String charSetEnc =
                        (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

                format.setDoOptimize(false);
                format.setDoingSWA(false);
                format.setCharSetEncoding(charSetEnc);
                outputMessage.serializeAndConsume(out, format);
                out.flush();
            } catch (Exception e) {
                throw new AxisFault(e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("outMessageNull"));
        }
    }
}
