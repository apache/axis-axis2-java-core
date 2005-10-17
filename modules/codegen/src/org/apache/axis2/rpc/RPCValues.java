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
package org.apache.axis2.rpc;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * RPCValues
 */
public class RPCValues {
    Map values = new HashMap();

    public Object getValue(QName paramName) {
        return values.get(paramName);
    }

    public void setValue(QName paramName, Object value) {
        values.put(paramName, value);
    }

    public void setIndexedValue(QName paramName, int index, Object value) {
        ArrayList coll = (ArrayList)values.get(paramName);
        if (coll == null) {
            coll = new ArrayList();
            values.put(paramName, coll);
        }
        if (coll.size() == index) {
            coll.add(value);
            return;
        }

        while (index + 1 > coll.size()) {
            coll.add(null);
        }
        coll.set(index, value);
    }
}
