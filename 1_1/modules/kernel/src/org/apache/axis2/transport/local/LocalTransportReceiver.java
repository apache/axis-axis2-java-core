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

import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalTransportReceiver {
    public static ConfigurationContext CONFIG_CONTEXT;
    private ConfigurationContext confContext;
    private LocalTransportSender sender;

    public LocalTransportReceiver(ConfigurationContext configContext) {
        confContext = configContext;
    }

    public LocalTransportReceiver(LocalTransportSender sender) {
        this(CONFIG_CONTEXT);
        this.sender = sender;
    }

    public void processMessage(InputStream in, EndpointReference to) throws AxisFault {
        try {
            TransportInDescription tIn = confContext.getAxisConfiguration().getTransportIn(
                    new QName(Constants.TRANSPORT_LOCAL));
            TransportOutDescription tOut = confContext.getAxisConfiguration().getTransportOut(
                    new QName(Constants.TRANSPORT_LOCAL));

            tOut.setSender(new LocalResponder(sender));

            MessageContext msgCtx = new MessageContext();
            msgCtx.setConfigurationContext(confContext);
            msgCtx.setTransportIn(tIn);
            msgCtx.setTransportOut(tOut);

            msgCtx.setTo(to);
            msgCtx.setServerSide(true);
            msgCtx.setProperty(MessageContext.TRANSPORT_OUT, sender.getResponse());

            XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                    new BufferedReader(new InputStreamReader(in)));
            StAXBuilder builder = new StAXSOAPModelBuilder(reader, null);
            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();

            msgCtx.setEnvelope(envelope);

            AxisEngine engine = new AxisEngine(confContext);

            if (envelope.getBody().hasFault()) {
                engine.receiveFault(msgCtx);
            } else {
                engine.receive(msgCtx);
            }
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
    }
}
