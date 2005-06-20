package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMElement;
import org.apache.axis.soap.*;
import org.apache.axis.soap.impl.llom.*;
import org.apache.axis.soap.impl.llom.soap12.SOAP12HeaderImpl;
import org.apache.axis.soap.impl.llom.factory.SOAPLinkedListImplFactory;

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
public class SOAP11Factory extends SOAPLinkedListImplFactory {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    public SOAPEnvelope createSOAPEnvelope() {
        return new SOAPEnvelopeImpl(new OMNamespaceImpl(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX));
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope) throws SOAPProcessingException {
        return new SOAP11HeaderImpl(envelope);
    }

    public SOAPHeader createSOAPHeader(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        return new SOAP11HeaderImpl(envelope, builder);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName, OMNamespace ns, SOAPHeader parent) throws SOAPProcessingException {
        return new SOAP11HeaderBlockImpl(localName, ns, parent);
    }

    public SOAPHeaderBlock createSOAPHeaderBlock(String localName, OMNamespace ns, SOAPHeader parent, OMXMLParserWrapper builder) throws SOAPProcessingException {
        return new SOAP11HeaderBlockImpl(localName, ns, parent, builder);
    }

    public SOAPFault createSOAPFault(SOAPBody parent, Exception e) throws SOAPProcessingException {
        return new SOAP11FaultImpl(parent, e);
    }

    public SOAPFault createSOAPFault(SOAPBody parent) throws SOAPProcessingException {
        return new SOAP11FaultImpl(parent);
    }

    public SOAPFault createSOAPFault(SOAPBody parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultImpl(parent, builder);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope) throws SOAPProcessingException {
        return new SOAP11BodyImpl(envelope);
    }

    public SOAPBody createSOAPBody(SOAPEnvelope envelope, OMXMLParserWrapper builder) {
        return new SOAP11BodyImpl(envelope, builder);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP11FaultCodeImpl(parent);
    }

    public SOAPFaultCode createSOAPFaultCode(SOAPFault parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultCodeImpl(parent, builder);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent) throws SOAPProcessingException {
        return new SOAP11FaultValueImpl(parent);
    }

    public SOAPFaultValue createSOAPFaultValue(SOAPFaultCode parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultValueImpl(parent, builder);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent) throws SOAPProcessingException {
        return new SOAP11FaultValueImpl(parent);
    }

    //added
    public SOAPFaultValue createSOAPFaultValue(SOAPFaultSubCode parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultValueImpl(parent, builder);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent) throws SOAPProcessingException {
        return new SOAP11FaultSubCodeImpl(parent);
    }

    //changed
    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultCode parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultSubCodeImpl(parent, builder);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent) throws SOAPProcessingException {
        return new SOAP11FaultSubCodeImpl(parent);
    }

    public SOAPFaultSubCode createSOAPFaultSubCode(SOAPFaultSubCode parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultSubCodeImpl(parent, builder);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP11FaultReasonImpl(parent);
    }

    public SOAPFaultReason createSOAPFaultReason(SOAPFault parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultReasonImpl(parent, builder);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent) throws SOAPProcessingException {
        return new SOAP11FaultTextImpl(parent);
    }

    public SOAPFaultText createSOAPFaultText(SOAPFaultReason parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultTextImpl(parent, builder);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP11FaultNodeImpl(parent);
    }

    public SOAPFaultNode createSOAPFaultNode(SOAPFault parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultNodeImpl(parent, builder);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP11FaultRoleImpl(parent);
    }

    public SOAPFaultRole createSOAPFaultRole(SOAPFault parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultRoleImpl(parent, builder);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent) throws SOAPProcessingException {
        return new SOAP11FaultDetailImpl(parent);
    }

    public SOAPFaultDetail createSOAPFaultDetail(SOAPFault parent, OMXMLParserWrapper builder) {
        return new SOAP11FaultDetailImpl(parent, builder);
    }

    public SOAPEnvelope getDefaultEnvelope() throws SOAPProcessingException {
        OMNamespace ns =
        new OMNamespaceImpl(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        SOAPEnvelopeImpl env = new SOAPEnvelopeImpl(ns);


        SOAPHeaderImpl headerImpl = new SOAP11HeaderImpl(env);
        headerImpl.setComplete(true);

        SOAPBodyImpl bodyImpl = new SOAP11BodyImpl(env);
        bodyImpl.setComplete(true);
        return env;
    }



}
