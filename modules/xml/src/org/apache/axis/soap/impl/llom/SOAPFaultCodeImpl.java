package org.apache.axis.soap.impl.llom;

import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.OMException;
import org.apache.axis.soap.SOAPFaultCode;
import org.apache.axis.soap.SOAPFaultValue;
import org.apache.axis.soap.SOAPFaultSubCode;
import org.apache.axis.soap.SOAPFault;
import org.apache.axis.soap.impl.llom.util.UtilProvider;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;

import javax.xml.namespace.QName;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public abstract class SOAPFaultCodeImpl extends SOAPElement implements SOAPFaultCode {

    protected SOAPFaultValue value;
    protected SOAPFaultSubCode subCode;

    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAPFaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, builder);
    }

    /**
     * @param parent
     * @param parent
     */
    public SOAPFaultCodeImpl(SOAPFault parent, boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, extractNamespaceFromParent);
    }

       /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        UtilProvider.setNewElement(this, value, value);
    }

    public SOAPFaultValue getValue() {
        if (value == null) {
            value = (SOAPFaultValue) UtilProvider.getChildWithName(this, SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME);
        }
        return value;
    }

    public void setSubCode(SOAPFaultSubCode value) throws SOAPProcessingException {
        UtilProvider.setNewElement(this, subCode, value);
    }

    public SOAPFaultSubCode getSubCode() {
        if (subCode == null) {
            subCode = (SOAPFaultSubCode) UtilProvider.getChildWithName(this, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
        }
        return subCode;
    }
}
