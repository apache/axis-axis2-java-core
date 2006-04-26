/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.jibx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.ValidationContext;
import org.jibx.runtime.JiBXException;

/**
 * Framework-linked code used by JiBX data binding support. This is accessed via
 * reflection from the JiBX code generation extension when JiBX data binding is
 * selected.
 */
public class CodeGenerationUtility {

    /**
     * Get map from qname to corresponding class name from binding definition.
     * Only the global &lt;mapping> elements in the binding definition are
     * included, since these are the only ones accessible from the Axis2
     * interface.
     * 
     * @param path binding definition file path
     * @return map from qname to class name
     */
    public static HashMap getBindingMap(String path) {
        
        // make sure the binding definition file is present
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("jibx binding definition file " + path + " not found");
//                CodegenMessages.getMessage("extension.encodedNotSupported"));
        }
        
        // Read the JiBX binding definition into memory. The binding definition
        // is not currently validated so as not to require the user to have all
        // the referenced classes in the classpath, though this does make for
        // added work in finding the namespaces.
        try {
            ValidationContext vctx = BindingElement.newValidationContext();
            BindingElement binding =
                BindingElement.readBinding(new FileInputStream(file), path, vctx);
            if (vctx.getErrorCount() != 0 || vctx.getFatalCount() != 0) {
                throw new RuntimeException("invalid jibx binding definition file " + path);
            }
            
            // create map from qname to class for all top-level mappings
            return defineBoundClasses(binding);
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JiBXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create mapping from qnames to classes for top level mappings in JiBX binding.
     * 
     * @param binding
     * @return map from qname to class
     */
    private static HashMap defineBoundClasses(BindingElement binding) {
        
        // check default namespace set at top level of binding
        String defaultns = findDefaultNS(binding.topChildIterator());
        
        // add all top level mapping definitions to map from qname to class
        HashMap mappings = new HashMap();
        for (Iterator iter = binding.topChildIterator(); iter.hasNext();) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.MAPPING_ELEMENT) {
                MappingElement mapping = (MappingElement)child;
                String name = mapping.getName();
                if (name != null) {
                    String uri = mapping.getUri();
                    if (uri == null) {
                        uri = findDefaultNS(mapping.topChildIterator());
                        if (uri == null) {
                            uri = defaultns;
                        }
                    }
                    mappings.put(new QName(uri, name), mapping.getClassName());
                }
            }
        }
        return mappings;
    }

    /**
     * Find the default namespace within a list of JiBX binding model elements
     * possibly including namespace definitions. Once a non-namespace definition
     * element is seen in the list, this just returns (since the namespace
     * definitions always come first in JiBX's binding format).
     * 
     * @param iter iterator for elements in list
     */
    private static String findDefaultNS(Iterator iter) {
        while (iter.hasNext()) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.NAMESPACE_ELEMENT) {
                NamespaceElement namespace = (NamespaceElement)child;
                String defaultName = namespace.getDefaultName();
                if ("elements".equals(defaultName) || "all".equals(defaultName)) {
                    return namespace.getUri();
                }
            } else {
               break;
            }
        }
        return null;
    }
}