/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.clientapi;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.llom.builder.StAXBuilder;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public abstract class AbstractCall {
    public MessageContext createIncomingMessageContext(InputStream in, AxisEngine engine) throws AxisFault {
        MessageContext msgContext;
        try {
            msgContext = new MessageContext(engine.getRegistry());
            InputStreamReader isr = new InputStreamReader(in);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(isr);
            StAXBuilder builder = new StAXSOAPModelBuilder(OMFactory.newInstance(), reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
        return msgContext;

    }
}
