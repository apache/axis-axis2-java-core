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

package org.apache.axis.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author hemapani@opensource.lk
 */
public class HashedBaundle {
    private ArrayList list;
    private HashMap map;
    public HashedBaundle(){
        list = new ArrayList();
        map = new HashMap();
    }
    public void add(Object key,Object obj){
        list.add(obj);
        map.put(key,obj);
    }
    public Object get(Object key){
        return map.get(key);
    }
    public int getCount(){
        return list.size();
    }
    public Object get(int index){
        return this.list.get(index);
    }
    public void remove(Object key){
        Object obj = map.remove(key);
        list.remove(obj);
    }
    
}
