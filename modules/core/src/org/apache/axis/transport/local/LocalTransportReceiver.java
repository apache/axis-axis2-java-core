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
 *
 *  Runtime state of the engine
 */
package org.apache.axis.transport.local;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;

public class LocalTransportReceiver {
    public static ConfigurationContext CONFIG_CONTEXT;

    private ConfigurationContext confContext;
    public LocalTransportReceiver(ConfigurationContext configContext) {
        confContext = configContext;
    }

    public LocalTransportReceiver() {
        this(CONFIG_CONTEXT);
    }

    public void processMessage(InputStream in, EndpointReference to) throws AxisFault {
        try {
            TransportInDescription tIn =
                confContext.getAxisConfiguration().getTransportIn(
                    new QName(Constants.TRANSPORT_LOCAL));
            TransportOutDescription tOut =
                confContext.getAxisConfiguration().getTransportOut(
                    new QName(Constants.TRANSPORT_LOCAL));
            MessageContext msgCtx = new MessageContext(confContext, tIn, tOut);
            msgCtx.setTo(to);
            msgCtx.setServerSide(true);

            XMLStreamReader reader =
                XMLInputFactory.newInstance().createXMLStreamReader(
                    new BufferedReader(new InputStreamReader(in)));

            StAXBuilder builder = new StAXSOAPModelBuilder(reader);
            msgCtx.setEnvelope((SOAPEnvelope) builder.getDocumentElement());
            AxisEngine engine = new AxisEngine(confContext);
            engine.receive(msgCtx);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
    }

}
