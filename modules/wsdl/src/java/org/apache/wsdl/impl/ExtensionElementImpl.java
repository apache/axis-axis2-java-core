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
package org.apache.wsdl.impl;

import org.apache.wsdl.ExtensionElement;
import org.w3c.dom.Element;

/**
 * @author chathura@opensource.lk
 */
public class ExtensionElementImpl implements ExtensionElement {
    /**
     * Field element
     */
    private Element element;

    /**
     * Field required
     */
    private boolean required;

    /**
     * Method isRequired
     *
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Method setRequired
     *
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Method getElement
     *
     * @return
     */
    public Element getElement() {
        return element;
    }

    /**
     * Method setElement
     *
     * @param element
     */
    public void setElement(Element element) {
        this.element = element;
    }
}
