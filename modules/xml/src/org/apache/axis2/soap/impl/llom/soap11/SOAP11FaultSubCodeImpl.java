package org.apache.axis2.soap.impl.llom.soap11;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.impl.llom.SOAPFaultSubCodeImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;
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

public class SOAP11FaultSubCodeImpl extends SOAPFaultSubCodeImpl {
    //changed
    public SOAP11FaultSubCodeImpl(SOAPFaultCode parent) throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
    }

    //changed
    public SOAP11FaultSubCodeImpl(SOAPFaultCode parent,
                                  OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME, builder);
    }

    public SOAP11FaultSubCodeImpl(SOAPFaultSubCode parent) throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
    }

    public SOAP11FaultSubCodeImpl(SOAPFaultSubCode parent,
                                  OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME, builder);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultSubCodeImpl) ||
                (parent instanceof SOAP11FaultCodeImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP FaultSubCode or SOAP FaultCode as the parent. But received some other implementation");
        }
    }

    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!((parent instanceof SOAP11FaultSubCodeImpl) || (parent instanceof SOAP11FaultCodeImpl))) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Sub Code. But received some other implementation");
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue soapFaultSubCodeValue) throws SOAPProcessingException {
        if (!(soapFaultSubCodeValue instanceof SOAP11FaultValueImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.1 implementation of SOAP Fault Value. But received some other implementation");
        }
        super.setValue(soapFaultSubCodeValue);
    }


}
