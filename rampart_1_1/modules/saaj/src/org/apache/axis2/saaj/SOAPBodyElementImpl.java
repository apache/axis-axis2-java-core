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
package org.apache.axis2.saaj;

import org.apache.axiom.om.impl.dom.ElementImpl;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

public class SOAPBodyElementImpl extends SOAPElementImpl implements SOAPBodyElement {

    /**
     * @param element
     */
    public SOAPBodyElementImpl(ElementImpl element) {
        super(element);
    }

    public void setParentElement(SOAPElement parent) throws SOAPException {
        if (!(parent instanceof SOAPBody)){
            throw new IllegalArgumentException("Parent is not a SOAPBody");
        }
        this.parentElement = parent;
    }
}
