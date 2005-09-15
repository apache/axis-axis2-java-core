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

package org.apache.axis2.storage.impl;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterIncludeImpl;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.AxisFault;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

public class AxisFileStorage extends AbstractStorage {

    private File file;
    private FileOutputStream fos;
    private HashMap map;
    private ParameterInclude paramter;

    public AxisFileStorage() {
        map = new HashMap();
        paramter = new ParameterIncludeImpl();
    }

    public AxisFileStorage(File file) {
        this();
        this.setFile(file);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        try {
            this.file = file;
            this.fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException("No such file!");
        }
    }

    public Object put(Object value) {

        try {
            String key = getUniqueKey();
            map.put(key, value);

            updateFileState();

            return key;
        } catch (IOException e) {
            throw new UnsupportedOperationException(e.getMessage());
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getMessage());
        }

    }

    public Object get(Object key) {
        return map.get(key);
    }

    public Object remove(Object key) {
        try {
            Object objToRemove = map.remove(key);

            updateFileState();

            return objToRemove;
        } catch (IOException e) {
            throw new UnsupportedOperationException(" file writing failed!");
        }
    }

    private void updateFileState() throws IOException {
//        ObjectOutput out = new ObjectOutputStream(fos);
//        out.writeObject(map);
//        out.close();
    }

    public boolean clean() {
        try {
            map.clear();
            updateFileState();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void addParameter(Parameter param) throws AxisFault{
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
