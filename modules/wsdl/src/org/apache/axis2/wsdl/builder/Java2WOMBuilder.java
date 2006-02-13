package org.apache.axis2.wsdl.builder;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.ExtensionFactory;
import org.apache.wsdl.extensions.impl.SOAPAddressImpl;
import org.apache.wsdl.extensions.impl.SOAPBindingImpl;
import org.apache.wsdl.extensions.impl.SOAPBodyImpl;
import org.apache.wsdl.extensions.impl.SOAPOperationImpl;
import org.apache.wsdl.impl.WSDLDescriptionImpl;
import org.apache.axis2.namespace.Constants;
import org.codehaus.jam.JMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
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
*/

public class Java2WOMBuilder {

    private TypeTable table;
    private JMethod method [];
    private XmlSchema schema;
    private String serviceName;
    private String targetNamespace;
    private String targetNamespacePrefix;

    public static final String DEFAULT_SOAP_NAMESPACE_PREFIX = "soap";
    public static final String DEFAULT_SCHEMA_NAMESPACE_PREFIX = "xs";
    public static final String BINDING_NAME_SUFFIX = "Binding";
    public static final String PORT_TYPE_SUFFIX = "PortType";
    public static final String PORT_NAME_SUFFIX = "Port";
    public static final String DEFAULT_TARGET_NAMESPACE = "http://ws.apache.org/axis2";
    public static final String DEFAULT_TARGET_NAMESPACE_PREFIX = "axis2";

    public Java2WOMBuilder(TypeTable table, JMethod[] method, XmlSchema schema, String serviceName,
                           String targetNamespace,
                           String targetNamespacePrefix) {
        this.table = table;
        this.method = method;
        this.schema = schema;
        this.serviceName = serviceName;

        if (targetNamespace != null && !targetNamespace.trim().equals("")) {
            this.targetNamespace = targetNamespace;
        }else{
             this.targetNamespace = DEFAULT_TARGET_NAMESPACE ;
        }
        if (targetNamespacePrefix != null && !targetNamespacePrefix.trim().equals("")) {
            this.targetNamespacePrefix = targetNamespacePrefix;
        }else{
            this.targetNamespacePrefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
        }

    }

    public WSDLDescription generateWOM() throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        StringWriter writer = new StringWriter();

        writeSchema(writer);

        Document doc = domBuilder.parse(new ByteArrayInputStream(writer.toString().getBytes()));
        Element documentElement = doc.getDocumentElement();
        WSDLDescription womDescription;
        WSDLComponentFactory wsdlComponentFactory = new WSDLDescriptionImpl();
        womDescription = wsdlComponentFactory.createDescription();
        HashMap namspaceMap = loadNamespaces();
        womDescription.setNamespaces(namspaceMap);
        womDescription.setTargetNameSpace(targetNamespace);

        //generating port type
        WSDLInterface portType = generatePortType(womDescription, wsdlComponentFactory, documentElement);
        womDescription.addInterface(portType);

        QName bindingName = new QName(targetNamespace, serviceName + BINDING_NAME_SUFFIX
                , targetNamespacePrefix);

        //generating binding. Our bidings are strictly doc/lit, atleast for now
        WSDLBinding binding = generateBinding(wsdlComponentFactory,
                portType,
                bindingName,
                //todo these need to be constants
                "document",
                "literal",
                Constants.URI_SOAP11_HTTP,
                targetNamespace);

        womDescription.addBinding(binding);

        //generating service
        WSDLService service = generateService(wsdlComponentFactory, womDescription, binding, serviceName);
        womDescription.addService(service);
        return womDescription;
    }

    /**
     * Loads the namespaces
     * @return
     */
    private HashMap loadNamespaces() {
        HashMap namspaceMap = new HashMap();
        namspaceMap.put(DEFAULT_SOAP_NAMESPACE_PREFIX, Constants.URI_WSDL11_SOAP);
        namspaceMap.put(targetNamespacePrefix, targetNamespace);
        namspaceMap.put(DEFAULT_SCHEMA_NAMESPACE_PREFIX, Constants.URI_2001_SCHEMA_XSD);
        return namspaceMap;
    }

    /**
     * write the schema
     */

    private void writeSchema(StringWriter writer) {
        schema.write(writer);
        writer.flush();
    }

    /**
     * Generate the porttypes
     * @param womDescription
     * @param wsdlComponentFactory
     * @param documentElement
     * @return
     */
    public WSDLInterface generatePortType(WSDLDescription womDescription,
                                          WSDLComponentFactory wsdlComponentFactory,
                                          Element documentElement) {
        WSDLTypes wsdlTypes = wsdlComponentFactory.createTypes();
        ExtensionFactory extensionFactory = wsdlComponentFactory.createExtensionFactory();
        org.apache.wsdl.extensions.Schema schemaExtensibilityElement =
                (org.apache.wsdl.extensions.Schema) extensionFactory.getExtensionElement(
                        ExtensionConstants.SCHEMA);
        wsdlTypes.addExtensibilityElement(schemaExtensibilityElement);
        schemaExtensibilityElement.setElement(documentElement);
        womDescription.setTypes(wsdlTypes);

        WSDLInterface portType = womDescription.createInterface();
        portType.setName(new QName(serviceName + PORT_TYPE_SUFFIX));

        //adding message refs
        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            //creating WSDLOperation
            WSDLOperation operation = womDescription.createOperation();
            operation.setName(new QName(jmethod.getSimpleName()));

            MessageReference messageRefinput = wsdlComponentFactory.createMessageReference();
            QName typeName = table.getComplexScheamType(jmethod.getSimpleName() +
                    SchemaGenerator.METHOD_REQUEST_WRAPPER);
            messageRefinput.setElementQName(typeName);
            messageRefinput.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
            operation.setInputMessage(messageRefinput);
            portType.setOperation(operation);

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

    /**
     * Generate the service
     * @param wsdlComponentFactory
     * @param womDescription
     * @param binding
     * @param ServiceName
     * @return
     */
    public WSDLService generateService(WSDLComponentFactory wsdlComponentFactory,
                                       WSDLDescription womDescription,
                                       WSDLBinding binding, String ServiceName) {
        WSDLService service = wsdlComponentFactory.createService();
        service.setName(new QName(ServiceName));
        WSDLEndpoint endpoints = wsdlComponentFactory.createEndpoint();
        endpoints.setBinding(binding);
        endpoints.setName(new QName(ServiceName + PORT_NAME_SUFFIX));
        SOAPAddressImpl address = new SOAPAddressImpl();
        address.setLocationURI("http://127.0.0.1:8080/axis2/services/" + ServiceName);  // ???
        endpoints.addExtensibilityElement(address);
        service.setEndpoint(endpoints);
        return service;
    }


    /**
     * Generate the bindings
     * @param wsdlComponentFactory
     * @param portType
     * @param bindingName
     * @param style
     * @param use
     * @param transportURI
     * @param namespaceURI
     * @return
     */
    private WSDLBinding generateBinding(WSDLComponentFactory wsdlComponentFactory,
                                        WSDLInterface portType, QName bindingName,
                                        String style,
                                        String use,
                                        String transportURI,
                                        String namespaceURI) {
        WSDLBinding binding = wsdlComponentFactory.createBinding();
        binding.setBoundInterface(portType);
        binding.setName(bindingName);

        SOAPBindingImpl soapbindingImpl = new SOAPBindingImpl();
        soapbindingImpl.setStyle(style);
        soapbindingImpl.setTransportURI(transportURI);
        binding.addExtensibilityElement(soapbindingImpl);

        Iterator op_itr = portType.getOperations().keySet().iterator();
        while (op_itr.hasNext()) {
            String opName = (String) op_itr.next();
            WSDLOperation wsdlOperation = portType.getOperation(opName);
            MessageReference inMessage = wsdlOperation.getInputMessage();

            WSDLBindingOperation bindingoperation = wsdlComponentFactory.createWSDLBindingOperation();
            bindingoperation.setName(new QName(opName));
            bindingoperation.setOperation(wsdlOperation);
            binding.addBindingOperation(bindingoperation);

            SOAPOperationImpl soapOpimpl = new SOAPOperationImpl();
            soapOpimpl.setStyle(style);
            //to do heve to set a proper SOAPAction
            soapOpimpl.setSoapAction(opName);
            bindingoperation.addExtensibilityElement(soapOpimpl);
            if (inMessage != null) {
                WSDLBindingMessageReference bindingInMessage = wsdlComponentFactory.createWSDLBindingMessageReference();
                bindingInMessage.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
                bindingoperation.setInput(bindingInMessage);
                SOAPBodyImpl requestSoapbody = new SOAPBodyImpl();
                requestSoapbody.setUse(use);
                //todo need to fix this
                requestSoapbody.setNamespaceURI(namespaceURI);
                bindingInMessage.addExtensibilityElement(requestSoapbody);
            }

            MessageReference outMessage = wsdlOperation.getOutputMessage();
            if (outMessage != null) {
                WSDLBindingMessageReference bindingOutMessage = wsdlComponentFactory.createWSDLBindingMessageReference();

                bindingOutMessage.setDirection(org.apache.wsdl.WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
                bindingoperation.setOutput(bindingOutMessage);
                SOAPBodyImpl resSoapbody = new SOAPBodyImpl();
                resSoapbody.setUse(use);
                resSoapbody.setNamespaceURI(namespaceURI);
                bindingOutMessage.addExtensibilityElement(resSoapbody);
            }
        }
        return binding;
    }


}
