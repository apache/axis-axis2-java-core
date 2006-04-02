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

package org.apache.wsdl.extensions.impl;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.wsdl.impl.WSDLExtensibilityElementImpl;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Stack;

public class SchemaImpl extends WSDLExtensibilityElementImpl implements ExtensionConstants,
        Schema {

    private Element element;
    private QName name;
    private Stack importedSchemaStack= new Stack();
    private XmlSchema schema;

    public SchemaImpl() {
        type = SCHEMA;
    }

    public Stack getImportedSchemaStack() {
        return importedSchemaStack;
    }

    public void setImportedSchemaStack(Stack importedSchemaStack) {
        this.importedSchemaStack = importedSchemaStack;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
       this.name = name;
    }

    /**
     * @return The schema Element as a DOM element
     */
    public Element getElement() {
        return element;
    }

    /**
     * Sets the Schema Element as a DOM Element.
     *
     * @param elelment
     */
    public void setElement(Element elelment) {
        this.element = elelment;
        if (importedSchemaStack.isEmpty()){
            importedSchemaStack.push(elelment);
        }
    }

    /**
     * @see org.apache.wsdl.extensions.Schema#getSchema()
     * @return
     */
    public XmlSchema getSchema() {
        return schema;
    }

    /**
     * @see Schema#setSchema(org.apache.ws.commons.schema.XmlSchema) 
     * @param s
     */
    public void setSchema(XmlSchema s) {
        schema = s;
    }
}
