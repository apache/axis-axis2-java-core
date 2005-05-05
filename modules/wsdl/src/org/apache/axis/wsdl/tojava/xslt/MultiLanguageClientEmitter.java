package org.apache.axis.wsdl.tojava.xslt;

import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.axis.wsdl.tojava.CodeGenConfiguration;
import org.apache.axis.wsdl.tojava.CodeGenerationException;
import org.apache.axis.wsdl.tojava.emitter.Emitter;
import org.apache.axis.wsdl.tojava.extension.AxisBindingBuilder;
import org.apache.crimson.tree.XmlDocument;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLOperation;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

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

/*
Format of the XML  - Interface
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
*/
public abstract class MultiLanguageClientEmitter implements Emitter{

    protected InputStream xsltStream = null;
    protected CodeGenConfiguration configuration;
    protected TypeMapper mapper;


    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }


    public void emitStub() throws CodeGenerationException {
        try {
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

    private void writeInterfaces(WSDLBinding axisBinding) throws Exception {
        XmlDocument interfaceModel = createDOMDocuementForInterface(axisBinding);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage()
                );
       writeClasses(interfaceModel,interfaceWriter);
    }

    private void writeInterfaceImplementations(WSDLBinding axisBinding) throws Exception {
        XmlDocument interfaceImplModel = createDOMDocuementForInterfaceImplementation(axisBinding);
        InterfaceImplementationWriter interfaceImplWriter =
                new InterfaceImplementationWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage()
                );
        writeClasses(interfaceImplModel,interfaceImplWriter);
    }
    private void writeClasses(XmlDocument model,ClassWriter writer) throws IOException,Exception {
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        model.write(memoryStream);
        writer.loadTemplate();
        writer.createOutFile(model.getDocumentElement().getAttribute("package"),
                model.getDocumentElement().getAttribute("name"));
        writer.writeOutFile(new ByteArrayInputStream(memoryStream.toByteArray()));
    }
    public void emitSkeleton() throws CodeGenerationException {
        throw new UnsupportedOperationException("Not supported yet");
    }

    protected abstract XmlDocument createDOMDocuementForInterface(WSDLBinding binding);

    protected abstract XmlDocument createDOMDocuementForInterfaceImplementation(WSDLBinding binding);

    protected Element getInputElement(XmlDocument doc,WSDLOperation operation){
        Element inputElt = doc.createElement("input");
        //todo this should be multiple
        Element param = doc.createElement("param");
        Attr paramNameAttr = doc.createAttribute("name");
        paramNameAttr.setValue(this.mapper.getParameterName(operation.getInputMessage().getElement()));
        param.setAttributeNode(paramNameAttr);
        Attr paramTypeAttr = doc.createAttribute("type");
        paramTypeAttr.setValue(this.mapper.getTypeMapping(operation.getInputMessage().getElement()).getName());
        param.setAttributeNode(paramTypeAttr);

        inputElt.appendChild(param);

        return inputElt;
    }

    protected Element getOutputElement(XmlDocument doc,WSDLOperation operation){
        Element outputElt = doc.createElement("output");
        Element param = doc.createElement("param");
        Attr paramNameAttr = doc.createAttribute("name");
        paramNameAttr.setValue(this.mapper.getParameterName(operation.getOutputMessage().getElement()));
        param.setAttributeNode(paramNameAttr);
        Attr paramTypeAttr = doc.createAttribute("type");
        paramTypeAttr.setValue(this.mapper.getTypeMapping(operation.getOutputMessage().getElement()).getName());
        param.setAttributeNode(paramTypeAttr);

        outputElt.appendChild(param);

        return outputElt;
    }
}

