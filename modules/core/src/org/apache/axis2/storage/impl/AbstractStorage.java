package org.apache.axis2.storage.impl;

import org.apache.axis2.storage.AxisStorage;

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

public abstract class AbstractStorage implements AxisStorage {

    private static int key = 0;

    protected String getUniqueKey() {
        if (key == Integer.MAX_VALUE) {
            key = 0;
        }
        return Integer.toString(key++);
    }
}
