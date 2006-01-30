package org.apache.axis2.description;

import org.apache.axis2.wsdl.builder.WSDLComponentFactory;
import org.apache.axis2.wsdl.builder.SchemaGenerator;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.ExtensionFactory;
import org.apache.wsdl.extensions.SOAPBinding;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.wsdl.extensions.SOAPOperation;
import org.apache.wsdl.extensions.Schema;
import org.apache.wsdl.extensions.impl.SOAPAddressImpl;
import org.apache.wsdl.impl.WSDLDescriptionImpl;
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

public class AxisService2WOM {

    private XmlSchema schema;
    private AxisService axisService;
    private String url;
    private String targetNamespece;
    private String targetNamespecheprefix;


    public AxisService2WOM(XmlSchema schema, AxisService service,
                           String targetNamespece,
                           String targetNamespecheprefix, String serviceURL) {
        this.schema = schema;
        this.axisService = service;
        url = serviceURL;

        if (targetNamespece != null && !targetNamespece.trim().equals("")) {
            this.targetNamespece = targetNamespece;
        } else {
            this.targetNamespece = SchemaGenerator.TARGET_NAMESPACE;
        }
        if (targetNamespecheprefix != null && !targetNamespecheprefix.trim().equals("")) {
            this.targetNamespecheprefix = targetNamespecheprefix;
        } else {
            this.targetNamespecheprefix = SchemaGenerator.TARGET_NAMESPACE_PREFIX;
        }

    }

    public WSDLDescription generateWOM() throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        StringWriter writer = new StringWriter();
        if(schema == null){
            throw new Exception("no scheam found for the service");
        }
        schema.write(writer);
        writer.flush();
        Document doc = domBuilder.parse(new ByteArrayInputStream(writer.toString().getBytes()));
        Element documentElement = doc.getDocumentElement();
        WSDLDescription womDescription;
        WSDLComponentFactory wsdlComponentFactory = new WSDLDescriptionImpl();
        womDescription = wsdlComponentFactory.createDescription();
        HashMap namespaceMap = new HashMap();
        namespaceMap.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
        namespaceMap.put(targetNamespecheprefix, targetNamespece);
        namespaceMap.put("ns1", "http://org.apache.axis2/xsd");
        namespaceMap.put("xs", "http://www.w3.org/2001/XMLSchema");
        womDescription.setNamespaces(namespaceMap);
        womDescription.setTargetNameSpace(targetNamespece);

        //generating port type
        WSDLInterface portType = generatePortType(womDescription, wsdlComponentFactory, documentElement);
        womDescription.addInterface(portType);

        QName bindingName = new QName(targetNamespece, axisService.getName() + "Binding"
                , targetNamespecheprefix);
        //generating binding
        WSDLBinding binding = generateBinding(wsdlComponentFactory,
                portType,
                bindingName,
                "document", "literal", "http://schemas.xmlsoap.org/soap/http",
                "http://www.org.apache.axis2");
        womDescription.addBinding(binding);

        //generating axisService
        WSDLService service = generateService(wsdlComponentFactory, binding,
                axisService.getName(), url);
        womDescription.addService(service);
        return womDescription;
    }

    private WSDLInterface generatePortType(WSDLDescription womDescription,
                                           WSDLComponentFactory wsdlComponentFactory,
                                           Element documentElement) {
        WSDLTypes wsdlTypes = wsdlComponentFactory.createTypes();
        ExtensionFactory extensionFactory = wsdlComponentFactory.createExtensionFactory();
        Schema schemaExtensibilityElement =
                (Schema) extensionFactory.getExtensionElement(
                        ExtensionConstants.SCHEMA);
        wsdlTypes.addExtensibilityElement(schemaExtensibilityElement);
        schemaExtensibilityElement.setElement(documentElement);
        womDescription.setTypes(wsdlTypes);

        WSDLInterface portType = womDescription.createInterface();
        portType.setName(new QName(axisService.getName() + "Port"));


        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                //we do not need to expose control operation in the WSDL
                continue;
            }
            WSDLOperation wsdlOperation = womDescription.createOperation();
            wsdlOperation.setName(axisOperation.getName());

            AxisMessage inaxisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inaxisMessage != null) {
                MessageReference messageRefinput = wsdlComponentFactory.createMessageReference();
                messageRefinput.setElementQName(inaxisMessage.getElementQName());
                messageRefinput.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
                wsdlOperation.setInputMessage(messageRefinput);
            }

            try {
                AxisMessage outaxisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outaxisMessage != null && outaxisMessage.getElementQName() != null) {
                    MessageReference messageRefout = wsdlComponentFactory.createMessageReference();
                    messageRefout.setElementQName(outaxisMessage.getElementQName());
                    messageRefout.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
                    wsdlOperation.setOutputMessage(messageRefout);
                }
            } catch (UnsupportedOperationException e) {
                // operation does not have an out message so , no need to do anything here
            }
            portType.setOperation(wsdlOperation);

        }
        return portType;
    }

    private WSDLService generateService(WSDLComponentFactory wsdlComponentFactory,
                                        WSDLBinding binding, String ServiceName, String URL) {
        WSDLService service = wsdlComponentFactory.createService();
        service.setName(new QName(ServiceName));
        WSDLEndpoint endpoints = wsdlComponentFactory.createEndpoint();
        endpoints.setBinding(binding);
        endpoints.setName(new QName(ServiceName + "PortType"));
        SOAPAddressImpl address = new SOAPAddressImpl();
        address.setLocationURI(URL);
        endpoints.addExtensibilityElement(address);
        service.setEndpoint(endpoints);
        return service;
    }


    private WSDLBinding generateBinding(WSDLComponentFactory wsdlComponentFactory,
                                        WSDLInterface portType, QName bindingName,
                                        String style,
                                        String use,
                                        String trsportURI,
                                        String namespeceURI) {
        WSDLBinding binding = wsdlComponentFactory.createBinding();

        ExtensionFactory extensionFactory = wsdlComponentFactory.createExtensionFactory();

        binding.setBoundInterface(portType);
        binding.setName(bindingName);

        SOAPBinding soapbindingImpl = (SOAPBinding) extensionFactory.getExtensionElement(
                ExtensionConstants.SOAP_11_BINDING);
        soapbindingImpl.setStyle(style);
        soapbindingImpl.setTransportURI(trsportURI);
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

            SOAPOperation soapOpimpl = (SOAPOperation) extensionFactory.getExtensionElement(
                    ExtensionConstants.SOAP_11_OPERATION);
            soapOpimpl.setStyle(style);
            //to do heve to set a proper SOAPAction
            soapOpimpl.setSoapAction(opName);
            bindingoperation.addExtensibilityElement(soapOpimpl);
            if (inMessage != null) {
                WSDLBindingMessageReference bindingInMessage = wsdlComponentFactory.createWSDLBindingMessageReference();
                bindingInMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
                bindingoperation.setInput(bindingInMessage);

                SOAPBody requestSoapbody = (SOAPBody) extensionFactory.getExtensionElement(
                        ExtensionConstants.SOAP_11_BODY);
                requestSoapbody.setUse(use);
                //todo need to fix this
                requestSoapbody.setNamespaceURI(namespeceURI);
                bindingInMessage.addExtensibilityElement(requestSoapbody);
            }

            MessageReference outMessage = wsdlOperation.getOutputMessage();
            if (outMessage != null) {
                WSDLBindingMessageReference bindingOutMessage = wsdlComponentFactory.createWSDLBindingMessageReference();

                bindingOutMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
                bindingoperation.setOutput(bindingOutMessage);
                SOAPBody resSoapbody = (SOAPBody) extensionFactory.getExtensionElement(
                        ExtensionConstants.SOAP_11_BODY);
                resSoapbody.setUse(use);
                resSoapbody.setNamespaceURI(namespeceURI);
                bindingOutMessage.addExtensibilityElement(resSoapbody);
            }
        }
        return binding;
    }


}

