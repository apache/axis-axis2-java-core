package org.apache.axis2.storage;

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
public interface AxisStorage {

    /**
     * puts a value to the storage
     * @param value
     * @return the key as an Object
     */
    Object put(Object value);

    /**
     * get a value from the storage. The value is not removed
     * @param key
     * @return the value as an Object
     */
    Object get(Object key);

    /**
     * Removes an object from the storage given the key.
     * @param key
     * @return the value being removed as an object
     */
    Object remove(Object key);

    /**
     * Cleans the whole storage.
     * @return a boolean saying whether the clean was successful or not
     */
    boolean clean();
}
