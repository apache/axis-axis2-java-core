package org.apache.axis2.wsdl.java2wsdl;

import org.apache.axis2.wsdl.builder.WSDLComponentFactory;
import org.apache.axis2.wsdl.writer.WOMWriter;
import org.apache.axis2.wsdl.writer.WOMWriterFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.ExtensionFactory;
import org.apache.wsdl.extensions.impl.SOAPAddressImpl;
import org.apache.wsdl.extensions.impl.SOAPBindingImpl;
import org.apache.wsdl.extensions.impl.SOAPBodyImpl;
import org.apache.wsdl.extensions.impl.SOAPOperationImpl;
import org.apache.wsdl.impl.WSDLDescriptionImpl;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
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
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class Java2WOM {

    private TypeTable table;
    private JMethod method [];
    private XmlSchema schema;
    private String serviceName;

    public Java2WOM(TypeTable table, JMethod[] method, XmlSchema schema, String serviceName) {
        this.table = table;
        this.method = method;
        this.schema = schema;
        this.serviceName = serviceName;
    }

    public WSDLDescription generateWOM() throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        StringWriter writer = new StringWriter();
        schema.write(writer);
        writer.flush();
        Document doc = domBuilder.parse(new ByteArrayInputStream(writer.toString().getBytes()));
        Element documentElement = doc.getDocumentElement();
        WSDLDescription womDescription;
        WSDLComponentFactory wsdlComponentFactory = new WSDLDescriptionImpl();
        womDescription = wsdlComponentFactory.createDescription();

        WOMWriter womWriter = WOMWriterFactory.createWriter(org.apache.axis2.wsdl.WSDLConstants.WSDL_1_1);
        womWriter.setdefaultWSDLPrefix("wsdl");
        womWriter.writeWOM(womDescription, System.out);
        WSDLInterface portType = generatePortType(womDescription, wsdlComponentFactory, documentElement);

        QName bindingName = new QName(SchemaGenerator.TARGET_NAMESPACE, serviceName + "Binding"
                , SchemaGenerator.TARGET_NAMESPACE_PRFIX);
        WSDLBinding binding = genareteBinding(wsdlComponentFactory,
                womDescription, portType,
                bindingName,
                method, "document", "literal", "http://schemas.xmlsoap.org/soap/http",
                "http://www.org.apache.axis2");
        WSDLService service = generateService(wsdlComponentFactory, womDescription, binding, serviceName);
        womDescription.addService(service);
        return womDescription;
    }

    public WSDLInterface generatePortType(WSDLDescription womDescription,
                                          WSDLComponentFactory wsdlComponentFactory,
                                          Element documentElement) {
        HashMap namspaseMap = new HashMap();
        namspaseMap.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
        namspaseMap.put(SchemaGenerator.TARGET_NAMESPACE_PRFIX, SchemaGenerator.TARGET_NAMESPACE);
        namspaseMap.put("ns1", "http://org.apache.axis2/xsd");
        namspaseMap.put("xs", "http://www.w3.org/2001/XMLSchema");
        womDescription.setNamespaces(namspaseMap);

        womDescription.setTargetNameSpace(SchemaGenerator.TARGET_NAMESPACE);
        WSDLTypes wsdlTypes = wsdlComponentFactory.createTypes();
        ExtensionFactory extensionFactory = wsdlComponentFactory.createExtensionFactory();
        org.apache.wsdl.extensions.Schema schemaExtensibilityElement =
                (org.apache.wsdl.extensions.Schema) extensionFactory.getExtensionElement(
                        ExtensionConstants.SCHEMA);
        wsdlTypes.addExtensibilityElement(schemaExtensibilityElement);
        schemaExtensibilityElement.setElement(documentElement);
        womDescription.setTypes(wsdlTypes);

        WSDLInterface portType = womDescription.createInterface();
        portType.setName(new QName(serviceName + "Port"));
        womDescription.addInterface(portType);
        //adding message refs
        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            //creating WSDLOperation
            WSDLOperation operation = womDescription.createOperation();
            operation.setName(new QName(jmethod.getSimpleName()));

            MessageReference messageRefinput = wsdlComponentFactory.createMessageReference();
            JParameter [] paras = jmethod.getParameters();
            QName typeName = null;
            boolean addMessage = false;
            if (paras.length == 0) {
                //todo have to fix this, method take no arugment
//                } else if (paras.length == 1) {
//                    typeName = table.getQNamefortheType(paras[0].getType().getQualifiedName());
//                    addMessage = true;
            } else {
                typeName = table.getComplexScheamType(jmethod.getSimpleName() +
                        SchemaGenerator.METHOD_REQUEST_WRAPPER);
                addMessage = true;
            }
            if (addMessage) {
                messageRefinput.setElementQName(typeName);
                messageRefinput.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
                operation.setInputMessage(messageRefinput);
                portType.setOperation(operation);
            }

            if (!jmethod.getReturnType().isVoidType()) {
                MessageReference messageRefiout = wsdlComponentFactory.createMessageReference();
                messageRefiout.setElementQName(table.getQNamefortheType(jmethod.getSimpleName() +
                        SchemaGenerator.METHOD_RESPONSE_WRAPPER));
                messageRefiout.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
                operation.setOutputMessage(messageRefiout);
            }
        }
        return portType;
    }

    private WSDLBinding genareteBinding(WSDLComponentFactory wsdlComponentFactory,
                                        WSDLDescription womDescription,
                                        WSDLInterface portType, QName bindingName,
                                        JMethod metods[],
                                        String style,
                                        String use,
                                        String trsportURI,
                                        String namespeceURI) {
        WSDLBinding binding = wsdlComponentFactory.createBinding();
        binding.setBoundInterface(portType);
        binding.setName(bindingName);
        womDescription.addBinding(binding);

        SOAPBindingImpl soapbindingImpl = new SOAPBindingImpl();
        soapbindingImpl.setStyle(style);
        soapbindingImpl.setTransportURI(trsportURI);
        binding.addExtensibilityElement(soapbindingImpl);
        for (int i = 0; i < metods.length; i++) {
            JMethod jmethod = metods[i];
            //creating WSDLOperation     for the binding
            WSDLBindingOperation bindingoperation = wsdlComponentFactory.createWSDLBindingOperation();
            String methodName = jmethod.getSimpleName();
            bindingoperation.setName(new QName(methodName));
            bindingoperation.setOperation(portType.getOperation(methodName));
            binding.addBindingOperation(bindingoperation);

            SOAPOperationImpl soapOpimpl = new SOAPOperationImpl();
            soapOpimpl.setStyle(style);
            //to do heve to set a proper SOAPAction
            soapOpimpl.setSoapAction(jmethod.getSimpleName());
            bindingoperation.addExtensibilityElement(soapOpimpl);

            //creating imput message
            WSDLBindingMessageReference inMessage = wsdlComponentFactory.createWSDLBindingMessageReference();
            inMessage.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
            bindingoperation.setInput(inMessage);
            SOAPBodyImpl requestSoapbody = new SOAPBodyImpl();
            requestSoapbody.setUse(use);
            //todo need to fix this
            requestSoapbody.setNamespaceURI(namespeceURI);
            inMessage.addExtensibilityElement(requestSoapbody);

            if (!jmethod.getReturnType().isVoidType()) {
                WSDLBindingMessageReference outMessage = wsdlComponentFactory.createWSDLBindingMessageReference();

                outMessage.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
                bindingoperation.setOutput(outMessage);
                SOAPBodyImpl resSoapbody = new SOAPBodyImpl();
                resSoapbody.setUse(use);
                resSoapbody.setNamespaceURI(namespeceURI);
                outMessage.addExtensibilityElement(resSoapbody);
            }
        }
        return binding;
    }

    public WSDLService generateService(WSDLComponentFactory wsdlComponentFactory,
                                       WSDLDescription womDescription,
                                       WSDLBinding binding, String ServiceName) {
        WSDLService service = wsdlComponentFactory.createService();
        service.setName(new QName(ServiceName));
        WSDLEndpoint endpoints = wsdlComponentFactory.createEndpoint();
        endpoints.setBinding(binding);
        endpoints.setName(new QName(ServiceName + "PortType"));
        SOAPAddressImpl address = new SOAPAddressImpl();
        address.setLocationURI("http://127.0.0.1:8080:/axis2/services/" + ServiceName);
        endpoints.addExtensibilityElement(address);
        service.setEndpoint(endpoints);
        return service;
    }

}
