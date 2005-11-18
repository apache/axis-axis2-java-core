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

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * A list of fault reasons
 */

public class FaultReasonList {

    private List reasons;

    public FaultReasonList(int size) {
        reasons = new ArrayList(size);
    }

    public FaultReasonList() {
        this(1);
    }

    /**
     * Add a new fault reason
     *
     * @param reason new reason
     */
    public void add(FaultReason reason) {
        reasons.add(reason);
    }

    /**
     * Add a new fault reason
     *
     * @param text     text of message
     * @param language language, can be ""
     */
    public void add(String text, String language) {
        add(new FaultReason(text, language));
    }

    /**
     * List iterator.
     *
     * @return iterator over elements of type FaultReason
     */
    public ListIterator iterator() {
        return reasons.listIterator();
    }

    /**
     * get the first reason text in the array. This is to downconvert to SOAP1.1
     *
     * @return the text of the first element in the array, or null for no
     *         reasons
     */
    public String getFirstReasonText() {
        if (reasons.size() > 0) {
            return reasons.get(0).toString();
        } else {
            return null;
        }
    }

    /**
     * get at the underlying reasons. Useful for java1.5 iteration.
     *
     * @return the list of reasons
     */
    public List getReasons() {
        return reasons;
    }

}
