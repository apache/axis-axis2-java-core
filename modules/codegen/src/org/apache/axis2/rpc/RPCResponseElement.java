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

package org.apache.axis2.rpc;

import org.apache.axis2.databinding.SerializationContext;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.impl.OMOutputImpl;
import org.apache.ws.commons.om.impl.llom.OMElementImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

/**
 * RPCResponseElement
 */
public class RPCResponseElement extends OMElementImpl {
    RPCMethod method;
    RPCValues values;

    public RPCResponseElement(RPCMethod method,
                              RPCValues values,
                              OMElement parent) {
        super(method.getResponseQName(), parent);
        this.method = method;
        this.values = values;
    }

    protected void serialize(OMOutputImpl omOutput, boolean cache)
            throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        SerializationContext context = new SerializationContext(writer);

        // Write wrapper element
        if (ns == null) {
            writer.writeStartElement(localName);
        } else {
            writer.writeStartElement(localName, ns.getName(), ns.getPrefix());
        }
        Iterator outParams = method.getOutParams();
        while (outParams.hasNext()) {
            RPCParameter parameter = (RPCParameter) outParams.next();
            try {
                parameter.serialize(context,
                        values.getValue(parameter.getQName()));
            } catch (Exception e) {
                throw new XMLStreamException("Couldn't serialize RPCParameter",
                        e);
            }
        }
        writer.writeEndElement();

        try {
            context.finish();
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }
}
