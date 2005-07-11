package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;

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
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public abstract class SOAPFaultRoleImpl extends SOAPElement implements org.apache.axis2.soap.SOAPFaultRole {

    public SOAPFaultRoleImpl(SOAPFault parent, boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, extractNamespaceFromParent);
    }

    public SOAPFaultRoleImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, builder);
    }

    public void setRoleValue(String uri) {
        this.setText(uri);
    }

    public String getRoleValue() {
        return this.getText();
    }
}
