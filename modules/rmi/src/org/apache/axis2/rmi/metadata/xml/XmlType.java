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
package org.apache.axis2.rmi.metadata.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.util.Constants;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;


public class XmlType {

    /**
     * Qualified name of the xmlType
     */
    private QName qname;

    /**
     * is this an anAnonymous type this case qname can be null
     */
    private boolean isAnonymous;

    /**
     *  is this is a basic type
     */
    private boolean isSimpleType;

    /**
     * list of child elements
     */
    private List elements;

    /**
     * complex type element for this XmlType
     */
    private Element complexElement;

    /**
     * parent type for this xml type if it is an extension
     *
     */
    private XmlType parentType;


    public void addElement(XmlElement xmlElement){
        this.elements.add(xmlElement);
    }

    /**
     * this generates the complex type only if it is annonymous and
     * is not a simple type
     * @param document
     * @param namespacesToPrefixMap
     * @throws SchemaGenerationException
     */
    public void generateWSDLSchema(Document document,
                                   Map namespacesToPrefixMap)
                                   throws SchemaGenerationException {
        // here we have to generate the complex type element for this xmlType
        if (!this.isSimpleType){
            String xsdPrefix = (String) namespacesToPrefixMap.get(Constants.URI_2001_SCHEMA_XSD);
            this.complexElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"complexType");
            this.complexElement.setPrefix(xsdPrefix);
            if (!this.isAnonymous){
               this.complexElement.setAttribute("name", this.qname.getLocalPart());
            }

            Element sequenceElement = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"sequence");
            sequenceElement.setPrefix(xsdPrefix);

            // set the extension details if there are
            if (this.parentType != null){

                // i.e this is an extension type
                Element complexContent = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"complexContent");
                complexContent.setPrefix(xsdPrefix);
                this.complexElement.appendChild(complexContent);

                Element extension = document.createElementNS(Constants.URI_2001_SCHEMA_XSD,"extension");
                extension.setPrefix(xsdPrefix);
                complexContent.appendChild(extension);

                String extensionPrefix =
                        (String) namespacesToPrefixMap.get(this.parentType.getQname().getNamespaceURI());
                String localPart = this.parentType.getQname().getLocalPart();
                if ((extensionPrefix == null) || extensionPrefix.equals("")){
                    extension.setAttribute("base",localPart);
                } else {
                    extension.setAttribute("base",extensionPrefix + ":" + localPart);
                }
               extension.appendChild(sequenceElement);
            } else {
               this.complexElement.appendChild(sequenceElement);
            }

            // add the other element children
            XmlElement xmlElement;
            for (Iterator iter = this.elements.iterator();iter.hasNext();){
                xmlElement = (XmlElement) iter.next();
                xmlElement.generateWSDLSchema(document,namespacesToPrefixMap);
                sequenceElement.appendChild(xmlElement.getElement());
            }
        }
    }

    public XmlType() {
        this.elements = new ArrayList();
    }

    public XmlType(QName qname) {
        this();
        this.qname = qname;
    }

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public boolean isSimpleType() {
        return isSimpleType;
    }

    public void setSimpleType(boolean simpleType) {
        isSimpleType = simpleType;
    }

    public List getElements() {
        return elements;
    }

    public void setElements(List elements) {
        this.elements = elements;
    }

    public Element getComplexElement() {
        return complexElement;
    }

    public void setComplexElement(Element complexElement) {
        this.complexElement = complexElement;
    }

    public XmlType getParentType() {
        return parentType;
    }

    public void setParentType(XmlType parentType) {
        this.parentType = parentType;
    }

}
