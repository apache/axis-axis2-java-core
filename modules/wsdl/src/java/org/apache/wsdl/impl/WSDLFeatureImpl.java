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

import org.apache.wsdl.WSDLFeature;

/**
 * @author chathura@opensource.lk
 */
public class WSDLFeatureImpl extends ComponentImpl implements WSDLFeature {
    /**
     * Field name
     */
    private String name;

    /**
     * Field required
     */
    private boolean required;

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return name;
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
     * Method isRequired
     *
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Method setRequired
     *
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }
}
