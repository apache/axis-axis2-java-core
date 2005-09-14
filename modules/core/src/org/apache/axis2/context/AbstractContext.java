package org.apache.axis2.context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;

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
/**
 *  This is the topmost level of the Context hierachy, is potentially a bag of 
 *  properties. 
 */
public abstract class AbstractContext implements Serializable {

    protected transient HashMap nonPersistentMap;
    protected final HashMap persistentMap;
    protected AbstractContext parent;

	public abstract void init (AxisConfiguration axisConfiguration) throws AxisFault;
	
    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();    	
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	nonPersistentMap = new HashMap ();
    }
    
    protected AbstractContext(AbstractContext parent) {
        this.persistentMap = new HashMap();
        this.nonPersistentMap = new HashMap();
        this.parent = parent;
    }

    /**
     * Store an object. depending on the persistent flag the
     * object is either saved in the persistent way or the non-persistent
     * way
     *
     * @param key
     * @param value
     * @param persistent
     */
    public void setProperty(String key, Object value, boolean persistent) {
        if (persistent) {
            persistentMap.put(key, value);
        } else {
            nonPersistentMap.put(key, value);
        }
    }

    /**
     * Store an object with the default persistent flag.
     * default is no persistance
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        this.setProperty(key, value, false);
    }

    /**
     * Retrieve an object. Default search is done in the non persistent
     * group
     *
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        return this.getProperty(key, false);
    }

    /**
     * @param key
     * @param persistent
     * @return
     */
    public Object getProperty(String key, boolean persistent) {
        Object obj = null;
        if (persistent) {
            obj = persistentMap.get(key);
        }
        if (obj == null) {
            obj = nonPersistentMap.get(key);
        }
        if (obj == null && parent != null) {
            obj = parent.getProperty(key, persistent);
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
     * @return
     */
    public AbstractContext getParent() {
        return parent;
    }

}
