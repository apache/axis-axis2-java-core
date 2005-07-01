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
package org.apache.axis2.description;

/**
 * Class ParameterImpl
 */
public class ParameterImpl implements Parameter {
    /**
     * Field name
     */
    private String name;

    /**
     * Field value
     */
    private String value;

    /**
     * Field locked
     */
    private boolean locked;

    /**
     * Field type
     */
    private int type = TEXT_PARAMETER;

    /**
     * Constructor ParameterImpl
     */
    public ParameterImpl() {
    }

    /**
     * Constructor ParameterImpl
     *
     * @param name
     * @param value
     */
    public ParameterImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method setValue
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Method isLocked
     *
     * @return
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Method setLocked
     *
     * @param value
     */
    public void setLocked(boolean value) {
        locked = value;
    }

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Method getValue
     *
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * Method getParameterType
     *
     * @return
     */
    public int getParameterType() {
        return type;
    }
}
