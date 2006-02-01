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

package org.apache.axis2.soap.impl.dom.soap11;

import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.om.impl.OMNodeEx;
import org.apache.ws.commons.om.impl.llom.traverse.OMChildrenWithSpecificAttributeIterator;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import org.apache.ws.commons.soap.SOAPProcessingException;
import org.apache.axis2.soap.impl.dom.SOAPHeaderImpl;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class SOAP11HeaderImpl extends SOAPHeaderImpl {
    /**
     * @param envelope
     */
    public SOAP11HeaderImpl(SOAPEnvelope envelope) throws SOAPProcessingException {
        super(envelope);
    }

    /**
     * Constructor SOAPHeaderImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAP11HeaderImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        super(envelope, builder);
    }

    public SOAPHeaderBlock addHeaderBlock(String localName, OMNamespace ns) throws OMException {
        if (ns == null || ns.getName() == null || "".equals(ns.getName())) {
            throw new OMException(
                    "All the SOAP Header blocks should be namespace qualified");
        }

        OMNamespace namespace = findNamespace(ns.getName(), ns.getPrefix());
        if (namespace != null) {
            ns = namespace;
        }

        SOAPHeaderBlock soapHeaderBlock = null;
        try {
            soapHeaderBlock = new SOAP11HeaderBlockImpl(localName, ns, this);
        } catch (SOAPProcessingException e) {
            throw new OMException(e);
        }
        ((OMNodeEx)soapHeaderBlock).setComplete(true);
        return soapHeaderBlock;
    }

    public Iterator extractHeaderBlocks(String role) {
        return new OMChildrenWithSpecificAttributeIterator(getFirstOMChild(),
                new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP11Constants.ATTR_ACTOR),
                role,
                true);

    }


}
