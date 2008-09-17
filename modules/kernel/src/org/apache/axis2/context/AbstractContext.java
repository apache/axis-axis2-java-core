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


package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.context.Replicator;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the top most level of the Context hierarchy and is a bag of properties.
 */
public abstract class AbstractContext {

    private static final Log log = LogFactory.getLog(AbstractContext.class);
    
    private static final int DEFAULT_MAP_SIZE = 64;
    private static boolean DEBUG_ENABLED = log.isDebugEnabled();
    private static boolean DEBUG_PROPERTY_SET = false;
    
    /**
     * Property used to indicate copying of properties is needed by context.
     */
    public static final String COPY_PROPERTIES = "CopyProperties";

    protected long lastTouchedTime;

    protected transient AbstractContext parent;

    protected transient Map properties;
    private transient Map propertyDifferences;

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
     * @param context
     * @return true if the context is an ancestor
     */
    public boolean isAncestor(AbstractContext context) {
        if (context == null) {
            return false;
        }
        for (AbstractContext ancestor = getParent();
            ancestor != null;
            ancestor = ancestor.getParent()) {
            if (ancestor == context) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return The properties
     * @deprecated Use {@link #getPropertyNames()}, {@link #getProperty(String)},
     *             {@link #setProperty(String, Object)} & {@link #removeProperty(String)}instead.
     */
    public Map getProperties() {
        initPropertiesMap();
        return properties;
    }

    /**
     * An iterator over a collection of <code>String</code> objects, which are the
     * keys in the properties object.
     *
     * @return Iterator over a collection of keys
     */
    public Iterator getPropertyNames() {
        initPropertiesMap();
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
        if (obj!=null) {
            // Assume that a property which is read may be updated.
            // i.e. The object pointed to by 'value' may be modified after it is read
            addPropertyDifference(key, obj, false);
        } else if (parent!=null) {
            obj = parent.getProperty(key);
        } 
        return obj;
    }

    /**
     * Retrieves an object given a key. Only searches at this level
     * i.e. getLocalProperty on MessageContext does not look in
     * the OperationContext properties map if a local result is not
     * found.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getLocalProperty(String key) {
        Object obj = properties == null ? null : properties.get(key);
        if ((obj == null) && (parent != null)) {
            // This is getLocalProperty() don't search the hierarchy.
        } else {

            // Assume that a property is which is read may be updated.
            // i.e. The object pointed to by 'value' may be modified after it is read
            addPropertyDifference(key, obj, false);
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
        initPropertiesMap();
        properties.put(key, value);
        addPropertyDifference(key, value, false);
        if (DEBUG_ENABLED) {
            debugPropertySet(key, value);
        }
    }

    private void addPropertyDifference(String key, Object value,  boolean isRemoved) {
        
        if (!needPropertyDifferences()) {
            return;
        }
        // Narrowed the synchronization so that we only wait
        // if a property difference is added.
        synchronized(this) {
            // Lazizly create propertyDifferences map
            if (propertyDifferences == null) {
                propertyDifferences = new HashMap(DEFAULT_MAP_SIZE);
            }
            propertyDifferences.put(key, new PropertyDifference(key, value, isRemoved));
        }
    }
    
    /**
     * @return true if we need to store property differences for this 
     * context in this scenario.
     */
    private boolean needPropertyDifferences() {
        
        // Don't store property differences if there are no 
        // cluster members.
        
        ConfigurationContext cc = getRootContext();
        if (cc == null) {
            return false;
        }
        // Add the property differences only if Context replication is enabled,
        // and there are members in the cluster
        ClusterManager clusterManager = cc.getAxisConfiguration().getClusterManager();
        if (clusterManager == null ||
            clusterManager.getContextManager() == null) {
            return false;
        }
        return true;
    }

    /**
     * Store a property in this context.
     * But these properties should not be replicated when Axis2 is clustered.
     *
     * @param key
     * @param value
     */
    public void setNonReplicableProperty(String key, Object value) {
        initPropertiesMap();
        properties.put(key, value);
    }

    /**
     * Remove a property. Only properties at this level will be removed.
     * Properties of the parents cannot be removed using this method.
     *
     * @param key
     */
    public synchronized void removeProperty(String key) {
        if(properties == null){
            return;
        }
        Object value = properties.get(key);
        if (value != null) {
            if (properties != null) {
                properties.remove(key);
            }
            addPropertyDifference(key, value, true);
        }
    }

    /**
     * Remove a property. Only properties at this level will be removed.
     * Properties of the parents cannot be removed using this method.
     * The removal of the property will not be replicated when Axis2 is clustered.
     *
     * @param key
     */
    public synchronized void removePropertyNonReplicable(String key) {
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
    public synchronized Map getPropertyDifferences() {
        if (propertyDifferences == null) {
            propertyDifferences = new HashMap(DEFAULT_MAP_SIZE);
        }
        return propertyDifferences;
    }

    /**
     * Once the clustering mechanism transmits the property differences,
     * it should call this method to avoid retransmitting stuff that has already
     * been sent.
     */
    public synchronized void clearPropertyDifferences() {
        if (propertyDifferences != null) {
            propertyDifferences.clear();
        }
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
                
                if (this.properties != properties) {
                    if (DEBUG_ENABLED) {
                        for (Iterator iterator = properties.entrySet().iterator();
                        iterator.hasNext();) {
                            Entry entry = (Entry) iterator.next();
                            debugPropertySet((String) entry.getKey(), entry.getValue());

                        }
                    }
                }
                // The Map we got argument is probably NOT an instance of the Concurrent 
                // map we use to store properties, so create a new one using the values from the
                // argument map.
                this.properties = new ConcurrentHashMapNullSemantics(properties);
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
            initPropertiesMap();
            for (Iterator iterator = props.keySet().iterator();
                 iterator.hasNext();) {
                Object key = iterator.next();
                Object value = props.get(key);
                this.properties.put(key, value);
                if (DEBUG_ENABLED) {
                    debugPropertySet((String) key, value);
                }
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

    /**
     * Debug for for property key and value.
     * @param key
     * @param value
     */
    private void debugPropertySet(String key, Object value) {
        if (DEBUG_PROPERTY_SET) {
            String className = (value == null) ? "null" : value.getClass().getName();
            String classloader = "null";
            if(value != null) {
                ClassLoader cl = Utils.getObjectClassLoader(value);
                if(cl != null) {
                    classloader = cl.toString();
                }
            }
            String valueText = (value instanceof String) ? value.toString() : null;
            String identity = getClass().getName() + '@' + 
                Integer.toHexString(System.identityHashCode(this));
            
            log.debug("==================");
            log.debug(" Property set on object " + identity);
            log.debug("  Key =" + key);
            if (valueText != null) {
                log.debug("  Value =" + valueText);
            }
            log.debug("  Value Class = " + className);
            log.debug("  Value Classloader = " + classloader);
            log.debug(  "Call Stack = " + JavaUtils.callStackToString());
            log.debug("==================");
        }
    }
    
    /**
     * If the 'properties' map has not been allocated yet, then allocate it. 
     */
    private void initPropertiesMap() {
        if (properties == null) {
            // This needs to be a concurrent collection to prevent ConcurrentModificationExcpetions
            // for async-on-the-wire.  It was originally: 
//            properties = new HashMap(DEFAULT_MAP_SIZE);
            properties = new ConcurrentHashMapNullSemantics(DEFAULT_MAP_SIZE);
        }
    }
}

/**
 * ConcurrentHashMap that supports the same null semantics of HashMap, which means allowing null
 * as a key and/or value.  ConcurrentHashMap throws an NullPointerException if either of those
 * are null.  This is done by representing null keys and/or values with a non-null value stored in
 * the collection and mapping to actual null values for the methods such as put, get, and remove.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
class ConcurrentHashMapNullSemantics<K,V> extends ConcurrentHashMap<K,V> {

    private static final long serialVersionUID = 3740068332380174316L;
    
    // Constants to represent a null key or value in the collection since actual null values 
    // will cause a NullPointerExcpetion in a ConcurrentHashMap collection.
    private static final Object NULL_KEY_INDICATOR = new Object();
    private static final Object NULL_VALUE_INDICATOR = new Object();
    
    public ConcurrentHashMapNullSemantics() {
        super();
    }
    public ConcurrentHashMapNullSemantics(int initialCapacity) {
        super(initialCapacity);
    }

    public ConcurrentHashMapNullSemantics(Map<? extends K, ? extends V> map) { 
        super(map);
    }
    
    /**
     * Similar to ConcurrentHashMap.put except null is allowed for the key and/or value.
     * @see java.util.concurrent.ConcurrentHashMap#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
        return (V) valueFromMap(super.put((K) keyIntoMap(key), (V) valueIntoMap(value)));
    }
    
    /**
     * Similar to ConcurrentHashMap.get except null is allowed for the key and/or value.
     * @see java.util.concurrent.ConcurrentHashMap#get(java.lang.Object)
     */
    public V get(Object key) {
        return (V) valueFromMap(super.get(keyIntoMap(key)));
    }
        
    /**
     * Similar to ConcurrentHashMap.remove except null is allowed for the key and/or value.
     * @see java.util.concurrent.ConcurrentHashMap#remove(java.lang.Object)
     */
    public V remove(Object key) {
        // If the key is null, then look for the null key constant value in the table.
        return (V) valueFromMap(super.remove(keyIntoMap(key)));
    }
    
    /**
     * Similar to entrySet EXCEPT (1) nulls are allowed for keys and values and (2) this 
     * does NOT RETURN a LIVE SET.  Any changes made to the returned set WILL NOT be reflected in 
     * the underlying collection.  Also, any changes made  to the collection after the set is 
     * returned WILL NOT be reflected in the set.  This method returns a copy of the entries in the 
     * underlying collection at the point it is called.  
     * 
     * @see java.util.concurrent.ConcurrentHashMap#entrySet()
     */
    public Set<Map.Entry<K,V>> entrySet() { 
        // The super returns a ConcurrentHashMap$EntrySet
        Set<Map.Entry<K, V>> collectionSet = super.entrySet();
        Set<Map.Entry<K, V>> returnSet = new HashSet<Map.Entry<K, V>>();
        Iterator<Map.Entry<K, V>> collectionSetIterator = collectionSet.iterator();
        // Go through the set that will be returned mapping keys and values back to null as
        // appropriate.
        while (collectionSetIterator.hasNext()) {
            Map.Entry entry = collectionSetIterator.next();
            Object useKey = keyFromMap(entry.getKey());
            Object useValue = valueFromMap(entry.getValue());
            Map.Entry<K, V> returnEntry = new SetEntry<K, V>((K) useKey, (V) useValue);
            returnSet.add(returnEntry);
        }
        return returnSet;
    }
    
    /**
     * Similar to keySet EXCEPT (1) nulls are allowed for keys and (2) this 
     * does NOT RETURN a LIVE SET.  Any changes made to the returned set WILL NOT be reflected in 
     * the underlying collection.  Also, any changes made  to the collection after the set is 
     * returned WILL NOT be reflected in the set.  This method returns a copy of the keys in the 
     * underlying collection at the point it is called.  
     * 
     * @see java.util.concurrent.ConcurrentHashMap#keySet()
     */
    public Set<K> keySet() {
        Set<K> keySet = new SetKey<K>(super.keySet());
        return keySet;
    }

    /**
     * Similar to containsKey except a null key is allowed.
     * 
     * @see java.util.concurrent.ConcurrentHashMap#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return super.containsKey(keyIntoMap(key));
    }

    // REVIEW: Some of these may already work.  Any that are using put or get to access the elements
    // in the Map will work.  These are not currently used for AbstractContext properties, so there 
    // are no tests yet to verify they work (except as noted below)
    public boolean contains(Object value) {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public Enumeration<V> elements() {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public Enumeration<K> keys() {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    // Note that putAll(..) works for nulls because it is using put(K,V) for each element in the
    // argument Map.
//    public void putAll(Map<? extends K, ? extends V> t) {
//        throw new UnsupportedOperationException("Not implemented for null semantics");
//    }
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public V replace(K key, V value) {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    public Collection<V> values() {
        throw new UnsupportedOperationException("Not implemented for null semantics");
    }
    
    /**
     * Returns a key that can be set into the collection.  If the key is null, a non-null value 
     * representing a null key is returned.
     * @param key The key to be set into the collection; it can be null
     * @return key if it is non-null, or a constant representing a null key if key was null.
     */
    private Object keyIntoMap(Object key) {
        if (key == null) {
            return NULL_KEY_INDICATOR;
        } else {
            return key;
        }
    }

    /**
     * Returns a key that was retrieved from the collection.  If the key the constant representing
     * a previously put null key, then null will be returned.  Otherwise, the key is returned.
     * @param key The key retreived from the collection
     * @return null if the key represents a previously put key that was null, otherwise the 
     * value of key is returned.
     */
    private Object keyFromMap(Object key) {
        if (key == NULL_KEY_INDICATOR) {
            return null;
        } else {
            return key;
        }
    }
    
    /**
     * Returns a value that can be set into the collection.  If the valuse is null, a non-null value 
     * representing a null value is returned.
     * @param value The value to be set into the collection; it can be null
     * @return value if it is non-null, or a constant representing a null value if value was null.
     */
    private Object valueIntoMap(Object value) {
        if (value == null) {
            return NULL_VALUE_INDICATOR;
        } else {
            return value;
        }
    }

    /**
     * Returns a value that was retrieved from the collection.  If the value the constant representing
     * a previously put null value, then null will be returned.  Otherwise, the value is returned.
     * @param value The value retreived from the collection
     * @return null if the value represents a previously put value that was null, otherwise the 
     * value of value is returned.
     */
    private Object valueFromMap(Object value) {
        if (value == NULL_VALUE_INDICATOR) {
            return null;
        } else {
            return value;
        }
    }
    
    /**
     * An Entry to be returned in a Set for the elements in the collection.  Note that both the key
     * and the value may be null.
     * 
     * @param <K> Key type
     * @param <V> Value type
     */
    class SetEntry<K, V> implements Map.Entry<K, V> {
        private K theKey = null;
        private V theValue = null;
        
        SetEntry(K key, V value) {
            this.theKey = key;
            this.theValue = value;
        }
        
        public K getKey() {
            return this.theKey;
        }

        public V getValue() {
            return this.theValue;
        }

        public V setValue(V value) {
            this.theValue = value;
            return this.theValue;
        }
        
    }
    
    /**
     * A set of Keys returned from the collection.  Note that a key may be null.
     * 
     * @param <K> Key type
     */
    class SetKey<K> extends AbstractSet<K> {
        Set<K> set = null;
        SetKey(Set collectionKeySet) {
            set = new HashSet<K>();
            Iterator collectionIterator = collectionKeySet.iterator();
            while (collectionIterator.hasNext()) {
                Object useKey = collectionIterator.next();
                set.add((K) keyFromMap(useKey));
            }
        }
        @Override
        public Iterator<K> iterator() {
            return set.iterator();
        }

        @Override
        public int size() {
            return set.size();
        }
    }
}
