/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.wsdl.impl.WSDLExtensibilityElementImpl;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public class SOAPBodyImpl extends WSDLExtensibilityElementImpl implements ExtensionConstants,
        SOAPBody {

    protected String use;
    protected String namespaceURI;

    public SOAPBodyImpl() {
        this.type = SOAP_11_BODY;
    }

    public SOAPBodyImpl(QName type) {
        this.type = type;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }
}
