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

package org.apache.wsdl;

import org.apache.ws.commons.schema.XmlSchemaElement;

import javax.xml.namespace.QName;

public interface MessageReference extends ExtensibleComponent {
    /**
     * Method getDirection
     *
     * @return
     */
    public String getDirection();

    /**
     * Method setDirection
     *
     * @param direction
     */
    public void setDirection(String direction);

    /**
     * This Element refers to the actual message that will get transported. This Element
     * Abstracts all the Message Parts that was defined in the WSDL 1.1.
     *
     */
    public QName getElementQName();

    /**
     * Method setElementQName
     *
     * @param element
     */
    public void setElementQName(QName element);

    /**
     * Method getMessageLabel
     *
     * @return
     */
    public String getMessageLabel();

    /**
     * Method setMessageLabel
     *
     * @param messageLabel
     */
    public void setMessageLabel(String messageLabel);

    public XmlSchemaElement getElementSchema();
    public void setElementSchema(XmlSchemaElement element);
}
