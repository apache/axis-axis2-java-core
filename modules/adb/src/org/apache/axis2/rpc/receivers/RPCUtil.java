package org.apache.axis2.rpc.receivers;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.reader.NullXMLStreamReader;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.util.StreamWrapper;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
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
*
*
*/

public class RPCUtil {

    private static String RETURN_WRAPPER = "return";

    public static void processResponse(SOAPFactory fac, Object resObject,
                                       OMElement bodyContent,
                                       OMNamespace ns,
                                       SOAPEnvelope envelope,
                                       Method method,
                                       boolean qualified,
                                       TypeTable typeTable) {
        if (resObject != null) {
            //simple type
            if (resObject instanceof OMElement) {
                OMElement result = (OMElement)resObject;
                bodyContent = fac.createOMElement(
                        method.getName() + "Response", ns);
                OMElement resWrapper;
                if (qualified) {
                    resWrapper = fac.createOMElement(RETURN_WRAPPER, ns.getNamespaceURI(),
                                                     ns.getPrefix());
                } else {
                    resWrapper = fac.createOMElement(RETURN_WRAPPER, null);
                }
                resWrapper.addChild(result);
                bodyContent.addChild(resWrapper);
            } else if (SimpleTypeMapper.isSimpleType(resObject)) {
                bodyContent = fac.createOMElement(
                        method.getName() + "Response", ns);
                OMElement child;
                if (qualified) {
                    child = fac.createOMElement(RETURN_WRAPPER, ns);
                } else {
                    child = fac.createOMElement(RETURN_WRAPPER, null);
                }
                child.addChild(fac.createOMText(child, SimpleTypeMapper.getStringValue(resObject)));
                bodyContent.addChild(child);
            } else {
                bodyContent = fac.createOMElement(
                        method.getName() + "Response", ns);
                // Java Beans
                QName returnWrapper;
                if (qualified) {
                    returnWrapper = new QName(ns.getNamespaceURI(), RETURN_WRAPPER, ns.getPrefix());
                } else {
                    returnWrapper = new QName(RETURN_WRAPPER);
                }
                XMLStreamReader xr = BeanUtil.getPullParser(resObject,
                                                            returnWrapper, typeTable, qualified);
                StAXOMBuilder stAXOMBuilder =
                        OMXMLBuilderFactory.createStAXOMBuilder(
                                OMAbstractFactory.getOMFactory(), new StreamWrapper(xr));
                OMElement documentElement = stAXOMBuilder.getDocumentElement();
                if (documentElement != null) {
                    bodyContent.addChild(documentElement);
                }
            }
        }
        if (bodyContent != null) {
            envelope.getBody().addChild(bodyContent);
        }
    }

    public static Object[] processRequest(OMElement methodElement,
                                          Method method, ObjectSupplier objectSupplier)
            throws AxisFault {
        Class[] parameters = method.getParameterTypes();
        return BeanUtil.deserialize(methodElement, parameters, objectSupplier);
    }

    public static OMElement getResponseElement(QName resname,
                                               Object [] objs,
                                               boolean qualified,
                                               TypeTable typeTable) {
        if (qualified) {
            return BeanUtil.getOMElement(resname, objs,
                                         new QName(resname.getNamespaceURI(),
                                                   RETURN_WRAPPER,
                                                   resname.getPrefix()),
                                         qualified,
                                         typeTable);
        } else {
            return BeanUtil.getOMElement(resname, objs,
                                         new QName(RETURN_WRAPPER), qualified,
                                         typeTable);
        }
    }

    public static void processResponse(Object resObject,
                                       AxisService service,
                                       Method method,
                                       SOAPEnvelope envelope,
                                       SOAPFactory fac,
                                       OMNamespace ns,
                                       OMElement bodyContent,
                                       MessageContext outMessage
    ) throws Exception {
        QName elementQName = outMessage.getAxisMessage().getElementQName();
        if (resObject == null) {
            ns = fac.createOMNamespace(service.getSchematargetNamespace(),
                                       service.getSchemaTargetNamespacePrefix());
            OMElement bodyChild = fac.createOMElement(method.getName() + "Response", ns);
            envelope.getBody().addChild(bodyChild);
        } else {
            if (resObject instanceof Object[]) {
                if(Array.getLength(resObject)==0){
                    QName resName;
                    if (service.isElementFormDefault()) {
                        resName = new QName(service.getSchematargetNamespace(),
                                            RETURN_WRAPPER,
                                            service.getSchemaTargetNamespacePrefix());
                    } else {
                        resName = new QName(RETURN_WRAPPER);
                    }
                    XMLStreamReader xr = new NullXMLStreamReader(resName);
                    StreamWrapper parser = new StreamWrapper(xr);
                    StAXOMBuilder stAXOMBuilder =
                            OMXMLBuilderFactory.createStAXOMBuilder(
                                    OMAbstractFactory.getSOAP11Factory(), parser);
                    ns = fac.createOMNamespace(service.getSchematargetNamespace(),
                                               service.getSchemaTargetNamespacePrefix());
                    OMElement bodyChild = fac.createOMElement(method.getName() + "Response", ns);
                    bodyChild.addChild(stAXOMBuilder.getDocumentElement());
                    envelope.getBody().addChild(bodyChild);
                } else {
                    QName resName = new QName(elementQName.getNamespaceURI(),
                                              method.getName() + "Response",
                                              elementQName.getPrefix());
                    OMElement bodyChild = RPCUtil.getResponseElement(resName,
                                                                     (Object[])resObject,
                                                                     service.isElementFormDefault(),
                                                                     service.getTypeTable());
                    envelope.getBody().addChild(bodyChild);
                }
            } else {
                if (resObject.getClass().isArray()) {
                    int length = Array.getLength(resObject);
                    Object objArray [];
                    if (resObject instanceof byte[]) {
                        objArray = new Object[1];
                        objArray[0] = Base64.encode((byte[])resObject);
                    } else {
                        objArray = new Object[length];
                        for (int i = 0; i < length; i++) {
                            objArray[i] = Array.get(resObject, i);
                        }
                    }

                    QName resName = new QName(elementQName.getNamespaceURI(),
                                              method.getName() + "Response",
                                              elementQName.getPrefix());
                    OMElement bodyChild = RPCUtil.getResponseElement(resName,
                                                                             objArray,
                                                                             service.isElementFormDefault(),
                                                                             service.getTypeTable());
                    envelope.getBody().addChild(bodyChild);
                } else {
                    if (SimpleTypeMapper.isCollection(resObject.getClass())) {
                        Collection collection = (Collection)resObject;
                        int size = collection.size();
                        Object values [] = new Object[size];
                        int count = 0;
                        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
                            values[count] = iterator.next();
                            count ++;

                        }
                        QName resName = new QName(elementQName.getNamespaceURI(),
                                                  method.getName() + "Response",
                                                  elementQName.getPrefix());
                        OMElement bodyChild = RPCUtil.getResponseElement(resName,
                                                                         values,
                                                                         service.isElementFormDefault(),
                                                                         service.getTypeTable());
                        envelope.getBody().addChild(bodyChild);
                    } else if (SimpleTypeMapper.isDataHandler(resObject.getClass())) {
                        OMElement resElemt = fac.createOMElement(method.getName() + "Response", ns);
                        OMText text = fac.createOMText(resObject, true);
                        OMElement returnElement;
                        if (service.isElementFormDefault()) {
                            returnElement = fac.createOMElement(RETURN_WRAPPER, ns);
                        } else {
                            returnElement = fac.createOMElement(RETURN_WRAPPER, null);
                        }
                        returnElement.addChild(text);
                        resElemt.addChild(returnElement);
                        envelope.getBody().addChild(resElemt);
                    } else {
                        if (service.isElementFormDefault()) {
                            RPCUtil.processResponse(fac, resObject, bodyContent, ns,
                                                    envelope, method,
                                                    service.isElementFormDefault(),
                                                    service.getTypeTable());
                        } else {
                            RPCUtil.processResponse(fac, resObject, bodyContent, ns,
                                                    envelope, method,
                                                    service.isElementFormDefault(),
                                                    null);
                        }
                    }
                }
            }
        }
        outMessage.setEnvelope(envelope);
    }

    /**
     * This can be used to get the part name of the response
     *
     * @param outMessage : AxisMessage
     * @return String
     */
    private static String getReturnName(AxisMessage outMessage) {
        if (outMessage != null) {
            Object element = outMessage.getSchemaElement();
            if (element instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType xmlSchemaComplexType = (XmlSchemaComplexType)element;
                Object particle = xmlSchemaComplexType.getParticle();
                if (particle instanceof XmlSchemaSequence) {
                    XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence)particle;
                    Object items = xmlSchemaSequence.getItems();
                    if (items instanceof XmlSchemaObjectCollection) {
                        XmlSchemaObjectCollection xmlSchemaObjectCollection =
                                (XmlSchemaObjectCollection)items;
                        Object schemaElement = xmlSchemaObjectCollection.getItem(0);
                        if (schemaElement instanceof XmlSchemaElement) {
                            return ((XmlSchemaElement)schemaElement).getName();
                        }
                    }
                }
            }
        }
        return RETURN_WRAPPER;
    }
}
