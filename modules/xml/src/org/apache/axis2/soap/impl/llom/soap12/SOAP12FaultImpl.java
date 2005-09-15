package org.apache.axis2.soap.impl.llom.soap12;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.*;
import org.apache.axis2.soap.impl.llom.SOAPFaultImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;

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
public class SOAP12FaultImpl extends SOAPFaultImpl {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    public SOAP12FaultImpl(SOAPBody parent, Exception e) throws SOAPProcessingException {
        super(parent, e);
    }

    public SOAP12FaultImpl(SOAPBody parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    /**
     * This is a convenience method for the SOAP Fault Impl.
     *
     * @param parent
     * @param e
     */
    public SOAP12FaultImpl(SOAPBody parent) throws SOAPProcessingException {
        super(parent);
    }

    protected SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault) {
        return new SOAP12FaultDetailImpl(fault);

    }

    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException {
        if (!(soapFaultCode instanceof SOAP12FaultCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Code. But received some other implementation");
        }
        super.setCode(soapFaultCode);
    }


    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        if (!(reason instanceof SOAP12FaultReasonImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Reason. But received some other implementation");
        }
        super.setReason(reason);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        if (!(node instanceof SOAP12FaultNodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Node. But received some other implementation");
        }
        super.setNode(node);
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        if (!(role instanceof SOAP12FaultRoleImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Role. But received some other implementation");
        }
        super.setRole(role);
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        if (!(detail instanceof SOAP12FaultDetailImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Detail. But received some other implementation");
        }
        super.setDetail(detail);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12BodyImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

    protected void serialiseFaultNode(org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        SOAPFaultNode faultNode = getNode();
        if (faultNode != null) {
            faultNode.serializeWithCache(omOutput);
        }
    }
}
