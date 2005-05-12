package org.apache.axis.wsdl.codegen.emitter;

import java.util.Collection;
import java.util.Iterator;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.databinding.CsharpTypeMapper;
import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.crimson.tree.XmlDocument;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

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
 *
 * 
 */
public class CSharpEmitter extends MultiLanguageClientEmitter{

    public CSharpEmitter(CodeGenConfiguration configuration) {
        this.configuration = configuration;
        this.mapper = new CsharpTypeMapper();

    }

     public CSharpEmitter(CodeGenConfiguration configuration,TypeMapper mapper) {
        this.configuration = configuration;
        this.mapper =mapper;

    }


    /**
     *  @see org.apache.axis.wsdl.codegen.emitter.MultiLanguageClientEmitter#createDOMDocuementForInterface(org.apache.wsdl.WSDLBinding)
     * @param binding
     * @return
     */
    protected XmlDocument createDOMDocuementForInterface(WSDLBinding binding){
        WSDLInterface boundInterface = binding.getBoundInterface();

        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("interface");

        Attr packageAttribute = doc.createAttribute("package");
        packageAttribute.setValue("something"); //todo set this
        rootElement.setAttributeNode(packageAttribute);

        Attr nameAttribute = doc.createAttribute("name");
        nameAttribute.setValue(boundInterface.getName().getLocalPart());
        rootElement.setAttributeNode(nameAttribute);

        Collection col = boundInterface.getOperations().values();

        Element methodElement = null;
        Attr methodNameAttr = null;
        WSDLOperation operation = null;

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {

            operation = (WSDLOperation) iterator.next();

            methodElement = doc.createElement("method");
            methodNameAttr = doc.createAttribute("name");
            methodNameAttr.setValue(operation.getName().getLocalPart());
            methodElement.setAttributeNode(methodNameAttr);

            methodElement.appendChild(getInputElement(doc,operation));
            methodElement.appendChild(getOutputElement(doc,operation));

            rootElement.appendChild(methodElement);

        }
       doc.appendChild(rootElement);
       return doc;

    }

    /**
     * @see org.apache.axis.wsdl.codegen.emitter.MultiLanguageClientEmitter#createDOMDocuementForInterfaceImplementation(org.apache.wsdl.WSDLBinding)
     * @param binding
     * @return
     */
    protected XmlDocument createDOMDocuementForInterfaceImplementation(WSDLBinding binding) {
        return null;  
    }
}
