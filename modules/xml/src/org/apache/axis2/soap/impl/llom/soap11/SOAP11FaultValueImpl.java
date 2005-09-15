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

package org.apache.axis2.soap.impl.llom.soap11;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.impl.llom.SOAPFaultValueImpl;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;

public class SOAP11FaultValueImpl extends SOAPFaultValueImpl {
    public SOAP11FaultValueImpl(OMElement parent) throws SOAPProcessingException {
        super(parent);
    }

    public SOAP11FaultValueImpl(OMElement parent, OMXMLParserWrapper builder) {
        super(parent, builder);
    }


    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!((parent instanceof SOAP11FaultSubCodeImpl) ||
                (parent instanceof SOAP11FaultCodeImpl))) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP FaultSubCode or SOAP FaultCode as the parent. But received some other implementation." +
                    parent.getClass());
        }
    }
}
