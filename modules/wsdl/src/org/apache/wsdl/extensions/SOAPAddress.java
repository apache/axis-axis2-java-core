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

package org.apache.wsdl.extensions;

import org.apache.wsdl.WSDLExtensibilityElement;

/**
 * @author chathura@opensource.lk
 *         This Extensibility Element is extended to handle particularly the
 *         SOAP Adress or the Endpoint URL.
 */
public interface SOAPAddress extends WSDLExtensibilityElement {

    /**
     * Gets the Endpoint adress
     */
    public String getLocationURI();

    /**
     * Sets the Endpoint Address
     */
    public void setLocationURI(String locationURI);
}