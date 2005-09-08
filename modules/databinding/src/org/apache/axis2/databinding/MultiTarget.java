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
package org.apache.axis2.databinding;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A MultiTarget aggregates a bunch of targets into a single one.
 * This is used when multiple targets are waiting for a single SOAP
 * multiref deserialization, for instance.
 */
public class MultiTarget implements DeserializationTarget {
    ArrayList targets = new ArrayList();

    /**
     * Add a target to the list of targets which will be updated when
     * we receive a value.
     *
     * @param target
     */
    public void addTarget(DeserializationTarget target) {
        targets.add(target);
    }

    public void setValue(Object value) throws Exception {
        for (Iterator i = targets.iterator(); i.hasNext();) {
            DeserializationTarget target = (DeserializationTarget) i.next();
            target.setValue(value);
        }
    }
}
