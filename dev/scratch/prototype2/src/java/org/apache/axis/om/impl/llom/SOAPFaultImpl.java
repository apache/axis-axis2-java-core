package org.apache.axis.om.impl.llom;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPFault;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
public class SOAPFaultImpl extends OMElementImpl implements SOAPFault, OMConstants {

    private Exception e;
    private OMElementImpl faultCodeElement;
    private OMElementImpl faultActorElement;
    private OMElementImpl faultStringElement;
    private OMElementImpl detailElement;

    public SOAPFaultImpl(OMElement parent, Exception e) {
        super(SOAPFAULT_LOCAL_NAME,
                new OMNamespaceImpl(SOAPFAULT_NAMESPACE_URI, SOAPFAULT_NAMESPACE_PREFIX));
        this.parent = (OMElementImpl) parent;
        this.e = e;
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        this.setDetailInformation(OMFactory.newInstance().createText(this, sw.getBuffer().toString()));
    }

    public SOAPFaultImpl(OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(SOAPFAULT_LOCAL_NAME, ns, parent, builder);
    }

    public void setFaultCode(QName faultCode) throws OMException {

        if (faultCodeElement != null) {
            faultCodeElement.detach();
        }
        faultCodeElement = new OMElementImpl(OMConstants.SOAPFAULT_CODE_LOCAL_NAME, this.ns);
        this.addChild(faultCodeElement);
        faultCodeElement.addChild(new OMTextImpl(faultCodeElement, faultCode.getPrefix() + ':' + faultCode.getLocalPart()));

    }

    public QName getFaultCode() {
        if (faultCodeElement != null) {
            Iterator childrenIter = faultCodeElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if (o instanceof OMText && !((OMText) o).getValue().trim().equals("")) {
                    String[] strings = ((OMText) o).getValue().split(":");
                    return new QName("", strings[1], strings[0]);
                }
            }
        }else {
            faultCodeElement = (OMElementImpl) this.getChildWithName(new QName(this.ns.getName(), OMConstants.SOAPFAULT_CODE_LOCAL_NAME, this.ns.getPrefix()));
            if(faultCodeElement != null){
                return this.getFaultCode();
            }
        }
        return null;
    }

    public void setFaultActor(String faultActor) throws OMException {
        if (faultActorElement != null) {
            faultActorElement.detach();
        }
        faultActorElement = new OMElementImpl(OMConstants.SOAPFAULT_ACTOR_LOCAL_NAME, this.ns);
        this.addChild(faultActorElement);
        faultActorElement.addChild(new OMTextImpl(faultActorElement, faultActor));
    }

    public String getFaultActor() {
        if (faultActorElement != null) {
            Iterator childrenIter = faultActorElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if (o instanceof OMText && !"".equals(((OMText) o).getValue())) {
                    return ((OMText) o).getValue();
                }
            }

        }else {
            faultActorElement = (OMElementImpl) this.getChildWithName(new QName(this.ns.getName(), OMConstants.SOAPFAULT_ACTOR_LOCAL_NAME, this.ns.getPrefix()));
            if(faultActorElement != null){
                return this.getFaultString();
            }
        }
        return null;
    }


    public void setFaultString(String faultString) throws OMException {
        if (faultStringElement != null) {
            faultStringElement.detach();
        }
        faultStringElement = new OMElementImpl(OMConstants.SOAPFAULT_STRING_LOCAL_NAME, this.ns);
        this.addChild(faultStringElement);
        faultStringElement.addChild(new OMTextImpl(faultStringElement, faultString));
    }

    public String getFaultString() {
        if (faultStringElement != null) {
            Iterator childrenIter = faultStringElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if (o instanceof OMText && !"".equals(((OMText) o).getValue())) {
                    return ((OMText) o).getValue();
                }
            }
        }else {
            faultStringElement = (OMElementImpl) this.getChildWithName(new QName(this.ns.getName(), OMConstants.SOAPFAULT_STRING_LOCAL_NAME, this.ns.getPrefix()));
            if(faultStringElement != null){
                return this.getFaultString();
            }
        }
        return null;
    }

    public void setDetailInformation(OMNode detail) {
        if (detailElement != null) {
            detailElement.detach();
        }
        detailElement = new OMElementImpl(OMConstants.SOAPFAULT_DETAIL_LOCAL_NAME, this.ns);
        this.addChild(detailElement);
        detailElement.addChild(detail);
    }

    public OMNode getDetailInformation() {
        if (detailElement != null) {
            Iterator childrenIter = detailElement.getChildren();
            while (childrenIter.hasNext()) {
                Object o = childrenIter.next();
                if (!(o instanceof OMText && "".equals(((OMText) o).getValue()))) {
                    StringWriter sw = new StringWriter();
                    return (OMNode) o;
                }
            }
        }else {
            detailElement = (OMElementImpl) this.getChildWithName(new QName(this.ns.getName(), OMConstants.SOAPFAULT_DETAIL_LOCAL_NAME, this.ns.getPrefix()));
            if(detailElement != null){
                return this.getDetailInformation();
            }
        }
        return null;
    }


    public Exception getException() throws OMException {
        if (e == null) {
           
                OMNode detailsInformationNode = this.getDetailInformation();
                if (detailsInformationNode instanceof OMElement) {
                    try {
                        StringWriter sw = new StringWriter();
                        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                        ((OMElement) detailsInformationNode).serialize(writer, true);
                        writer.flush();
                        return new Exception(sw.toString());
                    } catch (XMLStreamException e1) {
                        throw new OMException("Exception in StAX Writer", e1);
                    }
                } else if (detailsInformationNode instanceof OMText) {

                    return new Exception(((OMText) detailsInformationNode).getValue());
                }

        } else {
            return e;
        }
        return null;
    }
}
