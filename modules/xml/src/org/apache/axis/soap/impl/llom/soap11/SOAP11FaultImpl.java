package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMOutput;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.soap.*;
import org.apache.axis.soap.impl.llom.SOAPFaultImpl;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;


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
public class SOAP11FaultImpl extends SOAPFaultImpl {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    public SOAP11FaultImpl(SOAPBody parent, Exception e) throws SOAPProcessingException {
        super(parent, e);
    }

    public SOAP11FaultImpl(SOAPBody parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    /**
     * This is a convenience method for the SOAP Fault Impl.
     *
     * @param parent
     * @param e
     */
    public SOAP11FaultImpl(SOAPBody parent) throws SOAPProcessingException {
        super(parent);

    }

    protected SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault) throws SOAPProcessingException {
        return new SOAP11FaultDetailImpl(fault);
    }

    public void serializeWithCache(OMOutput omOutput) throws XMLStreamException {
        super.serializeWithCache(omOutput);
    }

    public void serialize(OMOutput omOutput) throws XMLStreamException {
        super.serialize(omOutput);
    }

    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException {
        if (!(soapFaultCode instanceof SOAP11FaultCodeImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Code. But received some other implementation");
        }
        super.setCode(soapFaultCode);
    }
    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        if (!(reason instanceof SOAP11FaultReasonImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Reason. But received some other implementation");
        }
        super.setReason(reason);
    }
//
    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        if (!(node instanceof SOAP11FaultNodeImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Node. But received some other implementation");
        }
        super.setNode(node);
    }
    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        if (!(role instanceof SOAP11FaultRoleImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Role. But received some other implementation");
        }
        super.setRole(role);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11BodyImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        if (!(detail instanceof SOAP11FaultDetailImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Detail. But received some other implementation");
        }
        super.setDetail(detail);
    }

}
