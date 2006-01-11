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

package org.apache.wsdl.extensions.impl;

import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.ExtensionFactory;

import javax.xml.namespace.QName;

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
        if (SOAP_11_ADDRESS.equals(qName))
            return new SOAPAddressImpl();
        if (SCHEMA.equals(qName))
            return new SchemaImpl();
        if (SOAP_11_OPERATION.equals(qName))
            return new SOAPOperationImpl();
        if (SOAP_11_BODY.equals(qName))
            return new SOAPBodyImpl();
        if (SOAP_11_BINDING.equals(qName))
            return new SOAPBindingImpl();
        if (SOAP_11_HEADER.equals(qName))
            return new SOAPHeadeImpl();
        //soap 1.2 objects.
        if (SOAP_12_OPERATION.equals(qName))
            return new SOAPOperationImpl(SOAP_12_OPERATION);
        if (SOAP_12_BODY.equals(qName))
            return new SOAPBodyImpl(SOAP_12_BODY);
        if (SOAP_12_BINDING.equals(qName))
            return new SOAPBindingImpl(SOAP_12_BINDING);
        if (SOAP_12_HEADER.equals(qName))
            return new SOAPHeadeImpl(SOAP_12_HEADER);
        if (SOAP_12_ADDRESS.equals(qName))
            return new SOAPAddressImpl();
        if (POLICY.equals(qName)) {
            return new PolicyExtensitbilityElementImpl();
        }
        return new DefaultExtensibilityElementImpl();
    }

}
