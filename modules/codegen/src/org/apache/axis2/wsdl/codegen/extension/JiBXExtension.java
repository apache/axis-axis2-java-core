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

package org.apache.axis2.wsdl.codegen.extension;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPBody;

public class JiBXExtension extends AbstractDBProcessingExtension {
    
    public static final String BINDING_PATH_OPTION = "bindingfile";
    public static final String JIBX_MODEL_CLASS =
        "org.jibx.binding.model.BindingElement";
    public static final String JIBX_UTILITY_CLASS =
        "org.apache.axis2.jibx.CodeGenerationUtility";
    public static final String BINDING_MAP_METHOD = "getBindingMap";
    
    public void engage() {

        // just return if JiBX binding not active
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }

        // check the comptibilty - currently only doc/lit is supported
        checkCompatibility();
        
        // check the JiBX binding definition file specified
        String path = (String)configuration.getProperties().get(BINDING_PATH_OPTION);
        if (path == null) {
            throw new RuntimeException("jibx binding option requires -" +
                BINDING_PATH_OPTION + " {file path} parameter");
//                CodegenMessages.getMessage("extension.encodedNotSupported"));
        }
        try {
            
            // load and call JiBX utilities to handle binding
            Class clas = null;
            try {
                JiBXExtension.class.getClassLoader().loadClass(JIBX_MODEL_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JiBX framework jars not in classpath");
            }
            try {
                clas = JiBXExtension.class.getClassLoader().loadClass(JIBX_UTILITY_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JiBX binding extension not in classpath");
            }
            Method method = clas.getMethod(BINDING_MAP_METHOD,
                new Class[] { String.class });
            HashMap jibxmap = (HashMap)method.invoke(null, new Object[] { path });
            
            // Want to find all elements by working down from bindings (if any
            // supplied) or interfaces (if no bindings). Not sure why this dual
            // path is required, but based on the code in
            // org.apache.axis2.wsdl.builder.SchemaUnwrapper
            HashSet elements = new HashSet();
            Map bindings = configuration.getWom().getBindings();
            Map interfaces = configuration.getWom().getWsdlInterfaces();
            if (bindings != null && !bindings.isEmpty()) {
                for (Iterator iter = bindings.values().iterator(); iter.hasNext();) {
                    accumulateElements(((WSDLBinding)iter.next()).getBoundInterface(), elements);
                }
            } else if (interfaces != null && !interfaces.isEmpty()) {
                for (Iterator iter = interfaces.values().iterator(); iter.hasNext();) {
                    accumulateElements((WSDLInterface)iter.next(), elements);
                }
            }
            
            // build type mapping from JiBX mappings for elements
            JavaTypeMapper mapper = new JavaTypeMapper();
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                QName qname = (QName)iter.next();
                String cname = (String)jibxmap.get(qname);
                if (cname == null) {
                    throw new RuntimeException("No JiBX mapping defined for " + qname);
                }
                mapper.addTypeMappingName(qname, cname);
            }
            
            // set the type mapper to the config
            configuration.setTypeMapper(mapper);
            
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Accumulate the QNames of all message elements used by an interface. Based on
     * the code in {@link org.apache.axis2.wsdl.builder.SchemaUnwrapper}
     * 
     * @param interf
     * @param elements
     */
    private void accumulateElements(WSDLInterface interf, HashSet elements) {
        
        // we should be getting all the operation since we also need to consider the inherited ones
        for (Iterator iter = interf.getAllOperations().values().iterator(); iter.hasNext();) {
            WSDLOperation operation = (WSDLOperation)iter.next();
            if (operation.getInputMessage() != null) {
                elements.add(operation.getInputMessage().getElementQName());
            }
            if (operation.getOutputMessage() != null) {
                elements.add(operation.getOutputMessage().getElementQName());
            }
        }
    }

    /**
     * Checking the compatibilty has to do with generating RPC/encoded stubs.
     * If the XMLBeans bindings are used encoded binding cannot be done.
     */
    private void checkCompatibility() {
        Map bindingMap = this.configuration.getWom().getBindings();
        Collection col = bindingMap.values();

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            WSDLBinding b = (WSDLBinding) iterator.next();
            HashMap bindingOps = b.getBindingOperations();
            Collection bindingOpsCollection = bindingOps.values();
            for (Iterator iterator1 = bindingOpsCollection.iterator(); iterator1.hasNext();) {
                checkInvalidUse((WSDLBindingOperation) iterator1.next());
            }

        }
    }

    protected void checkInvalidUse(WSDLBindingOperation bindingOp) {
        WSDLBindingMessageReference input = bindingOp.getInput();
        if (input != null) {
            Iterator extIterator = input.getExtensibilityElements()
                    .iterator();
            while (extIterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
                if (ExtensionConstants.SOAP_11_BODY.equals(element.getType()) ||
                        ExtensionConstants.SOAP_12_BODY.equals(element.getType())) {
                    if (WSDLConstants.WSDL_USE_ENCODED.equals(
                            ((SOAPBody) element).getUse())) {
                        throw new RuntimeException(
                                CodegenMessages.getMessage("extension.encodedNotSupported"));
                    }
                }
            }
        }
    }
}