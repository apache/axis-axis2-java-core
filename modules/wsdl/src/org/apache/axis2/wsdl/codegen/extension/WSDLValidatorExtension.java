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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.NamedNodeMap;

import java.util.Iterator;

/**
 * @author chathura@opensource.lk
 */
public class WSDLValidatorExtension extends AbstractCodeGenerationExtension {
    private static String TARGETNAMESPACE_STRING = "targetNamespace";

    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() throws CodeGenerationException {
        WSDLDescription wom = this.configuration.getWom();
        Iterator iterator = wom.getTypes().getExtensibilityElements().iterator();
        while (iterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) iterator.next();
            boolean targetnamespaceFound = false;
            if (ExtensionConstants.SCHEMA.equals(element.getType())) {
                Schema schema = (Schema) element;
                NamedNodeMap attributes = schema.getElelment().getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (TARGETNAMESPACE_STRING.equalsIgnoreCase(attributes.item(i).getLocalName()))
                        targetnamespaceFound = true;
                }
            }
            if (!targetnamespaceFound)
                throw new CodeGenerationException("Invalid WSDL: The WSDL Types Schema does not define a targetNamespace");
        }
    }
}
