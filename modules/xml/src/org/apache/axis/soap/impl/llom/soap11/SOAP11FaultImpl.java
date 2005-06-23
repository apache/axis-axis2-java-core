package org.apache.axis.soap.impl.llom.soap11;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMOutputer;
import org.apache.axis.om.impl.llom.OMTextImpl;
import org.apache.axis.soap.impl.llom.*;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis.soap.impl.llom.soap12.SOAP12FaultCodeImpl;
import org.apache.axis.soap.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Iterator;


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

    public void serializeWithCache(OMOutputer outputer) throws XMLStreamException {
        super.serializeWithCache(outputer);
    }

    public void serialize(OMOutputer outputer) throws XMLStreamException {
        super.serialize(outputer);
    }

//    /**
//     * Method getException
//     *
//     * @return
//     * @throws OMException
//     */
//    public Exception getException() throws OMException {
//       getDetail();
//        if (faultDetail == null) {
//            return null;
//        }
//
//        OMElement exceptionElement = faultDetail.getFirstChildWithName(new QName(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY));
//        if(exceptionElement != null){
//             return new Exception(exceptionElement.getText());
//        }
//        return null;
//    }
//
//    protected void putExceptionToSOAPFault(Exception e) {
//        StringWriter sw = new StringWriter();
//        e.printStackTrace(new PrintWriter(sw));
//        getDetail();
//        if (faultDetail == null) {
//            faultDetail = new SOAP11FaultDetailImpl(this);
//
//        }
//        OMElement faultDetailEnty = new OMElementImpl(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY, this.getNamespace());
//        faultDetailEnty.setText(sw.getBuffer().toString());
//        faultDetail.addChild(faultDetailEnty);
//    }
//
//    /**
//     * Equivalent for FaultCode in SOAP 1.1 is faultCode.
//     * So creating faultCode element and putting the value as SOAPFaultCode.SOAPFaultCodeValue.value
//     *
//     * @param soapFaultCode
//     * @throws SOAPProcessingException
//     */
    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException {
        if (!(soapFaultCode instanceof SOAP11FaultCodeImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Code. But received some other implementation");
        }
        super.setCode(soapFaultCode);
    }
//
//    public SOAPFaultCode getCode() {
//        if (faultCode == null) {
//            faultCode = (SOAPFaultCode) getChildWithName(SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME);
//        }
//        return faultCode;
//    }
//
    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        if (!(reason instanceof SOAP11FaultReasonImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Reason. But received some other implementation");
        }
        super.setReason(reason);
    }
//
//    public SOAPFaultReason getReason() {
//        if (faultReason == null) {
//            faultReason = (SOAPFaultReason) getChildWithName(SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME);
//        }
//        return faultReason;
//    }
//
    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        if (!(node instanceof SOAP11FaultNodeImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Node. But received some other implementation");
        }
        super.setNode(node);
    }
//
//    public SOAPFaultNode getNode() {
//        throw new UnsupportedOperationException();
//    }
//
    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        if (!(role instanceof SOAP11FaultRoleImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Role. But received some other implementation");
        }
        super.setRole(role);
    }

    public SOAPFaultDetail getDetail()  {
        if (faultDetail == null || faultDetail.getParent() != this) {
            faultDetail = (SOAPFaultDetail) this.getChildWithName(SOAP11Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
        }
        return faultDetail;
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11BodyImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

}
