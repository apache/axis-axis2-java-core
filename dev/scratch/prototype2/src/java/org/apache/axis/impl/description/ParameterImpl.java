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
package org.apache.axis.impl.description;

import org.apache.axis.description.Parameter;




/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class ParameterImpl implements Parameter  {
    private String name;
    private String value;
    private boolean locked;
    private int type = TEXT_PARAMETER;

    public ParameterImpl() {
    }

    public ParameterImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean value) {
        locked = value;
    }


    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
    public int getParameterType() {
        return type;
    }

}
