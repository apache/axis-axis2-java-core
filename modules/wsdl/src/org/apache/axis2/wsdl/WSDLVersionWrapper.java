/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.wsdl;

import org.apache.wsdl.WSDLDescription;

import javax.wsdl.Definition;

/**
 * @author chathura@opensource.lk
 */
public class WSDLVersionWrapper {

    private Definition definition;

    private WSDLDescription description;


    /**
     * @param description WSDL 2.0 WOM description
     * @param definition  WSDL 1.1 WSDL4J based <code>Definition</code>
     */
    public WSDLVersionWrapper(WSDLDescription description,
                              Definition definition) {
        this.definition = definition;
        this.description = description;
    }

    /**
     * Returns the WSDL 1.1 Definition
     *
     * @return <code>DEfinition</code>
     */
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Returns a WOM description
     *
     * @return <code>WSDLDescription</code>
     */
    public WSDLDescription getDescription() {
        return description;
    }
}
