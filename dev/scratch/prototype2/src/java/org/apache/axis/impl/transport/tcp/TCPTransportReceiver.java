/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.impl.transport.tcp;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.TransportSenderLocator;
import org.apache.axis.impl.llom.builder.StAXBuilder;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.AbstractTransportReceiver;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @version $Rev: $ $Date: $
 */

public class TCPTransportReceiver extends AbstractTransportReceiver {
    public TCPTransportReceiver(EngineRegistry reg) {
        super(reg);
    }

    protected MessageContext parseTheTransport(AxisEngine engine,
                                               InputStream in)
            throws AxisFault {
        try {
            MessageContext msgContext = new MessageContext(this.engineReg);
            InputStreamReader isr = new InputStreamReader(in);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(isr);
            StAXBuilder builder = new StAXSOAPModelBuilder(OMFactory.newInstance(), reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());

            return msgContext;
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }

    protected void storeOutputInfo(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        // Send it on its way...
        //We do not have any Addressing Headers to put
        //let us put the information about incoming transport
        msgContext.setProperty(MessageContext.TRANSPORT_TYPE,
                TransportSenderLocator.TRANSPORT_TCP);
        msgContext.setProperty(MessageContext.TRANSPORT_DATA, out);

    }

}
