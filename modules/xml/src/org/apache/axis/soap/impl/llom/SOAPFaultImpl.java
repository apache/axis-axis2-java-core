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
package org.apache.axis.soap.impl.llom;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.OMTextImpl;
import org.apache.axis.soap.*;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * Class SOAPFaultImpl
 */
public class SOAPFaultImpl extends OMElementImpl
        implements SOAPFault, OMConstants {
    /**
     * Field e
     */
    private Exception e;

    /**
     * Field faultCodeElement
     */
    private OMElement faultCodeElement;

    /**
     * Field faultActorElement
     */
    private OMElement faultActorElement;

    /**
     * Field faultStringElement
     */
    private OMElement faultStringElement;

    /**
     * Field detailElement
     */
    private OMElement detailElement;

    /**
     * Constructor SOAPFaultImpl
     *
     * @param parent
     * @param e
     */
    public SOAPFaultImpl(OMElement parent, Exception e) {
        super(SOAPConstants.SOAPFAULT_LOCAL_NAME,
                new OMNamespaceImpl(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                        SOAPConstants.SOAPFAULT_NAMESPACE_PREFIX));
        this.parent = (OMElementImpl) parent;
        this.e = e;
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        this.setDetailInformation(OMAbstractFactory.getSOAP11Factory().createText(sw.getBuffer().toString()));
    }

    /**
     * Constructor SOAPFaultImpl
     *
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAPFaultImpl(OMNamespace ns, OMElement parent,
                         OMXMLParserWrapper builder) {
        super(SOAPConstants.SOAPFAULT_LOCAL_NAME, ns, parent, builder);
    }

    /**
     * Method setCode
     *
     * @param faultCode
     * @throws OMException
     */
    public void setCode(SOAPFaultCode faultCode) throws OMException {
        if (faultCodeElement != null) {
            faultCodeElement.detach();
        }
        faultCodeElement =
        new OMElementImpl(SOAPConstants.SOAPFAULT_CODE_LOCAL_NAME, this.ns);
        this.addChild(faultCodeElement);
        faultCodeElement.addChild(new OMTextImpl(faultCodeElement,
                        faultCode.getNamespace().getPrefix() + ':'
                                + faultCode.getLocalName()));
    }

    /**
     * Method getCode
     *
     * @return
     */
    public SOAPFaultCode getCode() {
//        if (faultCodeElement != null) {
//            Iterator childrenIter = faultCodeElement.getChildren();
//            while (childrenIter.hasNext()) {
//                Object o = childrenIter.next();
//                if ((o instanceof OMText)
//                        && !((OMText) o).getText().trim().equals("")) {
//                    String[] strings = ((OMText) o).getText().split(":");
//                    return new QName("", strings[1], strings[0]);
//                }
//            }
//        } else {
//            faultCodeElement =  this.getFirstChildWithName(
//                    new QName(
//                            this.ns.getName(), SOAPConstants.SOAPFAULT_CODE_LOCAL_NAME,
//                            this.ns.getPrefix()));
//            if (faultCodeElement != null) {
//                return this.getCode();
//            }
//        }
        return null;
    }

    public void setReason(SOAPFaultReason reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public SOAPFaultReason getReason() {


        SOAPFaultReason faultReason = new SOAPFaulReasonImpl(this);
        SOAPText soapText = new SOAPTextImpl(faultReason);
        soapText.setText(this.getFaultString());

        faultReason.setSOAPText(soapText);
        return faultReason;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setNode(SOAPFaultNode node) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public SOAPFaultNode getNode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setRole(SOAPFaultRole role) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public SOAPFaultRole getRole() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDetail(SOAPFaultDetail detail) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public SOAPFaultDetail getDetail() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method setFaultActor
     *
     * @param faultActor
     * @throws OMException
     */
    public void setFaultActor(String faultActor) throws OMException {
        if (faultActorElement != null) {
            faultActorElement.detach();
        }
        faultActorElement =
        new OMElementImpl(SOAPConstants.SOAPFAULT_ACTOR_LOCAL_NAME, this.ns);
        this.addChild(faultActorElement);
        faultActorElement.addChild(new OMTextImpl(faultActorElement,
                        faultActor));
    }

    /**
     * Method getFaultActor
     *
     * @return
     */
    public String getFaultActor() {
        if (faultActorElement != null) {
            Iterator childrenIter = faultActorElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if ((o instanceof OMText)
                        && !"".equals(((OMText) o).getText())) {
                    return ((OMText) o).getText();
                }
            }
        } else {
            faultActorElement = this.getFirstChildWithName(
                    new QName(
                            this.ns.getName(), SOAPConstants.SOAPFAULT_ACTOR_LOCAL_NAME,
                            this.ns.getPrefix()));
            if (faultActorElement != null) {
                return this.getFaultString();
            }
        }
        return null;
    }

    /**
     * Method setFaultString
     *
     * @param faultString
     * @throws OMException
     */
    public void setFaultString(String faultString) throws OMException {
        if (faultStringElement != null) {
            faultStringElement.detach();
        }
        faultStringElement =
        new OMElementImpl(SOAPConstants.SOAPFAULT_STRING_LOCAL_NAME, this.ns);
        this.addChild(faultStringElement);
        faultStringElement.addChild(new OMTextImpl(faultStringElement,
                        faultString));
    }

    /**
     * Method getFaultString
     *
     * @return
     */
    public String getFaultString() {
        if (faultStringElement != null) {
            Iterator childrenIter = faultStringElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if ((o instanceof OMText)
                        && !"".equals(((OMText) o).getText())) {
                    return ((OMText) o).getText();
                }
            }
        } else {
            faultStringElement =  this.getFirstChildWithName(
                    new QName(
                            this.ns.getName(), SOAPConstants.SOAPFAULT_STRING_LOCAL_NAME,
                            this.ns.getPrefix()));
            if (faultStringElement != null) {
                return this.getFaultString();
            }
        }
        return null;
    }

    /**
     * Method setDetailInformation
     *
     * @param detailInformation
     */
    public void setDetailInformation(OMNode detailInformation) {
        if (detailElement != null) {
            detailElement.detach();
        }
        detailElement =
        new OMElementImpl(SOAPConstants.SOAPFAULT_DETAIL_LOCAL_NAME, this.ns);
        this.addChild(detailElement);
        detailElement.addChild(detailInformation);
    }

    /**
     * Method getDetailInformation
     *
     * @return
     */
    public OMNode getDetailInformation() {
        if (detailElement != null) {
            Iterator childrenIter = detailElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if (!((o instanceof OMText)
                                 && "".equals(((OMText) o).getText()))) {
                    return (OMNode) o;
                }
            }
        } else {
            detailElement = this.getFirstChildWithName(
                    new QName(
                            this.ns.getName(), SOAPConstants.SOAPFAULT_DETAIL_LOCAL_NAME,
                            this.ns.getPrefix()));
            if (detailElement != null) {
                return detailElement;
            }
        }
        return null;
    }

    /**
     * Method getException
     *
     * @return
     * @throws OMException
     */
    public Exception getException() throws OMException {
        if (e == null) {
            OMNode detailsInformationNode = this.getDetailInformation();
            if (detailsInformationNode instanceof OMElement) {
                try {
                    StringWriter sw = new StringWriter();
                    XMLStreamWriter writer =
                            XMLOutputFactory.newInstance().createXMLStreamWriter(
                            sw);
                    ((OMElement) detailsInformationNode).serializeWithCache(writer);
                    writer.flush();
                    return new Exception(sw.toString());
                } catch (XMLStreamException e1) {
                    throw new OMException("Exception in StAX Writer", e1);
                }
            } else if (detailsInformationNode instanceof OMText) {
                return new Exception(
                        ((OMText) detailsInformationNode).getText());
            }
        } else {
            return e;
        }
        return null;
    }
}
