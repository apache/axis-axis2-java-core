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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.Iterator;

public class WSDLValidatorExtension extends AbstractCodeGenerationExtension {
    private static String TARGETNAMESPACE_STRING = "targetNamespace";


    public void engage() throws CodeGenerationException {
        //WSDLDescription wom = this.configuration.getWom();
        WSDLTypes typesList = configuration.getWom().getTypes();
        if (typesList == null) {
            //there are no types to be considered
            return;
        }
        Iterator iterator = typesList.getExtensibilityElements().iterator();
        while (iterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) iterator.next();
            boolean targetnamespaceFound = false;
            if (ExtensionConstants.SCHEMA.equals(element.getType())) {
                Schema schema = (Schema) element;
                Element schemaElement = schema.getElement();
                //first check whether the schema include import statements.
                //As per the nature of WSDL if the schema has imports ONLY, then the
                //schema element need not contain a target namespace.
                NodeList importNodeList = schemaElement.getElementsByTagNameNS(schemaElement.getNamespaceURI(), "import");
                NodeList allNodes = schemaElement.getElementsByTagName("*");

                //checking the number of child elements and the number of import elements should get us what we need
                //if these match, that means we have only import statements

                if (importNodeList.getLength()== allNodes.getLength()) {
                    return;
                }


                NamedNodeMap attributes = schemaElement.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {

                    if (TARGETNAMESPACE_STRING.equalsIgnoreCase(
                            attributes.item(i).getNodeName())) {
                        targetnamespaceFound = true;
                        break;
                    }
                }
                if (!targetnamespaceFound)
                    throw new CodeGenerationException(
                            CodegenMessages.getMessage("extension.invalidWSDL",schema.getName().toString()));

            }

        }
    }
}
