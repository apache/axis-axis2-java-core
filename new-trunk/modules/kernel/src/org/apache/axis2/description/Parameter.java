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

import org.apache.axiom.om.OMElement;

/**
 * Class ParameterImpl
 */
public class Parameter {

    /**
     * Field TEXT_PARAMETER
     */
    public static int TEXT_PARAMETER = 0;

    /**
     * Field OM_PARAMETER
     */
    public static int OM_PARAMETER = 1;

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
     * to store the parameter element
     * <parameter name="ServiceClass1" locked="false">
     * org.apache.axis2.sample.echo.EchoImpl</parameter>
     */
    private OMElement parameterElement;

    /**
     * Field value
     */
    private Object value;

    /**
     * Constructor.
     */
    public Parameter() {
    }

    /**
     * Constructor from name and value.
     *
     * @param name
     * @param value
     */
    public Parameter(String name, Object value) {
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

    public String toString() {
        return "Parameter : " + name + "=" + value;
    }

    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof Parameter){
            return ((Parameter) obj).name.equals(name);
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
