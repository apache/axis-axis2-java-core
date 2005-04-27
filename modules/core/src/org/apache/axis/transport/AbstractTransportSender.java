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

import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class AbstractTransportSender extends AbstractHandler implements TransportSender {
    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org", "TransportSender");

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
        
        EndpointReference epr = null;
        
        if (msgContext.isProcessingFault()) {
            // Means we are processing fault
            if (msgContext.getFaultTo() != null &&
                !AddressingConstants.EPR_ANONYMOUS_URL.equals(msgContext.getFaultTo().getAddress())) {
                    epr = msgContext.getFaultTo();
            } 
        } else {
            if (msgContext.getTo() != null && 
                    !AddressingConstants.EPR_ANONYMOUS_URL.equals(msgContext.getTo().getAddress())) {
                        epr = msgContext.getTo();
                } 
        }
        
        if(epr!= null){
            out =  openTheConnection(epr);
            startSendWithToAddress(msgContext,epr,out);
            writeMessage(msgContext,out);
            finalizeSendWithToAddress(msgContext,epr,out);
        }else{
            out = (Writer)msgContext.getProperty(MessageContext.TRANSPORT_WRITER);
            if(out != null){
                startSendWithOutputStreamFromIncomingConnection(msgContext,out);
                writeMessage(msgContext,out);
                finalizeSendWithOutputStreamFromIncomingConnection(msgContext,out);
            } else{
                throw new AxisFault("Both the TO and Property MessageContext.TRANSPORT_WRITER is Null, No where to send");
            }       
        }
    }

    public void writeMessage(MessageContext msgContext,Writer out) throws AxisFault{
        SOAPEnvelope envelope = msgContext.getEnvelope();
         if (envelope != null) {
             XMLStreamWriter outputWriter = null;
             try {
                 outputWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                 envelope.serialize(outputWriter);
                 outputWriter.flush();
                 out.flush();
             } catch (Exception e) {
                 throw new AxisFault("Stream error", e);
             }
         }
    }

    public abstract void startSendWithToAddress(MessageContext msgContext, EndpointReference epr,Writer writer)throws AxisFault;
    public abstract void finalizeSendWithToAddress(MessageContext msgContext,EndpointReference epr, Writer writer)throws AxisFault;

    public abstract void startSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,Writer writer)throws AxisFault;
    public abstract void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,Writer writer)throws AxisFault;

    protected abstract Writer openTheConnection(EndpointReference epr)throws AxisFault;
}
