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

import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.TransportSender;
import org.apache.axis.impl.handlers.AbstractHandler;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class AbstractTransportSender extends AbstractHandler implements TransportSender {
	private Log log = LogFactory.getLog(getClass());
	public static final QName NAME = new QName("http://axis.ws.apache.org","TransportSender");
	
	public AbstractTransportSender(){
		init(new HandlerMetaData(NAME));
	}
    public final void invoke(MessageContext msgContext) throws AxisFault {
		OutputStream out = null;
        if (msgContext.isProcessingFault()) {
            //Means we are processing fault
            if (msgContext.getFaultTo() != null) {
            	log.info("Obtain the output stream to send the fault flow to " + msgContext.getFaultTo().getAddress());
                out = obtainOutputStream(msgContext, msgContext.getFaultTo());
            } else {
				log.info("Obtain the output stream to send the fault flow to ANONYMOUS");
                out = obtainOutputStream(msgContext);
            }
        } else {
            if (msgContext.getTo() != null) {
				log.info("Obtain the output stream to send to To flow to " + msgContext.getTo().getAddress());
                out = obtainOutputStream(msgContext, msgContext.getTo());
            } else if (msgContext.getReplyTo() != null) {
				log.info("Obtain the output stream to send to ReplyTo flow to " + msgContext.getReplyTo().getAddress());
                out = obtainOutputStream(msgContext, msgContext.getTo());
            } else {
				log.info("Obtain the output stream to send the fault flow to ANONYMOUS");
                out = obtainOutputStream(msgContext);
            }
        }
        startSending();
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope != null) {
			XMLStreamWriter outputWriter = null;
            try {
                outputWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                envelope.serialize(outputWriter,false);
                outputWriter.flush();
                envelope.free();
            } catch (Exception e) {
                throw new AxisFault("Stream error",e);
            }
        }
		
        finalizeSending();
		log.info("Send the Response");
    }

    protected void startSending() {
    }

    protected abstract OutputStream obtainOutputStream(MessageContext msgContext, EndpointReference epr) throws AxisFault;

    protected abstract OutputStream obtainOutputStream(MessageContext msgContext) throws AxisFault;

    protected void finalizeSending() {
    }
}
