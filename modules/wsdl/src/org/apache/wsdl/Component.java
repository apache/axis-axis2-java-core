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

package org.apache.wsdl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chathura@opensource.lk
 */
public interface Component {
    /**
     * Returns the properties that are specific to this WSDL Component.
     *
     * @return
     */
    public HashMap getComponentProperties();

    /**
     * Sets the properties of the Component if any.
     *
     * @param properties
     */
    public void setComponentProperties(HashMap properties);

    /**
     * Will set the property keyed with the relavent key
     *
     * @param key Key in the map
     * @param obj Object to be put
     */
    public void setComponentProperty(Object key, Object obj);

    /**
     * Gets the component property
     *
     * @param key key for the map search.
     * @return
     */
    public Object getComponentProperty(Object key);

    /**
     * Adds the <code>ExtensibilityElement</code> to the Extensible Component.
     *
     * @param element
     */
    public void addExtensibilityElement(WSDLExtensibilityElement element);

    /**
     * Returns the Extensibility Elements of the Extensible component;
     *
     * @return List of <code>Element</code>s
     */
    public List getExtensibilityElements();

    /**
     * Returns a <code>List</code> of ExtensibleAttributes of this component.
     *
     * @return <code>List</code>
     */
    public List getExtensibilityAttributes();

    /**
     * Adds the <code>ExtensibilityAttribute</code> as a attrebute of this
     * Component.
     *
     * @param attribute <code>ExtensibilityAttribute</code>
     */
    public void addExtensibleAttributes(WSDLExtensibilityAttribute attribute);

    /**
     * Get access to the metadata bag associated with this component (which
     * contains anything we feel like hanging off it)
     *
     * @return the metadata Map.
     */
    public Map getMetadataBag();
}
