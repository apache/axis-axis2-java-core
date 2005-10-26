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

package org.apache.axis2.soap.impl.llom.soap11;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultDetail;
import org.apache.axis2.soap.SOAPFaultNode;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.SOAPFaultRole;
import org.apache.axis2.soap.SOAPProcessingException;
import org.apache.axis2.soap.impl.llom.SOAPFaultImpl;

import javax.xml.stream.XMLStreamException;


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

    public void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        super.serialize(omOutput);
    }

    public void serializeAndConsume(org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        super.serializeAndConsume(omOutput);
    }

    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException {
        if (!(soapFaultCode instanceof SOAP11FaultCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Code. But received some other implementation");
        }
        super.setCode(soapFaultCode);
    }

    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        if (!(reason instanceof SOAP11FaultReasonImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Reason. But received some other implementation");
        }
        super.setReason(reason);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        if (!(node instanceof SOAP11FaultNodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Node. But received some other implementation");
        }
        super.setNode(node);
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        if (!(role instanceof SOAP11FaultRoleImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Role. But received some other implementation");
        }
        super.setRole(role);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11BodyImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        if (!(detail instanceof SOAP11FaultDetailImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Detail. But received some other implementation");
        }
        super.setDetail(detail);
    }

    protected void serializeFaultNode(org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        
    }

}
