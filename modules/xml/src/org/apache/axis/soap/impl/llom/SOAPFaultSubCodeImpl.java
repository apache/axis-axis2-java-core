package org.apache.axis.soap.impl.llom;

import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.OMException;
import org.apache.axis.soap.SOAPFaultSubCode;
import org.apache.axis.soap.SOAPFaultValue;
import org.apache.axis.soap.SOAPFaultValue;
import org.apache.axis.soap.impl.llom.util.UtilProvider;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;

import javax.xml.namespace.QName;

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

public abstract class SOAPFaultSubCodeImpl extends SOAPElement implements SOAPFaultSubCode {

    protected SOAPFaultValue value;
    protected SOAPFaultSubCode subCode;


    protected SOAPFaultSubCodeImpl(OMElement parent, String localName) throws SOAPProcessingException {
        super(parent, localName, true);
    }

    protected SOAPFaultSubCodeImpl(OMElement parent, String localName, OMXMLParserWrapper builder) {
        super(parent, localName, builder);
    }

    public void setValue(SOAPFaultValue soapFaultSubCodeValue) throws SOAPProcessingException {
        UtilProvider.setNewElement(this, value, soapFaultSubCodeValue);
    }

    public SOAPFaultValue getValue() {
        if (value == null) {
            value = (SOAPFaultValue) UtilProvider.getChildWithName(this, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
        }
        return value;
    }

    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        UtilProvider.setNewElement(this, this.subCode, subCode);

    }

    public SOAPFaultSubCode getSubCode() {
        if (subCode == null) {
            subCode = (SOAPFaultSubCode) UtilProvider.getChildWithName(this, SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME);
        }
        return subCode;
    }
}
