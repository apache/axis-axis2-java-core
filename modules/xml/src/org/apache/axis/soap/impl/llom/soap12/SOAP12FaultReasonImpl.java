package org.apache.axis.soap.impl.llom.soap12;

import org.apache.axis.soap.impl.llom.SOAPFaultReasonImpl;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.soap.SOAPFaultText;
import org.apache.axis.soap.SOAPFault;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.OMException;

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
public class SOAP12FaultReasonImpl extends SOAPFaultReasonImpl{
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
            throw new SOAPProcessingException("Expecting SOAP 1.2 implementation of SOAP Fault Text. But received some other implementation");
        }
        super.setSOAPText(soapFaultText);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP12FaultImpl)) {
            throw new SOAPProcessingException("Expecting SOAP 1.2 implementation of SOAP Fault as the parent. But received some other implementation");
        }
    }
}
