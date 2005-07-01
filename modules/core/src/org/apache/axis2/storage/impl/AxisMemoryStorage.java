package org.apache.axis2.storage.impl;
import java.util.HashMap;

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
public class AxisMemoryStorage extends AbstractStorage{

    private HashMap objectMap;


    public AxisMemoryStorage() {
        objectMap = new HashMap();
    }

    public Object put(Object value) {
        String key = getUniqueKey();
        objectMap.put(key,value);
        return key;

    }

    public Object get(Object key) {
        return objectMap.get(key);
    }

    public Object remove(Object key) {
        return objectMap.remove(key);
    }

    public boolean clean() {
        boolean returnValue = false;
        try {
            objectMap.clear();
            returnValue = true;
        }catch(Exception e){
            returnValue = false;
        }
        return returnValue;
    }



}
