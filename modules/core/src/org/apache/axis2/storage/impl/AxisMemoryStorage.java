package org.apache.axis2.storage.impl;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.ParameterIncludeImpl;
import org.apache.axis2.AxisFault;

import java.util.HashMap;
import java.util.ArrayList;

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

public class AxisMemoryStorage extends AbstractStorage {

    private HashMap objectMap;
     private ParameterInclude paramter;


    public AxisMemoryStorage() {
        objectMap = new HashMap();
        paramter = new ParameterIncludeImpl();
    }

    public Object put(Object value) {
        String key = getUniqueKey();
        objectMap.put(key, value);
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
        } catch (Exception e) {
            returnValue = false;
        }
        return returnValue;
    }


    public void addParameter(Parameter param) throws AxisFault {
        paramter.addParameter(param);
    }

    public Parameter getParameter(String name) {
        return paramter.getParameter(name);
    }

    public ArrayList getParameters() {
        return paramter.getParameters();
    }

    //to check whether the paramter is locked at any levle
    public boolean isParamterLocked(String paramterName) {
        return paramter.isParamterLocked(paramterName);
    }

}
