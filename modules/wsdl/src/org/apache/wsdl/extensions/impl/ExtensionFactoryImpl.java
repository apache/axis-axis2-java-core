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

package org.apache.wsdl.extensions.impl;

import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.ExtensionFactory;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public class ExtensionFactoryImpl implements ExtensionFactory,
        ExtensionConstants {
    /**
     * Returns the correct "Specific" ExtensibilityElement given the
     * <code>QName</code>
     *
     * @param qName QName of the ExtensibilityElement found in the WSDL
     * @return the Specific implementation for the particular QName given.
     */
    public WSDLExtensibilityElement getExtensionElement(QName qName) {
        if (SOAP_ADDRESS.equals(qName))
            return new SOAPAddressImpl();
        if (SCHEMA.equals(qName))
            return new SchemaImpl();
        if (SOAP_OPERATION.equals(qName))
            return new SOAPOperationImpl();
        if (SOAP_BODY.equals(qName))
            return new SOAPBodyImpl();
        if (SOAP_BINDING.equals(qName))
            return new SOAPBindingImpl();
        if (SOAP_HEADER.equals(qName))
            return new SOAPHeadeImpl();
        return new DefaultExtensibilityElementImpl();
    }

}
