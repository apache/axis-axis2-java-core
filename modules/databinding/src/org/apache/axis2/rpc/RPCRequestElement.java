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
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.OMElementImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

/**
 * RPCResponseElement
 */
public class RPCRequestElement extends OMElementImpl {
    RPCMethod method;

    public RPCRequestElement(RPCMethod method, OMElement parent) {
        super(method.getQName(), parent);
        this.method = method;
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
        Iterator inParams = method.getInParams();
        while (inParams.hasNext()) {
            RPCParameter parameter = (RPCParameter) inParams.next();
            try {
                parameter.serialize(context);
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
