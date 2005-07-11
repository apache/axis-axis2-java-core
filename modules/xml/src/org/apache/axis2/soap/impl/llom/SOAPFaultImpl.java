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
package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.OMElementImpl;
import org.apache.axis2.soap.*;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;

import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * Class SOAPFaultImpl
 */
public abstract class SOAPFaultImpl extends SOAPElement
        implements SOAPFault, OMConstants {

    protected Exception e;

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param e
     */
    public SOAPFaultImpl(SOAPBody parent, Exception e) throws SOAPProcessingException {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, true);
        setException(e);
    }

    public void setException(Exception e) {
        this.e = e;
        putExceptionToSOAPFault(e);
    }

    public SOAPFaultImpl(SOAPBody parent) throws SOAPProcessingException {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, true);
    }

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param builder
     */
    public SOAPFaultImpl(SOAPBody parent, OMXMLParserWrapper builder) {
        super(parent, SOAPConstants.SOAPFAULT_LOCAL_NAME, builder);
    }


    protected abstract SOAPFaultDetail getNewSOAPFaultDetail(SOAPFault fault) throws SOAPProcessingException;

    // --------------- Getters and Settors --------------------------- //

    public void setCode(SOAPFaultCode soapFaultCode) throws SOAPProcessingException {
        setNewElement(getCode(), soapFaultCode);
    }

    public SOAPFaultCode getCode() {
        return (SOAPFaultCode) this.getChildWithName(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME);
    }

    public void setReason(SOAPFaultReason reason) throws SOAPProcessingException {
        setNewElement(getReason(), reason);
    }

    public SOAPFaultReason getReason() {
        return (SOAPFaultReason) this.getChildWithName(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME);
    }

    public void setNode(SOAPFaultNode node) throws SOAPProcessingException {
        setNewElement(getNode(), node);
    }

    public SOAPFaultNode getNode() {
        return (SOAPFaultNode) this.getChildWithName(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME);
    }

    public void setRole(SOAPFaultRole role) throws SOAPProcessingException {
        setNewElement(getRole(), role);
    }

    public SOAPFaultRole getRole() {
        return (SOAPFaultRoleImpl) this.getChildWithName(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME);
    }

    public void setDetail(SOAPFaultDetail detail) throws SOAPProcessingException {
        setNewElement(getDetail(), detail);
    }

    public SOAPFaultDetail getDetail() {
        return (SOAPFaultDetail) this.getChildWithName(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
    }


    // ---------------------------------------------------------------------------------------------//

    public Exception getException() throws OMException {
        getDetail();
        if (getDetail() == null) {
            return new Exception("No Exception element found in the SOAP Detail element");
        }

        OMElement exceptionElement = getDetail().getFirstChildWithName(new QName(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY));
        if (exceptionElement != null) {
            return new Exception(exceptionElement.getText());
        }
        return new Exception("No Exception element found in the SOAP Detail element");
    }

    protected void putExceptionToSOAPFault(Exception e) throws SOAPProcessingException {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        getDetail();
        if (getDetail() == null) {
            setDetail(getNewSOAPFaultDetail(this));

        }
        OMElement faultDetailEnty = new OMElementImpl(SOAPConstants.SOAP_FAULT_DETAIL_EXCEPTION_ENTRY, this.getNamespace());
        faultDetailEnty.setText(sw.getBuffer().toString());
        getDetail().addChild(faultDetailEnty);
    }

    protected void setNewElement(OMElement myElement, OMElement newElement) {
        if (myElement != null) {
            myElement.discard();
        }
        if (newElement != null && newElement.getParent() != null) {
            newElement.discard();
        }
        this.addChild(newElement);
        myElement = newElement;
    }

    protected OMElement getChildWithName(String childName) {
        Iterator childrenIter = getChildren();
        while (childrenIter.hasNext()) {
            OMNode node = (OMNode) childrenIter.next();
            if (node.getType() == OMNode.ELEMENT_NODE && childName.equals(((OMElement) node).getLocalName())) {
                return (OMElement) node;
            }
        }
        return null;
    }

}
