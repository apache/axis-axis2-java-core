/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.json.impl.utils;


import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class XmlNodeGenerator {

    List<XmlSchema> xmlSchemaList;

    QName elementQname;

    private XmlNode mainXmlNode;

    Queue<JsonObject> queue = new LinkedList<JsonObject>();

    public XmlNodeGenerator(List<XmlSchema> xmlSchemaList, QName elementQname) {
        this.xmlSchemaList = xmlSchemaList;
        this.elementQname = elementQname;
    }

    public XmlNodeGenerator() {
    }

    private void processSchemaList() {
        // get the response schema and find the type then process.
        XmlSchema responseSchema = getXmlSchema(elementQname);
        XmlSchemaElement methodElement = responseSchema.getElementByName(elementQname.getLocalPart());
        QName methodSchemaTypeName = methodElement.getSchemaTypeName();
        mainXmlNode = new XmlNode(elementQname.getLocalPart(), elementQname.getNamespaceURI() , false, (methodElement.getMaxOccurs() == 1 ? false : true) , "");
        XmlSchemaParticle particle = ((XmlSchemaComplexType) methodElement.getSchemaType()).getParticle();
        XmlSchemaSequence sequence = (XmlSchemaSequence) particle;
        XmlSchemaObjectCollection xmlSchemaObjectCollection = sequence.getItems();

        Iterator iterator = xmlSchemaObjectCollection.getIterator();
        while (iterator.hasNext()) {
            Object nextEle = iterator.next();
            if (nextEle instanceof XmlSchemaElement) {
                XmlSchemaElement innerElement = ((XmlSchemaElement) nextEle);   // todo add to xml node
                XmlSchemaType innerEleType = innerElement.getSchemaType();
                if (innerEleType == null) {
                    processSchemaTypeName(innerElement, mainXmlNode);
                } else if (innerEleType instanceof XmlSchemaComplexType) {
                    processComplexType(innerElement , mainXmlNode);
                } else if (innerEleType instanceof XmlSchemaSimpleType) {
                    processSimpleType(innerElement , mainXmlNode);
                }
            }
        }

/*        XmlSchemaElement argElement = (XmlSchemaElement) xmlSchemaObjectCollection.getItem(0);
        XmlSchemaType schemaType = argElement.getSchemaType();
        QName argQname = argElement.getQName();
        QName schemaTypeName = argElement.getSchemaTypeName();
        XmlNode temp;
        if (argQname == null) {
            temp = new XmlNode(argElement.getName(),elementQname.getNamespaceURI(), false, (argElement.getMaxOccurs() == 1 ? false : true) ,schemaTypeName.getLocalPart());
        } else {
            temp = new XmlNode(argQname.getLocalPart(),argQname.getNamespaceURI(), false, (argElement.getMaxOccurs() == 1 ? false : true) ,schemaTypeName.getLocalPart());

        }
        mainXmlNode.addChildtoList(temp);
        String pref = schemaTypeName.getPrefix();
        if (("xs").equals(pref)) {
        } else {
            XmlSchema tempXmlSchema = getXmlSchema(schemaTypeName);
            processXmlSchema(tempXmlSchema, schemaTypeName,temp);
        }*/
    }

    private XmlSchema getXmlSchema(QName qName) {
        for (XmlSchema xmlSchema : xmlSchemaList) {
            if (xmlSchema.getTargetNamespace().equals(qName.getNamespaceURI())) {
                return xmlSchema;
            }
        }
        return null;
    }

    private void processSchemaTypeName(XmlSchemaElement element, XmlNode parentNode) {
        QName schemaTypeName = element.getSchemaTypeName();
        QName qName = element.getQName();
        String pref = schemaTypeName.getPrefix();
        XmlNode temp;
        if (qName == null) {
            temp = new XmlNode(element.getName(), parentNode.getNamespaceUri(), false, (element.getMaxOccurs() == 1 ? false : true), schemaTypeName.getLocalPart());

        } else {
            temp = new XmlNode(qName.getLocalPart(), qName.getNamespaceURI(), false, (element.getMaxOccurs() == 1 ? false : true), schemaTypeName.getLocalPart());
        }
        parentNode.addChildtoList(temp);
        if (("xs").equals(pref)) {
        } else {
            XmlSchema tempXmlSchema = getXmlSchema(schemaTypeName);
            processXmlSchema(tempXmlSchema, schemaTypeName, temp);
        }
    }

    private void processXmlSchema(XmlSchema schema, QName elementQName, XmlNode xmlNode) {
        XmlSchemaElement element = schema.getElementByName(elementQName);
        if (element != null) {
            XmlNode temp = new XmlNode(elementQName.getLocalPart(), elementQName.getNamespaceURI(), false, (element.getMaxOccurs() == 1 ? false : true) , element.getSchemaTypeName().getLocalPart());
            xmlNode.addChildtoList(temp);
        } else {
            XmlSchemaObjectCollection schemaObjectCollection = schema.getItems();
            if (schemaObjectCollection.getCount() != 0) {
                Iterator schemaObjIterator = schemaObjectCollection.getIterator();
                while (schemaObjIterator.hasNext()) {
                    Object next = schemaObjIterator.next();
                    if (next instanceof XmlSchemaComplexType) {
                        XmlSchemaComplexType comtype = (XmlSchemaComplexType) next;
                        if (elementQName.getLocalPart().equals(comtype.getName())) {
                            XmlSchemaParticle particle = comtype.getParticle();
                            if (particle instanceof XmlSchemaSequence) {
                                XmlSchemaSequence schemaSequence = (XmlSchemaSequence) particle;
                                XmlSchemaObjectCollection InnerSchemaObjectCollection = schemaSequence.getItems();
                                Iterator iterator = InnerSchemaObjectCollection.getIterator();
                                while (iterator.hasNext()) {
                                    Object nextEle = iterator.next();
                                    if (nextEle instanceof XmlSchemaElement) {
                                        XmlSchemaElement innerElement = ((XmlSchemaElement) nextEle);   // todo add to xml node
                                        XmlSchemaType innerEleType = innerElement.getSchemaType();
                                        if (innerEleType == null) {
                                            processSchemaTypeName(innerElement, xmlNode);
                                        } else if (innerEleType instanceof XmlSchemaComplexType) {
                                            processComplexType(innerElement , xmlNode);
                                        } else if (innerEleType instanceof XmlSchemaSimpleType) {
                                            processSimpleType(innerElement , xmlNode);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // process simpletype
                    }
                }
            }
        }
    }

    private void generateQueue(XmlNode node) {
        if (node.isArray()) {
            if (node.getChildrenList().size() > 0) {
                queue.add(new JsonObject(node.getName(), JSONType.NESTED_ARRAY, node.getValueType() , node.getNamespaceUri()));
                processXmlNodeChildren(node.getChildrenList());
            } else {
                queue.add(new JsonObject(node.getName(), JSONType.ARRAY , node.getValueType() , node.getNamespaceUri()));
            }
        } else {
            if (node.getChildrenList().size() > 0) {
                queue.add(new JsonObject(node.getName(), JSONType.NESTED_OBJECT, node.getValueType() , node.getNamespaceUri()));
                processXmlNodeChildren(node.getChildrenList());
            } else {
                queue.add(new JsonObject(node.getName(), JSONType.OBJECT , node.getValueType() , node.getNamespaceUri()));
            }
        }
    }

    private void processComplexType(XmlSchemaElement xmlSchemaElement , XmlNode parentNode) {
        QName schemaTypeName = xmlSchemaElement.getSchemaTypeName();
        QName qName = xmlSchemaElement.getQName();
        XmlNode temp = new XmlNode(qName.getLocalPart(), qName.getNamespaceURI(), false, (xmlSchemaElement.getMaxOccurs() == 1 ? false : true) , schemaTypeName.getLocalPart());
        parentNode.addChildtoList(temp);
        XmlSchemaSequence schemaSequence;
        XmlSchemaParticle particle = ((XmlSchemaComplexType)xmlSchemaElement.getSchemaType()).getParticle();
        if (particle instanceof XmlSchemaSequence) {
            schemaSequence = (XmlSchemaSequence) particle;
            XmlSchemaObjectCollection schemaObjectCollection = schemaSequence.getItems();
            Iterator iterator = schemaObjectCollection.getIterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof XmlSchemaElement) {
                    XmlSchemaElement innerElement = ((XmlSchemaElement) element);
                    XmlSchemaType innerEleType = innerElement.getSchemaType();
                    if (innerEleType instanceof XmlSchemaComplexType) {
                        processComplexType(innerElement , temp);
                    } else if(innerEleType instanceof XmlSchemaSimpleType){
                        processSimpleType(innerElement , temp);
                    }
                }
            }
        }
    }

    private void processSimpleType(XmlSchemaElement xmlSchemaElement , XmlNode parentNode) {
        QName schemaTypeName= xmlSchemaElement.getSchemaTypeName();
        QName qName = xmlSchemaElement.getQName();
        XmlNode temp = new XmlNode(qName.getLocalPart(), qName.getNamespaceURI(), false, (xmlSchemaElement.getMaxOccurs() == 1 ? false : true) , schemaTypeName.getLocalPart());
        parentNode.addChildtoList(temp);
    }

    private void processXmlNodeChildren(List<XmlNode> childrenNodes) {
        for (int i = 0; i < childrenNodes.size(); i++) {
            generateQueue(childrenNodes.get(i));
        }
    }

    public XmlNode getMainXmlNode() {
        if (mainXmlNode == null) {
            processSchemaList();
        }
        return mainXmlNode;
    }

    public Queue<JsonObject> getQueue(XmlNode node) {
        generateQueue(node);
        return queue;
    }

}
