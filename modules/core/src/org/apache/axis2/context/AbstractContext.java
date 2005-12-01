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

package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the top most level of the Context hierachy and is a bag of properties.
 */
public abstract class AbstractContext {

    protected Map properties;

    protected AbstractContext parent;

    public abstract void init(AxisConfiguration axisConfiguration) throws AxisFault;

    protected AbstractContext(AbstractContext parent) {
        this.properties = new HashMap();
        this.parent = parent;
    }


    /**
     * Store a property for message context
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Retrieves an object given a key.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getProperty(String key) {
        Object obj = null;
        obj = properties.get(key);
        if (obj == null && parent != null) {
            obj = parent.getProperty(key);
        }
        return obj;
    }

    /**
     * @param context
     */
    public void setParent(AbstractContext context) {
        parent = context;
    }

    /**
     * @return Returns AbstractContext.
     */
    public AbstractContext getParent() {
        return parent;
    }

    public Map getProperties() {
        return properties;
    }

    /**
     * This will set the properties to the context. But in setting that one may need to "copy" all
     * the properties from the source properties to the target properties. To enable this we introduced
     * a property (org.apache.axis2.client.Options#COPY_PROPERTIES) so that if set to true, this code
     * will copy the whole thing, without just referencing to the source.
     *
     * @param properties
     * @see org.apache.axis2.client.Options#COPY_PROPERTIES
     */

    public void setProperties(Map properties) {
        if (properties == null) {
            this.properties = null;
        } else {
            Boolean copyProperties = ((Boolean) properties.get(Options.COPY_PROPERTIES));
            if (copyProperties != null && copyProperties.booleanValue()) {
                Iterator iterator = properties.keySet().iterator();
                while (iterator.hasNext()) {
                    Object key = iterator.next();
                    this.properties.put(key, properties.get(key));
                }
            } else {
                this.properties = properties;
            }
        }
    }
}
