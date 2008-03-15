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

package org.apache.axis2.jaxws.client;

import javax.xml.ws.BindingProvider;
import java.util.HashMap;

public class PropertyValidator {

    private static HashMap<String, Class> map = new HashMap<String, Class>();

    static {
        map.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.class);
        map.put(BindingProvider.USERNAME_PROPERTY, String.class);
        map.put(BindingProvider.PASSWORD_PROPERTY, String.class);
        map.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.class);
        map.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.class);
        map.put(BindingProvider.SOAPACTION_URI_PROPERTY, String.class);
    }

    /**
     * Checks to see if the property value is valid given the name of the property and the type that
     * is expected by JAX-WS.
     *
     * @param propName
     * @param value
     * @return
     */
    public static boolean validate(String propName, Object value) {
        Class expectedType = map.get(propName);
        if (expectedType != null) {
            if (expectedType.equals(value.getClass())) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    public static Class getExpectedValue(String key) {
        return map.get(key);
    }
}
