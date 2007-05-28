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

package org.apache.axis2.transport.http;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pass-Thru / delayed get of the values from HttpServletRequest
 */
public class TransportHeaders implements Map {
    HttpServletRequest req;
    HashMap headerMap = null;

    public TransportHeaders(HttpServletRequest req) {
        this.req = req;
    }

    private void init() {
        headerMap = new HashMap();
        Enumeration headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = req.getHeader(key);

            headerMap.put(key, value);
        }
    }

    public int size() {
        if (headerMap == null) {
            init();
        }
        return headerMap.size();
    }

    public void clear() {
        if (headerMap != null) {
            headerMap.clear();
        }
    }

    public boolean isEmpty() {
        if (headerMap == null) {
            init();
        }
        return headerMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        if (headerMap == null) {
            init();
        }
        return headerMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if (headerMap == null) {
            init();
        }
        return headerMap.containsValue(value);
    }

    public Collection values() {
        if (headerMap == null) {
            init();
        }
        return headerMap.values();
    }

    public void putAll(Map t) {
        if (headerMap == null) {
            init();
        }
        headerMap.putAll(t);
    }

    public Set entrySet() {
        if (headerMap == null) {
            init();
        }
        return headerMap.entrySet();
    }

    public Set keySet() {
        if (headerMap == null) {
            init();
        }
        return headerMap.keySet();
    }

    public Object get(Object key) {
        if (headerMap == null) {
            return req.getHeader((String) key);
        }
        return headerMap.get(key);
    }

    public Object remove(Object key) {
        if (headerMap == null) {
            init();
        }
        return headerMap.remove(key);
    }

    public Object put(Object key, Object value) {
        if (headerMap == null) {
            init();
        }
        return headerMap.put(key, value);
    }
}
