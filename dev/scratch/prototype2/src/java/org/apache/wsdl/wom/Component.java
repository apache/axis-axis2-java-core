/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.wsdl.wom;

import java.util.HashMap;

public interface Component {
    /**
     * Returns the properties that are specific to this WSDL Component.
     * 
     */
    public HashMap getComponentProperties();

    /**
     * Sets the properties of the Component if any.
     */
    public void setComponentProperties(HashMap properties);

    /**
     * Will set the property keyed with the relavent key
     * @param key Key in the map
     * @param obj Object to be put
     */
    public void setComponentProperty(Object key, Object obj);

    /**
     * Gets the component property
     * @param key key for the map search.
     * @return
     */
    public Object getComponentProperty(Object key);
}