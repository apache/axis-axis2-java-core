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

import java.util.ArrayList;
import java.util.List;
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
