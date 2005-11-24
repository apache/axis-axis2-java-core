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

import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultValue;

import javax.xml.namespace.QName;

/**
 * created 31-Oct-2005 13:08:33
 */

public class FaultCode extends AbstractFaultCode {

    String value;

    /**
     * simple constructor
     */
    public FaultCode() {
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

    /**
     * Create a fault code (and all subcodes) from a SOAP Fault Code
     *
     * @param source SOAPFaultCode to parse
     */
    public FaultCode(SOAPFaultCode source) {

        SOAPFaultValue value = source.getValue();
        //what if it is a qname already?
        setValueString(value.getText());
        SOAPFaultSubCode subCode = source.getSubCode();
        if (subCode != null) {
            setSubcode(new FaultSubcode(subCode));
        }
    }

    /**
     * local names are stuck in as a string and turned into a local qname
     *
     * @param value
     */
    public void setValueString(String value) {
        QName newName = new QName(value);
        this.value = newName.toString();
    }

    public void setValue(QName value) {
        setValueString(value.toString());
    }

    public String getValueString() {
        return value;
    }
}
