/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org

 */
package org.apache.axis2.fault;

import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.om.OMException;

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
     * simple constructor
     */
    public FaultSubcode() {
    }

    /**
     * Constructor to fill in subcodes
     *
     * @param value   fault value (may be null)
     * @param subcode fault subcode (may be null)
     */
    public FaultSubcode(QName value, FaultSubcode subcode) {
        super(subcode);
        this.value = value;
    }


    /**
     * Recursively construct from fault information
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
            //what to do here?
            throw new OMException("No QName from " + text);
        }
        SOAPFaultSubCode subCode = source.getSubCode();
        if (subCode != null) {
            setSubcode(new FaultSubcode(subCode));
        }
    }

    /**
     * Get the current failt code value
     *
     * @return
     */
    public QName getValue() {
        return value;
    }

    /**
     * set the value of the fault code
     *
     * @param value new value
     */
    public void setValue(QName value) {
        this.value = value;
    }


    /**
     * Returns a string representation of the object.
     * This only stringifies the base fault
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return value != null ? value.toString() : "[undefined fault]";
    }
}
