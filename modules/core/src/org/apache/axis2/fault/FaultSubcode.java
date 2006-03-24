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

import org.apache.axiom.om.OMException;
import org.apache.ws.commons.soap.SOAPFaultSubCode;
import org.apache.ws.commons.soap.SOAPFaultValue;

import javax.xml.namespace.QName;

/**
 * OM-neutral representation of a SOAP1.2 fault code for use in AxisFaults
 * created 28-Oct-2005 16:52:29
 */
public class FaultSubcode extends AbstractFaultCode {

    /**
     * in a subcode this can be an arbitrary qname.
     * In a s12:Fault/s12:Code/s12:Value form, a limited set of values
     * are permitted, as documented in
     *
     * @see <a href="http://www.w3.org/TR/2003/REC-soap12-part1-20030624/#faultcodes">SOAP Spec</a>
     */
    private QName value;

    /**
     * Simple constructor.
     */
    public FaultSubcode() {
    }

    /**
     * Recursively construct from fault information.
     *
     * @param source
     */
    public FaultSubcode(SOAPFaultSubCode source) {
        SOAPFaultValue value = source.getValue();
        String text = value.getText();
        QName qname = source.resolveQName(text);

        if (qname != null) {
            setValue(qname);
        } else {

            // what to do here?
            throw new OMException("No QName from " + text);
        }

        SOAPFaultSubCode subCode = source.getSubCode();

        if (subCode != null) {
            setSubcode(new FaultSubcode(subCode));
        }
    }

    /**
     * Constructor to fill in subcodes.
     *
     * @param value   fault value (may be null)
     * @param subcode fault subcode (may be null)
     */
    public FaultSubcode(QName value, FaultSubcode subcode) {
        super(subcode);
        this.value = value;
    }

    /**
     * Returns a string representation of the object (base fault).
     *
     * @return Returns a string representation of the object.
     */
    public String toString() {
        return (value != null)
                ? value.toString()
                : "[undefined fault]";
    }

    /**
     * Gets the current failt code value.
     *
     * @return Returns QName.
     */
    public QName getValue() {
        return value;
    }

    /**
     * Sets the value of the fault code.
     *
     * @param value new value
     */
    public void setValue(QName value) {
        this.value = value;
    }
}
