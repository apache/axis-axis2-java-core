/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.http;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pass-Thru / delayed get and put of the values from HttpServletRequest
 */
public class TransportHeaders implements Map<String,String> {
    HttpServletRequest req;
    // This map contains the headers from the request; it will be filled in lazily if needed, 
    // for performance 
    Map<String,String> headerMap = null;
    // This map contains properties that have been put onto the map; it is not populated by values
    // from the HttpServletRequest.  A null value means the headerMap has been fully populated and
    // any values that were in localHeaderMap have been migrated to headerMap.
    Map<String,String> localHeaderMap = new HashMap<String,String>();

    public TransportHeaders(HttpServletRequest req) {
        this.req = req;
    }

    /**
     * This will fully populate the HashMap with the value from the HttpSerlvetRequest and migrate
     * any values previously put onto localHeaderMap into the new HashMap.
     * 
     * Note this is a bit non-performant, so it is only done if needed.  
     * If/when it is done, there may be properties that have been set on the localHeaderMap.  
     * If headerMap must be created due to a call
     * to size, or isEmpty, or some other method which requires a fully populated map, then any 
     * previously created entries in the localHeaderMap are migrated to the new hashmap.  
     * After that localHeaderMap is released and only headerMap is used after that. 
     */
    private void init() {
        headerMap = new HashMap<String,String>();
        Enumeration<?> headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = req.getHeader(key);

            headerMap.put(key, value);
        }
        
        // Migrate any previously set local properties to the newly created hashmap then release
        // the local hashmap
        for (Map.Entry<String,String> localHeaderEntry : localHeaderMap.entrySet()) {
            headerMap.put(localHeaderEntry.getKey(), localHeaderEntry.getValue());
        }
        localHeaderMap = null;
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
        if (localHeaderMap != null) {
            localHeaderMap.clear();
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

    public Collection<String> values() {
        if (headerMap == null) {
            init();
        }
        return headerMap.values();
    }

    public void putAll(Map<? extends String,? extends String> t) {
        if (headerMap == null) {
            init();
        }
        headerMap.putAll(t);
    }

    public Set<Map.Entry<String,String>> entrySet() {
        if (headerMap == null) {
            init();
        }
        return headerMap.entrySet();
    }

    public Set<String> keySet() {
        if (headerMap == null) {
            init();
        }
        return headerMap.keySet();
    }

    public String get(Object key) {
        // If there is a local map, look there first.
        if (localHeaderMap != null) {
            String returnValue = null;
            returnValue = localHeaderMap.get(key);
            if (returnValue != null) {
                return returnValue;
            }
        }
        if (headerMap == null) {
            return req.getHeader((String) key);
        }
        return headerMap.get(key);
    }

    public String remove(Object key) {
        if (headerMap == null) {
            init();
        }
        return headerMap.remove(key);
    }

    public String put(String key, String value) {
        if (localHeaderMap != null) {
            return localHeaderMap.put(key, value);
        }
        if (headerMap == null) {
            init();
        }
        return headerMap.put(key, value);
    }
}
