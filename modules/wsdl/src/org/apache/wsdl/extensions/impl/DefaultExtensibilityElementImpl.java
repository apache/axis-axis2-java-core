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

import org.apache.wsdl.extensions.DefaultExtensibilityElement;
import org.apache.wsdl.impl.WSDLExtensibilityElementImpl;
import org.w3c.dom.Element;

/**
 *         This would be the default Extension class in case that the Extensibility Element
 *         in the WSDL file could not be mapped to a particular Extensibility Element. In
 *         which case the entire Extensibility Element will be kept as a DOM Element
 */
public class DefaultExtensibilityElementImpl extends
        WSDLExtensibilityElementImpl implements DefaultExtensibilityElement {

    private Element element;

    /**
     * @return The Extensibility Element as a DOM Element
     */
    public Element getElement() {
        return element;
    }

    /**
     * Sets the DOM Element as the extensibility Elements
     * Content.
     */
    public void setElement(Element element) {
        this.element = element;
    }

}