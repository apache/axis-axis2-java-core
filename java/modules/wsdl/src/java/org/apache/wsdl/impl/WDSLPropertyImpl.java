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
package org.apache.wsdl.impl;

import org.apache.wsdl.WSDLProperty;

/**
 * @author chathura@opensource.lk
 */
public class WDSLPropertyImpl extends ComponentImpl implements WSDLProperty {
    /**
     * Field uri
     */
    private String uri;

    // TODO replace with the  data binding object structure

    /**
     * Field constraint
     */
    private Object constraint;

    // TODO replace with the  data binding object structure

    /**
     * Field value
     */
    private Object value;

    /**
     * Method getConstraint
     *
     * @return
     */
    public Object getConstraint() {
        return constraint;
    }

    /**
     * Method setConstraint
     *
     * @param constraint
     */
    public void setConstraint(Object constraint) {
        this.constraint = constraint;
    }

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return uri;
    }

    /**
     * Method setName
     *
     * @param uri
     */
    public void setName(String uri) {
        this.uri = uri;
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
     * Method setValue
     *
     * @param value
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
