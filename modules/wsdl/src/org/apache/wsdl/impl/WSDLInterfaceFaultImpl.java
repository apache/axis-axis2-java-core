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

import org.apache.wsdl.WSDLInterfaceFault;

import javax.xml.namespace.QName;

public class WSDLInterfaceFaultImpl extends ComponentImpl implements WSDLInterfaceFault {

    /**
     * Consist of the NCName and the namespace as the target namespace of the
     * Description Component.
     */
    private QName name;

    private QName element;


    public QName getElement() {
        return element;
    }

    public void setElement(QName element) {
        this.element = element;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }
}
