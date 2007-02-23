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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the top most level of the Context hierarchy and is a bag of properties.
 */
public abstract class AbstractContext {

    /**
     * Property used to indicate copying of properties is needed by context. 
     */
    public static final String COPY_PROPERTIES = "CopyProperties";

    protected long lastTouchedTime;

    protected transient AbstractContext parent;
    protected transient Map properties;

    protected AbstractContext(AbstractContext parent) {
        this.parent = parent;
    }

    protected AbstractContext(){
    }

    /**
     * @return Returns AbstractContext.
     */
    public AbstractContext getParent() {
        return parent;
    }

    public Map getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        return properties;
    }

    /**
     * Retrieves an object given a key.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getProperty(String key) {
        Object obj;

        obj = properties == null ? null : properties.get(key);

        if ((obj == null) && (parent != null)) {
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
     * This will set the properties to the context. But in setting that one may need to "copy" all
     * the properties from the source properties to the target properties. To enable this we introduced
     * a property ({@link #COPY_PROPERTIES}) so that if set to true, this code
     * will copy the whole thing, without just referencing to the source.
     *
     * @param properties
     */
    public void setProperties(Map properties) {
        if (properties == null) {
            this.properties = null;
        } else {
            Boolean copyProperties = ((Boolean) properties.get(COPY_PROPERTIES));

            if ((copyProperties != null) && copyProperties.booleanValue()) {
                mergeProperties(properties);
            } else {
                this.properties = properties;
            }
        }
    }

    /**                         
     * This will do a copy of the given properties to the current properties
     * table.
     *
     * @param props The table of properties to copy
     */
    public void mergeProperties(Map props) {
        if (props != null)
        {
            Iterator iterator = props.keySet().iterator();

            while (iterator.hasNext()) {
                Object key = iterator.next();

                if (this.properties == null)
                {
                    this.properties = new HashMap();
                }

                this.properties.put(key, props.get(key));
            }

        }
    }


    /**
     * Store a property for message context
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        properties.put(key, value);
    }

    /**
     * ServiceContext and ServiceGroupContext are not getting automatically garbage collected. And there
     * is no specific way for some one to go and make it garbage collectible.
     * So the current solution is to make them time out. So the logic is that, there is a timer task
     * in each and every service group which will check for the last touched time. And if it has not
     * been touched for some time, the timer task will remove it from the memory.
     * The touching logic happens like this. Whenever there is a call to addMessageContext in the operationContext
     * it will go and update operationCOntext -> serviceContext -> serviceGroupContext.
     */
    protected void touch() {
        lastTouchedTime = System.currentTimeMillis();
        if (parent != null) {
            parent.touch();
        }
    }

    public long getLastTouchedTime() {
        return lastTouchedTime;
    }

    public void setLastTouchedTime(long t) {
        lastTouchedTime = t;
    }
}
