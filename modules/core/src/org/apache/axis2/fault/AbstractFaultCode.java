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

import javax.xml.namespace.QName;

/**
 * both FaultCodes and Subcodes implement this. They only really vary in how values are handled;
 * subcodes must have qnames; faultcodes are simple strings.
 * created 31-Oct-2005 14:20:40
 */

public abstract class AbstractFaultCode {
    /**
     * a subcode, may be null.
     */
    private FaultSubcode subcode;

    protected AbstractFaultCode(FaultSubcode subcode) {
        this.subcode = subcode;
    }

    protected AbstractFaultCode() {
    }

    /**
     * Set the value of the fault code.
     * <p/>
     * Subclasses must provide their own specific semantics
     *
     * @param value
     */
    public abstract void setValue(QName value);

    public FaultSubcode getSubcode() {
        return subcode;
    }

    public void setSubcode(FaultSubcode subcode) {
        this.subcode = subcode;
    }

}
