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
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.context.Replicator;

import java.util.Collections;
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
    private transient Map propertyDifferences = new HashMap();

    protected AbstractContext(AbstractContext parent) {
        this.parent = parent;
    }

    protected AbstractContext() {
    }

    /**
     * @return Returns the parent of this context.
     */
    public AbstractContext getParent() {
        return parent;
    }

    /**
     * @return The properties
     * @deprecated Use {@link #getPropertyNames()}, {@link #getProperty(String)},
     *             {@link #setProperty(String, Object)} & {@link #removeProperty(String)}instead.
     */
    public Map getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        return properties;
    }

    /**
     * An iterator over a collection of <code>String</code> objects, which are the
     * keys in the properties object.
     *
     * @return Iterator over a collection of keys
     */
    public Iterator getPropertyNames() {
        if (properties == null) {
            properties = new HashMap();
        }
        return properties.keySet().iterator();
    }

    /**
     * Retrieves an object given a key.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getProperty(String key) {
        Object obj = properties == null ? null : properties.get(key);
        if ((obj == null) && (parent != null)) {
            obj = parent.getProperty(key);
        } else {

            // Assume that a property is which is read may be updated.
            // i.e. The object pointed to by 'value' may be modified after it is read
            addPropertyDifference(key);
        }
        return obj;
    }

    /**
     * Retrieves an object given a key. The retrieved property will not be replicated to
     * other nodes in the clustered scenario.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getPropertyNonReplicable(String key) {
        Object obj = properties == null ? null : properties.get(key);
        if ((obj == null) && (parent != null)) {
            obj = parent.getPropertyNonReplicable(key);
        }
        return obj;
    }

    /**
     * Store a property in this context
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        properties.put(key, value);
        addPropertyDifference(key);
    }

    private void addPropertyDifference(String key) {
        // Add the property differences only if Context replication is enabled,
        // and there are members in the cluster
        ClusterManager clusterManager = getRootContext().getAxisConfiguration().getClusterManager();
        if (clusterManager != null &&
            clusterManager.getContextManager() != null &&
            clusterManager.getMemberCount() != 0) {
            propertyDifferences.put(key, new PropertyDifference(key, false));
        }
    }

    /**
     * Store a property in this context.
     * But these properties should not be replicated when Axis2 is clustered.
     *
     * @param key
     * @param value
     */
    public void setNonReplicableProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        properties.put(key, value);
    }

    /**
     * Remove a property. Only properties at this level will be removed.
     * Properties of the parents cannot be removed using this method.
     *
     * @param key
     */
    public void removeProperty(String key) {
        if (properties != null) {
            properties.remove(key);
        }
        propertyDifferences.put(key, new PropertyDifference(key, true));
    }

    /**
     * Remove a property. Only properties at this level will be removed.
     * Properties of the parents cannot be removed using this method.
     * The removal of the property will not be replicated when Axis2 is clustered.
     *
     * @param key
     */
    public void removePropertyNonReplicable(String key) {
        if (properties != null) {
            properties.remove(key);
        }
    }

    /**
     * Get the property differences since the last transmission by the clustering
     * mechanism
     *
     * @return The property differences
     */
    public Map getPropertyDifferences() {
        return Collections.unmodifiableMap(propertyDifferences);
    }

    /**
     * Once the clustering mechanism transmits the property differences,
     * it should call this method to avoid retransmitting stuff that has already
     * been sent.
     */
    public void clearPropertyDifferences() {
        propertyDifferences.clear();
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
        if (props != null) {
            if (this.properties == null) {
                this.properties = new HashMap();
            }
            for (Iterator iterator = props.keySet().iterator();
                 iterator.hasNext();) {
                Object key = iterator.next();
                this.properties.put(key, props.get(key));
            }
        }
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

    public void flush() throws AxisFault {
        Replicator.replicate(this);
    }

    public abstract ConfigurationContext getRootContext();

}
