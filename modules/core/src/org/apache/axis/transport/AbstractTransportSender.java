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

import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.impl.llom.OMOutput;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * By the time this Class is invoked either the To EPR on the MessageContext should be set or
 * TRANSPORT_WRITER property set in the message Context with a Writer. This Class would write the
 * SOAPMessage using either of the methods in the order To then Writer.
 */
public abstract class AbstractTransportSender extends AbstractHandler implements TransportSender {
    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());
    protected boolean doREST = false;

    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org", "TransportSender");

    /**
     * Constructor AbstractTransportSender
     */
    public AbstractTransportSender() {
        init(new HandlerDescription(NAME));
    }
    
    public void init(ConfigurationContext confContext,TransportOutDescription transportOut)throws AxisFault{
    
    }

    /**
     * Method invoke
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        //Check for the REST behaviour, if you desire rest beahaviour
        //put a <parameter name="doREST" value="true"/> at the axis2.xml
        Object doREST = msgContext.getProperty(Constants.Configuration.DO_REST);
        if (doREST != null && Constants.VALUE_TRUE.equals(doREST)) {
            this.doREST = true;
        }

        OutputStream out = null;

        EndpointReference epr = null;

        if (msgContext.getTo() != null
                && !AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(msgContext.getTo().getAddress())
                && !AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(msgContext.getTo().getAddress())) {
            epr = msgContext.getTo();
        }

        if (epr != null) {
            out = openTheConnection(epr,msgContext);
            OutputStream newOut = startSendWithToAddress(msgContext, out);
            if(newOut != null){
                out = newOut;
            }
            writeMessage(msgContext, out);
            finalizeSendWithToAddress(msgContext,out);
        } else {
            out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);
            if (out != null) {
                startSendWithOutputStreamFromIncomingConnection(msgContext, out);
                writeMessage(msgContext, out);
                finalizeSendWithOutputStreamFromIncomingConnection(msgContext,out);
            } else {
                throw new AxisFault("Both the TO and Property MessageContext.TRANSPORT_WRITER is Null, No where to send");
            }
        }
        //TODO fix this, we do not set the value if the operation context is not avalible
        if(msgContext.getOperationContext()!= null){
            msgContext.getOperationContext().setProperty(Constants.RESPONSE_WRITTEN, Constants.VALUE_TRUE);        
        }
    }

    public void writeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if (envelope != null && this.doREST) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            OMOutput omOutput = null;
            try {
                omOutput = new OMOutput(XMLOutputFactory.newInstance().createXMLStreamWriter(out));
                outputMessage.serialize(omOutput);
                omOutput.flush();
                out.flush();
            } catch (Exception e) {
                throw new AxisFault("Stream error", e);
            }
        } else {
            throw new AxisFault("the OUTPUT message is Null, nothing to write");
        }
    }

    public abstract OutputStream startSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault;

    public abstract void finalizeSendWithToAddress(MessageContext msgContext,OutputStream out)
            throws AxisFault;


    public abstract OutputStream startSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                         OutputStream out)
            throws AxisFault;

    public abstract void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,OutputStream out)
            throws AxisFault;


    protected abstract OutputStream openTheConnection(EndpointReference epr,MessageContext msgctx) throws AxisFault;
}
