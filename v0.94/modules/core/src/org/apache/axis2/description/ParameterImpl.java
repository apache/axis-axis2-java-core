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
     * Field type
     */
    private int type = TEXT_PARAMETER;

    /**
     * Field locked
     */
    private boolean locked;

    /**
     * Field name
     */
    private String name;

    /**
     * to store the parameter lement
     * <parameter name="ServiceClass1" locked="false">
     * org.apache.axis2.sample.echo.EchoImpl</parameter>
     */
    private OMElement parameterElement;

    /**
     * Field value
     */
    private Object value;

    /**
     * Constructor ParameterImpl.
     */
    public ParameterImpl() {
    }

    /**
     * Constructor ParameterImpl.
     *
     * @param name
     * @param value
     */
    public ParameterImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Method getName.
     *
     * @return Returns String.
     */
    public String getName() {
        return name;
    }

    public OMElement getParameterElement() {
        return this.parameterElement;
    }

    /**
     * Method getParameterType.
     *
     * @return Returns int.
     */
    public int getParameterType() {
        return type;
    }

    /**
     * Method getValue.
     *
     * @return Returns Object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Method isLocked.
     *
     * @return Returns boolean.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Method setLocked.
     *
     * @param value
     */
    public void setLocked(boolean value) {
        locked = value;
    }

    /**
     * Method setName.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setParameterElement(OMElement element) {
        this.parameterElement = element;
    }

    public void setParameterType(int type) {
        this.type = type;
    }

    /**
     * Method setValue.
     *
     * @param value
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
