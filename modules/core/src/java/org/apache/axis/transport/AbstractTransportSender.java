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
package org.apache.axis.transport;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;

/**
 */
public abstract class AbstractTransportSender extends AbstractHandler
        implements TransportSender {
    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org",
                    "TransportSender");

    /**
     * Field outS
     */
    protected OutputStream outS;

    /**
     * Constructor AbstractTransportSender
     */
    public AbstractTransportSender() {
        init(new HandlerMetadata(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        Writer out = null;
        if (msgContext.isProcessingFault()) {

            // Means we are processing fault
            if (msgContext.getFaultTo() != null) {
                log.info("Obtain the output stream to send the fault flow to "
                                + msgContext.getFaultTo().getAddress());
                out = obtainOutputStream(msgContext, msgContext.getFaultTo());
            } else {
                log.info(
                        "Obtain the output stream to send the fault flow to ANONYMOUS");
                out = obtainOutputStream(msgContext);
            }
        } else {
            if (msgContext.getTo() != null) {
                log.info("Obtain the output stream to send to To flow to "
                                + msgContext.getTo().getAddress());
                out = obtainOutputStream(msgContext, msgContext.getTo());
            } else if (msgContext.getReplyTo() != null) {
                log.info("Obtain the output stream to send to ReplyTo flow to "
                                + msgContext.getReplyTo().getAddress());
                out = obtainOutputStream(msgContext, msgContext.getTo());
            } else {
                log.info(
                        "Obtain the output stream to send the fault flow to ANONYMOUS");
                out = obtainOutputStream(msgContext);
            }
        }
        startSending(msgContext);
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope != null) {
            XMLStreamWriter outputWriter = null;
            try {
                outputWriter =
                XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                envelope.serialize(outputWriter, false);
                outputWriter.flush();
                if (outS != null) {
                    outS.flush();
                } else {
                    out.flush();
                }
            } catch (Exception e) {
                throw new AxisFault("Stream error", e);
            }
        }
        finalizeSending(msgContext);
        log.info("Send the Response");
    }

    /**
     * Method startSending
     *
     * @param msgContext
     * @throws AxisFault
     */
    protected void startSending(MessageContext msgContext) throws AxisFault {
    }

    /**
     * Method obtainOutputStream
     *
     * @param msgContext
     * @param epr
     * @return
     * @throws AxisFault
     */
    protected abstract Writer obtainOutputStream(
            MessageContext msgContext, EndpointReference epr) throws AxisFault;

    /**
     * Method obtainOutputStream
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected abstract Writer obtainOutputStream(MessageContext msgContext)
            throws AxisFault;

    /**
         * Method finalizeSending
         *
         * @param msgContext
         * @throws AxisFault
         */
    protected void finalizeSending(MessageContext msgContext)
            throws AxisFault {
    }
}
