/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.core.context;


import java.util.HashMap;

import org.apache.axis.core.aysnc.MessageQueue;
import org.apache.axis.core.registry.EngineRegistry;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class GlobalContext {
    
    private EngineRegistry registry;
    private HashMap map = new HashMap();
    private MessageQueue queue;
    
    
    /**
     * @return Returns the queue.
     */
    public MessageQueue getQueue() {
        return queue;
    }
    /**
     * @param queue The queue to set.
     */
    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }
    public GlobalContext(EngineRegistry er){
        this.registry = er;
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public void put(String key, Object obj) {
        map.put(key,obj);

    }

    /**
     * @return
     */
    public EngineRegistry getRegistry() {
        return registry;
    }

    /**
     * @param registry
     */
    public void setRegistry(EngineRegistry registry) {
        this.registry = registry;
    }

}
