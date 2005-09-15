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

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.wsdl.*;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.Vector;

/**
 * The purpose of this extension is to build the bindings. bindings however may not be present
 */
public class AxisBindingBuilder extends AbstractCodeGenerationExtension implements CodeGenExtension {

    public static final String AXIS_NAMESPACE = "http://ws.apache.org/axis2/";

    public static final QName AXIS_BINDING_QNAME = new QName(AXIS_NAMESPACE,
            "codeGenerationBinding",
            "axis");


    public AxisBindingBuilder() {
    }

    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() {
        WSDLDescription wom = this.configuration.getWom();
        Map bindingMap = wom.getBindings();
        Vector bindingVector = new Vector();

        if (bindingMap==null || bindingMap.isEmpty()) {
            log.info(" bindings are not present in the original document");
            //just fall through
        }else{
            Collection bindingCollection = bindingMap.values();
            for (Iterator iterator = bindingCollection.iterator(); iterator.hasNext();) {

                WSDLBinding binding = (WSDLBinding)iterator.next();

                WSDLBinding newBinding = wom.createBinding();
                newBinding.setName(AXIS_BINDING_QNAME);

                WSDLInterface boundInterface = binding.getBoundInterface();
                newBinding.setBoundInterface(boundInterface);

                newBinding.setBindingFaults(binding.getBindingFaults());
                newBinding.setBindingOperations(binding.getBindingOperations());
                Iterator elementIterator = binding.getExtensibilityElements().iterator();
                while (elementIterator.hasNext()) {
                    newBinding.addExtensibilityElement(
                            (WSDLExtensibilityElement) elementIterator.next());
                }

                Iterator attributeIterator = binding.getExtensibilityAttributes()
                        .iterator();
                while (attributeIterator.hasNext()) {
                    newBinding.addExtensibleAttributes(
                            (WSDLExtensibilityAttribute) attributeIterator.next());
                }
                bindingVector.add(newBinding);

            }
            //drop all the bindings and add the new ones
            wom.getBindings().clear();
            for (int i = 0; i < bindingVector.size(); i++) {
                wom.addBinding ((WSDLBinding) bindingVector.get(i));
            }

        }
    }
}
