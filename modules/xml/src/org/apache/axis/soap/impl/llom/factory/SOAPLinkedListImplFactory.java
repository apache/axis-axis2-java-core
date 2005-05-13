package org.apache.axis.soap.impl.llom.factory;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axis.soap.*;
import org.apache.axis.soap.impl.llom.*;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Constants;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class SOAPLinkedListImplFactory extends OMLinkedListImplFactory implements SOAPFactory {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    /**
     * Method createSOAPBody
     *
     * @param envelope
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope) {
        SOAPBody soapBody = new SOAPBodyImpl(envelope);
        return soapBody;
    }

    /**
     * Method createSOAPBody
     *
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                   OMXMLParserWrapper builder) {
        return new SOAPBodyImpl(envelope, builder);
    }

    /**
     * Method createSOAPEnvelope
     *
     * @param ns
     * @param builder
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns,
                                           OMXMLParserWrapper builder) {
        return new SOAPEnvelopeImpl(ns, builder);
    }

    public SOAPEnvelope createSOAPEnvelope() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method createSOAPEnvelope
     *
     * @param ns
     * @return
     */
    public SOAPEnvelope createSOAPEnvelope(OMNamespace ns) {
        return new SOAPEnvelopeImpl(ns);
    }

       /**
     * Method createSOAPHeader
     *
     * @param envelope
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope) {
        return new SOAPHeaderImpl(envelope);
    }

    /**
     * Method createSOAPHeader
     *
     * @param envelope
     * @param builder
     * @return
     */
    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                       OMXMLParserWrapper builder) {
        return new SOAPHeaderImpl(envelope, builder);
    }

    /**
     * Method createSOAPHeaderBlock
     *
     * @param localName
     * @param ns
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns) {
        return new SOAPHeaderBlockImpl(localName, ns);
    }

    /**
     * Method createSOAPHeaderBlock
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        return new SOAPHeaderBlockImpl(localName, ns, parent, builder);
    }

    /**
     * Method createSOAPFault
     *
     * @param parent
     * @param e
     * @return
     */
    public SOAPFault createSOAPFault(SOAPBody parent, Exception e) {
        return new SOAPFaultImpl(parent, e);
    }

    /**
     * Method createSOAPFault
     *
     * @param ns
     * @param parent
     * @param builder
     * @return
     */
    public SOAPFault createSOAPFault(OMNamespace ns, SOAPBody parent,
                                     OMXMLParserWrapper builder) {
        return new SOAPFaultImpl(ns, parent, builder);
    }

    /**
     * Method getDefaultEnvelope
     *
     * @return
     */
    public SOAPEnvelope getDefaultEnvelope() {
        // Create an envelope
        OMNamespace ns =
        new OMNamespaceImpl(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(ns);
        SOAPBodyImpl bodyImpl = new SOAPBodyImpl(env);
        env.addChild(bodyImpl);

        SOAPHeaderImpl headerImpl = new SOAPHeaderImpl(env);
        headerImpl.setComplete(true);
        env.addChild(headerImpl);
        return env;
    }
}
