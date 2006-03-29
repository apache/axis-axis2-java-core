package org.apache.axis2.description;

import org.apache.wsdl.WSDLExtensibilityElement;
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
*
*
*/

public class AxisExtensiblityElementWrapper {
    //from where this element came from , Port, Portype , biding etc
    public static final int PORT = 1;
    public static final int PORT_TYPE = 2;
    public static final int PORT_BINDING = 3;

    private int type;
    private WSDLExtensibilityElement extensibilityElement;

    public AxisExtensiblityElementWrapper(int type,
                                          WSDLExtensibilityElement extensibilityElement) {
        this.type = type;
        this.extensibilityElement = extensibilityElement;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public WSDLExtensibilityElement getExtensibilityElement() {
        return extensibilityElement;
    }

    public void setExtensibilityElement(WSDLExtensibilityElement extensibilityElement) {
        this.extensibilityElement = extensibilityElement;
    }

}
