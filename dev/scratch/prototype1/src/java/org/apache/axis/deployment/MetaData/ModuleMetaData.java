package org.apache.axis.deployment.MetaData;

import org.apache.axis.deployment.MetaData.ParameterMetaData;

import java.util.Vector;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 18, 2004
 *         3:14:40 PM
 *
 */

/**
 * Either Desirialization of module.xml or <module>...</module> element
 * in service.xml
 */
public class ModuleMetaData {

    private String ref;
    private Vector parameters = new Vector();

    public ModuleMetaData() {
        //just to clear the vector
        parameters.removeAllElements();
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void addParameter(ParameterMetaData parameter) {
        parameters.add(parameter);
    }

    public ParameterMetaData getParameter(int index) {
        return (ParameterMetaData) parameters.get(index);
    }

    public int getParameterCount() {
        return parameters.size();
    }

}
