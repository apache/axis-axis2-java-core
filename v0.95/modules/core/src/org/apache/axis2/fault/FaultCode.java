/*
* Copyright 2005 The Apache Software Foundation.
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


package org.apache.axis2.fault;

import org.apache.ws.commons.soap.SOAPFaultCode;
import org.apache.ws.commons.soap.SOAPFaultSubCode;
import org.apache.ws.commons.soap.SOAPFaultValue;

import javax.xml.namespace.QName;

public class FaultCode extends AbstractFaultCode {
    QName value;

    /**
     * simple constructor
     */
    public FaultCode() {
    }

    /**
     * Create a fault code (and all subcodes) from a SOAP Fault Code
     *
     * @param source SOAPFaultCode to parse
     */
    public FaultCode(SOAPFaultCode source) {
        SOAPFaultValue value = source.getValue();

        // what if it is a qname already?
        setValueString(value.getText());

        SOAPFaultSubCode subCode = source.getSubCode();

        if (subCode != null) {
            setSubcode(new FaultSubcode(subCode));
        }
    }

    /**
     * Constructor to fill in subcodes
     *
     * @param value   fault value (may be null)
     * @param subcode fault subcode (may be null)
     */
    public FaultCode(String value, FaultSubcode subcode) {
        super(subcode);
        setValueString(value);
    }

    public String getValueString() {
        return value.getLocalPart();
    }

    public void setValue(QName value) {
        this.value = value;
    }

    public QName getValue() {
        return value;
    }

    /**
     * local names are stuck in as a string and turned into a local qname
     *
     * @param value
     */
    public void setValueString(String value) {
       this.value = new QName(value);
    }
}
