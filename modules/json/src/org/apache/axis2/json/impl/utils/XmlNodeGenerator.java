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
import org.apache.ws.commons.schema.XmlSchemaObject;
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
        // get the operation schema and process.
        XmlSchema operationSchema = getXmlSchema(elementQname);
        XmlSchemaElement methodElement = operationSchema.getElementByName(elementQname.getLocalPart());
        mainXmlNode = new XmlNode(elementQname.getLocalPart(), elementQname.getNamespaceURI() , false, (methodElement.getMaxOccurs() == 1 ? false : true) , "");
        QName methodSchemaTypeName = methodElement.getSchemaTypeName();
        XmlSchemaType schemaType = null;
        if (methodSchemaTypeName != null){
            schemaType = getXmlSchema(methodSchemaTypeName).getTypeByName(methodSchemaTypeName.getLocalPart());
        } else {
            schemaType = methodElement.getSchemaType();
        }

        if (schemaType != null) {
            processSchemaType(schemaType, mainXmlNode , operationSchema);
        } else {
            // nothing to do
        }
    }

    private void processElement(XmlSchemaElement element, XmlNode parentNode , XmlSchema schema) {
        QName schemaTypeName = element.getSchemaTypeName();
        QName qName = element.getQName();
        String pref = schemaTypeName.getPrefix();
        XmlNode tempNode;
        if (qName == null) {
            tempNode = new XmlNode(element.getName(), parentNode.getNamespaceUri(), false, (element.getMaxOccurs() == 1 ? false : true), schemaTypeName.getLocalPart());

        } else {
            tempNode = new XmlNode(qName.getLocalPart(), qName.getNamespaceURI(), false, (element.getMaxOccurs() == 1 ? false : true), schemaTypeName.getLocalPart());
        }
        parentNode.addChildtoList(tempNode);
        if (("xs").equals(pref)) {
            // this element doesn't has child elements
        } else {
            XmlSchema childSchema = null;
            XmlSchemaElement childEle = schema.getElementByName(schemaTypeName);
            XmlSchemaType childType = schema.getTypeByName(schemaTypeName);
            if (childEle == null && childType == null) {
                childSchema = getXmlSchema(schemaTypeName);
                childEle = childSchema.getElementByName(schemaTypeName);
                childType = childSchema.getTypeByName(schemaTypeName);
            } else {
                childSchema = schema;
            }

            if (childEle == null) {
                processSchemaType(childType, tempNode, childSchema);
            } else {
                processElement(childEle, tempNode, childSchema);
            }
        }

    }


    private void processSchemaType(XmlSchemaType xmlSchemaType , XmlNode parentNode , XmlSchema schema) {
        if (xmlSchemaType instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complexType = (XmlSchemaComplexType)xmlSchemaType;
            XmlSchemaParticle particle = complexType.getParticle();
            if (particle instanceof XmlSchemaSequence) {
                XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
                XmlSchemaObjectCollection objectCollection = sequence.getItems();
                Iterator objectIterator = objectCollection.getIterator();
                while (objectIterator.hasNext()) {
                    Object obj = objectIterator.next();
                    if (obj instanceof XmlSchemaElement) {
                        processElement((XmlSchemaElement)obj , parentNode , schema);
                    }else if (obj instanceof XmlSchemaComplexType || obj instanceof  XmlSchemaSimpleType) {     // never come to this
                        XmlSchemaType schemaType = (XmlSchemaType)obj;
                        processSchemaType(schemaType , parentNode , schema);
                    }
                }
            }
        }else if (xmlSchemaType instanceof XmlSchemaSimpleType) {
            // nothing to do with simpleType
        }
    }


    private XmlSchema getXmlSchema(QName qName) {
        for (XmlSchema xmlSchema : xmlSchemaList) {
            if (xmlSchema.getTargetNamespace().equals(qName.getNamespaceURI())) {
                return xmlSchema;
            }
        }
        return null;
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
