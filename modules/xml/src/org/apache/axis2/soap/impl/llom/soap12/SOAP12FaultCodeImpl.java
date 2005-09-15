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

package org.apache.axis2.soap.impl.llom.soap12;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.impl.llom.SOAPFaultCodeImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;

public class SOAP12FaultCodeImpl extends SOAPFaultCodeImpl {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAP12FaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    /**
     * @param parent
     * @param parent
     */
    public SOAP12FaultCodeImpl(SOAPFault parent) throws SOAPProcessingException {
        super(parent, true);
    }


    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!(subCode instanceof SOAP12FaultSubCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Sub Code. But received some other implementation");
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        if (!(value instanceof SOAP12FaultValueImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Value. But received some other implementation");
        }
        super.setValue(value);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }
}
