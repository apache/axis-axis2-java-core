/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.TransportSender;
import org.apache.axis.impl.handlers.AbstractHandler;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.addressing.EndpointReference;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

/**
 */
public abstract class AbstractTrasnportSender extends AbstractHandler implements TransportSender {

    public final void invoke(MessageContext msgContext) throws AxisFault {
        OutputStream out = null;
        if (msgContext.isProcessingFault()) {
            //Means we are processing fault
            if (msgContext.getFaultTo() != null) {
                out = obtainOutPutStream(msgContext, msgContext.getFaultTo());
            } else {
                out = obtainOutPutStream(msgContext);
            }
        } else {
            if (msgContext.getTo() != null) {
                out = obtainOutPutStream(msgContext, msgContext.getTo());
            } else if (msgContext.getReplyTo() != null) {
                out = obtainOutPutStream(msgContext, msgContext.getTo());
            } else {
                out = obtainOutPutStream(msgContext);
            }
        }
        startSending();
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope != null) {
            try {
                SimpleOMSerializer serializer = new SimpleOMSerializer();
                XMLStreamWriter outputWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                serializer.serialize(envelope, outputWriter);
            } catch (XMLStreamException e) {
                throw new AxisFault("Stream error",e);
            }

        }
        finalizeSending();
    }

    protected void startSending() {
    }

    protected abstract OutputStream obtainOutPutStream(MessageContext msgContext, EndpointReference epr) throws AxisFault;

    protected abstract OutputStream obtainOutPutStream(MessageContext msgContext) throws AxisFault;

    protected void finalizeSending() {
    }
}
