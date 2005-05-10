package org.apache.axis.wsdl.codegen.emitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.codegen.CodeGenerationException;
import org.apache.axis.wsdl.codegen.CodeGenerationException;
import org.apache.axis.wsdl.codegen.emitter.Emitter;
import org.apache.axis.wsdl.codegen.extension.AxisBindingBuilder;
import org.apache.axis.wsdl.codegen.extension.AxisBindingBuilder;
import org.apache.axis.wsdl.codegen.writer.ClassWriter;
import org.apache.axis.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.crimson.tree.XmlDocument;
import org.apache.wsdl.WSDLBinding;
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
* Abstract Client emitter
* the XML will look like the following
* todo escape the following
* <pre>
    <interface package="">
    <method name="">
    <input>
        <param name="" type=""/>*
    </input> ?
    <output>
        <param name="" type=""/>?
    </output>?
    </method>
    </interface>
  </pre>
*/


public abstract class MultiLanguageClientEmitter implements Emitter{

    protected InputStream xsltStream = null;
    protected CodeGenConfiguration configuration;
    protected TypeMapper mapper;

   /**
    * Sets the mapper
    * @see org.apache.axis.wsdl.databinding.TypeMapper
    * @param mapper
    */
    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Sets the code generator configuration
     * @param configuration
     */
    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

   /**
    *
    * @see org.apache.axis.wsdl.codegen.emitter.Emitter#emitStub()
    */
    public void emitStub() throws CodeGenerationException {
        try {
            //get the binding
            WSDLBinding axisBinding = this.configuration.getWom().getBinding(AxisBindingBuilder.AXIS_BINDING_QNAME);
            //write interfaces
            writeInterfaces(axisBinding);
            //write interface implementations
            writeInterfaceImplementations(axisBinding);
        } catch (Exception e) {
            // e.printStackTrace();
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Writes the interfaces
     * @param axisBinding
     * @throws Exception
     */
    private void writeInterfaces(WSDLBinding axisBinding) throws Exception {
        XmlDocument interfaceModel = createDOMDocuementForInterface(axisBinding);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage()
                );
       writeClasses(interfaceModel,interfaceWriter);
    }

    /**
     * Writes the implementations
     * @param axisBinding
     * @throws Exception
     */
    private void writeInterfaceImplementations(WSDLBinding axisBinding) throws Exception {
        XmlDocument interfaceImplModel = createDOMDocuementForInterfaceImplementation(axisBinding);
        InterfaceImplementationWriter interfaceImplWriter =
                new InterfaceImplementationWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage()
                );
        writeClasses(interfaceImplModel,interfaceImplWriter);
    }

    /**
     * A resusable method for the implementation of interface and implementation writing
     * @param model
     * @param writer
     * @throws IOException
     * @throws Exception
     */
    private void writeClasses(XmlDocument model,ClassWriter writer) throws IOException,Exception {
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        model.write(memoryStream);
        writer.loadTemplate();
        writer.createOutFile(model.getDocumentElement().getAttribute("package"),
                model.getDocumentElement().getAttribute("name"));
        writer.writeOutFile(new ByteArrayInputStream(memoryStream.toByteArray()));
    }

    /**
     * @see org.apache.axis.wsdl.codegen.emitter.Emitter#emitSkeleton()
     */
    public void emitSkeleton() throws CodeGenerationException {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Creates the DOM tree for the interface creation
     * @param binding
     * @return
     */
    protected abstract XmlDocument createDOMDocuementForInterface(WSDLBinding binding);

    /**
     * Creates the DOM tree for implementations
     * @param binding
     * @return
     */
    protected abstract XmlDocument createDOMDocuementForInterfaceImplementation(WSDLBinding binding);

    /**
     * Finds the input element for the xml document
     * @param doc
     * @param operation
     * @return
     */
    protected Element getInputElement(XmlDocument doc,WSDLOperation operation){
        Element inputElt = doc.createElement("input");
        //todo this should be multiple
        Element param = doc.createElement("param");
        Attr paramNameAttr = doc.createAttribute("name");
        paramNameAttr.setValue(this.mapper.getParameterName(operation.getInputMessage().getElement()));
        param.setAttributeNode(paramNameAttr);
        Attr paramTypeAttr = doc.createAttribute("type");
        Class typeMapping = this.mapper.getTypeMapping(operation.getInputMessage().getElement());
        paramTypeAttr.setValue(typeMapping==null?"":typeMapping.getName());
        param.setAttributeNode(paramTypeAttr);

        inputElt.appendChild(param);

        return inputElt;
    }

    /**
     * Finds the output element for the output element
     * @param doc
     * @param operation
     * @return
     */
    protected Element getOutputElement(XmlDocument doc,WSDLOperation operation){
        Element outputElt = doc.createElement("output");
        Element param = doc.createElement("param");
        Attr paramNameAttr = doc.createAttribute("name");
        paramNameAttr.setValue(this.mapper.getParameterName(operation.getOutputMessage().getElement()));
        param.setAttributeNode(paramNameAttr);
        Attr paramTypeAttr = doc.createAttribute("type");
        Class typeMapping = this.mapper.getTypeMapping(operation.getOutputMessage().getElement());
        paramTypeAttr.setValue(typeMapping==null?"":typeMapping.getName());
        param.setAttributeNode(paramTypeAttr);

        outputElt.appendChild(param);

        return outputElt;
    }
}

