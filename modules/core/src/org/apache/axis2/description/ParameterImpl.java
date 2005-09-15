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

import org.apache.axis2.om.OMElement;

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
    private Object value;

    /**
     * Field locked
     */
    private boolean locked;

    /**
     * Field type
     */
    private int type = TEXT_PARAMETER;


    /**
     * to store the parameter lement
     * <parameter name="ServiceClass1" locked="false">
     * org.apache.axis2.sample.echo.EchoImpl</parameter>
     */
    private OMElement parameterElement ;

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
    public void setValue(Object value) {
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

    public void setParamterType(int type) {
        this.type =type;
    }

    public void setParameterElement(OMElement element) {
        this.parameterElement = element;
    }

    public OMElement getParameterElement() {
        return this.parameterElement;
    }
}
