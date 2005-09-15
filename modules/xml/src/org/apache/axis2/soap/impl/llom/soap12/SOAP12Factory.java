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

package org.apache.axis2.soap.impl.llom.soap12;

import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.OMNamespaceImpl;
import org.apache.axis2.soap.*;
import org.apache.axis2.soap.impl.llom.SOAPEnvelopeImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;
import org.apache.axis2.soap.impl.llom.factory.SOAPLinkedListImplFactory;

public class SOAP12Factory extends SOAPLinkedListImplFactory {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    public SOAPEnvelope createSOAPEnvelope() {
        return new SOAPEnvelopeImpl(
                new OMNamespaceImpl(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX),
                this);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope) throws SOAPProcessingException {
        return new SOAP12HeaderImpl(envelope);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope,
                                       OMXMLParserWrapper builder) {
        return new SOAP12HeaderImpl(envelope, builder);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 SOAPHeader parent) throws SOAPProcessingException {
        return new SOAP12HeaderBlockImpl(localName, ns, parent);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName,
                                                 OMNamespace ns,
                                                 SOAPHeader parent,
                                                 OMXMLParserWrapper builder) throws SOAPProcessingException {
        return new SOAP12HeaderBlockImpl(localName, ns, parent, builder);
    }

    public SOAPFault createSOAPFault(SOAPBody parent, Exception e) throws SOAPProcessingException {
        return new SOAP12FaultImpl(parent, e);
    }

    public SOAPFault createSOAPFault(SOAPBody parent) throws SOAPProcessingException {
        return new SOAP12FaultImpl(parent);
    }

    public SOAPFault createSOAPFault(SOAPBody parent,
                                     OMXMLParserWrapper builder) {
        return new SOAP12FaultImpl(parent, builder);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope) throws SOAPProcessingException {
        return new SOAP12BodyImpl(envelope);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope,
                                   OMXMLParserWrapper builder) {
        return new SOAP12BodyImpl(envelope, builder);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP12FaultCodeImpl(parent);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultCodeImpl(parent, builder);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent) throws SOAPProcessingException {
        return new SOAP12FaultValueImpl(parent);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent,
                                               OMXMLParserWrapper builder) {
        return new SOAP12FaultValueImpl(parent, builder);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent) throws SOAPProcessingException {
        return new SOAP12FaultValueImpl(parent);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent,
                                               OMXMLParserWrapper builder) {
        return new SOAP12FaultValueImpl(parent, builder);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent) throws SOAPProcessingException {
        return new SOAP12FaultSubCodeImpl(parent);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent,
                                                   OMXMLParserWrapper builder) {
        return new SOAP12FaultSubCodeImpl(parent, builder);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent) throws SOAPProcessingException {
        return new SOAP12FaultSubCodeImpl(parent);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent,
                                                   OMXMLParserWrapper builder) {
        return new SOAP12FaultSubCodeImpl(parent, builder);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP12FaultReasonImpl(parent);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent,
                                                 OMXMLParserWrapper builder) {
        return new SOAP12FaultReasonImpl(parent, builder);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent) throws SOAPProcessingException {
        return new SOAP12FaultTextImpl(parent);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultTextImpl(parent, builder);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP12FaultNodeImpl(parent);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultNodeImpl(parent, builder);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP12FaultRoleImpl(parent);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent,
                                             OMXMLParserWrapper builder) {
        return new SOAP12FaultRoleImpl(parent, builder);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP12FaultDetailImpl(parent);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent,
                                                 OMXMLParserWrapper builder) {
        return new SOAP12FaultDetailImpl(parent, builder);
    }

    public SOAPEnvelope getDefaultEnvelope() throws SOAPProcessingException {
        OMNamespace ns =
                new OMNamespaceImpl(
                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(ns, this);
        createSOAPHeader(env);
        createSOAPBody(env);

        return env;
    }

}
