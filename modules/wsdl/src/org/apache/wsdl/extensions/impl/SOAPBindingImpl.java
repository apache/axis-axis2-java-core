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

import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPBinding;
import org.apache.wsdl.impl.WSDLExtensibilityElementImpl;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public class SOAPBindingImpl extends WSDLExtensibilityElementImpl implements ExtensionConstants,
        SOAPBinding {

    protected String style;
    protected String transportURI;

    public SOAPBindingImpl() {
        this.type = SOAP_11_BINDING;
    }

    public SOAPBindingImpl(QName type) {
        this.type = type;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getTransportURI() {
        return transportURI;
    }

    public void setTransportURI(String transportURI) {
        this.transportURI = transportURI;
    }
}
