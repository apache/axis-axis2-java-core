package org.apache.axis.context;

import java.io.Serializable;
import java.util.HashMap;

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
 *
 * 
 */
public abstract class AbstractContext implements Serializable {

    protected transient final HashMap nonPersistentMap;
    protected final HashMap persistentMap;
    private final AbstractContext parent;

    protected AbstractContext(AbstractContext parent) {
        this.persistentMap = new HashMap();
        this.nonPersistentMap = new HashMap();
        this.parent = parent;
    }

    /**
     * Store an object. depending on the persistent flag the
     * object is either saved in the persistent way or the non-persistent
     * way
     * @param key
     * @param value
     * @param persistent
     */
    public void put(Object key, Object value, boolean persistent) {
        if (persistent) {
            persistentMap.put(key, value);
        } else {
            nonPersistentMap.put(key, value);
        }
    }

    /**
     * Store an object with the default persistent flag.
     * default is no persistance
     * @param key
     * @param value
     */
    public void put(Object key, Object value) {
        this.put(key, value, false);
    }
    /**
     * Retrieve an object. Default search is done in the non persistent
     * group
     * @param key
     * @return
     */
    public Object get(Object key) {
        return this.get(key, false);
    }

    /**
     *
     * @param key
     * @param persistent
     * @return
     */
    public Object get(Object key, boolean persistent) {
        Object obj;
        if (persistent) {
            obj = persistentMap.get(key);

        } else {
            return nonPersistentMap.get(key);
        }
        if (obj == null && parent != null) {
            obj = parent.get(key, persistent);
        }
        return obj;
    }
}
