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

package org.apache.wsdl.extensions;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Stack;

public interface Schema extends WSDLExtensibilityElement {

    /**
     *
     * @return The QName of this schema
     */
    public QName getName();

    /**
     *  @param  name of this schema
     */
     public void setName(QName name);

    /**
     * @return The schema Element as a DOM element
     */
    public Element getElement();

    /**
     * Sets the Schema Element as a DOM Element.
     * @param element
     */
    public void setElement(Element element);
    /**
     *
     *
     */
    public Stack getImportedSchemaStack();
    /**
     * 
     * @param importedSchemaStack
     */
    public void setImportedSchemaStack(Stack importedSchemaStack) ;

    /**
     * gets the commons XMLSchema object associated 
     * @return
     */
    public XmlSchema getSchema();

    /**
     *
     * @param s
     */
    public void setSchema(XmlSchema s);

}