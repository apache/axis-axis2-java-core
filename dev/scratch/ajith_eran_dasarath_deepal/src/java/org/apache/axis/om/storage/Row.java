package org.apache.axis.om.storage;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Ajith Ranabahu
 *         Date: Sep 16, 2004
 *         Time: 6:17:59 PM
 */
public abstract class Row  {

    // TODO sizeof(object) > sizeof(int) ???
    /* this key is of type Object, simply because we wanted to make this a unique one.
     * and this Object reference is unique. */

    protected Object key;

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }
}
