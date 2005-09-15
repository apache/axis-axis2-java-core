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
import org.apache.axis2.soap.SOAPFaultText;
import org.apache.axis2.soap.impl.llom.SOAPFaultReasonImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;

public class SOAP12FaultReasonImpl extends SOAPFaultReasonImpl {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    public SOAP12FaultReasonImpl(SOAPFault parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }

    /**
     * @param parent
     */
    public SOAP12FaultReasonImpl(SOAPFault parent) throws SOAPProcessingException {
        super(parent, true);
    }

    public void setSOAPText(SOAPFaultText soapFaultText) throws SOAPProcessingException {
        if (!(soapFaultText instanceof SOAP12FaultTextImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault Text. But received some other implementation");
        }
        super.setSOAPText(soapFaultText);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.2 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }
}
