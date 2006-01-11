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

package org.apache.axis2.soap.impl.dom;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.util.ElementHelper;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.SOAPProcessingException;

public abstract class SOAPFaultSubCodeImpl extends SOAPElement implements SOAPFaultSubCode {

    protected SOAPFaultValue value;
    protected SOAPFaultSubCode subCode;


    protected SOAPFaultSubCodeImpl(OMElement parent, String localName) throws SOAPProcessingException {
        super(parent, localName, true);
    }

    protected SOAPFaultSubCodeImpl(OMElement parent,
                                   String localName,
                                   OMXMLParserWrapper builder) {
        super(parent, localName, builder);
    }

    public void setValue(SOAPFaultValue soapFaultSubCodeValue) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, value, soapFaultSubCodeValue);
    }

    public SOAPFaultValue getValue() {
        if (value == null) {
            value =
                    (SOAPFaultValue) ElementHelper.getChildWithName(this,
                            SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME);
        }
        return value;
    }

    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, this.subCode, subCode);

    }

    public SOAPFaultSubCode getSubCode() {
        if (subCode == null) {
            subCode =
                    (SOAPFaultSubCode) ElementHelper.getChildWithName(this,
                            SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
        }
        return subCode;
    }
}
