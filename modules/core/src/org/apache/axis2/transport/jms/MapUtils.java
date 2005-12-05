/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

package org.apache.axis2.transport.jms;

import java.util.Map;

/**
 * MapUtils provides convenience methods for accessing a java.util.Map
 */
public class MapUtils {
    /**
     * Returns an int property from a Map and removes it.
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return old value
     */
    public static int removeIntProperty(Map properties, String key, int defaultValue) {
        int value = defaultValue;
        if (properties != null && properties.containsKey(key)) {
            try {
                value = ((Integer) properties.remove(key)).intValue();
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    /**
     * Returns a long property from a Map and removes it.
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return old value
     */
    public static long removeLongProperty(Map properties, String key, long defaultValue) {
        long value = defaultValue;
        if (properties != null && properties.containsKey(key)) {
            try {
                value = ((Long) properties.remove(key)).longValue();
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    /**
     * Returns a String property from a Map and removes it.
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return old value
     */
    public static String removeStringProperty(Map properties, String key, String defaultValue) {
        String value = defaultValue;
        if (properties != null && properties.containsKey(key)) {
            try {
                value = (String) properties.remove(key);
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    /**
     * Returns a boolean property from a Map and removes it.
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return old value
     */
    public static boolean removeBooleanProperty(Map properties, String key, boolean defaultValue) {
        boolean value = defaultValue;
        if (properties != null && properties.containsKey(key)) {
            try {
                value = ((Boolean) properties.remove(key)).booleanValue();
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    /**
     * Returns an Object property from a Map and removes it.
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return old value
     */
    public static Object removeObjectProperty(Map properties, String key, Object defaultValue) {
        Object value = defaultValue;
        if (properties != null && properties.containsKey(key)) {
            value = properties.remove(key);
        }
        return value;
    }
}
